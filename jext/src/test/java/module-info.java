import es.iti.commons.jext.test.*;

module iti.commons.jext.test {
    requires iti.commons.jext;
    requires org.assertj.core;
    requires junit;
    exports es.iti.commons.jext.test to junit;
    uses ExtensionPointSingleton;
    uses ExtensionPointFresh;
    uses MyExtensionPointV2_5;
    provides ExtensionPointSingleton with MyExtensionSingleton;
    provides ExtensionPointFresh with MyExtensionFresh;
    provides MyExtensionPointV2_5 with MyExtensionV1_0, MyExtensionV2_0, MyExtensionV2_5, MyExtensionV2_6;
    opens es.iti.commons.jext.test to iti.commons.jext;
}