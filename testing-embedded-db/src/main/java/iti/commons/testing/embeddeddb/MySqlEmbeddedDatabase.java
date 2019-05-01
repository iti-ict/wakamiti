package iti.commons.testing.embeddeddb;

import java.io.File;
import java.util.Optional;

import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.EmbeddedMysql.Builder;
import com.wix.mysql.Sources;
import com.wix.mysql.SqlScriptSource;
import com.wix.mysql.config.MysqldConfig;
import com.wix.mysql.distribution.Version;

public class MySqlEmbeddedDatabase extends EmbeddedDatabase {


    private EmbeddedMysql embeddedMysql;


    public MySqlEmbeddedDatabase(EmbeddedDatabaseTemplate template) {
        super(template);
    }

    private MysqldConfig config() {
        return MysqldConfig.aMysqldConfig(Version.v5_7_19)
            .withPort(template.port())
            .withUser(template.username(), template.password())
            .build();
    }

    public void start() {
        Builder builder = EmbeddedMysql.anEmbeddedMysql(config());
        Optional<SqlScriptSource> initialScript = scriptFile.map(File::new).map(Sources::fromFile);
        if (initialScript.isPresent()) {
            builder.addSchema(template.schema(), initialScript.get());
        } else {
            builder.addSchema(template.schema());
        }
        embeddedMysql = builder.start();
    }
       
    
    public void stop() {
        embeddedMysql.stop();
    }
}
