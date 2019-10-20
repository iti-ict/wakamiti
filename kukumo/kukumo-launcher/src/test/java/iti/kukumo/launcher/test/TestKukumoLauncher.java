/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.launcher.test;


import static org.assertj.core.api.Assertions.*;

import java.nio.file.Paths;

import org.junit.Test;

import iti.commons.configurer.ConfigurationException;
import iti.kukumo.launcher.Arguments;


public class TestKukumoLauncher {

    @Test
    public void testArguments() throws ConfigurationException {
        String args = "-modules iti.kukumo:kukumo.core:0.1.0 rest " + " -conf /var/lib/kukumo.conf " + " -Krest.host=localhost " + " -MremoteRepositories=http://maven.com ";
        Arguments result = new Arguments(Paths.get("x"), args.split(" "));
        assertThat(result.modules()).containsExactly("iti.kukumo:kukumo.core:0.1.0", "rest");
        assertThat(result.confFile()).contains("/var/lib/kukumo.conf");
        assertThat(result.kukumoConfiguration().asMap()).contains(entry("rest.host", "localhost"));
        assertThat(result.mavenFetcherConfiguration().asMap())
            .contains(entry("remoteRepositories", "http://maven.com"));
    }

}
