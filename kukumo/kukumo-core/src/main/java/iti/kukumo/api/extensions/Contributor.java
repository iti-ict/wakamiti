package iti.kukumo.api.extensions;

import iti.commons.jext.Extension;

/**
 * Base interface for all Kukumo extensions
 */
public interface Contributor {


    default String info() {
        Extension extensionData = this.getClass().getAnnotation(Extension.class);
        return extensionData.provider()+":"+extensionData.name()+":"+extensionData.version();
    }

    default Extension extensionMetadata() {
        return this.getClass().getAnnotation(Extension.class);
    }

}
