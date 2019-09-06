package iti.kukumo.api;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationBuilder;
import iti.commons.configurer.ConfigurationException;
import iti.commons.configurer.Configurator;
import iti.commons.configurer.Property;

public class KukumoConfiguration {

    public static final String PREFIX = "kukumo";

    // basic configuration

    /** Types of resources to be discovered and processed */
    public static final String RESOURCE_TYPES = "resourceTypes";

    /** Language used by a resource */
    public static final String LANGUAGE = "language";

    /** Path (file-based or classpath-based) where the resource files are located */
    public static final String RESOURCE_PATH = "resourcePath";

    /** List of names of the modules required */
    public static final String MODULES = "modules";

    /** List of full-qualified classes implementing KukumoStepProvider that are not declared as module */
    public static final String NON_REGISTERED_STEP_PROVIDERS = "nonRegisteredStepProviders";

    /** Output file path */
    public static final String OUTPUT_FILE_PATH = "outputFilePath";

    /** Report sources */
    public static final String REPORT_SOURCE = "report.source";

    /** Enable / disable the report generation */
    public static final String REPORT_GENERATION = "report.generation";

    /** Tag Expression filter */
    public static final String TAG_FILTER = "tagFilter";

    /** Pattern for use specific tag as an identifier */
    public static final String ID_TAG_PATTERN = "idTagPattern";

    /** Overriden locale for data formatting */
    public static final String DATA_FORMAT_LANGUAGE = "dataFormatLanguage";

    /** Set if the redefinition feature is enabled */
    public static final String REDEFINITION_ENABLED = "redefinition.enabled";

    /** Dash-separated number list that indicates how many implementation steps correspond to each definition step */
    public static final String REDEFINITION_STEP_MAP = "redefinition.stepMap";

    /** Tag used for annotate a feature as a definition */
    public static final String REDEFINITION_DEFINITION_TAG = "redefinition.definitionTag";

    /** Tag used for annotate a feature as an implementation */
    public static final String REDEFINITION_IMPLEMENTATION_TAG = "redefinition.implementationTag";

    /** Use Ansi characters in the logs */
    public static final String LOGS_ANSI_ENABLED = "logs.ansi.enabled";

    /** Use Ansi styles in the logs */
    public static final String LOGS_ANSI_STYLES = "logs.ansi.styles";

    /** Show the Kukumo logo in the logs */
    public static final String LOGS_SHOW_LOGO = "logs.showLogo";

    /** Show the source for each step in the logs */
    public static final String LOGS_SHOW_STEP_SOURCE = "logs.showStepSource";

    /** Show the elapsed time for each step in the logs */
    public static final String LOGS_SHOW_ELAPSED_TIME = "logs.showElapsedTime";


    @Configurator(properties={
        @Property(key=RESOURCE_PATH,value="."),
        @Property(key=OUTPUT_FILE_PATH,value=Defaults.DEFAULT_OUTPUT_FILE_PATH),
        @Property(key=REPORT_GENERATION,value="true"),
        @Property(key=ID_TAG_PATTERN,value=Defaults.DEFAULT_ID_TAG_PATTERN),
        @Property(key=REDEFINITION_ENABLED,value=Defaults.DEFAULT_REDEFINITION_ENABLED),
        @Property(key=REDEFINITION_DEFINITION_TAG,value=Defaults.DEFAULT_REDEFINITION_DEFINITION_TAG),
        @Property(key=REDEFINITION_IMPLEMENTATION_TAG,value=Defaults.DEFAULT_REDEFINITION_IMPLEMENTATION_TAG),
        @Property(key="logs.ansi.styles.logo",value="bold,green"),
        @Property(key="logs.ansi.styles.keyword",value="blue"),
        @Property(key="logs.ansi.styles.source",value="faint"),
        @Property(key="logs.ansi.styles.time",value="faint"),
        @Property(key="logs.ansi.styles.stepResult.PASSED",value="green,bold"),
        @Property(key="logs.ansi.styles.stepResult.SKIPPED",value="faint"),
        @Property(key="logs.ansi.styles.stepResult.UNDEFINED",value="yellow"),
        @Property(key="logs.ansi.styles.stepResult.FAILED",value="red,bold"),
        @Property(key="logs.ansi.styles.stepResult.ERROR",value="red,bold")
    })
    public static class Defaults {
        public static final String DEFAULT_OUTPUT_FILE_PATH = "kukumo.json";
        public static final String DEFAULT_ID_TAG_PATTERN = "ID-(.*)";
        public static final String DEFAULT_REDEFINITION_ENABLED = "true";
        public static final String DEFAULT_REDEFINITION_DEFINITION_TAG = "definition";
        public static final String DEFAULT_REDEFINITION_IMPLEMENTATION_TAG = "implementation";

        private Defaults() { /* avoid instantiation*/ }
    }



    public static Configuration defaultConfiguration() throws ConfigurationException {
        return ConfigurationBuilder.instance()
                .buildFromEnvironment(false)
                .filtered(PREFIX)
                .appendFromAnnotation(Defaults.class);
        // TODO append from file
    }


    private KukumoConfiguration() {
        // avoid instantation
    }
}
