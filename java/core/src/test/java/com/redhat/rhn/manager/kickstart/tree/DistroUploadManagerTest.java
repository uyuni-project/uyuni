/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.manager.kickstart.tree;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DistroUploadManagerTest {

    @TempDir
    private Path tempDir;

    @Test
    public void testUploadDistroExtractsIsoTree() throws Exception {
        byte[] iso = "test distro ISO content".getBytes();
        DistroUploadManager manager = new DistroUploadManager(tempDir, (isoPath, destinationPath) -> {
            assertArrayEquals(iso, Files.readAllBytes(isoPath));
            Files.createDirectories(destinationPath.resolve("boot"));
            Files.write(destinationPath.resolve("boot/kernel"), Files.readAllBytes(isoPath));
        });

        Path destination = manager.uploadDistro("test.iso", new ByteArrayInputStream(iso));

        assertEquals(tempDir.resolve("test"), destination);
        assertTrue(Files.isDirectory(destination));
        assertArrayEquals(iso, Files.readAllBytes(destination.resolve("boot/kernel")));
    }

    @Test
    public void testUploadDistroRejectsPathSegments() {
        DistroUploadManager manager = new DistroUploadManager(tempDir, (isoPath, destinationPath) -> { });

        assertThrows(IOException.class,
                () -> manager.uploadDistro("../test.iso", new ByteArrayInputStream(new byte[] {1})));
    }

    @Test
    public void testUploadDistroDoesNotOverwriteExistingTree() throws Exception {
        DistroUploadManager manager = new DistroUploadManager(tempDir, (isoPath, destinationPath) ->
                Files.write(destinationPath.resolve("marker"), Files.readAllBytes(isoPath)));
        manager.uploadDistro("test.iso", new ByteArrayInputStream(new byte[] {1}));

        assertThrows(FileAlreadyExistsException.class,
                () -> manager.uploadDistro("test.iso", new ByteArrayInputStream(new byte[] {2})));
    }

    @Test
    public void testUploadDistroCleansTemporaryFilesWhenExtractionFails() {
        DistroUploadManager manager = new DistroUploadManager(tempDir, (isoPath, destinationPath) -> {
            Files.write(destinationPath.resolve("partial"), Files.readAllBytes(isoPath));
            throw new IOException("xorriso failed");
        });

        assertThrows(IOException.class,
                () -> manager.uploadDistro("test.iso", new ByteArrayInputStream(new byte[] {1})));
        try (var entries = Files.list(tempDir)) {
            assertEquals(0, entries.count());
        }
        catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
