package iti.commons.jext;

import iti.commons.jext.ExtensionPoint;

@ExtensionPoint(version="2.5")
public interface MyExtensionPointV2_5 {

    default String value() {
        return getClass().getSimpleName();
    }
    
}