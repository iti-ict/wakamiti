package iti.commons.slf4jansi.impl;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class SimpleAnsiLogger implements Logger {





    private final Logger delegate;
    private final JAnsiSupport jAnsi = JAnsiSupport.instance;

    public SimpleAnsiLogger(Logger delegate) {
        this.delegate = delegate;
    }


    @Override
    public String getName() {
        return delegate.getName();
    }


    @Override
    public boolean isTraceEnabled() {
        return delegate.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi("trace",msg));
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi("trace",format),arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi("trace",format),arg1,arg2);
        }
    }


    @Override
    public void trace(String format, Object... arguments) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi("trace",format), arguments);
        }
    }


    @Override
    public void trace(String msg, Throwable t) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi("trace",msg),t);
        }
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }


    @Override
    public void trace(Marker marker, String msg) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi("trace",msg));
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi("trace",format),arg);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi("trace",format),arg1,arg2);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi("trace",format),argArray);
        }
    }


    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi("trace",msg),t);
        }
    }



    //
    @Override
    public boolean isDebugEnabled() {
        return delegate.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi("debug",msg));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi("debug",format),arg);
        }
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi("debug",format),arg1,arg2);
        }
    }


    @Override
    public void debug(String format, Object... arguments) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi("debug",format), arguments);
        }
    }


    @Override
    public void debug(String msg, Throwable t) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi("debug",msg),t);
        }
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }


    @Override
    public void debug(Marker marker, String msg) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi("debug",msg));
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi("debug",format),arg);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi("debug",format),arg1,arg2);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi("debug",format),argArray);
        }
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi("debug",msg),t);
        }
    }

    //


    @Override
    public boolean isInfoEnabled() {
        return delegate.isInfoEnabled();
    }


    @Override
    public void info(String msg) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi("info",msg));
        }
    }


    @Override
    public void info(String format, Object arg) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi("info",format),arg);
        }
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi("info",format),arg1,arg2);
        }
    }


    @Override
    public void info(String format, Object... arguments) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi("info",format), arguments);
        }
    }


    @Override
    public void info(String msg, Throwable t) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi("info",msg),t);
        }
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }


    @Override
    public void info(Marker marker, String msg) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi("info",msg));
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi("info",format),arg);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi("info",format),arg1,arg2);
        }
    }


    @Override
    public void info(Marker marker, String format, Object... argArray) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi("info",format),argArray);
        }
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi("info",msg),t);
        }
    }

    //


    @Override
    public boolean isWarnEnabled() {
        return delegate.isWarnEnabled();
    }


    @Override
    public void warn(String msg) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi("warn",msg));
        }
    }


    @Override
    public void warn(String format, Object arg) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi("warn",format),arg);
        }
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi("warn",format),arg1,arg2);
        }
    }


    @Override
    public void warn(String format, Object... arguments) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi("warn",format), arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi("warn",msg),t);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi("warn",msg));
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi("warn",format),arg);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi("warn",format),arg1,arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi("warn",format),argArray);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi("warn",msg),t);
        }
    }

    //


    @Override
    public boolean isErrorEnabled() {
        return delegate.isErrorEnabled();
    }


    @Override
    public void error(String msg) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi("error",msg));
        }
    }


    @Override
    public void error(String format, Object arg) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi("error",format),arg);
        }
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi("error",format),arg1,arg2);
        }
    }


    @Override
    public void error(String format, Object... arguments) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi("error",format), arguments);
        }
    }


    @Override
    public void error(String msg, Throwable t) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi("error",msg),t);
        }
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }


    @Override
    public void error(Marker marker, String msg) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi("error",msg));
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi("error",format),arg);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi("error",format),arg1,arg2);
        }
    }


    @Override
    public void error(Marker marker, String format, Object... argArray) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi("error",format),argArray);
        }
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi("error",msg),t);
        }
    }
}