package iti.commons.jext;

import iti.commons.jext.Extension;

@Extension(
    provider = "test", 
    name = "Singleton", 
    version = "1.0"
)
public class MyExtensionSingleton implements ExtensionPointSingleton {
    
}