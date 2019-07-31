package iti.commons.configurer;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;
import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;

import java.math.BigDecimal;
import java.math.BigInteger;

public class TestConfigurationBuilder {

    private static final String KEY_ENV = "test.env.key";
    private static final String VAL_ENV = "Test Environment Value";
    
    private static final String KEY_STRING = "properties.test.key.string";
    private static final String KEY_STRINGS = "properties.test.key.strings";
    private static final String KEY_BOOL = "properties.test.key.bool";
    private static final String KEY_BOOLS = "properties.test.key.bools";
    private static final String KEY_INTEGER = "properties.test.key.integer";
    private static final String KEY_INTEGERS = "properties.test.key.integers";
    private static final String KEY_LONG = "properties.test.key.long";
    private static final String KEY_LONGS = "properties.test.key.longs";
    private static final String KEY_DOUBLE = "properties.test.key.double";
    private static final String KEY_DOUBLES = "properties.test.key.doubles";
    private static final String KEY_FLOAT = "properties.test.key.float";
    private static final String KEY_FLOATS = "properties.test.key.floats";
    private static final String KEY_BIGDECIMAL = "properties.test.key.bigdecimal";
    private static final String KEY_BIGDECIMALS = "properties.test.key.bigdecimals";
    private static final String KEY_BIGINTEGER = "properties.test.key.biginteger";
    private static final String KEY_BIGINTEGERS = "properties.test.key.bigintegers";
    
    private static final String VAL_STRING = "Properties Test String Value";
    private static final String VAL_STRINGS_1 = "Properties Array Value 1";
    private static final String VAL_STRINGS_2 = "Properties Array Value 2";
    private static final String VAL_BOOL = "true";
    private static final String VAL_BOOLS_1 = "true";
    private static final String VAL_BOOLS_2 = "false";
    private static final String VAL_BOOLS_3 = "true";
    private static final String VAL_INTEGER = "77";
    private static final String VAL_INTEGERS_1 = "77";
    private static final String VAL_INTEGERS_2 = "79";
    private static final String VAL_INTEGERS_3 = "83";
    private static final String VAL_LONG = "54353";
    private static final String VAL_LONGS_1 = "54353";
    private static final String VAL_LONGS_2 = "65256";
    private static final String VAL_LONGS_3 = "98432";
    private static final String VAL_DOUBLE = "3.45";
    private static final String VAL_DOUBLES_1 = "3.45";
    private static final String VAL_DOUBLES_2 = "6.76";
    private static final String VAL_DOUBLES_3 = "9.32";
    private static final String VAL_FLOAT = "6.98";
    private static final String VAL_FLOATS_1 = "6.98";
    private static final String VAL_FLOATS_2 = "2.23";
    private static final String VAL_FLOATS_3 = "1.24";
    private static final String VAL_BIGDECIMAL = "755.87";
    private static final String VAL_BIGDECIMALS_1 = "755.87";
    private static final String VAL_BIGDECIMALS_2 = "876.43";
    private static final String VAL_BIGDECIMALS_3 = "908.32";
    private static final String VAL_BIGINTEGER = "123456789";
    private static final String VAL_BIGINTEGERS_1 = "123456789";
    private static final String VAL_BIGINTEGERS_2 = "543987532";
    private static final String VAL_BIGINTEGERS_3 = "549874348";
    
   
    @Rule
    public final EnvironmentVariables env = new EnvironmentVariables();

    private ConfigurationBuilder builder;


    @Configurator(path="src/test/resources/test-conf.properties", environmentProperties=true)
    public static class ConfProperties { }

    @Configurator(path="classpath:test-conf.json", environmentProperties=true)
    public static class ConfJSON { }

    @Configurator(path="classpath:test-conf.xml", environmentProperties=true)
    public static class ConfXML { }

    @Configurator(path="classpath:test-conf.yaml", environmentProperties=true)
    public static class ConfYAML { }

    @Configurator(path="classpath:non-existing.properties", environmentProperties=true)
    public static class ConfNoFile { }

    @Configurator(environmentProperties=true, properties={
        @Property(key=KEY_ENV,value=VAL_ENV),
        @Property(key=KEY_STRING,value=VAL_STRING),
        @Property(key=KEY_STRINGS,value={VAL_STRINGS_1,VAL_STRINGS_2}),
        @Property(key=KEY_BOOL,value=VAL_BOOL),
        @Property(key=KEY_BOOLS,value= {VAL_BOOLS_1,VAL_BOOLS_2,VAL_BOOLS_3}),
        @Property(key=KEY_INTEGER,value=VAL_INTEGER),
        @Property(key=KEY_INTEGERS,value= {VAL_INTEGERS_1,VAL_INTEGERS_2,VAL_INTEGERS_3}),
        @Property(key=KEY_LONG,value=VAL_LONG),
        @Property(key=KEY_LONGS,value= {VAL_LONGS_1,VAL_LONGS_2,VAL_LONGS_3}),
        @Property(key=KEY_FLOAT,value=VAL_FLOAT),
        @Property(key=KEY_FLOATS,value= {VAL_FLOATS_1,VAL_FLOATS_2,VAL_FLOATS_3}),
        @Property(key=KEY_DOUBLE,value=VAL_DOUBLE),
        @Property(key=KEY_DOUBLES,value= {VAL_DOUBLES_1,VAL_DOUBLES_2,VAL_DOUBLES_3}),
        @Property(key=KEY_BIGDECIMAL,value=VAL_BIGDECIMAL),
        @Property(key=KEY_BIGDECIMALS,value= {VAL_BIGDECIMALS_1,VAL_BIGDECIMALS_2,VAL_BIGDECIMALS_3}),
        @Property(key=KEY_BIGINTEGER,value=VAL_BIGINTEGER),
        @Property(key=KEY_BIGINTEGERS,value= {VAL_BIGINTEGERS_1, VAL_BIGINTEGERS_2, VAL_BIGINTEGERS_3}),
        @Property(key="properties2.test2.key.string",value=VAL_STRING)
    })
    public static class ConfAnnotatedProps { }

    @Configurator(path="classpath:malformed-conf.xml", environmentProperties=true)
    public static class ConfMalformed { }

    @Configurator(path="classpath:unrecognized-format.xyq", environmentProperties=true)
    public static class ConfUnrecognized { }



    @Before
    public void prepare() {
        builder = new ConfigurationBuilder();
        env.set(KEY_ENV, VAL_ENV);
    }



    @Test
    public void testBasicConfiguration() {
        Configuration conf = builder.buildFromEnvironment(false);
        assertEnvProperties(conf);
    }


    @Test
    public void testPropertyFileConfiguration() throws ConfigurationException  {
        Configuration conf = builder.buildFromEnvironment(false).appendFromAnnotation(ConfProperties.class);
        assertProperties(conf);
        assertEnvProperties(conf);
    }

    @Test
    public void testJSONConfiguration() throws ConfigurationException {
        Configuration conf = builder.buildFromEnvironment(false).appendFromAnnotation(ConfJSON.class);
        assertProperties(conf);
        assertEnvProperties(conf);
    }

    @Test
    public void testYAMLConfiguration() throws ConfigurationException {
        Configuration conf = builder.buildFromEnvironment(false).appendFromAnnotation(ConfYAML.class);
        assertProperties(conf);
        assertEnvProperties(conf);
    }

    @Test
    public void testXMLConfiguration() throws ConfigurationException {
        Configuration conf =builder.buildFromEnvironment(false).appendFromAnnotation(ConfXML.class);
        assertProperties(conf);
        assertEnvProperties(conf);
    }

    @Test
    public void testAnnotatedPropertiesConfiguration() throws ConfigurationException {
        Configuration conf = builder.buildFromEnvironment(false).appendFromAnnotation(ConfAnnotatedProps.class);
        assertProperties(conf);
        assertEnvProperties(conf);
    }


    @Test(expected=ConfigurationException.class)
    public void testMalformedConfiguration() throws ConfigurationException {
        builder.buildFromAnnotation(ConfMalformed.class);
    }




    @Test(expected=ConfigurationException.class)
    public void testUnrecognizedConfiguration() throws ConfigurationException {
        builder.buildFromAnnotation(ConfUnrecognized.class);
    }



    @Test(expected=ConfigurationException.class)
    public void testNonExistingFileConfiguratio() throws ConfigurationException {
        builder.buildFromAnnotation(ConfNoFile.class);
    }

    
    @Test
    public void testToString() throws ConfigurationException {
        Assertions.assertThat(builder.buildFromAnnotation(ConfAnnotatedProps.class).toString())
        .isEqualTo(
            "configuration:\n" + 
            "---------------\n" + 
            "test.env.key : Test Environment Value\n" + 
            "properties.test.key.string : Properties Test String Value\n" + 
            "properties.test.key.strings : [Properties Array Value 1, Properties Array Value 2]\n" + 
            "properties.test.key.bool : true\n" + 
            "properties.test.key.bools : [true, false, true]\n" + 
            "properties.test.key.integer : 77\n" + 
            "properties.test.key.integers : [77, 79, 83]\n" + 
            "properties.test.key.long : 54353\n" + 
            "properties.test.key.longs : [54353, 65256, 98432]\n" + 
            "properties.test.key.float : 6.98\n" + 
            "properties.test.key.floats : [6.98, 2.23, 1.24]\n" + 
            "properties.test.key.double : 3.45\n" + 
            "properties.test.key.doubles : [3.45, 6.76, 9.32]\n" + 
            "properties.test.key.bigdecimal : 755.87\n" + 
            "properties.test.key.bigdecimals : [755.87, 876.43, 908.32]\n" + 
            "properties.test.key.biginteger : 123456789\n" + 
            "properties.test.key.bigintegers : [123456789, 543987532, 549874348]\n" + 
            "properties2.test2.key.string : Properties Test String Value\n" + 
            "---------------"
        );
    }
    
    
    @Test
    public void testFilter() throws ConfigurationException {
        Configuration conf = builder.buildFromAnnotation(ConfAnnotatedProps.class).filter("properties2");
        System.out.println(conf);
        Assertions.assertThat(conf.prefix()).isEqualTo("properties2");
        Assertions.assertThat(conf.getString("properties.test.key.string")).isEmpty();
        Assertions.assertThat(conf.getString("properties2.test2.key.string").get()).isEqualTo(VAL_STRING);
    }


    private void assertProperties(Configuration conf) {
        Assertions.assertThat(conf.getString(KEY_STRING).get()).isEqualTo(VAL_STRING);
        Assertions.assertThat(conf.getStringList(KEY_STRINGS)).containsExactlyInAnyOrder(
            VAL_STRINGS_1,
            VAL_STRINGS_2
        );
        Assertions.assertThat(conf.getBoolean(KEY_BOOL).get()).isEqualTo(true);
        Assertions.assertThat(conf.getBooleanList(KEY_BOOLS)).containsExactlyInAnyOrder(true,false,true);
        Assertions.assertThat(conf.getInteger(KEY_INTEGER).get()).isEqualTo(77);
        Assertions.assertThat(conf.getIntegerList(KEY_INTEGERS)).containsExactlyInAnyOrder(77,79,83);
        Assertions.assertThat(conf.getLong(KEY_LONG).get()).isEqualTo(54353L);
        Assertions.assertThat(conf.getLongList(KEY_LONGS)).containsExactlyInAnyOrder(54353L,65256L,98432L);
        Assertions.assertThat(conf.getDouble(KEY_DOUBLE).get()).isEqualTo(3.45);
        Assertions.assertThat(conf.getDoubleList(KEY_DOUBLES)).containsExactlyInAnyOrder(3.45,6.76,9.32);
        Assertions.assertThat(conf.getFloat(KEY_FLOAT).get()).isEqualTo(6.98f);
        Assertions.assertThat(conf.getFloatList(KEY_FLOATS)).containsExactlyInAnyOrder(6.98f,2.23f,1.24f);
        Assertions.assertThat(conf.getBigDecimal(KEY_BIGDECIMAL).get()).isEqualByComparingTo(new BigDecimal(VAL_BIGDECIMAL));
        Assertions.assertThat(conf.getBigDecimalList(KEY_BIGDECIMALS)).containsExactlyInAnyOrder(
            new BigDecimal(VAL_BIGDECIMALS_1),
            new BigDecimal(VAL_BIGDECIMALS_2), 
            new BigDecimal(VAL_BIGDECIMALS_3)
        );
        Assertions.assertThat(conf.getBigInteger(KEY_BIGINTEGER).get()).isEqualByComparingTo(new BigInteger(VAL_BIGINTEGER));
        Assertions.assertThat(conf.getBigIntegerList(KEY_BIGINTEGERS)).containsExactlyInAnyOrder(
            new BigInteger(VAL_BIGINTEGERS_1),
            new BigInteger(VAL_BIGINTEGERS_2), 
            new BigInteger(VAL_BIGINTEGERS_3)
        );

        assertNullProperties(conf);
    }



    private void assertNullProperties(Configuration conf) {
        String nonExistingKey = "xxx";
        Assertions.assertThat(conf.getString(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getStringList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBoolean(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBooleanList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getInteger(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getIntegerList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getLong(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getLongList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getDouble(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getDoubleList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getFloat(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getFloatList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBigDecimal(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBigDecimalList(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBigInteger(nonExistingKey)).isEmpty();
        Assertions.assertThat(conf.getBigIntegerList(nonExistingKey)).isEmpty();
    }



    private void assertEnvProperties(Configuration conf) {
        Assertions.assertThat(conf.getString(KEY_ENV).get()).isEqualTo(VAL_ENV);
    }

}
