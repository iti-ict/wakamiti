package es.iti.wakamiti.mail;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;

@Extension(
    provider =  "es.iti.wakamiti",
    name = "mail-step-config",
    version = "1.1",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class MailConfigContributor implements ConfigContributor<MailStepContributor> {
    public static final String PROTOCOL = "mail.protocol";
    public static final String HOST = "mail.host";
    public static final String PORT = "mail.port";
    public static final String ADDRESS = "mail.login";
    public static final String PASSWORD = "mail.password";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            PROTOCOL, "imaps",
            HOST, "imap.gmail.com",
            PORT, "993"
    );

    @Override
    public Configuration defaultConfiguration() { return DEFAULTS; }

    @Override
    public Configurer<MailStepContributor> configurer() {
        return this::configure;
    }

    private void configure(MailStepContributor contributor, Configuration configuration) {
        configuration.get(HOST, String.class);
        configuration.get(PORT, Integer.class);
        configuration.get(PROTOCOL, String.class);
        configuration.get((ADDRESS), String.class);
        configuration.get((PASSWORD), String.class);
    }

}
