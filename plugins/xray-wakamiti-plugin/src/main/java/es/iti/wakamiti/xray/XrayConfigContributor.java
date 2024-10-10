package es.iti.wakamiti.xray;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;

import java.util.function.BiConsumer;

@Extension(
        provider = "com.wakamiti",
        name = "xray-config",
        version = "1.0",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class XrayConfigContributor implements ConfigContributor<XrayReporter> {

    public static final String XRAY_DISABLED = "xray.disabled";
    public static final String XRAY_JIRA_URL = "xray.jira.url";
    public static final String XRAY_EMAIL = "xray.email";
    public static final String XRAY_API_TOKEN = "xray.api.token";

    // Configuraciones por defecto
    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                XRAY_DISABLED, "false",
                XRAY_JIRA_URL, "",
                XRAY_EMAIL, "",
                XRAY_API_TOKEN, ""
        );
    }

    @Override
    public Configurer<XrayReporter> configurer() {
        return this::configure;
    }

    private void configure(XrayReporter reporter, Configuration configuration) {
        reporter.setDisabled(configuration.get(XRAY_DISABLED, Boolean.class).orElse(false));
        requiredProperty(configuration, reporter, XRAY_JIRA_URL, XrayReporter::setJiraUrl);
        requiredProperty(configuration, reporter, XRAY_EMAIL, XrayReporter::setEmail);
        requiredProperty(configuration, reporter, XRAY_API_TOKEN, XrayReporter::setApiToken);
    }

    private void requiredProperty(Configuration config, XrayReporter reporter, String property, BiConsumer<XrayReporter, String> setter) {
        String value = config.get(property, String.class).orElseThrow(() -> new WakamitiException("Property " + property + " is required"));
        setter.accept(reporter, value);
    }
}
