/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.manager.content.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;


public class MgrSyncUtilsTest extends BaseTestCaseWithUser {

    private static Path fromdir;
    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        fromdir = Files.createTempDirectory("sumatest");
        Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Config.get().remove(ContentSyncManager.RESOURCE_PATH);
        FileUtils.deleteDirectory(fromdir.toFile());
    }

    public void testurlToFSPathSLE() throws Exception {

        String url = "https://updates.suse.com/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update/";
        String name = "SLE-Module-Basesystem15-SP3-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathSLEWithToken() throws Exception {

        String url = "https://updates.suse.com/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update/?123456789abcde";
        String name = "SLE-Module-Basesystem15-SP3-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathUbuntu() throws Exception {

        String url = "http://archive.ubuntu.com/ubuntu/dists/focal/main/binary-amd64/";
        String name = "ubuntu-2004-amd64-main-amd64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/archive.ubuntu.com/ubuntu/dists/focal/main/binary-amd64", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathMirrorlist() throws Exception {

        String url = "http://mirrorlist.centos.org/?release=8&arch=x86_64&repo=AppStream&infra=stock";
        String name = "centos-7-appstream-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format(
                "file://%s/mirrorlist.centos.org/arch/x86_64/infra/stock/release/8/repo/AppStream", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathMirrorlistNormalize() throws Exception {

        String url = "http://mirrorlist.centos.org/../?release=8&arch=x86_64&repo=AppStream&infra=stock&..=..&x=%2E%2E";
        String name = "centos-7-appstream-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s", fromdir));
        assertContains(opath.toString(), expected.toString());
        assertFalse("Decoding error: " + opath.toString(), opath.toString().contains("%"));
    }

    public void testurlToFSPathLegacy() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathLegacyNormalize() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "../../../etc/passwd SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/download.nvidia.com/suse/sle12sp4", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    public void testurlToFSPathLegacyQuoteNormalize() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "%2F%2E%2E%2Fetc/passwd SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/download.nvidia.com/suse/sle12sp4", fromdir));
        assertContains(opath.toString(), expected.toString());
        assertFalse("Decoding error: " + opath.toString(), opath.toString().contains("%"));
    }

    public void testurlToFSPathLegacyQuoteNormalize2() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        // CHECKSTYLE:OFF
        String url = "https://download.nvidia.com/suse/sle12sp4%2F%2E%2E%2F%2E%2E%2F%2E%2E%2F%2E%2E%2F%2E%2E/etc/passwd";
        // CHECKSTYLE:ON
        String name = "SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s", fromdir));
        assertContains(opath.toString(), expected.toString());
        assertFalse("Decoding error: " + opath.toString(), opath.toString().contains("%"));
    }
}
