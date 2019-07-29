package iti.kukumo.db;

import iti.commons.configurer.Configuration;
import iti.commons.jext.Extension;
import iti.kukumo.api.extensions.Configurator;

import java.util.Optional;

/**
 * @author ITI
 * Created by ITI on 18/04/19
 */
@Extension(provider="iti.kukumo", name="kukumo-db-step-config", version="1.0")
public class DatabaseStepConfigurator implements Configurator<DatabaseStepContributor> {

    
    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof DatabaseStepContributor;
    }


    @Override
    public void configure(DatabaseStepContributor contributor, Configuration configuration) {

        Optional<String> url = configuration.getString(DatabaseStepConfiguration.DATABASE_CONNECTION_URL);
        if (url.isPresent()) {
            String user = configuration.getString(DatabaseStepConfiguration.DATABASE_CONNECTION_USERNAME).orElse("");
            String password = configuration.getString(DatabaseStepConfiguration.DATABASE_CONNECTION_PASSWORD).orElse("");
            contributor.setConnectionParameters(new ConnectionParameters(url.get(),user,password));
        }

        contributor.setXlsIgnoreSheetRegex(
            configuration.getString(DatabaseStepConfiguration.DATABASE_XLS_IGNORE_SHEET_PATTERN)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_XLS_IGNORE_SHEET_PATTERN)
        );
        
        contributor.setXlsNullSymbol(
            configuration.getString(DatabaseStepConfiguration.DATABASE_XLS_NULL_SYMBOL)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_XLS_NULL_SYMBOL)
        );

        contributor.setCsvFormat(
            configuration.getString(DatabaseStepConfiguration.DATABASE_CSV_FORMAT)
            .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_CSV_FORMAT)
        );
        
        contributor.setEnableCleanupUponCompletion(
             configuration.getBoolean(DatabaseStepConfiguration.DATABASE_ENABLE_CLEANUP_UPON_COMPLETION)
             .orElse(DatabaseStepConfiguration.Defaults.DEFAULT_DATABASE_ENABLE_CLEANUP_UPON_COMPLETION)
        );
        
    }


}
