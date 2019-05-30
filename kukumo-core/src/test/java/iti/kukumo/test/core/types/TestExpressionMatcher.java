package iti.kukumo.test.core.types;

import iti.kukumo.core.backend.ExpressionMatcher;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * @author ITI
 * Created by ITI on 29/05/19
 */
public class TestExpressionMatcher {

    @Test
    public void testExpression() {
        String readableExpression = "(que) el|la|lo|los|las siguiente(s) * se inserta(n) en la tabla de BBDD:";
        String regularExpression = ExpressionMatcher.computeRegularExpression(readableExpression);
        Pattern regex = Pattern.compile(regularExpression);
        System.out.println(regex.toString());
        assertTrue(regex.matcher("que los siguientes datos se insertan en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("que el siguiente dato se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("que lo siguiente se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("los siguientes datos se insertan en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("el siguiente dato se inserta en la tabla de BBDD:").matches());
        assertTrue(regex.matcher("lo siguiente se inserta en la tabla de BBDD:").matches());

    }

}
