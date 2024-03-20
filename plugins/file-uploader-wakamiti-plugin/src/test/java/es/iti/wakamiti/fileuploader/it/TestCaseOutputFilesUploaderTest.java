/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader.it;


import es.iti.wakamiti.fileuploader.MockFtpServer;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import imconfig.AnnotatedConfiguration;
import imconfig.Property;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static org.junit.Assert.assertTrue;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/features/dummy.feature"),
        @Property(key = "fileUploader.host", value = "localhost:5432"),
        @Property(key = "fileUploader.protocol", value = "ftp"),
        @Property(key = "fileUploader.credentials.username", value = "test"),
        @Property(key = "fileUploader.credentials.password", value = "test"),
        @Property(key = "fileUploader.testCaseOutputs.destinationDir", value = "dira/dirb/%DATE%"),
        @Property(key = OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = OUTPUT_FILE_PER_TEST_CASE, value = "true"),
        @Property(key = OUTPUT_FILE_PER_TEST_CASE_PATH, value = "target"),
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.fileuploader.WakamitiSteps"),
})
@RunWith(WakamitiJUnitRunner.class)
public class TestCaseOutputFilesUploaderTest {

    private static final MockFtpServer ftpServer = new MockFtpServer(5432);

    @BeforeClass
    public static void setUp() throws FtpException, IOException {
        ftpServer.start();
    }

    @AfterClass
    public static void tearDown() {
        assertTrue(ftpServer.getTmpDir().resolve("dira/dirb/" + today() + "/ID-1.json").toFile().exists());
        ftpServer.stop();
        System.out.println("FTP stopped");
    }

    private static String today() {
        return DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.now());
    }
}
