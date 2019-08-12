package iti.commons.slf4jjansi.impl;

import static org.slf4j.spi.LocationAwareLogger.DEBUG_INT;
import static org.slf4j.spi.LocationAwareLogger.ERROR_INT;
import static org.slf4j.spi.LocationAwareLogger.INFO_INT;
import static org.slf4j.spi.LocationAwareLogger.TRACE_INT;
import static org.slf4j.spi.LocationAwareLogger.WARN_INT;

import java.util.Arrays;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

public class LocationAwareAnsiLogger implements Logger {

    private static final String FQCN = LocationAwareAnsiLogger.class.getName();

    private final LocationAwareLogger delegate;

    public LocationAwareAnsiLogger(LocationAwareLogger delegate) {
        this.delegate = delegate;
    }


    private static String ansi(String message) {
        return Ansi.ansi().render(message).toString();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }


    private void log (
        boolean enabled,
        int level,
        Marker marker,
        String message,
        Object... args
    ) {
        if (enabled) {
            Throwable throwable = null;
            if (args != null && args.length > 0 && args[args.length-1] instanceof Throwable) {
                throwable = (Throwable) args[args.length-1];
                args = Arrays.copyOf(args, args.length-1);
            }
            delegate.log(marker, FQCN, level, ansi(message), args, throwable);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }

    @Override
    public void trace(String message) {
        log(delegate.isTraceEnabled(),TRACE_INT,null,message);
    }

    @Override
    public void trace(String message, Object arg) {
        log(delegate.isTraceEnabled(),TRACE_INT,null,message,arg);
    }

    @Override
    public void trace(String message, Object arg1, Object arg2) {
        log(delegate.isTraceEnabled(),TRACE_INT,null,message,arg1,arg2);
    }

    @Override
    public void trace(String message, Object... arguments) {
        log(delegate.isTraceEnabled(),TRACE_INT,null,message,arguments);
    }

    @Override
    public void trace(String message, Throwable throwable) {
        log(delegate.isTraceEnabled(),TRACE_INT,null,message,throwable);
    }


    @Override
    public void trace(Marker marker, String message) {
        log(delegate.isTraceEnabled(marker),TRACE_INT,marker,message);
    }

    @Override
    public void trace(Marker marker, String message, Object arg) {
        log(delegate.isTraceEnabled(marker),TRACE_INT,marker,message,arg);
    }

    @Override
    public void trace(Marker marker, String message, Object arg1, Object arg2) {
        log(delegate.isTraceEnabled(marker),TRACE_INT,marker,message,arg1,arg2);
    }

    @Override
    public void trace(Marker marker, String message, Object... arguments) {
        log(delegate.isTraceEnabled(marker),TRACE_INT,marker,message,arguments);
    }

    @Override
    public void trace(Marker marker, String message, Throwable throwable) {
        log(delegate.isTraceEnabled(marker),TRACE_INT,marker,message,throwable);
    }

    //

    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }

    @Override
    public void debug(String message) {
        log(delegate.isDebugEnabled(),DEBUG_INT,null,message);
    }

    @Override
    public void debug(String message, Object arg) {
        log(delegate.isDebugEnabled(),DEBUG_INT,null,message,arg);
    }

    @Override
    public void debug(String message, Object arg1, Object arg2) {
        log(delegate.isDebugEnabled(),DEBUG_INT,null,message,arg1,arg2);
    }

    @Override
    public void debug(String message, Object... arguments) {
        log(delegate.isDebugEnabled(),DEBUG_INT,null,message,arguments);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        log(delegate.isDebugEnabled(),DEBUG_INT,null,message,throwable);
    }


    @Override
    public void debug(Marker marker, String message) {
        log(delegate.isDebugEnabled(marker),DEBUG_INT,marker,message);
    }

    @Override
    public void debug(Marker marker, String message, Object arg) {
        log(delegate.isDebugEnabled(marker),DEBUG_INT,marker,message,arg);
    }

    @Override
    public void debug(Marker marker, String message, Object arg1, Object arg2) {
        log(delegate.isDebugEnabled(marker),DEBUG_INT,marker,message,arg1,arg2);
    }

    @Override
    public void debug(Marker marker, String message, Object... arguments) {
        log(delegate.isDebugEnabled(marker),DEBUG_INT,marker,message,arguments);
    }

    @Override
    public void debug(Marker marker, String message, Throwable throwable) {
        log(delegate.isDebugEnabled(marker),DEBUG_INT,marker,message,throwable);
    }

    //

    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }

    @Override
    public void info(String message) {
        log(delegate.isInfoEnabled(),INFO_INT,null,message);
    }

    @Override
    public void info(String message, Object arg) {
        log(delegate.isInfoEnabled(),INFO_INT,null,message,arg);
    }

    @Override
    public void info(String message, Object arg1, Object arg2) {
        log(delegate.isInfoEnabled(),INFO_INT,null,message,arg1,arg2);
    }

    @Override
    public void info(String message, Object... arguments) {
        log(delegate.isInfoEnabled(),INFO_INT,null,message,arguments);
    }

    @Override
    public void info(String message, Throwable throwable) {
        log(delegate.isInfoEnabled(),INFO_INT,null,message,throwable);
    }


    @Override
    public void info(Marker marker, String message) {
        log(delegate.isInfoEnabled(marker),INFO_INT,marker,message);
    }

    @Override
    public void info(Marker marker, String message, Object arg) {
        log(delegate.isInfoEnabled(marker),INFO_INT,marker,message,arg);
    }

    @Override
    public void info(Marker marker, String message, Object arg1, Object arg2) {
        log(delegate.isInfoEnabled(marker),INFO_INT,marker,message,arg1,arg2);
    }

    @Override
    public void info(Marker marker, String message, Object... arguments) {
        log(delegate.isInfoEnabled(marker),INFO_INT,marker,message,arguments);
    }

    @Override
    public void info(Marker marker, String message, Throwable throwable) {
        log(delegate.isInfoEnabled(marker),INFO_INT,marker,message,throwable);
    }

    //

    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    @Override
    public void warn(String message) {
        log(delegate.isWarnEnabled(),WARN_INT,null,message);
    }

    @Override
    public void warn(String message, Object arg) {
        log(delegate.isWarnEnabled(),WARN_INT,null,message,arg);
    }

    @Override
    public void warn(String message, Object arg1, Object arg2) {
        log(delegate.isWarnEnabled(),WARN_INT,null,message,arg1,arg2);
    }

    @Override
    public void warn(String message, Object... arguments) {
        log(delegate.isWarnEnabled(),WARN_INT,null,message,arguments);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log(delegate.isWarnEnabled(),WARN_INT,null,message,throwable);
    }


    @Override
    public void warn(Marker marker, String message) {
        log(delegate.isWarnEnabled(marker),WARN_INT,marker,message);
    }

    @Override
    public void warn(Marker marker, String message, Object arg) {
        log(delegate.isWarnEnabled(marker),WARN_INT,marker,message,arg);
    }

    @Override
    public void warn(Marker marker, String message, Object arg1, Object arg2) {
        log(delegate.isWarnEnabled(marker),WARN_INT,marker,message,arg1,arg2);
    }

    @Override
    public void warn(Marker marker, String message, Object... arguments) {
        log(delegate.isWarnEnabled(marker),WARN_INT,marker,message,arguments);
    }

    @Override
    public void warn(Marker marker, String message, Throwable throwable) {
        log(delegate.isWarnEnabled(marker),WARN_INT,marker,message,throwable);
    }

    //

    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }

    @Override
    public void error(String message) {
        log(delegate.isErrorEnabled(),ERROR_INT,null,message);
    }

    @Override
    public void error(String message, Object arg) {
        log(delegate.isErrorEnabled(),ERROR_INT,null,message,arg);
    }

    @Override
    public void error(String message, Object arg1, Object arg2) {
        log(delegate.isErrorEnabled(),ERROR_INT,null,message,arg1,arg2);
    }

    @Override
    public void error(String message, Object... arguments) {
        log(delegate.isErrorEnabled(),ERROR_INT,null,message,arguments);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log(delegate.isErrorEnabled(),ERROR_INT,null,message,throwable);
    }


    @Override
    public void error(Marker marker, String message) {
        log(delegate.isErrorEnabled(marker),ERROR_INT,marker,message);
    }

    @Override
    public void error(Marker marker, String message, Object arg) {
        log(delegate.isErrorEnabled(marker),ERROR_INT,marker,message,arg);
    }

    @Override
    public void error(Marker marker, String message, Object arg1, Object arg2) {
        log(delegate.isErrorEnabled(marker),ERROR_INT,marker,message,arg1,arg2);
    }

    @Override
    public void error(Marker marker, String message, Object... arguments) {
        log(delegate.isErrorEnabled(marker),ERROR_INT,marker,message,arguments);
    }

    @Override
    public void error(Marker marker, String message, Throwable throwable) {
        log(delegate.isErrorEnabled(marker),ERROR_INT,marker,message,throwable);
    }


}