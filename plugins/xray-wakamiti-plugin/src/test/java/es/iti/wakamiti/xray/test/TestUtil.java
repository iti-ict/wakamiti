/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.test;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

import es.iti.wakamiti.xray.internal.Util;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;


public class TestUtil {

    @Test
    public void testDistincByKey() {
        List<TestCase> tests = List.of(
                new TestCase()
                        .issue(new JiraIssue()
                                .summary("Test Summary")),
                new TestCase()
                        .issue(new JiraIssue()
                                .summary("Test Summary")),
                new TestCase()
                        .issue(new JiraIssue()
                                .summary("Test Summary 2")));

        List<TestCase> result = tests.stream()
                .filter(Util.distinctByKey(testCase -> testCase.getJira().getSummary()))
                .collect(Collectors.toList());

        assertThat(result).hasSize(2);
    }

}
