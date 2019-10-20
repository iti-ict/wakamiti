/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.api;


import iti.kukumo.api.plan.PlanNode;


public interface Backend {

    void setUp();


    void tearDown();


    void runStep(PlanNode modelStep);


    KukumoDataTypeRegistry getTypeRegistry();

}
