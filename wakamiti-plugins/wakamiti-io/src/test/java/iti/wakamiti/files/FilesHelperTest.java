/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.files;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FilesHelperTest {

    private static final Logger log = LoggerFactory.getLogger("iti.wakamiti.test");
    private static final long TIMEOUT = 3;

    private final FilesHelper helper = new FilesHelper();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void waitForFileWhenIsFileCreationWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.newFolder("src"), "test.dat");
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.createNewFile();
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_CREATE, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test
    public void waitForFileWhenIsDirectoryCreationWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.getRoot(), "src");
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.mkdir();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_CREATE, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test
    public void waitForFileWhenIsFileDeletionWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.newFolder("src"), "test.dat");
        file.createNewFile();
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.delete();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_DELETE, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test
    public void waitForFileWhenIsDirectoryDeletionWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.getRoot(), "src");
        file.mkdir();
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.delete();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_DELETE, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test
    public void waitForFileWhenIsFileModificationWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.newFolder("src"), "test.dat");
        file.createNewFile();
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.setLastModified(System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_MODIFY, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test
    public void waitForFileWhenIsDirectoryModificationWithSuccess() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.getRoot(), "src");
        file.mkdir();
        long expectedMillis = 1000;

        Runnable backgroundAction = () -> {
            try {
                Thread.sleep(expectedMillis+10);
                file.setLastModified(System.currentTimeMillis());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        };

        // act
        long start = System.currentTimeMillis();
        new Thread(backgroundAction).start();
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_MODIFY, TIMEOUT);
        long result = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
        log.debug("Result: {} seconds", result);

        // check
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(expectedMillis), result);
    }

    @Test(expected = NoSuchFileException.class)
    public void waitForFileWhenParentNotExistsWithError() throws IOException, InterruptedException, TimeoutException {
        // prepare
        File file = new File(temporaryFolder.getRoot(), "src/test.dat");

        // act
        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_MODIFY, TIMEOUT);
    }
}