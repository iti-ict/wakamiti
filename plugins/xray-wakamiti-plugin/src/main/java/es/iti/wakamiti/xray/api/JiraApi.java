package es.iti.wakamiti.xray.api;

import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.model.Attachment;
import es.iti.wakamiti.xray.model.TestCase;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static es.iti.wakamiti.api.util.JsonUtils.json;

public class JiraApi extends BaseApi {

    private static final String API_ISSUE = "/rest/api/2/issue";
    private final Logger logger;

    public JiraApi(URL urlBase, String credentials, Logger logger) {
        super(urlBase, "Basic " + credentials, logger);
        this.logger = logger;
    }

    public void addAttachment(String id, Path attachment) {
        post(API_ISSUE + "/" + id + "/attachments", attachment.toFile());
    }

    public void updateTestCases(List<Pair<TestCase, TestCase>> testCases) {
        testCases.forEach(p -> {
            TestCase oldTest = p.key();
            TestCase newTest = p.value();

            updateIssue(oldTest.getIssueId(), newTest.getJira().getSummary());
        });
    }

    private void updateIssue(String id, String newSummary) {

        String payload = toJSON(Map.of(
                "update", Map.of(
                        "summary", Collections.singletonList(Map.of(
                                "set", newSummary
                        ))
                )));

        put(API_ISSUE + "/" + id, payload);

        logger.debug("Updated summary '{}' to Test case '{}'", newSummary, id);
    }

}
