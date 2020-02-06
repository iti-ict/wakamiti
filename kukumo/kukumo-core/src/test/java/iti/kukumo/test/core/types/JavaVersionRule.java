package iti.kukumo.test.core.types;

import java.util.function.IntPredicate;

import org.junit.AssumptionViolatedException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class JavaVersionRule implements TestRule {

    private final IntPredicate versionPredicate;
    private final int actualVersion;

    public JavaVersionRule(IntPredicate versionPredicate) {
        this.versionPredicate = versionPredicate;
        this.actualVersion = getJavaVersion();
    }


    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (!versionPredicate.test(actualVersion)) {
                    throw new AssumptionViolatedException(
                        "Test not applicable for Java version "+actualVersion
                    );
                } else {
                    base.evaluate();
                }
            }
        };
    }



    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2);
        }
        // Allow these formats:
        // 1.8.0_72-ea
        // 9-ea
        // 9
        // 9.0.1
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        return Integer.parseInt(
            version.substring(
                0,
                dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : 1
            )
        );
    }


}
