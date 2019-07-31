package iti.commons.jext;

import iti.commons.jext.Extension;

@Extension(
    provider = "test", 
    name = "A2_0", 
    version = "1.0", 
    extensionPointVersion = "2.0"
)
public class MyExtensionV2_0 implements MyExtensionPointV2_5 {  }