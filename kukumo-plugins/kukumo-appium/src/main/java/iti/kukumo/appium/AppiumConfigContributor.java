package iti.kukumo.appium;

import imconfig.Configuration;
import imconfig.Configurer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.ConfigContributor;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

@Extension(
    provider = "iti.kukumo",
    name = "appium-config",
    version = "1.0",
    extensionPoint = "iti.kukumo.api.extensions.ConfigContributor"
)
public class AppiumConfigContributor implements ConfigContributor<AppiumStepContributor> {

    public static final String APPIUM_CAPABILITIES = "appium.capabilities";
    public static final String APPIUM_URL = "appium.url";

    private static final Configuration DEFAULTS = Configuration.factory().empty();


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof AppiumStepContributor;
    }


    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }


    @Override
    public Configurer<AppiumStepContributor> configurer() {
        return this::configure;
    }


    private void configure(AppiumStepContributor contributor, Configuration configuration) {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        configuration.inner(APPIUM_CAPABILITIES).forEach(capabilities::setCapability);
        // if app is passed, transform to absolute path
        configuration.get(APPIUM_CAPABILITIES+".app",String.class)
                .map(it->Path.of(it).toAbsolutePath().toString()).ifPresent(it -> capabilities.setCapability("app",it));
        contributor.setCapabilities(capabilities);
        configuration.get(APPIUM_URL,String.class).ifPresent(contributor::setAppiumURL);
    }


}
