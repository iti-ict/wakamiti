package iti.commons.jext;

/**
 *  The different strategies that can be used each time an extension is requested 
 *  using the {@link ExtensionManager}. 
 */
public enum LoadStrategy {

    /** Keep a single instance */ 
    SINGLETON,
    
    /** Creates a new instance each time */
    FRESH,
    
    /** The behaviour is decided by the underline implementation */
    UNDEFINED
}
