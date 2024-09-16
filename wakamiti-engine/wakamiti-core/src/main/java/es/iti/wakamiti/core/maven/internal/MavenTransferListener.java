/**
 * @author Luis Iñesta Gelabert -  luiinge@gmail.com
 */
package es.iti.wakamiti.core.maven.internal;


import java.util.*;

import org.eclipse.aether.transfer.TransferCancelledException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferListener;
import org.slf4j.Logger;



public class MavenTransferListener implements TransferListener {


    private final Logger logger;
    private final List<String> succededTransfers = new ArrayList<>();
    private final List<String> failedTransfers = new ArrayList<>();



    public MavenTransferListener(Logger logger) {
        Objects.requireNonNull(logger);
        this.logger = logger;
    }


    public List<String> succededTransfers() {
        return List.copyOf(succededTransfers);
    }


    public List<String> failedTransfers() {
        return List.copyOf(failedTransfers);
    }


    @Override
    public void transferInitiated(TransferEvent event) throws TransferCancelledException {
        //
    }


    @Override
    public void transferStarted(TransferEvent event) throws TransferCancelledException {
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
    public void transferProgressed(TransferEvent event) throws TransferCancelledException {
        
    }


    @Override
    public void transferCorrupted(TransferEvent event) throws TransferCancelledException {
        if (event.getResource().getResourceName().endsWith(".jar") && logger.isErrorEnabled()) {
            logger.error("Checksum validation failed for [{artifact}]", resourceName(event));
        }
    }


    @Override
    public void transferSucceeded(TransferEvent event) {
        if (event.getResource().getResourceName().endsWith(".jar")) {
            this.succededTransfers.add(resourceNameTrimmed(event));
            this.failedTransfers.remove(resourceNameTrimmed(event));
            if (logger.isInfoEnabled() && event.getResource().getContentLength() > 0 ) {
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
    public void transferFailed(TransferEvent event) {
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


    private String resourceName(TransferEvent event) {
        return String.format("%-40s",resourceNameTrimmed(event));
    }


    private String resourceNameTrimmed(TransferEvent event) {
        int index = event.getResource().getResourceName().lastIndexOf('/');
        return event.getResource().getResourceName().substring(index < 0 ? 0 : index + 1);
    }



    private String resourceSize(TransferEvent event) {
        long size = event.getResource().getContentLength();
        return String.format("%7s",size > 1000L ? size / 1000L + " Kb" : size + " bytes");
    }


    private String repository(TransferEvent event) {
        return event.getResource().getRepositoryUrl();
    }
}
