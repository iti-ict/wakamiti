/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.util.ResourceLoader;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Provides the Files Helper functionality used by Wakamiti.
 */
public class FilesHelper {

    private static final Logger LOGGER = WakamitiLogger
            .of(LoggerFactory.getLogger("es.iti.wakamiti.files"));
    private static final String TMP_PREFIX = "wakamiti";
    private static final Path TMP_DIRECTORY = Path.of(
            System.getProperty("user.home"),
            ".wakamiti",
            "tmp"
    );

    private final Deque<Runnable> cleanUpOperations = new LinkedList<>();

    private static Path createSymbolicLink(
            Path link,
            Path path
    ) {
        try {
            LOGGER.debug("Creating symbolic link [{}] to [{}]", link, path);
            return Files.createSymbolicLink(absolutePath(link), absolutePath(path));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void deleteSymbolicLink(
            Path path
    ) {
        try {
            LOGGER.debug("Deleting symbolic link [{}]", path);
            Files.delete(absolutePath(path));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveFile(
            File source,
            File target
    ) {
        try {
            LOGGER.debug("Moving file [{}] to [{}]", source, target);
            FileUtils.moveFile(absolutePath(source), absolutePath(target));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveFileToDirectory(
            File source,
            File target
    ) {
        try {
            LOGGER.debug("Moving [{}] to directory [{}]", source, target);
            FileUtils.moveFileToDirectory(absolutePath(source), absolutePath(target), true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void deleteDirectory(
            File file
    ) {
        try {
            LOGGER.debug("Deleting directory [{}]", file);
            FileUtils.deleteDirectory(absolutePath(file));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void moveDirectory(
            File source,
            File target
    ) {
        try {
            LOGGER.debug("Moving [{}] to directory [{}]", source, target);
            FileUtils.moveDirectory(absolutePath(source), absolutePath(target));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyFile(
            File source,
            File target
    ) {
        try {
            LOGGER.debug("Copying file [{}] to [{}]", source, target);
            FileUtils.copyFile(absolutePath(source), absolutePath(target), true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyFileToDirectory(
            File source,
            File target
    ) {
        try {
            LOGGER.debug("Creating [{}] to directory [{}]", source, target);
            FileUtils.copyFileToDirectory(absolutePath(source), absolutePath(target), true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void copyDirectory(
            File source,
            File target
    ) {
        try {
            LOGGER.debug(" Copy[{}] to directory [{}]", source, target);
            FileUtils.copyDirectory(absolutePath(source), absolutePath(target), true);
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static void cleanDirectory(
            File dir
    ) {
        try {
            LOGGER.debug("Cleaning up directory [{}]", dir);
            FileUtils.cleanDirectory(absolutePath(dir));
        } catch (IOException e) {
            throw new FilesHelperException(e);
        }
    }

    private static File absolutePath(
            File file
    ) {
        return resourceLoader().absolutePath(file);
    }

    private static Path absolutePath(
            Path path
    ) {
        return resourceLoader().absolutePath(path);
    }

    private static ResourceLoader resourceLoader() {
        return WakamitiAPI.instance().resourceLoader();
    }

    private static File createTemporaryFile() throws IOException {
        return Files.createTempFile(
                Files.createDirectories(TMP_DIRECTORY),
                TMP_PREFIX,
                null
        ).toFile();
    }

    private static File createTemporaryDirectory() throws IOException {
        return Files.createTempDirectory(
                Files.createDirectories(TMP_DIRECTORY),
                TMP_PREFIX
        ).toFile();
    }

    /**
     * Waits for a named entry event in the file's parent directory.
     *
     * @param file exact child name to observe
     * @param eventKind creation, modification or deletion event kind
     * @param timeout maximum wait in seconds for each polling cycle
     * @throws IOException if the directory cannot be watched
     * @throws InterruptedException if the waiting thread is interrupted
     * @throws TimeoutException if no matching event arrives before the timeout
     */
    public void waitForFile(
            File file,
            WatchEvent.Kind<Path> eventKind,
            long timeout
    ) throws IOException, InterruptedException, TimeoutException {
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

    /**
     * Runs registered restoration operations in their stored order.
     * Operations remain registered, so this method is intended to run once at
     * scenario teardown.
     */
    public void cleanup() {
        cleanUpOperations.forEach(Runnable::run);
    }

    /**
     * Creates a symbolic link and schedules its removal during cleanup.
     *
     * @param link path of the link to create
     * @param path target path referenced by the link
     * @throws FilesHelperException if the link cannot be created
     */
    public void createSymLink(
            Path link,
            Path path
    ) {
        Path symLink = FilesHelper.createSymbolicLink(link, path);
        cleanUpOperations.addLast(() -> FilesHelper.deleteSymbolicLink(symLink));
    }

    /**
     * Moves a source file to an exact target path and records enough state to
     * restore the source and clean created target directories.
     *
     * @param source existing source file
     * @param target destination file path
     * @throws IOException if temporary backup creation fails or the source is
     *                     absent
     * @throws FilesHelperException if copying or moving fails
     */
    public void moveToFile(
            File source,
            File target
    ) throws IOException {
        if (!source.exists()) {
            throwSourceNotExistsException(source);
        }

        File p = getFirstExistingParent(target);
        File tmp = createTemporaryFile();
        FilesHelper.copyFile(source, tmp);

        FilesHelper.moveFile(source, target);

        cleanUpOperations.addFirst(() -> {
            FilesHelper.cleanDirectory(p);
            FilesHelper.copyFile(tmp, source);
        });
    }

    private void throwSourceNotExistsException(
            File source
    ) throws FileNotFoundException {
        throw new FileNotFoundException("Source '" + source + "' does not exist");
    }

    /**
     * Moves a file or directory into a target directory and schedules
     * restoration of its original tree.
     *
     * @param source existing file or directory
     * @param target destination directory
     * @throws IOException if backup creation fails or the source is absent
     * @throws FilesHelperException if a filesystem operation fails
     */
    public void moveToDir(
            File source,
            File target
    ) throws IOException {
        if (!source.exists()) {
            throwSourceNotExistsException(source);
        }

        File p = getFirstExistingParent(target);

        if (source.isDirectory()) {
            File tmp = createTemporaryDirectory();
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
            File tmp = createTemporaryFile();
            FilesHelper.copyFile(source, tmp);

            FilesHelper.moveFileToDirectory(source, target);

            cleanUpOperations.addFirst(() -> {
                FilesHelper.cleanDirectory(p);
                FilesHelper.copyFile(tmp, source);
            });
        }
    }

    /**
     * Copies a source to an exact file path and schedules cleanup of newly
     * created target content.
     *
     * @param source source file
     * @param target destination file
     * @throws FilesHelperException if copying fails
     */
    public void copyToFile(
            File source,
            File target
    ) {
        File p = getFirstExistingParent(target);

        FilesHelper.copyFile(source, target);

        cleanUpOperations.addFirst(() -> FilesHelper.cleanDirectory(p));
    }

    /**
     * Copies a file or directory into a destination directory and schedules
     * cleanup of the affected target tree.
     *
     * @param source existing file or directory
     * @param target destination directory
     * @throws FileNotFoundException if the source does not exist
     * @throws FilesHelperException if copying fails
     */
    public void copyToDir(
            File source,
            File target
    ) throws FileNotFoundException {
        if (!source.exists()) {
            throwSourceNotExistsException(source);
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

    /**
     * Deletes a file or directory after making a temporary backup, then
     * schedules restoration during cleanup.
     *
     * @param file existing path to delete
     * @throws IOException if backup creation or direct file deletion fails
     * @throws FilesHelperException if directory operations fail
     */
    public void delete(
            File file
    ) throws IOException {
        if (!file.exists()) {
            throwSourceNotExistsException(file);
        }

        if (file.isDirectory()) {
            File tmp = createTemporaryDirectory();
            FilesHelper.copyDirectory(file, tmp);

            FilesHelper.deleteDirectory(file);

            cleanUpOperations.addFirst(() -> FilesHelper.copyDirectory(tmp, file));
        } else {
            File tmp = createTemporaryFile();
            FilesHelper.copyFile(file, tmp);

            Files.delete(file.toPath());

            cleanUpOperations.addFirst(() -> FilesHelper.copyFile(tmp, file));
        }
    }

    private File getFirstExistingParent(
            File file
    ) {
        File parent = file.getParentFile();
        if (parent == null) {
            return file;
        }
        if (!parent.exists()) {
            return getFirstExistingParent(parent);
        }
        return parent;
    }

}
