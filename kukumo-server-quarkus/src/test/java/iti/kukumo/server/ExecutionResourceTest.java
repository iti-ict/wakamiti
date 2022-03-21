/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.server;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import iti.kukumo.api.KukumoConfiguration;

@QuarkusTest
@TestSecurity(authorizationEnabled = false)
class ExecutionResourceTest {

    private static final int OK = 200;
    private String executionID;


    @Test
    void runKukumoWithSingleFile() throws Exception {
        runKukumoExecutionWithContentOf("simpleScenario.feature").then()
            .statusCode(OK);
        given()
            .when()
            .get("/executions/{executionID}",executionID)
            .then()
            .statusCode(200);
    }


    @Test
    void runKukumoWithWorkspace() throws Exception {
        runKukumoExecutionWithWorkspace("src/test/resources").then()
            .statusCode(OK);
        given()
            .when()
            .get("/executions/{executionID}",executionID)
            .then()
            .statusCode(200);
    }



    @Test
    void searchExecutions() throws Exception {
        runKukumoExecutionWithContentOf("simpleScenario.feature");
        runKukumoExecutionWithContentOf("simpleScenario.feature");
        runKukumoExecutionWithContentOf("simpleScenario.feature");
        given()
            .when()
            .get("/executions?size=1&page=2")
            .prettyPeek()
            .then()
            .statusCode(OK);
    }


    private Response runKukumoExecutionWithContentOf(String feature) throws Exception {
        return extractExecutionId(
            given()
            .when()
            .contentType(ContentType.TEXT)
            .body(Files.readString(Path.of("src/test/resources/"+feature)))
            .post("/executions?resourceType=gherkin")
        );
    }

    private Response runKukumoExecutionWithWorkspace(String workspace) throws Exception {
        return extractExecutionId(
            given()
            .when()
            .contentType(ContentType.TEXT)
            .post("/executions?workspace="+workspace)
        );
    }



    private Response extractExecutionId(Response response) throws IOException {
        var data = response.body().print();
        if (response.statusCode() >= 400) {
            return response;
        }
        var json = new ObjectMapper().readValue(data, HashMap.class);
        this.executionID = (String)((Map<?,?>)json.get("data")).get(KukumoConfiguration.EXECUTION_ID);
        return response;
    }

}