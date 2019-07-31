package ${package};

import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.${extensionPointType};

@Extension(
      provider="${groupId}",
      name="${artifactId}",
      version="${version}",
      extensionPoint="iti.kukumo.api.extensions.${extensionPointType}",
      extensionPointVersion="${extensionPointVersion}"
)
public class ${extensionClass} implements ${extensionPointType} {

}
