/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.azure;

import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.extensions.StepContributor;


@I18nResource("mock-steps")
public class MockSteps implements StepContributor {


    @Override
    public String info() {
        return "test";
    }

    @Step(value = "step.ok")
    public void ok() {

    }

    @Step(value = "step.fail")
    public void fail() {
        throw new AssertionError("\"The expected and actual responses have differences:\n" +
                "\t-segment 'message' expected: 'Entity with identifier -5 can not be found.', actual: 'The Entorno can not be found due to the following error: Entity does not exists.'\n" +
                " expected:<{\n" +
                "  \"[\n" +
                "\t\t\tstatus\" : 404,\n" +
                "  \"error\" : \"NotFound\",\n" +
                "  \"message\" : \"Entitywithidentifier-5cannotbefound.\n" +
                "\t\t]\"\n" +
                "}> but was:<{\n" +
                "  \"[\n" +
                "\t\t\ttimestamp\" : \"2023-09-19T15: 08: 26.637+00: 00\",\n" +
                "  \"status\" : 404,\n" +
                "  \"error\" : \"NotFound\",\n" +
                "  \"message\" : \"TheEntornocannotbefoundduetothefollowingerror: Entitydoesnotexists.\",\n" +
                "  \"path\" : \"/api/configuracion-service/entornos/-5\n" +
                "\t\t]\"\n" +
                "}>\"");
    }


}