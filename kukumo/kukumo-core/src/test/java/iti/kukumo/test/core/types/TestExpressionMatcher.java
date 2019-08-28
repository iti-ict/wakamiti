package iti.kukumo.test.core.types;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoDataType;
import iti.kukumo.api.KukumoDataTypeRegistry;
import iti.kukumo.api.extensions.DataTypeContributor;
import iti.kukumo.core.backend.ExpressionMatcher;
import iti.kukumo.core.plan.DefaultPlanStep;
import org.junit.Test;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * @author ITI
 * Created by ITI on 29/05/19
 */
public class TestExpressionMatcher {

    @Test
    public void testExpression() {
        String readableExpression = "(que) (el|la|lo|los|las) siguiente(s) * se inserta(n) en la tabla de BBDD:";
        String regularExpression = ExpressionMatcher.computeRegularExpression(readableExpression);
        Pattern regex = Pattern.compile(regularExpression);
        System.out.println(regex.toString());
        assertTrue(regex.matcher("que los siguientes datos se insertan en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("que el siguiente dato se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("que lo siguiente se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("los siguientes datos se insertan en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("el siguiente dato se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("lo siguiente se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("que siguiente se inserta en la tabla de BBDD:").matches());

    }


    @Test
    public void testExpressionStep() {
        String step = "the following data inserted in the database table USER:";
        Matcher matcher = ExpressionMatcher.matcherFor(
            "(that) the following * (is|are) inserted in the database table {word}:",
            coreTypes(),
            Locale.ENGLISH,
            new DefaultPlanStep().setName(step)
        );
        assertTrue(matcher.matches());
    }




    private KukumoDataTypeRegistry coreTypes() {
        Map<String,KukumoDataType<?>> types = new HashMap<>();
        for (DataTypeContributor contributor: Kukumo.getAllDataTypeContributors()) {
            for (KukumoDataType<?> type : contributor.contributeTypes()) {
                types.put(type.getName(), type);
            }
        }
        return new KukumoDataTypeRegistry(types);
    }

}
