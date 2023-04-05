package iti.kukumo.appium.test;


import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.appium.AppiumConfigContributor;
import iti.kukumo.core.junit.KukumoJUnitRunner;
import org.junit.runner.RunWith;

@RunWith(KukumoJUnitRunner.class)
@AnnotatedConfiguration({
        @Property(key = KukumoConfiguration.RESOURCE_TYPES, value = "gherkin"),
        @Property(key = KukumoConfiguration.RESOURCE_PATH, value = "src/test/resources/features"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformName", value = "Android"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".platformVersion", value = "11"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appPackage", value = "io.appium.android.apis"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".appActivity", value = ".view.TextFields"),
        @Property(key = AppiumConfigContributor.APPIUM_CAPABILITIES+".app", value = "ApiDemos-debug.apk"),
        @Property(key = AppiumConfigContributor.APPIUM_URL, value = "http://127.0.0.1:4723/wd/hub")
})
public class TestAppium {

}
