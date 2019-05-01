package iti.kukumo.api;

import iti.kukumo.api.plan.PlanStep;

public interface Backend {

    void setUp();

    void tearDown();

    void runStep(PlanStep modelStep);

    void skipStep(PlanStep step);

    KukumoDataTypeRegistry getTypeRegistry();


}
