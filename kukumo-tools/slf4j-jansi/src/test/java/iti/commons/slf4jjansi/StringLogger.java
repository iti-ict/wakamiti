package iti.commons.slf4jjansi;

import org.slf4j.helpers.SubstituteLogger;

public class StringLogger extends SubstituteLogger {

    private final StringBuilder contents = new StringBuilder();

    public StringLogger() {
        super("TEST", null, true);
    }

    @Override
    public void info(String msg) {
        contents.append(msg);
    }


    public String getContent() {
        return contents.toString();
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

}
