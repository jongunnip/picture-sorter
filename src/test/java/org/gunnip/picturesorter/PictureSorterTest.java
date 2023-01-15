package org.gunnip.picturesorter;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class PictureSorterTest {

    private PictureSorter sorter = new PictureSorter();

    @Rule
    public TemporaryFolder source = new TemporaryFolder();
    @Rule
    public TemporaryFolder dest = new TemporaryFolder();

    @Test
    public void testProcessImageDirDestEmpty() throws IOException {
        File file1 = source.newFile("image1.jpg");
        file1.setLastModified(0);

        File destDateDir = new File(dest.getRoot(), "1969_12_31");
        Set<String> expectedDestFiles = new HashSet<>();
        expectedDestFiles.add(new File(destDateDir, "jon_image1.jpg").getAbsolutePath());

        sorter.processImageDir(source.getRoot().toPath(), dest.getRoot().toPath(), "jon");

        Set<String> actualDestFiles = new HashSet<>();
        for (File file : destDateDir.listFiles()) {
            actualDestFiles.add(file.getAbsolutePath());
        }

        assertEquals(actualDestFiles, expectedDestFiles);
    }

    @Test
    public void testProcessImageDirDestContainsFileThatIsDifferent() throws IOException {
        File file1 = source.newFile("image1.jpg");
        FileUtils.write(file1, "foo", "UTF-8");
        file1.setLastModified(0);

        File destDateDir = new File(dest.getRoot(), "1969_12_31");
        assertTrue(destDateDir.mkdir());
        FileUtils.write(new File(destDateDir, "jon_image1.jpg"), "bar", "UTF-8");
        assertTrue(new File(destDateDir, "jon_image2.jpg").createNewFile());

        Set<String> expectedDestFiles = new HashSet<>();
        expectedDestFiles.add(new File(destDateDir, "jon_image1.jpg").getAbsolutePath());
        expectedDestFiles.add(new File(destDateDir, "jon_image2.jpg").getAbsolutePath());
        expectedDestFiles.add(new File(destDateDir, "jon_image1_1.jpg").getAbsolutePath());

        sorter.processImageDir(source.getRoot().toPath(), dest.getRoot().toPath(), "jon");

        Set<String> actualDestFiles = new HashSet<>();
        for (File file : destDateDir.listFiles()) {
            actualDestFiles.add(file.getAbsolutePath());
        }

        assertEquals(expectedDestFiles, actualDestFiles);
    }

    @Test
    public void testProcessImageDirDestContainsFileThatIsSame() throws IOException {
        File file1 = source.newFile("image1.jpg");
        FileUtils.write(file1, "foo", "UTF-8");
        file1.setLastModified(0);

        File destDateDir = new File(dest.getRoot(), "1969_12_31");
        assertTrue(destDateDir.mkdir());
        FileUtils.write(new File(destDateDir, "jon_image1.jpg"), "foo", "UTF-8");
        assertTrue(new File(destDateDir, "jon_image2.jpg").createNewFile());

        Set<String> expectedDestFiles = new HashSet<>();
        expectedDestFiles.add(new File(destDateDir, "jon_image1.jpg").getAbsolutePath());
        expectedDestFiles.add(new File(destDateDir, "jon_image2.jpg").getAbsolutePath());

        sorter.processImageDir(source.getRoot().toPath(), dest.getRoot().toPath(), "jon");

        Set<String> actualDestFiles = new HashSet<>();
        for (File file : destDateDir.listFiles()) {
            actualDestFiles.add(file.getAbsolutePath());
        }

        assertEquals(expectedDestFiles, actualDestFiles);
    }

    @Test
    public void testProcessImageDirDestContainsFileThatIsSameWithoutPrefix() throws IOException {
        File file1 = source.newFile("image1.jpg");
        FileUtils.write(file1, "foo", "UTF-8");
        file1.setLastModified(0);

        File destDateDir = new File(dest.getRoot(), "1969_12_31");
        assertTrue(destDateDir.mkdir());
        FileUtils.write(new File(destDateDir, "image1.jpg"), "foo", "UTF-8");
        assertTrue(new File(destDateDir, "image2.jpg").createNewFile());

        Set<String> expectedDestFiles = new HashSet<>();
        expectedDestFiles.add(new File(destDateDir, "image1.jpg").getAbsolutePath());
        expectedDestFiles.add(new File(destDateDir, "image2.jpg").getAbsolutePath());

        sorter.processImageDir(source.getRoot().toPath(), dest.getRoot().toPath(), "jon");

        Set<String> actualDestFiles = new HashSet<>();
        for (File file : destDateDir.listFiles()) {
            actualDestFiles.add(file.getAbsolutePath());
        }

        assertEquals(expectedDestFiles, actualDestFiles);
    }
}
