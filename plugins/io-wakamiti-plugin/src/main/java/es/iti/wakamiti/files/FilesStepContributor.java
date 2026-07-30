/*
 * Copyright (c) 2022-2026 Instituto Tecnológico de Informática (ITI)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.files;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.time.temporal.ValueRange;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.iti.commons.jext.Extension;
import es.iti.wakamiti.api.annotations.I18nResource;
import es.iti.wakamiti.api.annotations.SetUp;
import es.iti.wakamiti.api.annotations.Step;
import es.iti.wakamiti.api.annotations.TearDown;
import es.iti.wakamiti.api.extensions.StepContributor;
import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import es.iti.wakamiti.api.util.WakamitiLogger;


/**
 * Step contributor for filesystem operations and file-content assertions.
 * <p>
 * Operations are delegated to {@link FilesHelper}, which can record reversible
 * actions for optional teardown restoration.
 * </p>
 */
@Extension(
        provider = "es.iti.wakamiti",
        name = "io-steps",
        version = "2.13"
)
@I18nResource("iti_wakamiti_wakamiti-files")
public class FilesStepContributor implements StepContributor {

    private static final Logger LOGGER = WakamitiLogger
            .of(LoggerFactory.getLogger("es.iti.wakamiti.files"));
    private static final String DIRECTORY = "directory";

    private FilesHelper helper = new FilesHelper();

    private Long timeout;
    private boolean enableCleanupUponCompletion;
    private Map<Path, Path> links = new LinkedHashMap<>();

    /**
     * Creates every configured symbolic link before scenario steps run.
     * Created links are registered for optional teardown cleanup.
     */
    @SetUp
    public void setUp() {
        links.forEach(helper::createSymLink);
    }

    /**
     * Restores filesystem state when cleanup-on-completion is enabled.
     * When disabled, all moves, copies, deletions and links remain in place.
     */
    @TearDown
    public void cleanUp() {
        if (!enableCleanupUponCompletion) {
            return;
        }
        LOGGER.debug("Performing clean-up files operations...");
        helper.cleanup();
        LOGGER.debug("Clean-up files finished");
    }

    /**
     * Sets the maximum wait for filesystem watch events.
     *
     * @param value timeout in seconds
     */
    @Step(value = "io.define.timeout", args = {"value:long"})
    public void setTimeout(
            Long value
    ) {
        LOGGER.debug("Setting timeout [{}]", value);
        this.timeout = value;
    }

    /**
     * Selects whether reversible file operations are undone at teardown.
     *
     * @param flag {@code true} to restore sources and remove created targets
     */
    public void setEnableCleanupUponCompletion(
            boolean flag
    ) {
        this.enableCleanupUponCompletion = flag;
    }

    /**
     * Adds symbolic links created during setup.
     * <p>
     * Entries are copied into the contributor internal map. Existing mappings
     * are retained and duplicate link paths are replaced by newer values.
     * </p>
     *
     * @param links map from link path to target path
     */
    public void setLinks(
            Map<Path, Path> links
    ) {
        LOGGER.debug("Setting links {}", links);
        this.links.putAll(links);
    }

    /**
     * Moves a source file to an exact destination file path.
     *
     * @param src existing source file
     * @param dest destination file path
     * @throws IOException if the source is absent or the move cannot complete
     */
    @Step(value = "io.action.move.file", args = {"src:file", "dest:file"})
    public void moveToFile(
            File src,
            File dest
    ) throws IOException {
        LOGGER.debug(
                "Moving {} [{}] to file [{}]",
                src.isFile() ? "file" : DIRECTORY,
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.moveToFile(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    /**
     * Moves a file or directory into a destination directory.
     *
     * @param src existing source file or directory
     * @param dest destination directory, created when necessary
     * @throws IOException if the source is absent or the move cannot complete
     */
    @Step(value = "io.action.move.dir", args = {"src:file", "dest:file"})
    public void moveToDir(
            File src,
            File dest
    ) throws IOException {
        LOGGER.debug(
                "Moving {} [{}] to directory [{}]",
                src.isFile() ? "file" : DIRECTORY,
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.moveToDir(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    /**
     * Copies a source file to an exact destination file path.
     *
     * @param src source file
     * @param dest destination file path
     * @throws IOException if copying fails
     */
    @Step(value = "io.action.copy.file", args = {"src:file", "dest:file"})
    public void copyToFile(
            File src,
            File dest
    ) throws IOException {
        LOGGER.debug(
                "Copying {} [{}] to file [{}]",
                src.isFile() ? "file" : DIRECTORY,
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.copyToFile(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    /**
     * Copies a file or directory into a destination directory.
     *
     * @param src existing source file or directory
     * @param dest destination directory
     * @throws IOException if the source is absent or copying fails
     */
    @Step(value = "io.action.copy.dir", args = {"src:file", "dest:file"})
    public void copyToDir(
            File src,
            File dest
    ) throws IOException {
        LOGGER.debug(
                "Copying {} [{}] to directory [{}]",
                src.isFile() ? "file" : DIRECTORY,
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.copyToDir(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    /**
     * Deletes a file or directory, retaining a temporary backup when cleanup is
     * later requested.
     *
     * @param file existing path to delete
     * @throws IOException if the path is absent or deletion fails
     */
    @Step(value = "io.action.delete", args = {"file"})
    public void delete(
            File file
    ) throws IOException {
        LOGGER.debug(
                "Deleting {} [{}]",
                file.isFile() ? "file" : DIRECTORY,
                file.getAbsolutePath()
        );

        try {
            helper.delete(file);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    /**
     * Waits for a deletion event for an exact child name.
     *
     * @param file path whose parent directory is watched
     * @throws IOException if the directory cannot be watched
     * @throws InterruptedException if waiting is interrupted
     * @throws TimeoutException if no deletion arrives within the configured
     *                          number of seconds
     */
    @Step(value = "io.action.wait.file.deletion", args = {"file"})
    public void waitForFileDeletion(
            File file
    ) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] deletion",
                file.isFile() ? "file" : DIRECTORY,
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_DELETE, timeout);
    }

    /**
     * Waits for a creation event for an exact child name.
     *
     * @param file path whose parent directory is watched
     * @throws IOException if the directory cannot be watched
     * @throws InterruptedException if waiting is interrupted
     * @throws TimeoutException if no creation arrives within the configured
     *                          number of seconds
     */
    @Step(value = "io.action.wait.file.creation", args = {"file"})
    public void waitForFileCreation(
            File file
    ) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] creation",
                file.isFile() ? "file" : DIRECTORY,
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_CREATE, timeout);
    }

    /**
     * Waits for a modification event for an exact child name.
     *
     * @param file path whose parent directory is watched
     * @throws IOException if the directory cannot be watched
     * @throws InterruptedException if waiting is interrupted
     * @throws TimeoutException if no modification arrives within the configured
     *                          number of seconds
     */
    @Step(value = "io.action.wait.file.modification", args = {"file"})
    public void waitForFileModification(
            File file
    ) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] modification",
                file.isFile() ? "file" : DIRECTORY,
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_MODIFY, timeout);
    }

    /**
     * Requires a filesystem path to exist.
     *
     * @param file path to inspect
     * @throws AssertionError when the path does not exist
     */
    @Step(value = "io.assert.file.exists", args = {"file"})
    public void checkExists(
            File file
    ) {
        Assertions.assertThat(file.exists()).as("The file must exist").isTrue();
    }

    /**
     * Requires a filesystem path not to exist.
     *
     * @param file path to inspect
     * @throws AssertionError when the path exists
     */
    @Step(value = "io.assert.file.not.exists", args = {"file"})
    public void checkNotExists(
            File file
    ) {
        Assertions.assertThat(file.exists()).as("The file mustn't exist").isFalse();
    }

    /**
     * Compares a UTF-8 file with document content after trimming both ends.
     *
     * @param file text file to read
     * @param document expected text
     * @throws IOException if the file cannot be read
     * @throws AssertionError when the trimmed texts differ
     */
    @Step(value = "io.assert.file.contains.document", args = {"file"})
    public void checkContainsText(
            File file,
            Document document
    ) throws IOException {
        Assertions.assertThat(FileUtils.readFileToString(file, StandardCharsets.UTF_8).trim())
                .isEqualTo(document.getContent().trim());
    }

    /**
     * Validates selected half-open character ranges in a UTF-8 file.
     * <p>
     * Each table row supplies {@code from}, {@code to} and {@code value}; only
     * the expected value is trimmed before comparison.
     *
     * @param file text file to inspect
     * @param table positional expectations
     * @throws IOException if the file cannot be read
     * @throws AssertionError when any selected substring differs
     */
    @Step(value = "io.assert.file.contains.table", args = {"file"})
    public void checkContainsTable(
            File file,
            DataTable table
    ) throws IOException {
        DataTableHelper helper = new DataTableHelper(table);
        String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8).trim();
        for (int row = 0; row < helper.count(); row++) {
            ValueRange range = helper.getRange(row);
            Assertions.assertThat(content.substring((int) range.getMinimum(), (int) range.getMaximum()))
                    .as("The value of row %s is not as expected", row + 1)
                    .isEqualTo(helper.getExpectedValue(row).trim());
        }
    }

    /**
     * Requires the filesystem-reported byte length to equal a value.
     * Despite the localized argument name, this uses {@link File#length()} and
     * therefore measures bytes rather than Unicode characters.
     *
     * @param file file to measure
     * @param chars expected byte count
     * @throws AssertionError when the byte length differs
     */
    @Step(value = "io.assert.file.length", args = {"file", "chars:int"})
    public void checkFileLength(
            File file,
            Integer chars
    ) {
        Assertions.assertThat(file.length()).isEqualTo(chars);
    }

}
