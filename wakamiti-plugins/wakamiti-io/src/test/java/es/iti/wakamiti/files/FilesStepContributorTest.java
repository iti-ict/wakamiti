/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.files;

import es.iti.wakamiti.api.plan.DataTable;
import es.iti.wakamiti.api.plan.Document;
import org.apache.commons.io.FileExistsException;
import org.apache.commons.io.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FilesStepContributorTest {

    private static Logger log = LoggerFactory.getLogger( "es.iti.wakamiti.test");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private FilesStepContributor contributor = new FilesStepContributor();

    @Before
    public void startup() {
        contributor.setEnableCleanupUponCompletion(true);
    }

    @After
    public void teardown() throws IOException {
        contributor.cleanUp();
    }

    @Test
    public void copyToDirWhenUsingLinkWithSuccess() throws IOException {
        // prepare
        File link = Path.of("testlink").toFile();

        Map<Path, Path> links = Collections.singletonMap(link.toPath(), temporaryFolder.getRoot().toPath());
        contributor.setLinks(links);
        contributor.setUp();

        String file = "test.dat";
        File srcDir = temporaryFolder.newFolder("src");
        new File(srcDir, file).createNewFile();
        File destDir = temporaryFolder.newFolder("dest");

        // act
        contributor.copyToDir(srcDir, new File(link, "dest"));
        log.debug("Destination folder {}: {}", destDir, destDir.list());
        log.debug("Source folder {}: {}", srcDir, srcDir.list());

        // check
        assertTrue(new File(destDir, file).exists());
        assertTrue(srcDir.exists());
    }

    /**
     * The method must copy the files that are in the source folder to the destination folder and delete the source
     * directory.
     *
     * @throws IOException
     */
    @Test
    public void moveToDirWhenSrcIsAnExistentDirectoryAndDestIsAnExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File srcDir = temporaryFolder.newFolder("src");
        new File(srcDir, file).createNewFile();
        File destDir = temporaryFolder.newFolder("dest");

        // act
        contributor.moveToDir(srcDir, destDir);
        log.debug("Destination folder {}: {}", destDir, destDir.list());
        log.debug("Source folder {}: {}", srcDir, srcDir.list());

        // check
        assertTrue(new File(destDir, file).exists());
        assertFalse(srcDir.exists());
    }

    /**
     * The method must create a destination folder, copy there the files in the source folder, and delete the source
     * directory.
     *
     * @throws IOException
     */
    @Test
    public void moveToDirWhenSrcIsAnExistentDirectoryAndDestIsANotExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = temporaryFolder.newFolder("src");
        new File(src, file).createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest");

        // act
        contributor.moveToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.list());

        // check
        assertTrue(new File(dest, file).exists());
        assertFalse(src.exists());
    }

    /**
     * The method must remove the source folder.
     *
     * @throws IOException
     */
    @Test
    public void moveToDirWhenSrcIsAnEmptyDirectoryWithSuccess() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");
        File dest = temporaryFolder.newFolder("dest");

        // act
        contributor.moveToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.list());

        // check
        assertTrue(dest.exists());
        assertFalse(src.exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void moveToDirWhenSrcIsANotExistentFileWithError() throws IOException {
        // prepare
        File src = new File(temporaryFolder.getRoot(), "src");
        File dest = temporaryFolder.newFolder("dest");

        // act
        try {
            contributor.moveToDir(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * The method must copy the source file in the destination folder, and then delete it.
     *
     * @throws IOException
     */
    @Test
    public void moveToDirWhenSrcIsAnExistentFileAndDestIsExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = temporaryFolder.newFile(file);
        File dest = temporaryFolder.newFolder("dest");

        // act
        contributor.moveToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(new File(dest, file).exists());
        assertFalse(src.exists());
    }

    /**
     * The method must create a destination folder, copy the source file in there, and then remove it.
     *
     * @throws IOException
     */
    @Test
    public void moveToDirWhenSrcIsAFileAndDestIsNotExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = new File(temporaryFolder.newFolder("src"), file);
        src.createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest");

        // act
        contributor.moveToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(new File(dest, file).exists());
        assertFalse(src.exists());
    }

    /**
     * The method must copy the source file in the destination directory, rename it, and then remove the source file.
     *
     * @throws IOException
     */
    @Test
    public void moveToFileWhenSrcIsAnExistentFileAndDestinationFolderExistsWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        contributor.moveToFile(src, dest);
        log.debug("Destination file {}: {}", dest, dest.exists() ? "exists" : "not exists");
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(dest.exists());
        assertFalse(src.exists());
    }

    /**
     * The method must create a destination folder, copy the source file there, rename it, and then remove the source
     * file.
     *
     * @throws IOException
     */
    @Test
    public void moveToFileWhenSrcIsAnExistentFileAndDestinationFolderNotExistsWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest/renamed.dat");

        // act
        contributor.moveToFile(src, dest);
        log.debug("Destination file {}: {}", dest, dest.exists() ? "exists" : "not exists");
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(dest.exists());
        assertFalse(src.exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void moveToFileWhenSrcIsANotExistentFileWithError() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        try {
            contributor.moveToFile(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test(expected = FileExistsException.class)
    public void moveToFileWhenDestExistsWithError() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");
        dest.createNewFile();

        // act
        try {
            contributor.moveToFile(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void moveToFileWhenSrcIsADirectoryWithError() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        try {
            contributor.moveToFile(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * The method must copy the files that are in the source folder to the destination folder.
     *
     * @throws IOException
     */
    @Test
    public void copyToDirWhenSrcIsAnExistentDirectoryAndDestIsAnExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File srcDir = temporaryFolder.newFolder("src");
        new File(srcDir, file).createNewFile();
        File destDir = temporaryFolder.newFolder("dest");

        // act
        contributor.copyToDir(srcDir, destDir);
        log.debug("Destination folder {}: {}", destDir, destDir.list());
        log.debug("Source folder {}: {}", srcDir, srcDir.list());

        // check
        assertTrue(new File(destDir, file).exists());
        assertTrue(srcDir.exists());
    }

    /**
     * The method must create a destination folder and copy there the files in the source folder.
     *
     * @throws IllegalArgumentException
     */
    @Test
    public void copyToDirWhenSrcIsAnExistentDirectoryAndDestIsANotExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = temporaryFolder.newFolder("src");
        new File(src, file).createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest");

        // act
        contributor.copyToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.list());

        // check
        assertTrue(new File(dest, file).exists());
        assertTrue(src.exists());
    }

    /**
     * The method must do nothing.
     *
     * @throws IOException
     */
    @Test
    public void copyToDirWhenSrcIsAnEmptyDirectoryWithSuccess() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");
        File dest = temporaryFolder.newFolder("dest");

        // act
        contributor.copyToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.list());

        // check
        assertTrue(dest.exists());
        assertTrue(src.exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void copyToDirWhenSrcIsANotExistentFileWithError() throws IOException {
        // prepare
        File src = new File(temporaryFolder.getRoot(), "src");
        File dest = temporaryFolder.newFolder("dest");

        // act
        try {
            contributor.copyToDir(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    /**
     * The method must copy the source file in the destination folder.
     *
     * @throws IOException
     */
    @Test
    public void copyToDirWhenSrcIsAnExistentFileAndDestIsExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = temporaryFolder.newFile(file);
        File dest = temporaryFolder.newFolder("dest");

        // act
        contributor.copyToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source folder {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(new File(dest, file).exists());
        assertTrue(src.exists());
    }

    /**
     * The method must create a destination folder and copy the source file in there.
     *
     * @throws IOException
     */
    @Test
    public void copyToDirWhenSrcIsAFileAndDestIsNotExistentDirectoryWithSuccess() throws IOException {
        // prepare
        String file = "test.dat";
        File src = new File(temporaryFolder.newFolder("src"), file);
        src.createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest");

        // act
        contributor.copyToDir(src, dest);
        log.debug("Destination folder {}: {}", dest, dest.list());
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(new File(dest, file).exists());
        assertTrue(src.exists());
    }

    /**
     * The method must copy the source file in the destination directory and rename it.
     *
     * @throws IOException
     */
    @Test
    public void copyToFileWhenSrcIsAnExistentFileAndDestinationFolderExistsWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        contributor.copyToFile(src, dest);
        log.debug("Destination file {}: {}", dest, dest.exists() ? "exists" : "not exists");
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(dest.exists());
        assertTrue(src.exists());
    }

    /**
     * The method must create a destination folder, copy the source file there and rename it.
     *
     * @throws IOException
     */
    @Test
    public void copyToFileWhenSrcIsAnExistentFileAndDestinationFolderNotExistsWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();
        File dest = new File(temporaryFolder.getRoot(), "dest/renamed.dat");

        // act
        contributor.copyToFile(src, dest);
        log.debug("Destination file {}: {}", dest, dest.exists() ? "exists" : "not exists");
        log.debug("Source file {}: {}", src, src.exists() ? "exists" : "not exists");

        // check
        assertTrue(dest.exists());
        assertTrue(src.exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void copyToFileWhenSrcIsANotExistentFileWithError() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        try {
            contributor.copyToFile(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void copyToFileWhenSrcIsADirectoryWithError() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");
        File dest = new File(temporaryFolder.newFolder("dest"), "renamed.dat");

        // act
        try {
            contributor.copyToFile(src, dest);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test
    public void deleteWhenSrcIsAFileWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        src.createNewFile();

        // act
        contributor.delete(src);

        // check
        assertFalse(src.exists());
    }

    @Test
    public void deleteWhenSrcIsADirectoryWithSuccess() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");

        // act
        contributor.delete(src);

        // check
        assertFalse(src.exists());
    }

    @Test(expected = FileNotFoundException.class)
    public void deleteWhenSrcNotExistsWithSuccess() throws IOException {
        // prepare
        File src = new File(temporaryFolder.getRoot(), "src");

        // act
        try {
            contributor.delete(src);
        } catch (Exception e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    @Test
    public void checkExistsWhenFileExistsWithSuccess() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");

        // act
        contributor.checkExists(src);
    }

    @Test(expected = ComparisonFailure.class)
    public void checkExistsWhenFileNotExistsWithSuccess() {
        // prepare
        File src = new File(temporaryFolder.getRoot(), "src");

        // act
        contributor.checkExists(src);
    }

    @Test
    public void checkNotExistsWhenFileNotExistsWithSuccess() {
        // prepare
        File src = new File(temporaryFolder.getRoot(), "src");

        // act
        contributor.checkNotExists(src);
    }

    @Test(expected = ComparisonFailure.class)
    public void checkNotExistsWhenFileExistsWithSuccess() throws IOException {
        // prepare
        File src = temporaryFolder.newFolder("src");

        // act
        contributor.checkNotExists(src);
    }

    @Test
    public void checkContainsTextWhenFileContainsTextWithSuccess() throws IOException {
        // prepare
        String content = "TEST ";
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        FileUtils.write(src, content, "UTF-8");

        // act
        contributor.checkContainsText(src, new Document(" " + content));
    }

    @Test(expected = ComparisonFailure.class)
    public void checkContainsTextWhenFileNotContainsTextWithSuccess() throws IOException {
        // prepare
        String content = "TEST ";
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        FileUtils.write(src, content, "UTF-8");

        // act
        contributor.checkContainsText(src, new Document("AAAA"));
    }

    @Test
    public void checkContainsTableWhenFileContainsDataWithSuccess() throws IOException {
        // prepare
        String content = "TEST ";
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        FileUtils.write(src, content, "UTF-8");

        String[][] table = new String[][]{
                {"from", "to", "value"},
                {"3", "4", "T"},
                {"0", "3", "TES"}
        };

        // act
        contributor.checkContainsTable(src, new DataTable(table));
    }

    @Test(expected = ComparisonFailure.class)
    public void checkContainsTableWhenFileNotContainsDataWithSuccess() throws IOException {
        // prepare
        String content = "TEST ";
        File src = new File(temporaryFolder.newFolder("src"), "test.dat");
        FileUtils.write(src, content, "UTF-8");

        String[][] table = new String[][]{
                {"from", "to", "value"},
                {"0", "3", "TEST"}
        };

        // act
        try {
            contributor.checkContainsTable(src, new DataTable(table));
        } catch (ComparisonFailure e) {
            log.debug(e.getMessage());
            throw e;
        }
    }
}