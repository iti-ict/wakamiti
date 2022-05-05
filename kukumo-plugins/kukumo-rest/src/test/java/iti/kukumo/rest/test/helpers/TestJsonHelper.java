/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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

    private static final JSONHelper helper = new JSONHelper();

    private static final String jsonNormal = json(map(
        "a",1,
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44)
    ));
    private static final String jsonReordered = json(map(
        "b", List.of("x","y","z"),
        "a",1,
        "c", map("bb",33, "aa",22,"cc", 44)
    ));
    private static final String jsonMissing = json(map(
        "a",1,
        "c", map("aa",22,"bb",33, "cc", 44)
    ));
    private static final String jsonUnordererd = json(map(
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44),
        "a",1
    ));
    private static final String jsonWrong = json(map(
        "a",2,
        "b", List.of("x","y","w"),
        "c", map("aa",22,"bb",34, "cc", 44)
    ));
    private static final String jsonExtra = json(map(
        "a",1,
        "b", List.of("x","y","z"),
        "c", map("aa",22,"bb",33, "cc", 44),
        "d", 5.55
    ));
    private static final String jsonEmpty = json(map(
            "a",1,
            "b", List.of(),
            "c", map("aa",22,"bb",33, "cc", 44)
    ));



    @Test
    public void testStrictNormal() {
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.STRICT);
    }

    @Test
    public void testStrictAnyOrderNormal(){
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.STRICT_ANY_ORDER);
    }

    @Test
    public void testLooseNormal(){
        helper.assertContent(jsonNormal,jsonNormal,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictWrong(){
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.STRICT);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderWrong(){
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.STRICT_ANY_ORDER);
    }


    @Test(expected = ComparisonFailure.class)
    public void testLooseWrong() {
        helper.assertContent(jsonNormal,jsonWrong,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictMissing(){
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.STRICT);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictAnyOrderMissing(){
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.STRICT_ANY_ORDER);
    }


    @Test(expected = ComparisonFailure.class)
    public void testLooseMissing(){
        helper.assertContent(jsonNormal,jsonMissing,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictUnordered() {
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.STRICT);
    }


    @Test
    public void testStrictAnyOrderUnordered(){
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.STRICT_ANY_ORDER);
    }


    @Test
    public void testLooseUnordered(){
        helper.assertContent(jsonNormal,jsonUnordererd,MatchMode.LOOSE);
    }


    @Test(expected = ComparisonFailure.class)
    public void testStrictExtra(){
        helper.assertContent(jsonNormal,jsonExtra,MatchMode.STRICT);
    }


    @Test
    public void testStrictAnyOrderExtra(){
        helper.assertContent(jsonNormal,jsonExtra,MatchMode.STRICT_ANY_ORDER);
    }


    @Test
    public void testLooseExtra(){
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

    
    @Test
    public void testStrictAnyOrder() {
        helper.assertContent(jsonNormal, jsonReordered, MatchMode.STRICT_ANY_ORDER);
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