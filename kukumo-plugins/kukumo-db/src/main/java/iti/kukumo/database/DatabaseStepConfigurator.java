package iti.kukumo.database;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Configurator;

/**
 * @author ITI
 * Created by ITI on 18/04/19
 */
@Extension(provider="iti.kukumo", name="kukumo-database-step-config", version="1.0")
public class DatabaseStepConfigurator implements Configurator<DatabaseStepContributor> {


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof DatabaseStepContributor;
    }


    @Override
    public void configure(DatabaseStepContributor contributor, Configuration configuration) {

        ConnectionParameters connectionParameters = contributor.getConnectionParameters();
        configuration.get(DatabaseStepConfiguration.DATABASE_CONNECTION_URL,String.class)
           .ifPresent(connectionParameters::url);
        configuration.get(DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME,String.class)
           .ifPresent(connectionParameters::username);
        configuration.get(DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD,String.class)
           .ifPresent(connectionParameters::password);
        configuration.get(DatabaseStepConfiguration.DATABASE_CONNECTION_DRIVER,String.class)
           .ifPresent(connectionParameters::driver);

        contributor.setXlsIgnoreSheetRegex(
            configuration.get(DatabaseStepConfiguration.DATABASE_XLS_IGNORE_SHEET_PATTERN,String.class)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_XLS_IGNORE_SHEET_PATTERN)
        );

        contributor.setXlsNullSymbol(
            configuration.get(DatabaseStepConfiguration.DATABASE_XLS_NULL_SYMBOL,String.class)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_XLS_NULL_SYMBOL)
        );

        contributor.setCsvFormat(
            configuration.get(DatabaseStepConfiguration.DATABASE_CSV_FORMAT,String.class)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_CSV_FORMAT)
        );

        contributor.setEnableCleanupUponCompletion(
             configuration.get(DatabaseStepConfiguration.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION,Boolean.class)
             .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_ENABLE_CLEANUP_UPON_COMPLETION)
        );

    }


}
