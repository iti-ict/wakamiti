package iti.kukumo.api.extensions;

import iti.commons.jext.ExtensionPoint;
import iti.kukumo.api.KukumoDataType;

import java.util.List;

@ExtensionPoint
public interface DataTypeContributor extends Contributor {

    List<KukumoDataType<?>> contributeTypes();

}
