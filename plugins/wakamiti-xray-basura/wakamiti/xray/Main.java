package es.iti.wakamiti.xray;

import es.iti.wakamiti.services.AuthService;
import es.iti.wakamiti.services.TestRepositoryService;
import es.iti.wakamiti.services.TestSetService;
import es.iti.wakamiti.services.TestExecutionService;
import es.iti.wakamiti.services.TestPlanService;
import es.iti.wakamiti.services.TestRunService;

public class Main {

    public static void main(String[] args) {
        // Autenticación y obtención del JWT
        String jwt = AuthService.getJWT();
        if (jwt != null) {
            // Crear carpeta en el Test Repository
            TestRepositoryService.createTestFolder(jwt, "WAK", "Test Repository Name", "-1");

            // Crear Test Set
            String testSetResponse = TestSetService.createTestSet("New Test Set", "This is a description for the new test set.");
            if (testSetResponse != null) {
                System.out.println("Test Set Key: " + testSetResponse);
            }

            // Crear Test Execution
            String testExecutionResponse = TestExecutionService.createTestExecution("New Test Execution", "This is a description for the new test execution.");
            if (testExecutionResponse != null) {
                System.out.println("Test Execution Key: " + testExecutionResponse);
            }

            // Crear Test Plan
            String testPlanResponse = TestPlanService.createTestPlan("New Test Plan", "This is a description for the new test plan.");
            if (testPlanResponse != null) {
                System.out.println("Test Plan Key: " + testPlanResponse);
            }

            // Actualizar Test Run
            TestRunService.updateTestRun(jwt, 12345, "FAIL", "new comment", "ampr", new String[]{"test-114", "test-115", "test-116"}, new Evidence[]{
                    new Evidence("test1.txt", "plain/text", "(base64 encoding...)"),
                    new Evidence("test2.txt", "plain/text", "(base64 encoding...)")
            }, new Example[]{
                    new Example(1379, "TODO")
            });
            
        }
    }
}
