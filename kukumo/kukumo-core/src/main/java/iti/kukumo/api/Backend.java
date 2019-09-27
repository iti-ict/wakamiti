package iti.kukumo.api;

import iti.kukumo.api.plan.PlanNode;

public interface Backend {

    void setUp();

    void tearDown();

    void runStep(PlanNode modelStep);

    void skipStep(PlanNode step);

    KukumoDataTypeRegistry getTypeRegistry();


}
