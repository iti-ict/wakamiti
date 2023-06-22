package es.iti.wakamiti.azure;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

@Extension(
    provider =  "es.iti.wakamiti",
    name = "azure-config",
    version = "1.1",
    extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class AzureConfigContributor implements ConfigContributor<AzureReporter> {


    public static final String AZURE_HOST = "azure.host";
    public static final String AZURE_CREDENTIALS = "azure.credentials";
    public static final String AZURE_API_VERSION_PLAN = "azure.apiVersion.plan";
    public static final String AZURE_API_VERSION_RUN = "azure.apiVersion.run";
    public static final String AZURE_ORGANIZATION = "azure.organization";
    public static final String AZURE_PROJECT = "azure.project";


    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().empty();
    }



    @Override
    public Configurer<AzureReporter> configurer() {
        return this::configure;
    }



    private void configure(AzureReporter azureReporter, Configuration configuration) {
        AzureApiBuilder apiBuilder = new AzureApiBuilder();
        requiredProperty(configuration,azureReporter,AZURE_HOST, apiBuilder::host);
        requiredProperty(configuration,azureReporter,AZURE_CREDENTIALS,AzureReporter::setCredentials);
        requiredProperty(configuration,azureReporter,AZURE_API_VERSION_PLAN,AzureReporter::setPlanApiVersion);
        requiredProperty(configuration,azureReporter,AZURE_API_VERSION_RUN,AzureReporter::setRunApiVersion);
        requiredProperty(configuration,azureReporter,AZURE_ORGANIZATION,AzureReporter::setOrganization);
        requiredProperty(configuration,azureReporter,AZURE_PROJECT,AzureReporter::setProject);
    }




    private void requiredProperty(Configuration config, AzureApiBuilder builder, String property, BiConsumer<AzureApiBuilder,String> setter) {
        String value = config.get(property,String.class).orElseThrow(()->new WakamitiException("Property {} is required",property));
        setter.accept(reporter,value);
    }


}
