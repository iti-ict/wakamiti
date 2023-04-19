package iti.kukumo.appium.test;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.appium.AppiumConfigContributor;
import iti.kukumo.core.junit.KukumoJUnitRunner;
import org.junit.runner.RunWith;

// @RunWith(KukumoJUnitRunner.class)
// Appium tests requires Appium server and a AVD to work, cannot be included as part of automatic build
@AnnotatedConfiguration({
        @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features/accesos.feature"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformName", value = "Android"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformVersion", value = "11"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appPackage", value = "es.consum.appconsumeventos"),
       // @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appActivity", value = ""),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".app", value = "/home/linesta/Downloads/Accesos.2.2.3.PRE.apk"),
        @Property(key = AppiumConfigContributor.APPIUM_URL, value = "http://127.0.0.1:4723/wd/hub")
})
public class TestAppium2 {

}
