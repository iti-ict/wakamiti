package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;

import java.util.function.BiConsumer;

@Extension(
    provider =  "es.iti.wakamiti",
    name = "azure-config",
    version = "1.1",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class AzureConfigContributor implements ConfigContributor<AzureReporter> {


    public static final String AZURE_HOST = "azure.host";
    public static final String AZURE_CREDENTIALS_USER = "azure.credentials.user";
    public static final String AZURE_CREDENTIALS_PASSWORD = "azure.credentials.password";
    public static final String AZURE_API_VERSION = "azure.apiVersion";
    public static final String AZURE_ORGANIZATION = "azure.organization";
    public static final String AZURE_PROJECT = "azure.project";
    public static final String AZURE_TAG = "azure.tag";
    public static final String AZURE_ATTACHMENTS = "azure.attachments";
    public static final String DEFAULT_AZURE_TAG = "Azure";
    public static final String DEFAULT_AZURE_API_VERSION = "5.0-preview";


    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
            AZURE_CREDENTIALS_USER, "",
            AZURE_CREDENTIALS_PASSWORD, "",
            AZURE_TAG, DEFAULT_AZURE_TAG,
            AZURE_API_VERSION, DEFAULT_AZURE_API_VERSION
        );
    }



    @Override
    public Configurer<AzureReporter> configurer() {
        return this::configure;
    }



    private void configure(AzureReporter azureReporter, Configuration configuration) {
        requiredProperty(configuration,azureReporter,AZURE_HOST, AzureReporter::setHost);
        requiredProperty(configuration,azureReporter,AZURE_ORGANIZATION,AzureReporter::setOrganization);
        requiredProperty(configuration,azureReporter,AZURE_PROJECT,AzureReporter::setProject);
        azureReporter.setAzureTag(configuration.get(AZURE_TAG,String.class).orElse(DEFAULT_AZURE_TAG));
        azureReporter.setCredentialsUser(configuration.get(AZURE_CREDENTIALS_USER,String.class).orElse(""));
        azureReporter.setCredentialsPassword(configuration.get(AZURE_CREDENTIALS_PASSWORD,String.class).orElse(""));
        requiredProperty(configuration,azureReporter,AZURE_API_VERSION,AzureReporter::setApiVersion);
        azureReporter.setAttachments(configuration.getList(AZURE_ATTACHMENTS,String.class));
    }




    private void requiredProperty(Configuration config, AzureReporter reporter, String property, BiConsumer<AzureReporter,String> setter) {
        String value = config.get(property,String.class).orElseThrow(()->new WakamitiException("Property {} is required",property));
        setter.accept(reporter,value);
    }


}
