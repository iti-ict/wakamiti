package iti.commons.configurer;

public class ConfigurationException extends Exception {
    
    private static final long serialVersionUID = 7175876124782335084L;

    public ConfigurationException(Throwable throwable) {
        super(throwable);
    }

    public ConfigurationException(String message) {
        super(message);
    }
}
