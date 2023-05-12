/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.server;


import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.WakamitiStepRunContext;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.datatypes.Assertion;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.Locale;


@I18nResource("test-wakamiti-steps")
public class WakamitiTestSteps implements StepContributor {

    private int value1;
    private float value2;
    private double result;
    private float[] values;
    private float[] results;
    private String word;
    private String text;


    @Step(value = "given.set.of.numbers")
    public void setOfNumbers() {

    }


    @Step(value = "given.two.numbers", args = {"value1:int", "value2:float"})
    public void setNumbers(Integer value1, Float value2) throws Exception {
        this.value1 = value1;
        this.value2 = value2;
    }


    @Step(value = "when.calculate.product")
    public void multiply() {
        this.result = (value1 * value2);
    }


    @Step(value = "then.result.equals", args = "float")
    public void assertResultEquals(Float expectedResult) {
        Assertions.assertThat(result).isCloseTo(expectedResult.doubleValue(), Offset.offset(0.01));
    }


    @Step(value = "given.number.and.table", args = "float")
    public void setNumberAndTable(Float n, DataTable table) throws ParseException {
        this.value2 = n;
        this.values = new float[table.rows()];
        for (int i = 0; i < values.length; i++) {
            values[i] = parseFloat(table.value(i, 0));
        }
    }


    @Step(value = "when.operation.table")
    public void multiplyTable() {
        this.results = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            results[i] = values[i] * value2;
        }
    }


    @Step(value = "then.result.table")
    public void assertTableResultEquals(DataTable table) throws ParseException {
        final float[] tableValues = new float[table.rows()];
        for (int i = 0; i < tableValues.length; i++) {
            tableValues[i] = parseFloat(table.value(i, 0));
        }
        Assertions.assertThat(results).containsExactly(tableValues, Offset.offset(0.01f));
    }


    @Step(value = "given.word.and.text", args = "word")
    public void setWordAndText(String word, Document text) {
        this.word = word;
        this.text = text.getContent();
    }


    @Step(value = "then.each.line.starts.with.text")
    public void assertTextStartWithWord() throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                Assertions.assertThat(line).startsWith(word);
            }
        }
    }


    @Step(value = "simple.step.with.multiple.asserts", args = {
            "a:integer-assertion",
            "b:integer",
            "c:text-assertion"
    })
    public void simpleStepWithMultipleAsserts(Assertion<Integer> a, Long b, Assertion<String> c) {
        // nothing
    }


    private float parseFloat(String string) {
        Locale locale = WakamitiStepRunContext.current().stepLocale();
        WakamitiDataTypeRegistry typeRegistry = WakamitiStepRunContext.current().typeRegistry();
        return (Float) typeRegistry.getType("float").parse(locale, string);
    }

}