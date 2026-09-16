/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.checkstyle.checks;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;


class HeaderConfigurationTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void acceptsFlexibleCopyrightEndYear() throws Exception {
        assertEquals(0, violations(header("2022-2026")).size());
        assertEquals(0, violations(header("2022-2027")).size());
    }

    @Test
    void rejectsCopyrightYearsOutsideTheSupportedRange() throws Exception {
        assertEquals(1, violations(header("2022-2021")).size());
        assertEquals(1, violations(header("2023-2027")).size());
        assertEquals(1, violations(header("2022-2100")).size());
    }

    @Test
    void rejectsChangesToTheOwnerOrLicense() throws Exception {
        assertEquals(
                1,
                violations(header("2022-2027").replace("(ITI)", "(OTHER)")).size()
        );
        assertEquals(
                1,
                violations(header("2022-2027").replace("MPL/2.0", "MPL/3.0")).size()
        );
    }

    private String header(
            String years
    ) {
        return String.join(
                "\n",
                "/*",
                " * Copyright (c) %s Instituto Tecnológico de Informática (ITI)".formatted(years),
                " *",
                " * This Source Code Form is subject to the terms of the Mozilla Public",
                " * License, v. 2.0. If a copy of the MPL was not distributed with this",
                " * file, You can obtain one at https://mozilla.org/MPL/2.0/.",
                " *" + "/",
                "package example;",
                ""
        );
    }

    private List<AuditEvent> violations(
            String source
    ) throws Exception {
        Path sourceFile = temporaryDirectory.resolve("Sample.java");
        Files.writeString(sourceFile, source);

        DefaultConfiguration checkerConfiguration = new DefaultConfiguration("Checker");
        checkerConfiguration.addAttribute("charset", "UTF-8");
        DefaultConfiguration headerConfiguration = new DefaultConfiguration("RegexpHeader");
        headerConfiguration.addAttribute(
                "headerFile",
                Path.of(getClass().getResource("/es/iti/wakamiti/checkstyle/java.header").toURI())
                        .toUri()
                        .toString()
        );
        headerConfiguration.addAttribute("fileExtensions", "java");
        checkerConfiguration.addChild(headerConfiguration);

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
