/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.commons.jext;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.StandardLocation;


@SupportedAnnotationTypes( "es.iti.commons.jext.Extension")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class ExtensionProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Map<String, List<String>> serviceImplementations = new LinkedHashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            validateAndRegisterExtension(element, serviceImplementations);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(ExtensionPoint.class)) {
            validateExtensionPoint(element);
        }
        writeOutputFiles(serviceImplementations);
        return false;
    }


    private void validateExtensionPoint(Element element) {
        if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.INTERFACE) {
            log(
                Kind.ERROR,
                element,
                "@ExtensionPoint not valid for {} (only processed for classes or interfaces)",
                element.getSimpleName()
            );
        } else {
            var extensionPointAnnotation = element.getAnnotation(ExtensionPoint.class);
            validateVersionFormat(extensionPointAnnotation.version(), element, "version");
        }
    }



    private void validateAndRegisterExtension(
        Element element,
        Map<String, List<String>> serviceImplementations
    ) {
        boolean hasError = false;
        if (element.getKind() != ElementKind.CLASS) {
            log(
                Kind.WARNING,
                element,
                "@Extension ignored for {} (only processed for classes)",
                element.getSimpleName()
            );
            return;
        }
        var extensionClassElement = (TypeElement) element;
        var extensionAnnotation = element.getAnnotation(Extension.class);

        if (extensionAnnotation.externallyManaged() || // not handling externally managed extensions
           !validateVersionFormat(extensionAnnotation.version(), element, "version") ||
           !validateVersionFormat(
                extensionAnnotation.extensionPointVersion(),
                element,
                "extensionPointVersion"
           )
        ) {
            return;
        }


        String extensionPoint = extensionAnnotation.extensionPoint();
        if (extensionPoint.isEmpty()) {
            for (TypeMirror implementedInterface : extensionClassElement.getInterfaces()) {
                extensionPoint = implementedInterface.toString();
                // remove the <..> part in case it is a generic class
                extensionPoint = extensionPoint.replaceAll("\\<[^\\>]*\\>", "");
            }
        }

        String extension = extensionClassElement.getQualifiedName().toString();

        TypeElement extensionPointClassElement = processingEnv.getElementUtils()
            .getTypeElement(extensionPoint);

        if (extensionPointClassElement == null) {
            log(
                Kind.ERROR,
                element,
                "Cannot find extension point class '{}'",
                extensionPoint
            );
            hasError = true;
        }

        if (!hasError && extensionPointClassElement.getAnnotation(ExtensionPoint.class) == null) {
            log(
                Kind.ERROR,
                element,
                "Expected extension point type '{}' is not annotated with @ExtensionPoint",
                extensionPoint
            );
            hasError = true;
        }

        if (!hasError &&
                        !isAssignable(
                            extensionClassElement.asType(),
                            extensionPointClassElement.asType()
                        )) {
            log(
                Kind.ERROR,
                element,
                "{} must implement or extend the extension point type {}",
                extension,
                extensionPoint
            );
            hasError = true;
        }

        if (!hasError) {
            serviceImplementations
                .computeIfAbsent(extensionPoint, x -> new ArrayList<>())
                .add(extension);
        }

    }


    private boolean validateVersionFormat(String version, Element element, String fieldName) {
        boolean valid = version.matches("\\d+\\.\\d+");
        if (!valid) {
            log(
                Kind.ERROR,
                element,
                "Content of field {} ('{}') must be in form '<major>.<minor>'",
                fieldName,
                version
            );
        }
        return valid;
    }


    private boolean isAssignable(TypeMirror type, TypeMirror typeTo) {
        if (nameWithoutGeneric(type).equals(nameWithoutGeneric(typeTo))) {
            return true;
        }
        for (TypeMirror superType : processingEnv.getTypeUtils().directSupertypes(type)) {
            if (isAssignable(superType, typeTo)) {
                return true;
            }
        }
        return false;
    }


    private String nameWithoutGeneric(TypeMirror type) {
        int genericPosition = type.toString().indexOf('<');
        return genericPosition < 0 ? type.toString()
                        : type.toString().substring(0, genericPosition);
    }


    private void writeOutputFiles(Map<String, List<String>> serviceImplementations) {
        Filer filer = this.processingEnv.getFiler();
        for (Entry<String, List<String>> mapEntry : serviceImplementations.entrySet()) {
            String extension = mapEntry.getKey();
            String resourcePath = "META-INF/services/" + extension;
            try {
                writeFile(filer, resourcePath, mapEntry);
            } catch (IOException e) {
                log(Kind.ERROR, "UNEXPECTED ERROR: {}", e.getMessage());
            }
        }
    }


    private void writeFile(
        Filer filer,
        String resourcePath,
        Entry<String, List<String>> entry
    ) throws IOException {
        FileObject resourceFile = filer
            .getResource(StandardLocation.CLASS_OUTPUT, "", resourcePath);
        Set<String> oldExtensions = read(resourceFile);
        Set<String> allExtensions = new LinkedHashSet<>();
        allExtensions.addAll(oldExtensions);
        allExtensions.addAll(entry.getValue());
        resourceFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourcePath);
        write(allExtensions, resourceFile);
        //log(Kind.WARNING, "Generated service declaration file {}", resourceFile);
        System.out.println("[jext] :: Generated service declaration file "+resourceFile.getName());
    }


    private Set<String> read(FileObject resourceFile) {
        Set<String> lines = new LinkedHashSet<>();
        try {
            try (BufferedReader reader = new BufferedReader(resourceFile.openReader(true))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            //
        }
        return lines;
    }


    private void write(Set<String> lines, FileObject resourceFile) {
        try {
            try (BufferedWriter writer = new BufferedWriter(resourceFile.openWriter())) {
                for (String line : lines) {
                    writer.append(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            log(Kind.ERROR, "error writing {} : {}", resourceFile.toUri(), e.getMessage());
        }
    }


    private void log(Kind kind, String message, Object... messageArgs) {
        processingEnv.getMessager().printMessage(
            kind,
            "[jext] :: " + String.format(message.replace("{}", "%s"), messageArgs)
        );
    }


    private void log(Kind kind, Element element, String message, Object... messageArgs) {
        processingEnv.getMessager().printMessage(
            kind,
            "[jext] at " + element.asType().toString() + " :: " + String
                .format(message.replace("{}", "%s"), messageArgs)
        );
    }

}