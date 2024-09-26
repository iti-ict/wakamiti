/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.fileuploader.it;


import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.fileuploader.AbstractFilesUploader;
import es.iti.wakamiti.fileuploader.MockFtpServer;
import es.iti.wakamiti.junit.WakamitiJUnitRunner;
import es.iti.wakamiti.api.imconfig.AnnotatedConfiguration;
import es.iti.wakamiti.api.imconfig.Property;
import org.apache.ftpserver.ftplet.FtpException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static es.iti.wakamiti.api.WakamitiConfiguration.*;
import static org.junit.Assert.*;


@AnnotatedConfiguration({
        @Property(key = RESOURCE_TYPES, value = "gherkin"),
        @Property(key = RESOURCE_PATH, value = "src/test/resources/features/dummy.feature"),
        @Property(key = "fileUploader.host", value = "localhost:5432"),
        @Property(key = "fileUploader.protocol", value = "ftp"),
        @Property(key = "fileUploader.credentials.username", value = "test"),
        @Property(key = "fileUploader.credentials.password", value = "test"),
        @Property(key = "fileUploader.testCaseOutputs.destinationDir", value = "dira/dirb/%DATE%%TIME%"),
        @Property(key = OUTPUT_FILE_PATH, value = "target/wakamiti.json"),
        @Property(key = OUTPUT_FILE_PER_TEST_CASE, value = "true"),
        @Property(key = NON_REGISTERED_STEP_PROVIDERS, value = "es.iti.wakamiti.fileuploader.WakamitiSteps"),
})
@RunWith(WakamitiJUnitRunner.class)
public class TestCaseOutputFilesUploaderTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(AbstractFilesUploader.class);
    private static final MockFtpServer ftpServer = new MockFtpServer(5432);

    @BeforeClass
    public static void setUp() throws FtpException, IOException {
        ftpServer.start();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        if (LOGGER.isDebugEnabled()) {
            System.out.println(printDirectoryTree(ftpServer.getTmpDir().toFile()));
        }
        Path resultsPath = ftpServer.getTmpDir().resolve("dira/dirb/");
        File[] dateFiles = resultsPath.toFile().listFiles();
        assertNotNull(dateFiles);
        assertEquals(1, dateFiles.length);
        assertTrue(dateFiles[0].getName().startsWith(today()));
        File[] jsonFiles = dateFiles[0].listFiles();
        assertNotNull(jsonFiles);
        Arrays.sort(jsonFiles);
        assertEquals(2, jsonFiles.length);
        assertEquals("ID-1.json", jsonFiles[0].getName());
        assertEquals("ID-2.json", jsonFiles[1].getName());
        ftpServer.stop();
        System.out.println("FTP stopped");
    }

    private static String today() {
        return DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.from(ZoneOffset.UTC)).format(Instant.now());
    }

    /**
     * Pretty print the directory tree and its file names.
     *
     * @param folder
     *            must be a folder.
     * @return
     */
    public static String printDirectoryTree(File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        int indent = 0;
        StringBuilder sb = new StringBuilder();
        printDirectoryTree(folder, indent, sb);
        return sb.toString();
    }

    private static void printDirectoryTree(File folder, int indent,
                                           StringBuilder sb) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("folder is not a Directory");
        }
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(folder.getName());
        sb.append("/");
        sb.append("\n");
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                printDirectoryTree(file, indent + 1, sb);
            } else {
                printFile(file, indent + 1, sb);
            }
        }

    }

    private static void printFile(File file, int indent, StringBuilder sb) {
        sb.append(getIndentString(indent));
        sb.append("+--");
        sb.append(file.getName());
        sb.append("\n");
    }

    private static String getIndentString(int indent) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            sb.append("|  ");
        }
        return sb.toString();
    }
}
