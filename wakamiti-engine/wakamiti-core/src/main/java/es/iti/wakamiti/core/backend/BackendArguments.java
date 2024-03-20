/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.backend;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.Pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Represents the arguments associated with a step in the Wakamiti framework.
 * It parses, infers, and validates the arguments for a step based on the
 * Step annotation and the method signature of the step provider.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class BackendArguments implements Iterable<Pair<String, String>> {

    private final Class<?> stepProviderClass;
    private final Step stepAnnotation;
    private final Class<?>[] methodArgTypes;
    private final WakamitiDataTypeRegistry typeRegistry;
    private final List<Pair<String, String>> argumentMap = new ArrayList<>();


    public BackendArguments(
            Class<?> stepProviderClass, Method stepMethod,
            WakamitiDataTypeRegistry typeRegistry
    ) {
        this.stepProviderClass = stepProviderClass;
        this.stepAnnotation = stepMethod.getAnnotation(Step.class);
        this.methodArgTypes = stepMethod.getParameterTypes();
        this.typeRegistry = typeRegistry;
        parseDeclaredArguments();
        inferNonDeclaredArguments();
        completeExtraArguments();
        validateArguments();
    }

    /**
     * Gets the number of arguments associated with the step.
     *
     * @return The number of arguments.
     */
    public int size() {
        return argumentMap.size();
    }

    /**
     * Gets the argument pair at the specified index.
     *
     * @param index The index of the argument pair.
     * @return The argument pair at the specified index.
     */
    public Pair<String, String> get(int index) {
        return argumentMap.get(index);
    }

    /**
     * Returns an iterator over the argument pairs.
     *
     * @return An iterator over the argument pairs.
     */
    @Override
    public Iterator<Pair<String, String>> iterator() {
        return argumentMap.iterator();
    }

    /**
     * Parses the declared arguments from the Step annotation and populates the argument map.
     */
    protected void parseDeclaredArguments() {
        for (int i = 0; i < stepAnnotation.args().length; i++) {
            String argName = "";
            String argType = "";
            final String[] nameAndType = stepAnnotation.args()[i].split(":");
            if (nameAndType.length == 1) {
                argName = RunnableBackend.UNNAMED_ARG;
                argType = nameAndType[0];
            } else if (nameAndType.length == 2) {
                argName = nameAndType[0];
                argType = nameAndType[1];
            } else {
                throwWrongStepDefinitionException(
                        "Argument '{} is not well formed",
                        stepAnnotation.args()[i]
                );
            }
            argumentMap.add(new Pair<>(argName, argType));
        }
    }

    /**
     * Attempts to infer non-declared arguments automatically based on the method signature.
     * <p>
     * If the method annotated with @Step accepts arguments and the field '{@code args}'
     * of @Step is not declared, we'll attempt to infer the arguments
     * automatically. This contributes to make the step declarations less
     * verbose.
     * </p>
     */
    protected void inferNonDeclaredArguments() {
        if (argumentMap.isEmpty()) {
            for (Class<?> methodArgType : methodArgTypes) {
                Optional<WakamitiDataType<?>> argType = typeRegistry
                        .findTypesForJavaType(methodArgType).findAny();
                argType.ifPresent(wakamitiDataType ->
                        argumentMap.add(new Pair<>(RunnableBackend.UNNAMED_ARG, wakamitiDataType.getName())));
            }
        }
    }

    /**
     * Validates the arguments based on the Step annotation and method signature.
     */
    protected void validateArguments() {
        String error = null;
        int i;
        for (i = 0; i < argumentMap.size(); i++) {
            error = validateArgument(i);
            if (error != null) {
                break;
            }
        }
        if (error != null) {
            throwWrongStepDefinitionException(
                    "Argument '{}:{}' does not match implementation argument type {} {}",
                    argumentMap.get(i).key(),
                    argumentMap.get(i).value(),
                    methodArgTypes[i],
                    error
            );
        }
    }

    /**
     * Validates a specific argument at the given index.
     *
     * @param index The index of the argument to validate.
     * @return An error message if validation fails, or null if the argument is valid.
     */
    protected String validateArgument(int index) {
        String error = null;
        if (argumentMap.get(index).value().equals(RunnableBackend.DOCUMENT_ARG)) {
            if (!methodArgTypes[index].equals(Document.class)) {
                error = "(expected " + Document.class + ")";
            }
        } else if (argumentMap.get(index).value().equals(RunnableBackend.DATATABLE_ARG)) {
            if (!methodArgTypes[index].equals(DataTable.class)) {
                error = "(expected " + DataTable.class + ")";
            }
        } else {
            final String typeName = argumentMap.get(index).value();
            final WakamitiDataType<?> type = typeRegistry.getType(typeName);
            if (type == null) {
                throwWrongStepDefinitionException("Type {} not registered.", typeName);
            }
            if (!type.getJavaType().equals(methodArgTypes[index])) {
                error = "(expected " + type.getJavaType() + ")";
            }
        }
        return error;
    }

    /**
     * Completes the argument map with arguments that are not part of the
     * step definition (currently document string and datatables, but new extra
     * arguments may be added in the future).
     */
    protected void completeExtraArguments() {
        int index = argumentMap.size();
        if (methodArgTypes.length - index == 0) {
            return;
        }
        boolean error = false;
        if (methodArgTypes.length - index == 1) {
            if (methodArgTypes[index].equals(Document.class)) {
                argumentMap.add(new Pair<>(RunnableBackend.DOCUMENT_ARG, RunnableBackend.DOCUMENT_ARG));
            } else if (methodArgTypes[index].equals(DataTable.class)) {
                argumentMap.add(new Pair<>(RunnableBackend.DATATABLE_ARG, RunnableBackend.DATATABLE_ARG));
            } else {
                error = true;
            }
        } else {
            error = true;
        }
        if (error) {
            String definitionInfo = argumentMap.stream()
                    .map(pair -> pair.key() + ":" + pair.value()).collect(Collectors.joining(", "));
            String implementationInfo = Stream.of(methodArgTypes).map(String::valueOf)
                    .collect(Collectors.joining(", "));
            throw new WrongStepDefinitionException(
                    stepProviderClass, stepAnnotation.value(),
                    "Step arguments not machting implementation arguments\n\tdefinition={}, implementation={}",
                    definitionInfo, implementationInfo
            );
        }
    }

    /**
     * Returns a string representation of the BackendArguments instance.
     *
     * @return A string representation of the BackendArguments instance.
     */
    @Override
    public String toString() {
        return argumentMap.toString();
    }

    /**
     * Throws a WrongStepDefinitionException with the specified message and arguments.
     *
     * @param message The error message.
     * @param args    Additional arguments for the error message.
     * @throws WrongStepDefinitionException Always thrown to indicate a wrong step definition.
     */
    private void throwWrongStepDefinitionException(
            String message,
            Object... args
    ) throws WrongStepDefinitionException {
        throw new WrongStepDefinitionException(
                stepProviderClass, stepAnnotation.value(), message, args
        );
    }

}