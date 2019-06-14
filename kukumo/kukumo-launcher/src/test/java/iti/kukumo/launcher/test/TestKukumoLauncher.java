package iti.kukumo.launcher.test;


import iti.commons.configurer.ConfigurationException;
import iti.kukumo.launcher.Arguments;
import org.junit.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class TestKukumoLauncher {

    
    @Test
    public void testArguments() throws ConfigurationException {
        String args = "fetch -modules iti.kukumo:kukumo.core:0.1.0 rest "+
                     " -conf /var/lib/kukumo.conf "+
                     " -Krest.host=localhost "+
                     " -MremoteRepositories=http://maven.com "+
                     " -clean";
        Arguments result = new Arguments(Paths.get(""), args.split(" "));
        assertThat(result.modules()).containsExactly("iti.kukumo:kukumo.core:0.1.0","rest");
        assertThat(result.confFile()).contains("/var/lib/kukumo.conf");
        assertThat(result.kukumoProperties()).contains(entry("rest.host","localhost"));
        assertThat(result.mavenFetcherProperties()).contains(entry("remoteRepositories","http://maven.com"));
        assertThat(result.mustClean()).isTrue();
    }

}
