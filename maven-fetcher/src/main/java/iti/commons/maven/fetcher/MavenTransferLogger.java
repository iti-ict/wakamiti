package iti.commons.maven.fetcher;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.slf4j.Logger;

import java.util.Objects;

public class MavenTransferLogger implements TransferListener {

    private final Logger logger;

    public MavenTransferLogger(Logger logger) {
        Objects.requireNonNull(logger);
        this.logger = logger;
    }

    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isInfoEnabled()) {
            logger.info("Downloading [{}] from {} ...", resourceName(event), event.getResource().getRepositoryUrl());
        }
    }

    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
        //
    }

    @Override
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        //
    }

    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isErrorEnabled()) {
            logger.error("Checksum validation failed for [{}]", resourceName(event));
        }
    }

    @Override
    public void transferSucceeded(TransferEvent event) {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isInfoEnabled()) {
            logger.info("[{}] downloaded [{}]", resourceName(event), resourceSize(event));
        }
    }

    @Override
    public void transferFailed(TransferEvent event) {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isErrorEnabled()) {
            logger.warn("Cannot download [{}] from {}", resourceName(event), event.getResource().getRepositoryUrl());
        }
    }

    private String resourceName(TransferEvent event) {
        int index = event.getResource().getResourceName().lastIndexOf('/');
        return event.getResource().getResourceName().substring(index < 0 ? 0 : index + 1);
    }

    private String resourceSize(TransferEvent event) {
        long size = event.getResource().getContentLength();
        return size > 1000L ? size / 1000L + " Kb" : size + " bytes";
    }

}
