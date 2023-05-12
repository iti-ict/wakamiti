/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.files;


import es.iti.wakamiti.api.util.WakamitiLogger;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FilesHelper {

    private static final Logger LOGGER = WakamitiLogger
            .of(LoggerFactory.getLogger( "es.iti.wakamiti.files"));
    private static final String TMP_PREFIX = "wakamiti";

    private final Deque<Runnable> cleanUpOperations = new LinkedList<>();

    public void waitForFile(File file, WatchEvent.Kind<Path> eventKind, long timeout) throws IOException, InterruptedException, TimeoutException {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path.of(file.getParent()).register(watcher, eventKind);

        WATCH:
        while (true) {
            WatchKey watchKey = watcher.poll(timeout, TimeUnit.SECONDS);
            if (watchKey != null) {
                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String eventFile = event.context().toString();
                    if (file.getName().equals(eventFile)) {
                        break WATCH;
                    }
                }
                watchKey.reset();
            } else {
                throw new TimeoutException("Timeout to access to file [" + file.getAbsolutePath() + "] exceeded");
            }
        }
    }

    public void cleanup() {
        cleanUpOperations.forEach(Runnable::run);
    }

    public void createSymLink(Path link, Path path) {
        Path symLink = FilesHelper.createSymbolicLink(link, path);
        cleanUpOperations.addLast(() -> FilesHelper.deleteSymbolicLink(symLink));
    }

    public void moveToFile(File source, File target) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }

        File p = getFirstExistingParent(target);
        File tmp = File.createTempFile(TMP_PREFIX, null);
        FilesHelper.copyFile(source, tmp);

        FilesHelper.moveFile(source, target);

        cleanUpOperations.addFirst(() -> {
            FilesHelper.cleanDirectory(p);
            FilesHelper.copyFile(tmp, source);
        });
    }

    public void moveToDir(File source, File target) throws IOException {
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }

        File p = getFirstExistingParent(target);

        if (source.isDirectory()) {
            File tmp = Files.createTempDirectory(TMP_PREFIX).toFile();
            FilesHelper.copyDirectory(source, tmp);

            if (target.exists()) {
                for (File file : Optional.ofNullable(source.listFiles()).orElse(new File[0])) {
                    FilesHelper.moveFileToDirectory(file, target);
                }
                FilesHelper.deleteDirectory(source);
            } else {
                FilesHelper.moveDirectory(source, target);
            }

            cleanUpOperations.addFirst(() -> {
                FilesHelper.cleanDirectory(p);
                FilesHelper.copyDirectory(tmp, source);
            });
        } else {
            File tmp = File.createTempFile(TMP_PREFIX, null);
            FilesHelper.copyFile(source, tmp);

            FilesHelper.moveFileToDirectory(source, target);

            cleanUpOperations.addFirst(() -> {
                FilesHelper.cleanDirectory(p);
                FilesHelper.copyFile(tmp, source);
            });
        }
    }

    public void copyToFile(File source, File target) {
        File p = getFirstExistingParent(target);

        FilesHelper.copyFile(source, target);

        cleanUpOperations.addFirst(() -> FilesHelper.cleanDirectory(p));
    }

    public void copyToDir(File source, File target) throws FileNotFoundException {
        if (!source.exists()) {
            throw new FileNotFoundException("Source '" + source + "' does not exist");
        }

        File p = getFirstExistingParent(target);

        if (source.isDirectory()) {
            if (target.exists()) {
                for (File file : Optional.ofNullable(source.listFiles()).orElse(new File[0])) {
                    FilesHelper.copyFileToDirectory(file, target);
                }
            } else {
                FilesHelper.copyDirectory(source, target);
            }
        } else {
            FilesHelper.copyFileToDirectory(source, target);
        }

        cleanUpOperations.addFirst(() -> FilesHelper.cleanDirectory(p));
    }

    public void delete(File file) throws IOException {
        if (!file.exists()) {
            throw new FileNotFoundException("Source '" + file + "' does not exist");
        }

        if (file.isDirectory()) {
            File tmp = Files.createTempDirectory(TMP_PREFIX).toFile();
            FilesHelper.copyDirectory(file, tmp);

            FilesHelper.deleteDirectory(file);

            cleanUpOperations.addFirst(() -> FilesHelper.copyDirectory(tmp, file));
        } else {
            File tmp = File.createTempFile(TMP_PREFIX, null);
            FilesHelper.copyFile(file, tmp);

            file.delete();

            cleanUpOperations.addFirst(() -> FilesHelper.copyFile(tmp, file));
        }
    }

    private File getFirstExistingParent(File file) {
        File parent = file.getParentFile();
        if (parent == null) {
            return file;
        }
        if (!parent.exists()) {
            return getFirstExistingParent(parent);
        }
        return parent;
    }

    private static Path createSymbolicLink(Path link, Path path) {
        try {
            LOGGER.debug("Creating symbolic link [{}] to [{}]", link, path);
            return Files.createSymbolicLink(link, path);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void deleteSymbolicLink(Path path) {
        try {
            LOGGER.debug("Deleting symbolic link [{}]", path);
            Files.delete(path);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveFile(File source, File target) {
        try {
            LOGGER.debug("Moving file [{}] to [{}]", source, target);
            FileUtils.moveFile(source, target);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveFileToDirectory(File source, File target) {
        try {
            LOGGER.debug("Moving [{}] to directory [{}]", source, target);
            FileUtils.moveFileToDirectory(source, target, true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void deleteDirectory(File file) {
        try {
            LOGGER.debug("Deleting directory [{}]", file);
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveDirectory(File source, File target) {
        try {
            LOGGER.debug("Moving [{}] to directory [{}]", source, target);
            FileUtils.moveDirectory(source, target);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyFile(File source, File target) {
        try {
            LOGGER.debug("Copying file [{}] to [{}]", source, target);
            FileUtils.copyFile(source, target, true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyFileToDirectory(File source, File target) {
        try {
            LOGGER.debug("Creating [{}] to directory [{}]", source, target);
            FileUtils.copyFileToDirectory(source, target, true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyDirectory(File source, File target) {
        try {
            LOGGER.debug(" Copy[{}] to directory [{}]", source, target);
            FileUtils.copyDirectory(source, target, true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void cleanDirectory(File dir) {
        try {
            LOGGER.debug("Cleaning up directory [{}]", dir);
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

}