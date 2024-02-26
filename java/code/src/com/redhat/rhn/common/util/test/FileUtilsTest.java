/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.common.util.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;


/**
 * @author mmccune
 */
public class FileUtilsTest  {

    @Test
    public void testReadWrite() {
        String path = "/tmp/" +
            System.currentTimeMillis() +
            TestUtils.randomString() + ".cfg";
        // the BufferedWriter will automaticall append a
        // newline to the end of the file so our test should
        // just include one.
        String contents = "this is a test\n";
        FileUtils.writeStringToFile(contents, path);
        String reread = FileUtils.readStringFromFile(path);
        assertEquals(contents, reread);
    }

    @Test
    public void testGetBytesFromFile() throws Exception {

        File testFile = new File(TestUtils.findTestData("test.file").getFile());
        byte[] out = FileUtils.readByteArrayFromFile(testFile, 25, 30);
        String received = new String(out);
        String expect = "DEFGH";
        assertEquals(expect, received);
    }

    @Test
    public void testGetTailOfFile() throws Exception {
        String tail = FileUtils.getTailOfFile(
                TestUtils.findTestData("test.file").getPath(), 1);
        assertEquals("UVXYZ!@#$%\n", tail);
        tail = FileUtils.getTailOfFile(
                TestUtils.findTestData("test.file").getPath(), 2);
        assertEquals("KLMNOPQRST\nUVXYZ!@#$%\n", tail);
        tail = FileUtils.getTailOfFile(
                TestUtils.findTestData("test.file").getPath(), 3);
        assertEquals("ABCDEFGHIJ\nKLMNOPQRST\nUVXYZ!@#$%\n", tail);
    }

    @Test
    public void testValidateCanonicalPath() {
        Path verifiedPath = Path.of("/", "tmp", "mydir", "testfile");

        // Valid paths
        assertEquals(verifiedPath, FileUtils.validateCanonicalPath("/tmp", "mydir/testfile"));
        assertEquals(verifiedPath, FileUtils.validateCanonicalPath("/tmp/mydir", "testfile"));
        assertEquals(verifiedPath, FileUtils.validateCanonicalPath("/tmp/mydir", "./testfile"));
        assertEquals(verifiedPath, FileUtils.validateCanonicalPath("/tmp", "anotherdir/../mydir/testfile"));

        // Invalid paths
        IllegalArgumentException ex;

        ex = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.validateCanonicalPath("./myfolder", "anotherfile"));
        assertEquals("targetDirectory must be an absolute path", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.validateCanonicalPath("/tmp/mydir", "../subdir/file"));
        assertEquals("Entry is outside of the target directory", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.validateCanonicalPath("/tmp", "../etc/rhn/rhn.conf"));
        assertEquals("Entry is outside of the target directory", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class,
            () -> FileUtils.validateCanonicalPath("/tmp", "/etc/rhn/rhn.conf"));
        assertEquals("Entry is outside of the target directory", ex.getMessage());
    }
}
