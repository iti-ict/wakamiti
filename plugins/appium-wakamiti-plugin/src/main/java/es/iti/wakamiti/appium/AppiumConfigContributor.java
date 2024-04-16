package es.iti.wakamiti.appium;

import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.nio.file.Path;

@Extension(provider =  "es.iti.wakamiti", name = "appium-config", version = "2.5",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor")
public class AppiumConfigContributor implements ConfigContributor<AppiumStepContributor> {

    public static final String APPIUM_CAPABILITIES = "appium.capabilities";
    public static final String APPIUM_URL = "appium.url";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().empty();
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
