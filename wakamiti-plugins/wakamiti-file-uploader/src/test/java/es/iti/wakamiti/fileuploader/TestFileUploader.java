package es.iti.wakamiti.fileuploader;

import es.iti.wakamiti.api.event.Event;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.Assert.assertTrue;

public class TestFileUploader {

    static MockFtpServer ftpServer;

    @BeforeClass
    public static void setUp()  {
        try {
            ftpServer = new MockFtpServer().start();
        } catch (FtpException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void tearDown() {
        ftpServer.stop();
    }


    @Test
    public void testFtpUploader() throws URISyntaxException, FtpException {
        FilesUploader filesUploader = new FilesUploader();
        filesUploader.setEnable(true);
        filesUploader.setRemotePath("dira/dirb/dirc");
        filesUploader.setUsername("test");
        filesUploader.setPassword("test");
        filesUploader.setHost("localhost:"+ftpServer.getPort());
        filesUploader.setProtocol("ftp");

        filesUploader.eventReceived(new Event(Event.BEFORE_WRITE_OUTPUT_FILES,Instant.now(),null));
        Path path = Path.of(Thread.currentThread().getContextClassLoader().getResource("file.txt").toURI());
        filesUploader.eventReceived(new Event(Event.OUTPUT_FILE_PER_TEST_CASE_WRITTEN,Instant.now(),path));
        filesUploader.eventReceived(new Event(Event.AFTER_WRITE_OUTPUT_FILES,Instant.now(),null));

        assertTrue(Files.exists(ftpServer.getTmpDir().resolve("dira/dirb/dirc/file.txt")));

    }




}
