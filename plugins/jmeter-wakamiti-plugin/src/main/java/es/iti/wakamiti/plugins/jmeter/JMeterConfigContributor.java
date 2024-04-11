/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.plugins.jmeter;



import imconfig.Configuration;
import imconfig.Configurer;
import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import org.apache.regexp.RE;


@Extension(
        provider =  "es.iti.wakamiti",
        name = "jmeter-config",
        version = "1.1",
        extensionPoint =  "es.iti.wakamiti.api.extensions.ConfigContributor"
)
public class JMeterConfigContributor implements ConfigContributor<JMeterStepContributor> {
    private static final String RESULTSTREE_ENABLED = "jmeter.output.resultstree.enabled";
    private static final String INFLUXDB_ENABLED = "jmeter.output.influxdb.enabled";
    private static final String CSV_ENABLED = "jmeter.output.csv.enabled";
    private static final String INFLUXDB_URL = "jmeter.output.influxdb.url";
    private static final String CSV_PATH = "jmeter.output.csv.path";
    private static final String HTML_ENABLED = "jmeter.output.html.enabled";
    private static final String HTML_PATH = "jmeter.output.html.path";
    public static final String BASE_URL = "jmeter.baseURL";
    public static final String USERNAME = "jmeter.auth.username";
    public static final String PASSWORD = "jmeter.auth.password";

    private static final Configuration DEFAULTS = Configuration.factory().fromPairs(
            BASE_URL, "http://localhost:8080",
            RESULTSTREE_ENABLED, "true",
            INFLUXDB_ENABLED, "false",
            CSV_ENABLED, "false",
            HTML_ENABLED, "false",
            INFLUXDB_URL, "http://localhost:8086/write?db=jmeter",
            CSV_PATH, "./test-results.csv",
            HTML_PATH, "./test-results.html"
    );


    @Override
    public boolean accepts(Object contributor) {
        return contributor instanceof JMeterStepContributor;
    }



    @Override
    public Configuration defaultConfiguration() {
        return DEFAULTS;
    }


    @Override
    public Configurer<JMeterStepContributor> configurer() {
        return this::configure;
    }


    private void configure(JMeterStepContributor contributor, Configuration configuration) {

        configuration.get(BASE_URL, String.class).ifPresent(contributor::setBaseURL);
        boolean treeEnabled = configuration.get(RESULTSTREE_ENABLED, Boolean.class).orElse(true);
        boolean influxEnabled = configuration.get(INFLUXDB_ENABLED, Boolean.class).orElse(true);
        boolean csvEnabled = configuration.get(CSV_ENABLED, Boolean.class).orElse(false);
        boolean htmlEnabled = configuration.get(HTML_ENABLED, Boolean.class).orElse(false);
        String influxUrl = configuration.get(INFLUXDB_URL, String.class).orElse("");
        String csvPath = configuration.get(CSV_PATH, String.class).orElse("");
        String htmlPath = configuration.get(HTML_PATH, String.class).orElse("");
        String username = configuration.get(USERNAME, String.class).orElse("");
        String password = configuration.get(PASSWORD, String.class).orElse("");



        contributor.configureOutputOptions(treeEnabled, influxEnabled, csvEnabled, htmlEnabled, influxUrl, csvPath, htmlPath);
        contributor.setAuthCredentials(username,password);

    }



}