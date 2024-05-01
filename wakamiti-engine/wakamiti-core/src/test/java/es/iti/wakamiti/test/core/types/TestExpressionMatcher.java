/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.test.core.types;


import es.iti.wakamiti.api.WakamitiDataType;
import es.iti.wakamiti.api.WakamitiDataTypeRegistry;
import es.iti.wakamiti.api.util.Either;
import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.core.backend.ExpressionMatcher;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

import static org.junit.Assert.assertTrue;


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

        assertExpression(
                new Locale("es"),
                "(se recupera(n)) (el|los) valor(es) de",
                "se recupera el valor de",
                "se recuperan los valores de",
                "el valor de",
                "los valores de",
                "valor de"
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

        assertExpression(
                Locale.ENGLISH,
                "the following SQL query value(s) (is|are retrieved):",
                "the following SQL query value is retrieved:",
                "the following SQL query value is retrieved:",
                "the following SQL query values are retrieved:",
                "the following SQL query value:",
                "the following SQL query values:",
                "the following SQL query values retrieved:"
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

    @Test
    public void testExpressionStep6() {
        assertExpression(
                new Locale("es"),
                "se realiza la búsqueda en {duration}",
                "se realiza la búsqueda en 5 segundos",
                "se realiza la búsqueda en 1 hora"
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


    private WakamitiDataTypeRegistry coreTypes() {
        Map<String, WakamitiDataType<?>> types = new HashMap<>();
        Wakamiti.contributors().allDataTypeContributors().forEach(contributor -> {
            for (WakamitiDataType<?> type : contributor.contributeTypes()) {
                types.put(type.getName(), type);
            }
        });
        return new WakamitiDataTypeRegistry(types);
    }

}