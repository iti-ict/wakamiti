/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader;


import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.extensions.ConfigContributor;
import imconfig.Configuration;
import imconfig.Configurer;

import java.util.Arrays;

import static java.lang.String.format;


@Extension(provider = "es.iti.wakamiti", name = "file-uploader-configurator", version = "2.5",
        extensionPoint = "es.iti.wakamiti.api.extensions.ConfigContributor")
public class FilesUploaderConfigurator implements ConfigContributor<AbstractFilesUploader> {

    private static final String PREFIX = "fileUploader";
    private static final String ENABLED = "enabled";
    private static final String HOST = "host";
    private static final String USERNAME = "credentials.username";
    private static final String PASSWORD = "credentials.password";
    private static final String DESTINATION_DIR = "destinationDir";
    private static final String PROTOCOL = "protocol";
    private static final String IDENTITY = "identity";

    @Override
    public Configuration defaultConfiguration() {
        return Configuration.factory().fromPairs(
                format("%s.%s", PREFIX, ENABLED), "true",
                format("%s.%s", PREFIX, PROTOCOL), "ftps"
        );
    }

    @Override
    public Configurer<AbstractFilesUploader> configurer() {
        return this::configure;
    }

    private void configure(AbstractFilesUploader filesUploader, Configuration configuration) {
        Configuration global = configuration.inner(PREFIX);
        Configuration spec = global.inner(filesUploader.category());
        if (spec.isEmpty()) {
            filesUploader.setEnabled(false);
            return;
        }
        for (Configuration conf : Arrays.asList(global, spec)) {
            conf.get(ENABLED, Boolean.class).ifPresent(filesUploader::setEnabled);
            conf.get(HOST, String.class).ifPresent(filesUploader::setHost);
            conf.get(USERNAME, String.class).ifPresent(filesUploader::setUsername);
            conf.get(PASSWORD, String.class).ifPresent(filesUploader::setPassword);
            conf.get(DESTINATION_DIR, String.class).ifPresent(filesUploader::setRemotePath);
            conf.get(PROTOCOL, String.class).ifPresent(filesUploader::setProtocol);
            conf.get(IDENTITY, String.class).ifPresent(filesUploader::setIdentity);
        }
    }

}
