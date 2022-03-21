/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.kukumo.files;

import iti.commons.jext.Extension;
import iti.kukumo.api.annotations.I18nResource;
import iti.kukumo.api.annotations.SetUp;
import iti.kukumo.api.annotations.Step;
import iti.kukumo.api.annotations.TearDown;
import iti.kukumo.api.extensions.StepContributor;
import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.util.KukumoLogger;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.time.temporal.ValueRange;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Extension(provider = "iti.kukumo", name = "files-steps", version = "1.1")
@I18nResource("iti_kukumo_kukumo-files")
public class FilesStepContributor implements StepContributor {

    private static final Logger LOGGER = KukumoLogger
            .of(LoggerFactory.getLogger("iti.kukumo.files"));

    private FilesHelper helper = new FilesHelper();

    private Long timeout;
    private boolean enableCleanupUponCompletion;
    private Map<Path, Path> links = new LinkedHashMap<>();

    @SetUp
    public void setUp() {
        links.forEach(helper::createSymLink);
    }

    @TearDown
    public void cleanUp() throws IOException {
        if (!enableCleanupUponCompletion) {
            return;
        }
        LOGGER.debug("Performing clean-up files operations...");
        helper.cleanup();
        LOGGER.debug("Clean-up files finished");
    }

    @Step(value = "io.define.timeout", args = {"value:long"})
    public void setTimeout(Long value) {
        LOGGER.debug("Setting timeout [{}]", value);
        this.timeout = value;
    }

    public void setEnableCleanupUponCompletion(boolean flag) {
        this.enableCleanupUponCompletion = flag;
    }

    public void setLinks(Map<Path, Path> links) {
        LOGGER.debug("Setting links {}", links);
        this.links.putAll(links);
    }

    @Step(value = "io.action.move.file", args = {"src:file", "dest:file"})
    public void moveToFile(File src, File dest) throws IOException {
        LOGGER.debug(
                "Moving {} [{}] to file [{}]",
                src.isFile() ? "file" : "directory",
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.moveToFile(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    @Step(value = "io.action.move.dir", args = {"src:file", "dest:file"})
    public void moveToDir(File src, File dest) throws IOException {
        LOGGER.debug(
                "Moving {} [{}] to directory [{}]",
                src.isFile() ? "file" : "directory",
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.moveToDir(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    @Step(value = "io.action.copy.file", args = {"src:file", "dest:file"})
    public void copyToFile(File src, File dest) throws IOException {
        LOGGER.debug(
                "Copying {} [{}] to file [{}]",
                src.isFile() ? "file" : "directory",
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.copyToFile(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    @Step(value = "io.action.copy.dir", args = {"src:file", "dest:file"})
    public void copyToDir(File src, File dest) throws IOException {
        LOGGER.debug(
                "Copying {} [{}] to directory [{}]",
                src.isFile() ? "file" : "directory",
                src.getAbsolutePath(),
                dest.getAbsolutePath()
        );

        try {
            helper.copyToDir(src, dest);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    @Step(value = "io.action.delete", args = {"file"})
    public void delete(File file) throws IOException {
        LOGGER.debug(
                "Deleting {} [{}]",
                file.isFile() ? "file" : "directory",
                file.getAbsolutePath()
        );

        try {
            helper.delete(file);
        } catch (FilesHelperException e) {
            throw (IOException) e.getCause();
        }
    }

    @Step(value = "io.action.wait.file.deletion", args = {"file"})
    public void waitForFileDeletion(File file) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] deletion",
                file.isFile() ? "file" : "directory",
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_DELETE, timeout);
    }

    @Step(value = "io.action.wait.file.creation", args = {"file"})
    public void waitForFileCreation(File file) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] creation",
                file.isFile() ? "file" : "directory",
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_CREATE, timeout);
    }

    @Step(value = "io.action.wait.file.modification", args = {"file"})
    public void waitForFileModification(File file) throws IOException, InterruptedException, TimeoutException {
        LOGGER.debug(
                "Waiting for {} [{}] modification",
                file.isFile() ? "file" : "directory",
                file.getAbsolutePath()
        );

        helper.waitForFile(file, StandardWatchEventKinds.ENTRY_MODIFY, timeout);
    }

    @Step(value = "io.assert.file.exists", args = {"file"})
    public void checkExists(File file) {
        Assertions.assertThat(file.exists()).as("The file must exist").isTrue();
    }

    @Step(value = "io.assert.file.not.exists", args = {"file"})
    public void checkNotExists(File file) {
        Assertions.assertThat(file.exists()).as("The file mustn't exist").isFalse();
    }

    @Step(value = "io.assert.file.contains.document", args = {"file"})
    public void checkContainsText(File file, Document document) throws IOException {
        Assertions.assertThat(FileUtils.readFileToString(file, "UTF-8").trim())
                .isEqualTo(document.getContent().trim());
    }

    @Step(value = "io.assert.file.contains.table", args = {"file"})
    public void checkContainsTable(File file, DataTable table) throws IOException {
        DataTableHelper helper = new DataTableHelper(table);
        String content = FileUtils.readFileToString(file, "UTF-8").trim();
        for (int row = 0; row < helper.count(); row++) {
            ValueRange range = helper.getRange(row);
            Assertions.assertThat(content.substring((int) range.getMinimum(), (int) range.getMaximum()))
                    .as("The value of row %s is not as expected", row + 1)
                    .isEqualTo(helper.getExpectedValue(row).trim());
        }
    }

    @Step(value = "io.assert.file.length", args = {"file", "chars:int"})
    public void checkFileLength(File file, Integer chars) {
        Assertions.assertThat(file.length()).isEqualTo(chars);
    }

}