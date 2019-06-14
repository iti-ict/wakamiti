package iti.kukumo.test.plan;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import org.junit.Test;

import java.io.IOException;

/**
 * @author ITI
 *         Created by ITI on 13/03/19
 */
public class TestPlanSerializer {

    private static ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

    @Test
    public void a() throws IOException {
        PlanNodeDescriptor planDescriptor = Kukumo.getPlanSerializer().read(classLoader.getResourceAsStream("plan/plan.json"));
        planDescriptor.getDisplayName();
    }
}
