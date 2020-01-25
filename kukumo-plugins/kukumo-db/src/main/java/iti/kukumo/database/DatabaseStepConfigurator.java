/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationConsumer;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Configurator;
import static iti.kukumo.database.DatabaseStepConfiguration.*;



@Extension(provider = "iti.kukumo", name = "kukumo-database-step-config", version = "1.0")
public class DatabaseStepConfigurator implements Configurator<DatabaseStepContributor> {

    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof DatabaseStepContributor;
    }


    @Override
    public void configure(DatabaseStepContributor contributor, Configuration configuration) {

        ConnectionParameters connectionParameters = contributor.getConnectionParameters();

        ConfigurationConsumer.of(configuration,connectionParameters)
            .ifPresent(DATABASE_CONNECTION_URL, String.class, ConnectionParameters::url)
            .ifPresent(DATABASE_CONNECTION_USERNAME, String.class, ConnectionParameters::username)
            .ifPresent(DATABASE_CONNECTION_PASSWORD, String.class, ConnectionParameters::password)
            .ifPresent(DATABASE_CONNECTION_DRIVER, String.class, ConnectionParameters::driver)
            .ifPresent(DATABASE_METADATA_SCHEMA, String.class, ConnectionParameters::schema)
            .ifPresent(DATABASE_METADATA_CATALOG, String.class, ConnectionParameters::catalog)
         ;

        ConfigurationConsumer.of(configuration, contributor)
         .orDefault(
              DATABASE_XLS_IGNORE_SHEET_PATTERN,
              String.class,
              Defaults.DEFAULT_DATABASE_XLS_IGNORE_SHEET_PATTERN,
              DatabaseStepContributor::setXlsIgnoreSheetRegex
         )
         .orDefault(
              DATABASE_NULL_SYMBOL,
              String.class,
              Defaults.DEFAULT_DATABASE_NULL_SYMBOL,
              DatabaseStepContributor::setNullSymbol
         )
         .orDefault(
              DATABASE_CSV_FORMAT,
              String.class,
              Defaults.DEFAULT_DATABASE_CSV_FORMAT,
              DatabaseStepContributor::setCsvFormat
         )
         .orDefault(
              DATABASE_ENABLE_CLEANUP_UPON_COMPLETION,
              Boolean.class,
              Defaults.DEFAULT_DATABASE_ENABLE_CLEANUP_UPON_COMPLETION,
              DatabaseStepContributor::setEnableCleanupUponCompletion
         )
         ;

        configuration.get(DATABASE_METADATA_CASE_SENSITIVITY, String.class)
            .map(String::toUpperCase)
            .map(CaseSensitivity::valueOf)
            .ifPresent(contributor::setCaseSensitivity);

    }




}
