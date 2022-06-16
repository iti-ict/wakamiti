/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.test.core.types;


import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import iti.kukumo.api.util.Either;
import org.junit.Test;

import iti.kukumo.core.Kukumo;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.core.backend.ExpressionMatcher;




public class TestExpressionMatcher {

    @Test
    public void testExpressionStep1() {
        assertExpression(
            new Locale("es"),
            "(que) el|la|lo|los|las siguiente(s) * se inserta(n) en la tabla de BBDD {word}:",
            "que los siguientes datos se insertan en la tabla de BBDD USER:",
            "que el siguiente dato se inserta en la tabla de BBDD USER:",
            "que lo siguiente se inserta en la tabla de BBDD USER:",
            "los siguientes datos se insertan en la tabla de BBDD USER:",
            "lo siguiente se inserta en la tabla de BBDD USER:",
            "que siguiente se inserta en la tabla de BBDD USER:"
        );

    }


    @Test
    public void testExpressionStep2() {
        assertExpression(
            Locale.ENGLISH,
            "(that) the following * (is|are) inserted in the database table {word}:",
            "the following data is inserted in the database table USER:",
            "the following data are inserted in the database table USER:",
            "the following data inserted in the database table USER:",
            "the following is inserted in the database table USER:",
            "the following are inserted in the database table USER:",
            "the following inserted in the database table USER:",
            "the following inserted in the database table USER:"
        );
    }


    @Test
    public void testExpressionStep3() {
        assertExpression(
            new Locale("es"),
            "* identificad(o|a|os|as) por {text}",
            "un usuario identificado por '3'",
            "una usuaria identificada por '3'",
            "unos usuarios identificados por '3'",
            "unas usuarias identificadas por '3'",
            "identificado por '3'"
        );
    }


    @Test
    public void testExpressionStep4() {
        assertExpression(
            new Locale("es"),
            "(que) el|la|lo|los|las siguiente(s) * se inserta(n) en la tabla de BBDD {word}:",
            "que el siguiente dato se inserta en la tabla de BBDD USER:",
            "que la siguiente cosa se inserta en la tabla de BBDD USER:",
            "que lo siguiente se inserta en la tabla de BBDD USER:",
            "que los siguientes datos se insertan en la tabla de BBDD USER:",
            "que las siguientes cosas se insertan en la tabla de BBDD USER:",
            "el siguiente dato se inserta en la tabla de BBDD USER:",
            "la siguiente cosa se inserta en la tabla de BBDD USER:",
            "lo siguiente se inserta en la tabla de BBDD USER:",
            "los siguientes datos se insertan en la tabla de BBDD USER:",
            "las siguientes cosas se insertan en la tabla de BBDD USER:"
        );
    }


    @Test
    public void testExpressionStep5() {
        assertExpression(
            new Locale("es"),
            "se realiza la búsqueda *",
            "se realiza la búsqueda",
            "se realiza la búsqueda de algo"
        );
    }


    private void assertExpression(Locale locale, String expression, String... steps) {
        for (String step : steps) {
            Matcher matcher = ExpressionMatcher.matcherFor(
                expression,
                coreTypes(),
                locale,
                Either.fallback(step)
            );
            assertTrue("<<" + step + ">> not matching <<" + expression + ">>", matcher.matches());
        }
    }


    private KukumoDataTypeRegistry coreTypes() {
        Map<String, KukumoDataType<?>> types = new HashMap<>();
        Kukumo.contributors().allDataTypeContributors().forEach(contributor -> {
            for (KukumoDataType<?> type : contributor.contributeTypes()) {
                types.put(type.getName(), type);
            }
        });
        return new KukumoDataTypeRegistry(types);
    }

}