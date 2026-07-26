/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.checkstyle.checks;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;


class CustomChecksTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void declarationParametersAreCheckedStructurally() throws Exception {
        String source = """
                class Sample {
                    void valid(
                            String first,
                            int second
                    ) {
                        call(first, second);
                    }
                
                    void firstParameterOnDeclaration(String value) {
                    }
                
                    void twoParametersOnFollowingLine(
                            String first, int second
                    ) {
                    }
                
                    Sample(String value) {
                    }
                
                    <T> Sample(T value) {
                    }
                }
                """;

        assertEquals(
                4,
                violations(source, DeclarationParametersPerLineCheck.class).size()
        );
    }

    @Test
    void typeClosingLineIgnoresCallsAndSupportsNestedTypes() throws Exception {
        String valid = """
                class Sample {
                
                    void method() {
                        call("value");
                    }
                
                    class Nested {
                
                        void nestedMethod() {
                        }
                
                    }
                
                }
                """;
        String invalid = """
                class Sample {
                    void method() {
                    }
                }
                """;

        assertEquals(0, violations(valid, TypeClosingBlankLineCheck.class).size());
        assertEquals(1, violations(invalid, TypeClosingBlankLineCheck.class).size());
    }

    @Test
    void exactlyOneFinalLineBreakIsAllowedAndErrorPointsAtClosingBrace() throws Exception {
        String valid = "class Sample {\n\n}\n";
        String missingFinalLineBreak = "class Sample {\n\n}";
        String additionalFinalLineBreak = "class Sample {\n\n}\n\n";

        assertEquals(0, violations(valid, TypeClosingBlankLineCheck.class).size());
        List<AuditEvent> invalidViolations = violations(
                missingFinalLineBreak,
                TypeClosingBlankLineCheck.class
        );
        assertEquals(1, invalidViolations.size());
        assertEquals(3, invalidViolations.get(0).getLine());
        List<AuditEvent> additionalLineViolations = violations(
                additionalFinalLineBreak,
                TypeClosingBlankLineCheck.class
        );
        assertEquals(1, additionalLineViolations.size());
        assertEquals(3, additionalLineViolations.get(0).getLine());
    }

    @Test
    void typeBoundariesRejectMissingBlankLines() throws Exception {
        String missingAtStart = javaSource(
                "class Sample {",
                "    int value;",
                "",
                "}"
        );
        String missingAtEnd = javaSource(
                "class Sample {",
                "",
                "    int value;",
                "}"
        );
        String missingAfterClosingBrace = javaSource(
                "class Sample {",
                "",
                "}",
                "class Next {",
                "",
                "}"
        );

        assertEquals(
                1,
                violations(missingAtStart, TypeClosingBlankLineCheck.class).size()
        );
        assertEquals(
                1,
                violations(missingAtEnd, TypeClosingBlankLineCheck.class).size()
        );
        assertEquals(
                1,
                violations(missingAfterClosingBrace, TypeClosingBlankLineCheck.class).size()
        );
    }

    @Test
    void typeBoundariesRejectAdditionalBlankLines() throws Exception {
        String additionalAtStart = javaSource(
                "class Sample {",
                "",
                "",
                "    int value;",
                "",
                "}"
        );
        String additionalAtEnd = javaSource(
                "class Sample {",
                "",
                "    int value;",
                "",
                "",
                "}"
        );
        String additionalAfterClosingBrace = javaSource(
                "class Sample {",
                "",
                "}",
                "",
                "",
                "class Next {",
                "",
                "}"
        );

        assertEquals(
                1,
                violations(additionalAtStart, TypeClosingBlankLineCheck.class).size()
        );
        assertEquals(
                1,
                violations(additionalAtEnd, TypeClosingBlankLineCheck.class).size()
        );
        assertEquals(
                1,
                violations(additionalAfterClosingBrace, TypeClosingBlankLineCheck.class).size()
        );
    }

    @Test
    void everyTypeKindAndNestedTypeEnforcesItsBlankLines() throws Exception {
        List<String> declarations = List.of(
                "class Sample",
                "interface Sample",
                "enum Sample",
                "@interface Sample",
                "record Sample()"
        );
        for (String declaration : declarations) {
            String missingInnerBlankLine = javaSource(
                    declaration + " {",
                    "}"
            );
            assertEquals(
                    1,
                    violations(
                            missingInnerBlankLine,
                            TypeClosingBlankLineCheck.class
                    ).size(),
                    declaration
            );
        }

        String nestedTypeWithoutBlankLineAfterClosingBrace = javaSource(
                "class Outer {",
                "",
                "    class Nested {",
                "",
                "    }",
                "    int value;",
                "",
                "}"
        );
        assertEquals(
                1,
                violations(
                        nestedTypeWithoutBlankLineAfterClosingBrace,
                        TypeClosingBlankLineCheck.class
                ).size()
        );
    }

    @Test
    void methodsAreSeparatedBeforeJavadocOrComments() throws Exception {
        String valid = javaSource(
                "class Sample {",
                "",
                "    void first() {",
                "    }",
                "",
                "    /**",
                "     * Second method.",
                "     */",
                "    void second() {",
                "    }",
                "",
                "    // Third method.",
                "    void third() {",
                "    }",
                "    // This comment does not replace the separator.",
                "",
                "    void fourth() {",
                "    }",
                "",
                "}",
                ""
        );
        String missingBlankLine = javaSource(
                "class Sample {",
                "",
                "    void first() {",
                "    }",
                "    /** Second method. */",
                "    void second() {",
                "    }",
                "",
                "}",
                ""
        );
        String additionalBlankLine = javaSource(
                "class Sample {",
                "",
                "    void first() {",
                "    }",
                "",
                "",
                "    // Second method.",
                "    void second() {",
                "    }",
                "",
                "}",
                ""
        );

        assertEquals(0, violations(valid, CallableSeparationCheck.class).size());
        assertEquals(
                1,
                violations(missingBlankLine, CallableSeparationCheck.class).size()
        );
        assertEquals(
                1,
                violations(additionalBlankLine, CallableSeparationCheck.class).size()
        );
    }

    @Test
    void constructorsMustAlsoBeSeparatedByOneBlankLine() throws Exception {
        String invalid = javaSource(
                "class Sample {",
                "",
                "    Sample() {",
                "    }",
                "    Sample(",
                "            int value",
                "    ) {",
                "    }",
                "",
                "}",
                ""
        );

        assertEquals(1, violations(invalid, CallableSeparationCheck.class).size());
    }

    @Test
    void blockBoundariesDoNotConflictWithTypeClosingLine() throws Exception {
        String valid = """
                class Sample {
                
                    void method() {
                    }
                
                }
                """;
        String invalid = """
                class Sample {
                    void start() {
                
                        call();
                    }
                
                    void end() {
                        call();
                
                    }
                
                }
                """;

        assertEquals(0, violations(valid, BlockBoundaryBlankLineCheck.class).size());
        assertEquals(2, violations(invalid, BlockBoundaryBlankLineCheck.class).size());
    }

    @Test
    void packageAndImportSpacingRejectsInterveningContent() throws Exception {
        String valid = """
                package sample;
                
                
                import java.util.List;
                
                
                /**
                 * Sample.
                 */
                class Sample {
                
                }
                
                """;
        String invalid = """
                package sample;
                
                // Imports must immediately follow the two blank lines.
                import java.util.List;
                
                // A regular comment is not type documentation.
                class Sample {
                
                }
                
                """;

        assertEquals(0, violations(valid, PackageImportSpacingCheck.class).size());
        assertEquals(2, violations(invalid, PackageImportSpacingCheck.class).size());
    }

    @Test
    void packageAndImportSpacingIgnoresLineComments() throws Exception {
        String valid = javaSource(
                "package sample;",
                "",
                "// This comment is transparent.",
                "",
                "import java.util.List;",
                "",
                "// This comment is also transparent.",
                "",
                "/**",
                " * Sample.",
                " */",
                "class Sample {",
                "",
                "}",
                ""
        );

        assertEquals(0, violations(valid, PackageImportSpacingCheck.class).size());
    }

    private String javaSource(
            String... lines
    ) {
        return String.join("\n", lines) + "\n";
    }

    @SafeVarargs
    private List<AuditEvent> violations(
            String source,
            Class<? extends AbstractCheck>... checks
    ) throws Exception {
        Path sourceFile = temporaryDirectory.resolve("Sample.java");
        Files.writeString(sourceFile, source);

        DefaultConfiguration checkerConfiguration = new DefaultConfiguration("Checker");
        checkerConfiguration.addAttribute("charset", "UTF-8");
        DefaultConfiguration treeWalkerConfiguration = new DefaultConfiguration("TreeWalker");
        checkerConfiguration.addChild(treeWalkerConfiguration);
        for (Class<? extends AbstractCheck> check : checks) {
            treeWalkerConfiguration.addChild(new DefaultConfiguration(check.getName()));
        }

        List<AuditEvent> result = new ArrayList<>();
        Checker checker = new Checker();
        checker.setModuleClassLoader(getClass().getClassLoader());
        checker.addListener(new CollectingAuditListener(result));
        checker.configure(checkerConfiguration);
        try {
            checker.process(List.of(sourceFile.toFile()));
        } finally {
            checker.destroy();
        }
        return result;
    }

    private record CollectingAuditListener(List<AuditEvent> events) implements AuditListener {

        @Override
        public void auditStarted(
                AuditEvent event
        ) {
            // Nothing to collect.
        }

        @Override
        public void auditFinished(
                AuditEvent event
        ) {
            // Nothing to collect.
        }

        @Override
        public void fileStarted(
                AuditEvent event
        ) {
            // Nothing to collect.
        }

        @Override
        public void fileFinished(
                AuditEvent event
        ) {
            // Nothing to collect.
        }

        @Override
        public void addError(
                AuditEvent event
        ) {
            events.add(event);
        }

        @Override
        public void addException(
                AuditEvent event,
                Throwable throwable
        ) {
            throw new AssertionError(throwable);
        }

    }

}
