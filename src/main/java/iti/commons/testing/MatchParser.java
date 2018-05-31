/*
 * @author Luis Iñesta Gelabert linesta@iti.es
 */
package iti.commons.testing;


import static org.junit.Assert.assertThat;

import java.util.*;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

public class MatchParser {



    protected static final String NULL = "matcher.null";
    protected static final String EMPTY = "matcher.empty";
    protected static final String NOT_EMPTY = "matcher.not.empty";
    protected static final String NULL_EMPTY = "matcher.null.empty";
    protected static final String NOT_NULL_EMPTY = "matcher.not.null.empty";
    protected static final String NOT_NULL = "matcher.not.null";

    protected static final String EQUAL = "matcher.equal";
    protected static final String DISTINCT = "matcher.distinct";
    protected static final String EQUAL_IGNORE_CASE = "matcher.equalIgnoreCase";
    protected static final String GREATER = "matcher.greater";
    protected static final String GREATER_EQUAL = "matcher.greater.equal";
    protected static final String LESS = "matcher.less";
    protected static final String LESS_EQUAL = "matcher.less.equal";



    private ResourceBundle expressions;

    public MatchParser(Locale locale) {
        this("matchers", locale);
    }

    public MatchParser(String expressionsFile, Locale locale) {
        expressions = ResourceBundle.getBundle(expressionsFile,locale);
    }


    public Matcher<?> unaryMatcher(String expression) {
        Matcher<?> matcher = null;
        if (match(NULL,expression)) {
            matcher = Matchers.nullValue();
        } else if (match(NOT_NULL,expression)) {
            matcher = Matchers.notNullValue();
        } else if (match(EMPTY,expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
            matchers.add(matcher(Matchers.isEmptyString(),String.class));
            matchers.add(matcher(Matchers.empty(),Collection.class));
            matchers.add(matcher(Matchers.emptyIterable(), Iterable.class));
            matcher = Matchers.anyOf( matchers );
        } else if (match(NOT_EMPTY,expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
   	        matchers.add(matcher(Matchers.not(Matchers.isEmptyString()),String.class));
            matchers.add(matcher(Matchers.not(Matchers.empty()),Collection.class));
            matchers.add(matcher(Matchers.not(Matchers.emptyIterable()), Iterable.class));
            matcher = Matchers.anyOf( matchers );
        } else if (match(NULL_EMPTY,expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
   	        matchers.add(matcher(Matchers.isEmptyOrNullString(),String.class));
            matchers.add(matcher(Matchers.empty(),Collection.class));
            matchers.add(matcher(Matchers.emptyIterable(), Iterable.class));
            matcher = Matchers.anyOf( matchers );
        } else if (match(NOT_NULL_EMPTY,expression)) {
            List<Matcher<? super Object>> matchers = new ArrayList<>();
        	matchers.add(matcher(Matchers.not(Matchers.isEmptyOrNullString()),String.class));
            matchers.add(matcher(Matchers.not(Matchers.empty()),Collection.class));
            matchers.add(matcher(Matchers.not(Matchers.emptyIterable()), Iterable.class));
            matcher = Matchers.anyOf( matchers );
        } else {
            throw new TestingException("The binary expression '"+expression+"' does not correspond to any matcher");
        }
        return matcher;
    }




    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Comparable> Matcher<?> binaryMatcher(String expression, T value) {
        Matcher<T> matcher = null;
        if (match(EQUAL,expression)) {
            matcher = Matchers.equalTo(value);
        } else if (match(DISTINCT,expression)) {
            matcher = Matchers.not(value);
        } else if (match(EQUAL_IGNORE_CASE,expression) && value instanceof String) {
            assertThat(value,Matchers.instanceOf(String.class));
            matcher = (Matcher<T>) Matchers.equalToIgnoringCase((String)value);
        } else if (match(GREATER,expression)) {
            assertThat(value,Matchers.instanceOf(Comparable.class));
            matcher = Matchers.greaterThan(value);
        } else if (match(LESS,expression)) {
            assertThat(value,Matchers.instanceOf(Comparable.class));
            matcher = Matchers.lessThan(value);
        } else if (match(GREATER_EQUAL,expression)) {
            assertThat(value,Matchers.instanceOf(Comparable.class));
            matcher = Matchers.greaterThanOrEqualTo(value);
        } else if (match(LESS_EQUAL,expression)) {
            assertThat(value,Matchers.instanceOf(Comparable.class));
            matcher = Matchers.lessThanOrEqualTo(value);
        } else {
            throw new TestingException("The binary expression '"+expression+"' does not correspond to any matcher");
        }

        return matcher;
    }






    @SuppressWarnings("unchecked")
	private <T> Matcher<? super Object> matcher(Matcher<? super T> matcher, Class<? super T> expectedType) {
        return (Matcher<? super Object>) Matchers.allOf(Matchers.instanceOf(expectedType),matcher);
    }
    


    protected boolean match(String key, String expression) {
        return expression.matches(this.expressions.getString(key));
    }

}
