/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.maven.internal;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.slf4j.Logger;


/**
 * Provides the Maven Transfer Listener functionality used by Wakamiti.
 */
public class MavenTransferListener implements TransferListener {

    private static final long BYTES_PER_KILOBYTE = 1000L;
    private final Logger logger;
    private final List<String> succededTransfers = new ArrayList<>();
    private final List<String> failedTransfers = new ArrayList<>();

    /**
     * Creates a listener that records JAR outcomes and reports transfer
     * progress.
     *
     * @param logger non-null destination for transfer diagnostics
     * @throws NullPointerException if {@code logger} is {@code null}
     */
    public MavenTransferListener(
            Logger logger
    ) {
        Objects.requireNonNull(logger);
        this.logger = logger;
    }

    /**
     * Returns JAR file names whose latest observed transfer succeeded.
     *
     * @return an immutable snapshot of successful transfers
     */
    public List<String> succededTransfers() {
        return List.copyOf(succededTransfers);
    }

    /**
     * Returns JAR file names whose transfer failed and was not subsequently
     * superseded by a success.
     *
     * @return an immutable snapshot of failed transfers
     */
    public List<String> failedTransfers() {
        return List.copyOf(failedTransfers);
    }

    @Override
    public void transferInitiated(
            TransferEvent event
    ) throws TransferCancelledException {
        //
    }

    @Override
    public void transferStarted(
            TransferEvent event
    ) throws TransferCancelledException {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isInfoEnabled()) {
            logger.debug(
                    "Transferring {artifact} [{}] from {uri}  ...",
                    resourceName(event),
                    resourceSize(event),
                    repository(event)
            );
        }
    }

    @Override
    public void transferProgressed(
            TransferEvent event
    ) {
        // do nothing
    }

    @Override
    public void transferCorrupted(
            TransferEvent event
    ) throws TransferCancelledException {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isErrorEnabled()) {
            logger.error("Checksum validation failed for [{artifact}]", resourceName(event));
        }
    }

    @Override
    public void transferSucceeded(
            TransferEvent event
    ) {
        if (event.getResource().getResourceName().endsWith(".jar")) {
            this.succededTransfers.add(resourceNameTrimmed(event));
            this.failedTransfers.remove(resourceNameTrimmed(event));
            if (logger.isInfoEnabled() && event.getResource().getContentLength() > 0) {
                logger.info(
                        "{artifact} [{}] downloaded from {uri} ",
                        resourceName(event),
                        resourceSize(event),
                        repository(event)
                );
            }
        }
    }

    @Override
    public void transferFailed(
            TransferEvent event
    ) {
        if (event.getResource().getResourceName().endsWith(".jar")) {
            this.failedTransfers.add(resourceNameTrimmed(event));
            if (logger.isErrorEnabled()) {
                logger.warn(
                        "Cannot download {artifact} from {uri}",
                        resourceName(event),
                        event.getResource().getRepositoryUrl()
                );
            }
        }
    }

    private String resourceName(
            TransferEvent event
    ) {
        return String.format("%-40s", resourceNameTrimmed(event));
    }

    private String resourceNameTrimmed(
            TransferEvent event
    ) {
        int index = event.getResource().getResourceName().lastIndexOf('/');
        return event.getResource().getResourceName().substring(index < 0 ? 0 : index + 1);
    }

    private String resourceSize(
            TransferEvent event
    ) {
        long size = event.getResource().getContentLength();
        return String.format(
                "%7s",
                size > BYTES_PER_KILOBYTE ? size / BYTES_PER_KILOBYTE + " Kb" : size + " bytes"
        );
    }

    private String repository(
            TransferEvent event
    ) {
        return event.getResource().getRepositoryUrl();
    }

}
