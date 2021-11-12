package iti.kukumo.rest.test.helpers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.ComparisonFailure;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import iti.kukumo.rest.MatchMode;
import iti.kukumo.rest.helpers.JSONHelper;

public class TestJsonHelper {

    private static JSONHelper helper = new JSONHelper();

    private static String jsonNormal = json(map(
        "a",1,
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44)
    ));
    private static String jsonMissing = json(map(
        "a",1,
        "c", map("aa",22,"bb",33, "cc", 44)
    ));
    private static String jsonUnordererd = json(map(
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44),
        "a",1
    ));
    private static String jsonWrong = json(map(
        "a",2,
        "b", List.of("x","y","w"),
        "c", map("aa",22,"bb",34, "cc", 44)
    ));
    private static String jsonExtra = json(map(
        "a",1,
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44),
        "d", 5.55
    ));
    private static String jsonEmpty = json(map(
            "a",1,
            "b", List.of(),
            "c", map("aa",22,"bb",33, "cc", 44)
    ));



    @Test
    public void testStrictNormal() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.STRICT);
    }

    @Test
    public void testStrictAnyOrderNormal() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseNormal() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictWrong() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.STRICT);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderWrong() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.STRICT_ANY_ORDER);
    }


    @Test(expected = ComparisonFailure.class)
    public void testLooseWrong() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictMissing() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.STRICT);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderMissing() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.STRICT_ANY_ORDER);
    }


    @Test(expected = ComparisonFailure.class)
    public void testLooseMissing() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictUnordered() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.STRICT);
    }


    @Test
    public void testStrictAnyOrderUnordered() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.STRICT_ANY_ORDER);
    }


    @Test
    public void testLooseUnordered() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictExtra() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonExtra,MatchMode.STRICT);
    }


    @Test
    public void testStrictAnyOrderExtra() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonExtra,MatchMode.STRICT_ANY_ORDER);
    }


    @Test
    public void testLooseExtra() throws JsonProcessingException {
        helper.assertContent(jsonNormal,jsonExtra,MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictEmptyActualList() {
        helper.assertContent(jsonNormal, jsonEmpty, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderEmptyActualList() {
        helper.assertContent(jsonNormal, jsonEmpty, MatchMode.STRICT_ANY_ORDER);
    }

    @Test(expected = ComparisonFailure.class)
    public void testLooseEmptyActualList() {
        helper.assertContent(jsonNormal, jsonEmpty, MatchMode.LOOSE);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictEmptyExpectedList() {
        helper.assertContent(jsonEmpty, jsonNormal, MatchMode.STRICT);
    }

    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderEmptyExpectedList() {
        helper.assertContent(jsonEmpty, jsonNormal, MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseEmptyExpectedList() {
        helper.assertContent(jsonEmpty, jsonNormal, MatchMode.LOOSE);
    }


    private static String json (Map<?,?> map) {
        try {
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private static <K,V> Map<K,V> map(K key1, V value1, K key2, V value2, K key3, V value3, K key4, V value4) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1,value1);
        map.put(key2,value2);
        map.put(key3,value3);
        map.put(key4,value4);
        return map;
    }


    private static <K,V> Map<K,V> map(K key1, V value1, K key2, V value2, K key3, V value3) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1,value1);
        map.put(key2,value2);
        map.put(key3,value3);
        return map;
    }


    private static <K,V> Map<K,V> map(K key1, V value1, K key2, V value2) {
        var map = new LinkedHashMap<K, V>();
        map.put(key1,value1);
        map.put(key2,value2);
        return map;
    }

}
