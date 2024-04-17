package es.iti.wakamiti.email;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;


@Extension(provider =  "es.iti.wakamiti", name = "email-step-config", version = "2.5",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor")
public class EmailConfigContributor implements ConfigContributor<EmailStepContributor> {

    public static final String STORE_HOST = "email.store.host";
    public static final String STORE_PORT = "email.store.port";
    public static final String STORE_PROTOCOL = "email.store.protocol";
    public static final String ADDRESS = "email.address";
    public static final String PASSWORD = "email.password";
    public static final String STORE_FOLDER = "email.store.folder";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().empty();
    }

    @Override
    public Configurer<EmailStepContributor> configurer() {
        return this::configure;
    }

    private void configure(EmailStepContributor contributor, Configuration configuration) {
        configuration.get(STORE_PROTOCOL,String.class).ifPresent(contributor::setStoreProtocol);
        configuration.get(STORE_HOST,String.class).ifPresent(contributor::setHost);
        configuration.get(STORE_PORT,Integer.class).ifPresent(contributor::setPort);
        configuration.get(ADDRESS,String.class).ifPresent(contributor::setAddress);
        configuration.get(PASSWORD,String.class).ifPresent(contributor::setPassword);
        configuration.get(STORE_FOLDER,String.class).ifPresent(contributor::setFolder);
    }

}
