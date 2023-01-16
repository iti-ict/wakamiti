/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.gherkin;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import iti.kukumo.api.util.JsonUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;

import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.KukumoStepRunContext;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.datatypes.Assertion;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@I18nResource("steps/test-kukumo-steps")
public class KukumoSteps implements StepContributor {

    private static final Logger LOGGER = LoggerFactory.getLogger("iti.kukumo.test");

    private int value1;
    private float value2;
    private double result;
    private float[] values;
    private float[] results;
    private String word;
    private String text;


    @Step(value = "given.set.of.numbers")
    public void setOfNumbers() {
        LOGGER.debug("Numbers type set");
    }

    @Step(value = "given.two.numbers", args = { "value1:int", "value2:float" })
    public void setNumbers(Integer value1, Float value2) throws Exception {
        this.value1 = value1;
        this.value2 = value2;
    }


    @Step(value = "when.calculate.product")
    public Object multiply() {
        this.result = (value1 * value2);
        return result;
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
    public Object multiplyTable() {
        this.results = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            results[i] = values[i] * value2;
        }
        return JsonUtils.json(results);
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


    @Step(value = "simple.step.with.multiple.asserts", args = { "a:integer-assertion", "b:integer",
                    "c:text-assertion" })
    public void simpleStepWithMultipleAsserts(Assertion<Integer> a, Long b, Assertion<String> c) {
        // nothing
    }


    private float parseFloat(String string) {
        Locale locale = KukumoStepRunContext.current().stepLocale();
        KukumoDataTypeRegistry typeRegistry = KukumoStepRunContext.current().typeRegistry();
        return (Float) typeRegistry.getType("float").parse(locale, string);
    }



    @Step(value = "given.today.is", args = { "date" })
    public Object setDate(LocalDate date) {
        LOGGER.info("Today is: {}", date);

        assert date != null : "date is null";

        assert date.getYear() == 2023 : "Year is " + date.getYear();
        assert date.getMonthValue() == 1 : "Month is " + date.getMonthValue();
        assert date.getDayOfMonth() == 10 : "Day is " + date.getDayOfMonth();

        return date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Step(value = "given.now.is", args = { "time" })
    public Object setTime(LocalTime time) {
        LOGGER.info("It is now: {}", time);
        assert time != null : "time is null";
        assert time.getHour() == 10 : "Hour is " + time.getHour();
        assert time.getMinute() == 5 : "Minute is " + time.getMinute();
        assert List.of(0, 3).contains(time.getSecond()) : "Second is " + time.getSecond();
        assert List.of(0, 123000000).contains(time.getNano()) : "Nano is " + time.getNano();

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"));
    }

    @Step(value = "given.instant.is", args = { "datetime" })
    public Object setDatetime(LocalDateTime datetime) {
        LOGGER.info("This instant is: {}", datetime);

        assert datetime != null : "datetime is null";

        assert datetime.getYear() == 2023 : "Year is " + datetime.getYear();
        assert datetime.getMonthValue() == 1 : "Month is " + datetime.getMonthValue();
        assert datetime.getDayOfMonth() == 10 : "Day is " + datetime.getDayOfMonth();

        assert datetime.getHour() == 10 : "Hour is " + datetime.getHour();
        assert datetime.getMinute() == 5 : "Minute is " + datetime.getMinute();
        assert List.of(0, 3).contains(datetime.getSecond()) : "Second is " + datetime.getSecond();
        assert List.of(0, 123000000).contains(datetime.getNano()) : "Nano is " + datetime.getNano();

        return datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    @Step(value = "given.integer", args = { "integer" })
    public Object setInteger(Long number) {
        LOGGER.info("This integer is: {}", number);
        assert number == 6L : "Integer is " + number;
        return number;
    }

    @Step(value = "given.decimal", args = { "decimal" })
    public Object setDecimal(BigDecimal number) {
        LOGGER.info("This bigdecimal is: {}", number);
        assert number.doubleValue() == 3.2 : "BigDecimal is " + number;
        return number.toString().replace(".", ",");
    }

    @Step(value = "given.int", args = { "int" })
    public Object setInt(Integer number) {
        LOGGER.info("This int is: {}", number);
        assert number == 6 : "Int is " + number;
        return number;
    }

    @Step(value = "given.short", args = { "short" })
    public Object setShort(Short number) {
        LOGGER.info("This short is: {}", number);
        assert number.intValue() == 6 : "Short is " + number;
        return number;
    }

    @Step(value = "given.long", args = { "long" })
    public Object setLong(Long number) {
        LOGGER.info("This long is: {}", number);
        assert number == 6L : "Long is " + number;
        return number;
    }

    @Step(value = "given.biginteger", args = { "biginteger" })
    public Object setBigInteger(BigInteger number) {
        LOGGER.info("This biginteger is: {}", number);
        assert number.intValue() == 6 : "BigInteger is " + number;
        return number;
    }

    @Step(value = "given.byte", args = { "byte" })
    public Object setByte(Byte number) {
        LOGGER.info("This byte is: {}", number);
        assert number.intValue() == 6 : "Byte is " + number;
        return number;
    }

    @Step(value = "given.double", args = { "double" })
    public Object setDouble(Double number) {
        LOGGER.info("This double is: {}", number);
        assert number == 3.2 : "Double is " + number;
        return number.toString().replace(".", ",");
    }

    @Step(value = "given.float", args = { "float" })
    public Object setFloat(Float number) {
        LOGGER.info("This float is: {}", number);
        assert number == 3.2F : "Float is " + number;
        return number.toString().replace(".", ",");
    }

    @Step(value = "given.bigdecimal", args = { "bigdecimal" })
    public Object setBigDecimal(BigDecimal number) {
        LOGGER.info("This bigdecimal is: {}", number);
        assert number.doubleValue() == 3.2;
        return number.toString().replace(".", ",");
    }

    @Step(value = "given.string", args = { "string" })
    public Object setString(String string) {
        LOGGER.info("This string is: {}", string);
        assert Objects.equals(string, "ABC aa");
        return string;
    }

    @Step(value = "given.text", args = { "text" })
    public Object setText(String text) {
        LOGGER.info("This text is: {}", text);
        assert Objects.equals(text, "ABC aa");
        return text;
    }

    @Step(value = "given.word", args = { "word" })
    public Object setWord(String word) {
        LOGGER.info("This word is: {}", word);
        assert Objects.equals(word, "ABC");
        return word;
    }

    @Step(value = "given.id", args = { "id" })
    public Object setId(String id) {
        LOGGER.info("This id is: {}", id);
        assert Objects.equals(id, "ABC");
        return id;
    }

    @Step(value = "given.file", args = { "file" })
    public Object setFile(File file) {
        LOGGER.info("This file is: {}", file);
        assert file != null : "File is null";
        assert Objects.equals(file.getPath(), "src/test/resources/features/properties/ABC") : "File is " + file;
        return file.getPath();
    }

    @Step(value = "given.url" , args = { "url" })
    public Object setURL(URL url) throws MalformedURLException {
        LOGGER.info("This url is: {}", url);
        assert url != null : "URL is null";
        assert Objects.equals(url, new URL("https://test.es/ABC")) : "URL is " + url;
        return url.toString();
    }

}