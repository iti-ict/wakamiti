/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.rest.helpers;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.rest.MatchMode;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static es.iti.wakamiti.api.util.JsonUtils.json;
import static es.iti.wakamiti.api.util.MapUtils.map;
import static org.assertj.core.api.Assertions.assertThat;


public class JsonHelperTest {

    private static final Logger LOG = LoggerFactory.getLogger(JsonHelperTest.class);
    private final JSONHelper helper = new JSONHelper();

    private static final String SINGLE = json(map("id", 1, "name", "User 1")).toString();
    private static final String LIST = json(List.of(
            map("id", 1, "name", "User 1"),
            map("id", 2, "name", "User 2"),
            map("id", 3, "name", "User 3")
    )).toString();

    @Test
    public void testAssertContentWhenStrictAndSingleWithSuccess() {
        helper.assertContent(SINGLE, SINGLE, MatchMode.STRICT);
    }

    @Test
    public void testAssertContentWhenStrictAnyOrderAndUnorderedSingleWithSuccess() {
        // prepare
        String expected = json(map("name", "User 1", "id", 1)).toString();

        // act
        helper.assertContent(expected, SINGLE, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testAssertContentWhenStrictAndListWithSuccess() {
        helper.assertContent(LIST, LIST, MatchMode.STRICT);
    }

    @Test
    public void testAssertContentWhenStrictAnyOrderAndUnorderedListWithSuccess() {
        // prepare
        String expected = json(List.of(
                map("id", 2, "name", "User 2"),
                map("name", "User 3", "id", 3),
                map("id", 1, "name", "User 1")
        )).toString();

        // act
        helper.assertContent(expected, LIST, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testAssertContentWhenLooseAndSingleWithSuccess() {
        helper.assertContent(SINGLE, SINGLE, MatchMode.LOOSE);
    }

    @Test
    public void testAssertContentWhenLooseAndSingleMissingFieldWithSuccess() {
        // prepare
        String expected = json(map("name", "User 1")).toString();

        // act
        helper.assertContent(expected, SINGLE, MatchMode.LOOSE);
    }

    @Test
    public void testAssertContentWhenLooseAndListWithSuccess() {
        helper.assertContent(LIST, LIST, MatchMode.LOOSE);
    }

    @Test
    public void testAssertContentWhenLooseAndListMissingElementAndFieldsWithSuccess() {
        // prepare
        String expected = json(List.of(
                map("name", "User 3"),
                map("id", 1.0)
        )).toString();

        // act
        helper.assertContent(expected, LIST, MatchMode.LOOSE);
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAndSingleWrongDataWithError() {
        // prepare
        String actual = json(map("id", 1, "name", "User X")).toString();

        // act
        try {
            helper.assertContent(SINGLE, actual, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-segment 'name' expected: 'User 1', actual: 'User X'");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAndListWrongDataWithError() {
        // prepare
        String actual = json(List.of(
                map("id", 1, "name", "User 1"),
                map("id", 2, "name", "User 2"),
                map("id", 3, "name", "User X")
        )).toString();

        // act
        try {
            helper.assertContent(LIST, actual, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-segment '[2].name' expected: 'User 3', actual: 'User X'");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAndSingleMissingFieldWithError() {
        // prepare
        String actual = json(map("name", "User 1")).toString();

        // act
        try {
            helper.assertContent(SINGLE, actual, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-root segment expected to have fields [id], but they are not present");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAndListMissingFieldWithError() {
        // prepare
        String actual = json(List.of(
                map("name", "User 1"),
                map("id", 2, "name", "User 2"),
                map("id", 3, "name", "User 3")
        )).toString();

        // act
        try {
            helper.assertContent(LIST, actual, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-segment '[0]' expected to have fields [id], but they are not present");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAndListMissingElementWithError() {
        // prepare
        String actual = json(List.of(
                map("id", 2, "name", "User 2"),
                map("id", 3, "name", "User 3")
        )).toString();

        // act
        try {
            helper.assertContent(LIST, actual, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-root segment expected size: 3, actual size: 2");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAnyOrderAndSingleMissingFieldWithError() {
        // prepare
        String actual = json(map("name", "User 1")).toString();

        // act
        try {
            helper.assertContent(SINGLE, actual, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-root segment expected to have fields [id], but they are not present");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAnyOrderAndListMissingFieldWithError() {
        // prepare
        String actual = json(List.of(
                map("name", "User 1"),
                map("id", 2, "name", "User 2"),
                map("id", 3, "name", "User 3")
        )).toString();

        // act
        try {
            helper.assertContent(LIST, actual, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-segment '[0]' expected to have fields [id], but they are not present");
            throw new WakamitiException(e);
        }
    }

    @Test(expected = WakamitiException.class)
    public void testAssertContentWhenStrictAnyOrderAndListMissingElementWithError() {
        // prepare
        String actual = json(List.of(
                map("id", 2, "name", "User 2"),
                map("id", 3, "name", "User 3")
        )).toString();

        // act
        try {
            helper.assertContent(LIST, actual, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            LOG.debug("Result", e);
            assertThat(e).hasMessageContaining("-root segment expected size: 3, actual size: 2");
            throw new WakamitiException(e);
        }
    }

}
