/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import iti.wakamiti.api.WakamitiConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class ExecutionResourceTest {

    private static final int OK = 200;
    private String executionID;


    @Test
    void runWakamitiWithSingleFile() throws Exception {
        runWakamitiExecutionWithContentOf("simpleScenario.feature").then()
                .statusCode(OK);
        given()
                .when()
                .get("/executions/{executionID}", executionID)
                .then()
                .statusCode(200);
    }


    @Test
    void runWakamitiWithWorkspace() throws Exception {
        runWakamitiExecutionWithWorkspace("src/test/resources").then()
                .statusCode(OK);
        given()
                .when()
                .get("/executions/{executionID}", executionID)
                .then()
                .statusCode(200);
    }


    @Test
    void searchExecutions() throws Exception {
        runWakamitiExecutionWithContentOf("simpleScenario.feature");
        runWakamitiExecutionWithContentOf("simpleScenario.feature");
        runWakamitiExecutionWithContentOf("simpleScenario.feature");
        given()
                .when()
                .get("/executions?size=1&page=2")
                .prettyPeek()
                .then()
                .statusCode(OK);
    }


    private Response runWakamitiExecutionWithContentOf(String feature) throws Exception {
        return extractExecutionId(
                given()
                        .when()
                        .contentType(ContentType.TEXT)
                        .body(Files.readString(Path.of("src/test/resources/" + feature)))
                        .post("/executions?resourceType=gherkin")
        );
    }

    private Response runWakamitiExecutionWithWorkspace(String workspace) throws Exception {
        return extractExecutionId(
                given()
                        .when()
                        .contentType(ContentType.TEXT)
                        .post("/executions?workspace=" + workspace)
        );
    }

    private Response extractExecutionId(Response response) throws IOException {
        var data = response.body().print();
        if (response.statusCode() >= 400) {
            return response;
        }
        var json = new ObjectMapper().readValue(data, HashMap.class);
        this.executionID = (String) ((Map<?, ?>) json.get("data")).get(WakamitiConfiguration.EXECUTION_ID);
        return response;
    }

    private static RequestSpecification given() {
        return RestAssured.given().port(8081);
    }
}