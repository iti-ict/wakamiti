/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.xray.api;


import java.net.URL;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import es.iti.wakamiti.api.util.Pair;
import es.iti.wakamiti.xray.model.TestCase;


/**
 * Provides access to the Jira Api service.
 */
public class JiraApi extends BaseApi {

    private static final String API_ISSUE = "/rest/api/2/issue";
    private final Logger logger;

    /**
     * Creates a Jira REST client authenticated with a pre-encoded Basic credential.
     *
     * @param urlBase Jira base URL, such as {@code https://example.atlassian.net}
     * @param credentials Base64-encoded credentials, without the {@code Basic} scheme
     * @param logger logger used to trace Jira requests and synchronization decisions
     */
    public JiraApi(
            URL urlBase,
            String credentials,
            Logger logger
    ) {
        super(urlBase, "Basic " + credentials, logger);
        this.logger = logger;
    }

    /**
     * Uploads a file as an attachment to a Jira issue.
     * <p>
     * Jira identifies the target by its issue key or numeric identifier. The file
     * is sent as multipart form data and the Atlassian CSRF check is explicitly
     * disabled for this attachment request.
     *
     * @param id Jira issue key or identifier that will own the attachment
     * @param attachment local path of the file to upload
     */
    public void addAttachment(
            String id,
            Path attachment
    ) {
        post(API_ISSUE + "/" + id + "/attachments", attachment.toFile());
    }

    /**
     * Updates Jira summaries for test cases whose local representation changed.
     * <p>
     * Each pair contains the remote test currently stored in Xray as its key and
     * the desired local test as its value. Only the Jira summary is changed; the
     * Xray test definition and associations are left untouched.
     *
     * @param testCases changed test-case pairs, with the old remote value first and
     *        the replacement local value second
     */
    public void updateTestCases(
            List<Pair<TestCase, TestCase>> testCases
    ) {
        testCases.forEach(p -> {
            TestCase oldTest = p.key();
            TestCase newTest = p.value();

            updateIssue(oldTest.getIssueId(), newTest.getJira().getSummary());
        });
    }

    private void updateIssue(
            String id,
            String newSummary
    ) {
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
