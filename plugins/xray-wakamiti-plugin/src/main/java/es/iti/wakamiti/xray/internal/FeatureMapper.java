package es.iti.wakamiti.xray.internal;

import es.iti.wakamiti.xray.XRaySynchronizer;


public class FeatureMapper extends Mapper {

    public FeatureMapper(String suiteBase) {
        super(suiteBase);
    }

    public String type() {
        return XRaySynchronizer.GHERKIN_TYPE_FEATURE;
    }


}
