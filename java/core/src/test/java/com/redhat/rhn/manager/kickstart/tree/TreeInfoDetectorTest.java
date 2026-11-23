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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.manager.kickstart.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.manager.kickstart.tree.TreeInfoDetector.DetectedDistro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class TreeInfoDetectorTest {

    @TempDir
    private Path tempDir;

    @Test
    public void testDetectRhel() throws IOException {
        writeTreeInfo("""
                [general]
                arch = x86_64
                # comment
                [release]
                name = Red Hat Enterprise Linux
                version = 9
                """);

        assertEquals(Optional.of(new DetectedDistro("RHEL9", "rhel_9", "rhel9-pool-x86_64")),
                TreeInfoDetector.detect(tempDir));
    }

    @Test
    public void testDetectSlesIgnoresCase() throws IOException {
        writeTreeInfo("""
                [General]
                Arch=aarch64
                [Release]
                Name=SUSE Linux Enterprise
                Version=15 SP6
                """);

        assertEquals(Optional.of(new DetectedDistro("SLES15SP6", "sles15generic",
                        "sle-product-sles15-sp6-pool-aarch64")),
                TreeInfoDetector.detect(tempDir));
    }

    @Test
    public void testDetectUnknownProduct() throws IOException {
        writeTreeInfo("""
                [general]
                arch = s390x
                [release]
                name = Red Hat Enterprise Linux
                version = 9
                """);

        assertTrue(TreeInfoDetector.detect(tempDir).isEmpty());
    }

    @Test
    public void testDetectMalformedTreeInfo() throws IOException {
        writeTreeInfo("not an ini file\n[release\nname\n");

        assertTrue(TreeInfoDetector.detect(tempDir).isEmpty());
    }

    @Test
    public void testDetectWithoutTreeInfo() {
        assertTrue(TreeInfoDetector.detect(tempDir).isEmpty());
    }

    private void writeTreeInfo(String content) throws IOException {
        Files.writeString(tempDir.resolve(".treeinfo"), content);
    }
}
