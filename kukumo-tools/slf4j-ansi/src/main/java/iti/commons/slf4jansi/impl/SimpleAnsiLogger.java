package iti.commons.slf4jansi.impl;

import org.slf4j.Logger;
import org.slf4j.Marker;

public class SimpleAnsiLogger implements Logger {


    private static final String ERROR = "error";
    private static final String WARN = "warn";
    private static final String INFO = "info";
    private static final String DEBUG = "debug";
    private static final String TRACE = "trace";

    private static final JAnsiSupport jAnsi = JAnsiSupport.instance;

    private final Logger delegate;

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
            delegate.trace(jAnsi.ansi(TRACE,msg));
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi(TRACE,format),arg);
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi(TRACE,format),arg1,arg2);
        }
    }


    @Override
    public void trace(String format, Object... arguments) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi(TRACE,format), arguments);
        }
    }


    @Override
    public void trace(String msg, Throwable t) {
        if (delegate.isTraceEnabled()) {
            delegate.trace(jAnsi.ansi(TRACE,msg),t);
        }
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return delegate.isTraceEnabled(marker);
    }


    @Override
    public void trace(Marker marker, String msg) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi(TRACE,msg));
        }
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi(TRACE,format),arg);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi(TRACE,format),arg1,arg2);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi(TRACE,format),argArray);
        }
    }


    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (delegate.isTraceEnabled(marker)) {
            delegate.trace(marker,jAnsi.ansi(TRACE,msg),t);
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
            delegate.debug(jAnsi.ansi(DEBUG,msg));
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi(DEBUG,format),arg);
        }
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi(DEBUG,format),arg1,arg2);
        }
    }


    @Override
    public void debug(String format, Object... arguments) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi(DEBUG,format), arguments);
        }
    }


    @Override
    public void debug(String msg, Throwable t) {
        if (delegate.isDebugEnabled() ) {
            delegate.debug(jAnsi.ansi(DEBUG,msg),t);
        }
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return delegate.isDebugEnabled(marker);
    }


    @Override
    public void debug(Marker marker, String msg) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi(DEBUG,msg));
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi(DEBUG,format),arg);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi(DEBUG,format),arg1,arg2);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi(DEBUG,format),argArray);
        }
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (delegate.isDebugEnabled(marker) ) {
            delegate.debug(marker,jAnsi.ansi(DEBUG,msg),t);
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
            delegate.info(jAnsi.ansi(INFO,msg));
        }
    }


    @Override
    public void info(String format, Object arg) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi(INFO,format),arg);
        }
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi(INFO,format),arg1,arg2);
        }
    }


    @Override
    public void info(String format, Object... arguments) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi(INFO,format), arguments);
        }
    }


    @Override
    public void info(String msg, Throwable t) {
        if (delegate.isInfoEnabled() ) {
            delegate.info(jAnsi.ansi(INFO,msg),t);
        }
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return delegate.isInfoEnabled(marker);
    }


    @Override
    public void info(Marker marker, String msg) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi(INFO,msg));
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi(INFO,format),arg);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi(INFO,format),arg1,arg2);
        }
    }


    @Override
    public void info(Marker marker, String format, Object... argArray) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi(INFO,format),argArray);
        }
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (delegate.isInfoEnabled(marker) ) {
            delegate.info(marker,jAnsi.ansi(INFO,msg),t);
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
            delegate.warn(jAnsi.ansi(WARN,msg));
        }
    }


    @Override
    public void warn(String format, Object arg) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi(WARN,format),arg);
        }
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi(WARN,format),arg1,arg2);
        }
    }


    @Override
    public void warn(String format, Object... arguments) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi(WARN,format), arguments);
        }
    }

    @Override
    public void warn(String msg, Throwable t) {
        if (delegate.isWarnEnabled()) {
            delegate.warn(jAnsi.ansi(WARN,msg),t);
        }
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return delegate.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi(WARN,msg));
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi(WARN,format),arg);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi(WARN,format),arg1,arg2);
        }
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi(WARN,format),argArray);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (delegate.isWarnEnabled(marker)) {
            delegate.warn(marker,jAnsi.ansi(WARN,msg),t);
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
            delegate.error(jAnsi.ansi(ERROR,msg));
        }
    }


    @Override
    public void error(String format, Object arg) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi(ERROR,format),arg);
        }
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi(ERROR,format),arg1,arg2);
        }
    }


    @Override
    public void error(String format, Object... arguments) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi(ERROR,format), arguments);
        }
    }


    @Override
    public void error(String msg, Throwable t) {
        if (delegate.isErrorEnabled()) {
            delegate.error(jAnsi.ansi(ERROR,msg),t);
        }
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return delegate.isErrorEnabled(marker);
    }


    @Override
    public void error(Marker marker, String msg) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi(ERROR,msg));
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi(ERROR,format),arg);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi(ERROR,format),arg1,arg2);
        }
    }


    @Override
    public void error(Marker marker, String format, Object... argArray) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi(ERROR,format),argArray);
        }
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (delegate.isErrorEnabled(marker)) {
            delegate.error(marker,jAnsi.ansi(ERROR,msg),t);
        }
    }
}