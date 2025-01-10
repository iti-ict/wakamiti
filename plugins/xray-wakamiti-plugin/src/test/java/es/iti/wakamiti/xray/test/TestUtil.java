package es.iti.wakamiti.xray.test;

import es.iti.wakamiti.xray.internal.Util;
import es.iti.wakamiti.xray.model.JiraIssue;
import es.iti.wakamiti.xray.model.TestCase;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

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
