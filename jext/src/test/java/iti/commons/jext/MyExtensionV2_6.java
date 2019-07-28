package iti.commons.jext;

import iti.commons.jext.Extension;

@Extension(
    provider = "test", 
    name = "A2_6", 
    version = "1.0", 
    extensionPointVersion = "2.6", 
    priority = 2
)
public class MyExtensionV2_6 implements MyExtensionPointV2_5 {  }