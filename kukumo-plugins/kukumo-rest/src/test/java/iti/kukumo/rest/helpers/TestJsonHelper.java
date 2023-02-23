/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.rest.helpers;

import io.restassured.builder.ResponseBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import iti.kukumo.api.util.MatcherAssertion;
import iti.kukumo.rest.MatchMode;
import org.hamcrest.Matchers;
import org.junit.ComparisonFailure;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static iti.kukumo.rest.TestUtil.json;
import static iti.kukumo.rest.TestUtil.map;
import static org.assertj.core.api.Assertions.assertThat;

public class TestJsonHelper {

    private static final String jsonNormal = json(map(
            "a", 1,
            "b", List.of("x", "y", "z"),
            "c", map(
                    "aa", 22,
                    "bb", List.of(
                            map("a", 1, "b", 2),
                            map("c", 3, "d", 4)
                    ),
                    "cc", 44
            )
    ));
    private static final String jsonReordered = json(map(
            "a", 1,
            "b", List.of("x", "z", "y"),
            "c", map(
                    "aa", 22,
                    "bb", List.of(
                            map("c", 3, "d", 4),
                            map("a", 1, "b", 2)
                    ),
                    "cc", 44
            )
    ));
    private static final String jsonMissing = json(map(
            "a", 1,
            "c", map(
                    "aa", 22,
                    "bb", List.of(
                            map("a", 1, "b", 2),
                            map("c", 3, "d", 4)
                    ),
                    "cc", 44
            )
    ));
    private static final String jsonUnordered = json(map(
            "b", List.of("z", "y", "x"),
            "c", map(
                    "bb", List.of(
                            map("d", 4, "c", 3),
                            map("b", 2, "a", 1)
                    ),
                    "aa", 22,
                    "cc", 44),
            "a", 1
    ));
    private static final List<String> jsonWrong = List.of(
            json(map(
                    "a", 2,
                    "b", List.of("x", "y", "w"),
                    "c", map("aa", 22, "bb", 34, "cc", 44)
            )),
            json(map(
                    "a", 2,
                    "b", List.of("x", "y", "z"),
                    "c", map(
                            "aa", 22,
                            "bb", List.of(
                                    map("a", 1, "b", 2),
                                    map("c", 3, "d", 4)
                            ),
                            "cc", 44
                    )
            )),
            json(map(
                    "a", 1,
                    "b", List.of("x", "y", "w"),
                    "c", map(
                            "aa", 22,
                            "bb", List.of(
                                    map("a", 1, "b", 2),
                                    map("c", 3, "d", 4)
                            ),
                            "cc", 44
                    )
            )),
            json(map(
                    "a", 1,
                    "b", List.of("x", "y", "z"),
                    "c", map("aa", 22, "bb", 34, "cc", 44)
            ))
    );
    private static final String jsonExtra = json(map(
            "a", 1,
            "b", List.of("x", "y", "z"),
            "c", map(
                    "aa", 22,
                    "bb", List.of(
                            map("a", 1, "b", 2),
                            map("c", 3, "d", 4),
                            map("e", 5, "f", 6)
                    ),
                    "cc", 44,
                    "dd", 55
            ),
            "d", 5.55
    ));
    private static final String jsonEmpty = json(map(
            "a", 1,
            "b", List.of(),
            "c", map(
                    "aa", 22,
                    "bb", List.of(
                            map("a", 1, "b", 2),
                            map("c", 3, "d", 4)
                    ),
                    "cc", 44
            )
    ));
    private final JSONHelper helper = new JSONHelper();

    @Test
    public void testStrictNormal() {
        helper.assertContent(jsonNormal, jsonNormal, MatchMode.STRICT);
    }

    @Test
    public void testStrictAnyOrderNormal() {
        helper.assertContent(jsonNormal, jsonNormal, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseNormal() {
        helper.assertContent(jsonNormal, jsonNormal, MatchMode.LOOSE);
    }

    @Test
    public void testStrictWrong() {
        List<List<String>> segments = new LinkedList<>();

        for (String json : jsonWrong) {
            try {
                helper.assertContent(jsonNormal, json, MatchMode.STRICT);
            } catch (ComparisonFailure e) {
                segments.add(extractSegments(e));
            }
        }

        List<String> expected = List.of(
                "-segment 'a' expected: '1', actual: '2'",
                "-segment 'b[2]' expected: 'z', actual: 'w'",
                "-segment 'c.bb' expected to be a ARRAY, but it is NUMBER"
        );

        assertThat(segments).hasSize(4);
        assertThat(segments.get(0)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(segments.get(1)).containsExactlyInAnyOrder(expected.get(0));
        assertThat(segments.get(2)).containsExactlyInAnyOrder(expected.get(1));
        assertThat(segments.get(3)).containsExactlyInAnyOrder(expected.get(2));
    }

    @Test
    public void testStrictAnyOrderWrong() {
        List<List<String>> segments = new LinkedList<>();

        for (String json : jsonWrong) {
            try {
                helper.assertContent(jsonNormal, json, MatchMode.STRICT_ANY_ORDER);
            } catch (ComparisonFailure e) {
                segments.add(extractSegments(e));
            }
        }

        List<String> expected = List.of(
                "-segment 'a' expected: '1', actual: '2'",
                "-segment 'b[2]' expected: 'z', actual: 'w'",
                "-segment 'c.bb' expected to be a ARRAY, but it is NUMBER"
        );

        assertThat(segments).hasSize(4);
        assertThat(segments.get(0)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(segments.get(1)).containsExactlyInAnyOrder(expected.get(0));
        assertThat(segments.get(2)).containsExactlyInAnyOrder(expected.get(1));
        assertThat(segments.get(3)).containsExactlyInAnyOrder(expected.get(2));
    }

    @Test
    public void testLooseWrong() {
        List<List<String>> segments = new LinkedList<>();

        for (String json : jsonWrong) {
            try {
                helper.assertContent(jsonNormal, json, MatchMode.LOOSE);
            } catch (ComparisonFailure e) {
                segments.add(extractSegments(e));
            }
        }

        List<String> expected = List.of(
                "-segment 'a' expected: '1', actual: '2'",
                "-segment 'b[2]' expected: 'z', actual: 'w'",
                "-segment 'c.bb' expected to be a ARRAY, but it is NUMBER"
        );

        assertThat(segments).hasSize(4);
        assertThat(segments.get(0)).containsExactlyInAnyOrderElementsOf(expected);
        assertThat(segments.get(1)).containsExactlyInAnyOrder(expected.get(0));
        assertThat(segments.get(2)).containsExactlyInAnyOrder(expected.get(1));
        assertThat(segments.get(3)).containsExactlyInAnyOrder(expected.get(2));
    }

    @Test
    public void testStrictMissing() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonMissing, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected to have fields [b], but they are not present"
        );
    }

    @Test
    public void testStrictAnyOrderMissing() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonMissing, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected to have fields [b], but they are not present"
        );
    }

    @Test
    public void testLooseMissing() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonMissing, jsonNormal, MatchMode.LOOSE);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).isEmpty();
    }

    @Test
    public void testLooseMissingReverse() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonMissing, MatchMode.LOOSE);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected to have fields [b], but they are not present"
        );
    }

    @Test
    public void testStrictUnordered() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonUnordered, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected to have field 'a' at position 0 but it was 'b'",
                "-root segment expected to have field 'b' at position 1 but it was 'c'",
                "-root segment expected to have field 'c' at position 2 but it was 'a'"
        );
    }

    @Test
    public void testStrictAnyOrderUnordered() {
        helper.assertContent(jsonNormal, jsonUnordered, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseUnordered() {
        helper.assertContent(jsonNormal, jsonUnordered, MatchMode.LOOSE);
    }

    @Test
    public void testStrictExtra() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonExtra, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected not to have fields [d], but they are present",
                "-segment 'c' expected not to have fields [dd], but they are present",
                "-segment 'c.bb' expected size: 2, actual size: 3"
        );
    }

    @Test
    public void testStrictAnyOrderExtra() {
        List<String> segments = new ArrayList<>();

        try {
            helper.assertContent(jsonNormal, jsonExtra, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-root segment expected not to have fields [d], but they are present",
                "-segment 'c' expected not to have fields [dd], but they are present",
                "-segment 'c.bb' expected size: 2, actual size: 3"
        );
    }

    @Test
    public void testLooseExtra() {
        helper.assertContent(jsonNormal, jsonExtra, MatchMode.LOOSE);
    }

    @Test
    public void testStrictEmptyActualList() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonEmpty, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-segment 'b' expected size: 3, actual size: 0"
        );
    }

    @Test
    public void testStrictAnyOrderEmptyActualList() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonEmpty, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-segment 'b' expected size: 3, actual size: 0"
        );
    }

    @Test
    public void testLooseEmptyActualList() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonNormal, jsonEmpty, MatchMode.LOOSE);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-segment 'b' expected minimum size: 3, actual size: 0"
        );
    }

    @Test
    public void testStrictEmptyExpectedList() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonEmpty, jsonNormal, MatchMode.STRICT);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-segment 'b' expected size: 0, actual size: 3"
        );
    }

    @Test
    public void testStrictAnyOrderEmptyExpectedList() {
        List<String> segments = new LinkedList<>();

        try {
            helper.assertContent(jsonEmpty, jsonNormal, MatchMode.STRICT_ANY_ORDER);
        } catch (ComparisonFailure e) {
            segments.addAll(extractSegments(e));
        }

        assertThat(segments).containsExactlyInAnyOrder(
                "-segment 'b' expected size: 0, actual size: 3"
        );
    }

    @Test
    public void testLooseEmptyExpectedList() {
        helper.assertContent(jsonEmpty, jsonNormal, MatchMode.LOOSE);
    }

    @Test
    public void testStrictAnyOrder() {
        helper.assertContent(jsonNormal, jsonReordered, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testAssertFragmentSimpleWithSuccess() {
        ValidatableResponse response = new ResponseBuilder()
                .setContentType(ContentType.JSON)
                .setStatusCode(200)
                .setBody(jsonNormal)
                .build().then();
        helper.assertFragment("a", response, Integer.class, new MatcherAssertion<>(Matchers.equalTo(1)));
    }

    @Test(expected = AssertionError.class)
    public void testAssertFragmentSimpleWithError() {
        ValidatableResponse response = new ResponseBuilder()
                .setContentType(ContentType.JSON)
                .setStatusCode(200)
                .setBody(jsonNormal)
                .build().then();
        helper.assertFragment("a", response, Integer.class, new MatcherAssertion<>(Matchers.equalTo(2)));
    }

    private List<String> extractSegments(ComparisonFailure e) {
        List<String> segments = new ArrayList<>();
        Matcher matcher = Pattern.compile("^\\s*(-.+)$", Pattern.MULTILINE).matcher(e.getMessage());
        while (matcher.find()) {
            segments.add(matcher.group(1));
        }
        return segments;
    }
}