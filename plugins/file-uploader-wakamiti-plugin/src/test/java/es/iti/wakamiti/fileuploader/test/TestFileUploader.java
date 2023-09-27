package es.iti.wakamiti.fileuploader.test;


import es.iti.wakamiti.api.event.Event;
import es.iti.wakamiti.fileuploader.*;
import imconfig.Configuration;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestFileUploader {

    MockFtpServer ftpServer;

    @Before
    public void setUp() {
        try {
            ftpServer = new MockFtpServer().start();
        } catch (IOException | FtpException | RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        ftpServer.stop();
    }


    @Test
    public void testStandardOutputFileUploader() throws URISyntaxException {
        Configuration c = Configuration.factory().fromPairs(
                "fileUploader.host", "localhost:" + ftpServer.getPort(),
                "fileUploader.protocol", "ftp",
                "fileUploader.credentials.username", "test",
                "fileUploader.credentials.password", "test",
                "fileUploader.standardOutputs.enable", "true",
                "fileUploader.standardOutputs.destinationDir", "dira/dirb/dirc"

        );
        AbstractFilesUploader filesUploader = new StandardOutputFilesUploader();
        FilesUploaderConfigurator configurator = new FilesUploaderConfigurator();
        assertTrue(configurator.accepts(filesUploader));
        produceEvents(c, filesUploader, configurator, Event.STANDARD_OUTPUT_FILE_WRITTEN);
        assertTrue(Files.exists(ftpServer.getTmpDir().resolve("dira/dirb/dirc/file.txt")));
    }


    @Test
    public void testTestCasedOutputFileUploader() throws URISyntaxException {
        Configuration c = Configuration.factory().fromPairs(
                "fileUploader.host", "localhost:" + ftpServer.getPort(),
                "fileUploader.protocol", "ftp",
                "fileUploader.credentials.username", "test",
                "fileUploader.credentials.password", "test",
                "fileUploader.testCaseOutputs.enable", "true",
                "fileUploader.testCaseOutputs.destinationDir", "dira/dirb/dirc"

        );
        AbstractFilesUploader filesUploader = new TestCaseOutputFilesUploader();
        FilesUploaderConfigurator configurator = new FilesUploaderConfigurator();
        assertTrue(configurator.accepts(filesUploader));
        produceEvents(c, filesUploader, configurator, Event.TEST_CASE_OUTPUT_FILE_WRITTEN);
        assertTrue(Files.exists(ftpServer.getTmpDir().resolve("dira/dirb/dirc/file.txt")));
    }


    @Test
    public void testReportOutputFileUploader() throws URISyntaxException {
        Configuration c = Configuration.factory().fromPairs(
                "fileUploader.host", "localhost:" + ftpServer.getPort(),
                "fileUploader.protocol", "ftp",
                "fileUploader.credentials.username", "test",
                "fileUploader.credentials.password", "test",
                "fileUploader.reportOutputs.enable", "true",
                "fileUploader.reportOutputs.destinationDir", "dira/dirb/dirc"

        );
        AbstractFilesUploader filesUploader = new ReportOutputFilesUploader();
        FilesUploaderConfigurator configurator = new FilesUploaderConfigurator();
        assertTrue(configurator.accepts(filesUploader));
        produceEvents(c, filesUploader, configurator, Event.REPORT_OUTPUT_FILE_WRITTEN);
        assertTrue(Files.exists(ftpServer.getTmpDir().resolve("dira/dirb/dirc/file.txt")));
    }


    @Test
    public void testStandardOutputFileUploaderDisabledByDefault() throws URISyntaxException {
        Configuration c = Configuration.factory().fromPairs(
                "fileUploader.host", "localhost:" + ftpServer.getPort(),
                "fileUploader.protocol", "ftp",
                "fileUploader.credentials.username", "test",
                "fileUploader.credentials.password", "test",
                "fileUploader.standardOutputs.destinationDir", "dira/dirb/dirc"
        );
        StandardOutputFilesUploader filesUploader = new StandardOutputFilesUploader();
        FilesUploaderConfigurator configurator = new FilesUploaderConfigurator();
        produceEvents(c, filesUploader, configurator, Event.STANDARD_OUTPUT_FILE_WRITTEN);
        assertFalse(Files.exists(ftpServer.getTmpDir().resolve("dira/dirb/dirc/file.txt")));
    }


    private void produceEvents(
            Configuration c,
            AbstractFilesUploader filesUploader,
            FilesUploaderConfigurator configurator,
            String eventType
    ) throws URISyntaxException {
        assertTrue(configurator.accepts(filesUploader));
        configurator.configurer().configure(filesUploader, c);
        Path path = Path.of(Thread.currentThread().getContextClassLoader().getResource("file.txt").toURI());
        filesUploader.eventReceived(new Event(Event.BEFORE_WRITE_OUTPUT_FILES, Instant.now(), null));
        filesUploader.eventReceived(new Event(eventType, Instant.now(), path));
        filesUploader.eventReceived(new Event(Event.AFTER_WRITE_OUTPUT_FILES, Instant.now(), null));
    }


}
