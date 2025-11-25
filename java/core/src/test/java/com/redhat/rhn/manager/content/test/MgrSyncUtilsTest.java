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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.content.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.manager.content.ContentSyncException;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.content.MgrSyncUtils;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


public class MgrSyncUtilsTest extends BaseTestCaseWithUser {

    private static Path fromdir;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        fromdir = Files.createTempDirectory("sumatest");
        Config.get().setString(ContentSyncManager.RESOURCE_PATH, fromdir.toString());
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        Config.get().remove(ContentSyncManager.RESOURCE_PATH);
        FileUtils.deleteDirectory(fromdir.toFile());
        Config.get().setString(ConfigDefaults.SCC_UPDATE_HOST_DOMAIN, ".suse.com");
    }

    @Test
    public void testurlToFSPathSLE() throws Exception {

        String url = "https://updates.suse.com/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update/";
        String name = "SLE-Module-Basesystem15-SP3-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());

        Config.get().setString(ConfigDefaults.SCC_UPDATE_HOST_DOMAIN, ".ranchergovernment.com");
        url = "https://updates.scc-proxy.rgscc-dev.ranchergovernment.com/SUSE/Updates/SLE-Module-Basesystem/" +
                "15-SP3/x86_64/update/";
        opath = MgrSyncUtils.urlToFSPath(url, name);
        expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());

    }

    @Test
    public void testurlToFSPathSLEWithToken() throws Exception {

        String url = "https://updates.suse.com/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update/?123456789abcde";
        String name = "SLE-Module-Basesystem15-SP3-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());

        Config.get().setString(ConfigDefaults.SCC_UPDATE_HOST_DOMAIN, ".ranchergovernment.com");
        url = "https://updates.scc-proxy.rgscc-dev.ranchergovernment.com/SUSE/Updates/SLE-Module-Basesystem/" +
                "15-SP3/x86_64/update/?123456789abcde";

        opath = MgrSyncUtils.urlToFSPath(url, name);
        expected = new URI(
                String.format("file://%s/SUSE/Updates/SLE-Module-Basesystem/15-SP3/x86_64/update", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    @Test
    public void testurlToFSPathUbuntu() throws Exception {

        String url = "http://archive.ubuntu.com/ubuntu/dists/focal/main/binary-amd64/";
        String name = "ubuntu-2004-amd64-main-amd64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(
                String.format("file://%s/archive.ubuntu.com/ubuntu/dists/focal/main/binary-amd64", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    @Test
    public void testurlToFSPathMirrorlist() throws Exception {

        String url = "http://mirrorlist.centos.org/?release=8&arch=x86_64&repo=AppStream&infra=stock";
        String name = "centos-7-appstream-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format(
                "file://%s/mirrorlist.centos.org/arch/x86_64/infra/stock/release/8/repo/AppStream", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    @Test
    public void testurlToFSPathMirrorlistNormalize() throws Exception {

        String url = "http://mirrorlist.centos.org/../?release=8&arch=x86_64&repo=AppStream&infra=stock&..=..&x=%2E%2E";
        String name = "centos-7-appstream-x86_64";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s", fromdir));
        assertContains(opath.toString(), expected.toString());
        assertFalse(opath.toString().contains("%"), "Decoding error: " + opath);
    }

    @Test
    public void testurlToFSPathLegacy() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    @Test
    public void testurlToFSPathLegacyNormalize() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "../../../etc/passwd SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/download.nvidia.com/suse/sle12sp4", fromdir));
        assertContains(opath.toString(), expected.toString());
    }

    @Test
    public void testurlToFSPathLegacyQuoteNormalize() throws Exception {
        Files.createDirectories(new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/").toPath());
        new File(fromdir + "/repo/RPMMD/SLE-12-GA-Desktop-NVIDIA-Driver/repodata/repomd.xml").createNewFile();
        String url = "https://download.nvidia.com/suse/sle12sp4";
        String name = "%2F%2E%2E%2Fetc/passwd SLE-12-GA-Desktop-NVIDIA-Driver";

        URI opath = MgrSyncUtils.urlToFSPath(url, name);
        URI expected = new URI(String.format("file://%s/download.nvidia.com/suse/sle12sp4", fromdir));
        assertContains(opath.toString(), expected.toString());
        assertFalse(opath.toString().contains("%"), "Decoding error: " + opath);
    }

    @Test
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
        assertFalse(opath.toString().contains("%"), "Decoding error: " + opath);
    }

    @Test
    public void testTokens() {
        Map<String, Boolean> tokenMap = Map.of("os=RHEL", false,
                "dlauth=exp=1990353599~acl=/repo/$RCE/SLE11-WebYaST-SP2-Updates/sle-11-i586/*~" +
                        "hmac=984b6b366f696884d0e3ac619af3a41b2d678eeec523135cc70921c541e5ec60", true,
                "NoaBh00JIaJwSozVS2BK1G6x27JmfPxfKiiMZBlZ4SD1x3S_VUt7805g_G4XB0ShvcKDO4A5uhzvo74HNzCEAYh" +
                        "MxG8dIw0ZIMla3FzxXCKR5gUaW6PeLjCHG4LrgoXa3zG7KPyy8OQMSAni9F1bs2fqOjKqgQ", true,
                "", false,
                "susetk=exp=1990353599~acl=/repo/$RCE/SLE11-WebYaST-SP2-Updates/sle-11-i586/*~" +
                        "hmac=984b6b366f696884d0e3ac619af3a41b2d678eeec523135cc70921c541e5ec60", true,
                "hmac=984b6b366f696884d0e3ac619af3a41b2d678eeec523135cc70921c541e5ec60", false,
                "exp=18976547", false);
        tokenMap.forEach((token, result) -> assertEquals(result, MgrSyncUtils.isAuthToken(token)));

        assertThrows(ContentSyncException.class, () -> MgrSyncUtils.isAuthToken("exp=1234556&hmac=adf284875ee"));
    }

    @Test
    public void testUrlToPath() throws IOException {
        Path path = new File("/tmp/mirror").toPath();
        URI fsPath = MgrSyncUtils.urlToFSPath("http://mirrors.fedoraproject.org/mirrorlist?repo=epel-7&arch=x86_64",
                "epel7", path);
        assertEquals("/tmp/mirror/mirrors.fedoraproject.org/mirrorlist/arch/x86_64/repo/epel-7", fsPath.getPath());
        fsPath = MgrSyncUtils.urlToFSPath("http://localhost/pub/repositories/empty/",
                "empty", path);
        assertEquals("http://localhost/pub/repositories/empty/", fsPath.toString());
        fsPath = MgrSyncUtils.urlToFSPath("http://external.domain.top/myrepo?387465284375628347568347565723567",
                "myrepo", path);
        assertEquals("/tmp/mirror/external.domain.top/myrepo", fsPath.getPath());
        fsPath = MgrSyncUtils.urlToFSPath("http://external.domain.top/myrepo2?exttok=exp=1990353599~acl=*~" +
                        "hmac=984b6b366f696884d0e3ac619af3a41b2d678eeec523135cc70921c541e5ec60",
                "myrepo", path);
        assertEquals("/tmp/mirror/external.domain.top/myrepo2", fsPath.getPath());
        fsPath = MgrSyncUtils.urlToFSPath("http://dl.suse.com/myrepo3?exttok=exp=1990353599~acl=*~" +
                        "hmac=984b6b366f696884d0e3ac619af3a41b2d678eeec523135cc70921c541e5ec60",
                "myrepo", path);
        assertEquals("/tmp/mirror/myrepo3", fsPath.getPath());
        try {
            File tempLocalRepo = Paths.get(path.toString(), "repo", "RPMMD", "driverrepo").toFile();
            tempLocalRepo.delete();
            tempLocalRepo.mkdirs();
            fsPath = MgrSyncUtils.urlToFSPath("http://external.domain.top/myrepo4",
                    "driverrepo", path);
            assertEquals(tempLocalRepo.getPath() + "/", fsPath.getPath());

            tempLocalRepo = Paths.get(path.toString(), "mydriverrepo").toFile();
            tempLocalRepo.delete();
            tempLocalRepo.mkdirs();
            fsPath = MgrSyncUtils.urlToFSPath("http://other.domain.top/mydriverrepo",
                    "otherdriverrepo", path);
            assertEquals(tempLocalRepo.getPath() + "/", fsPath.getPath());
        }
        finally {
            FileUtils.deleteDirectory(path.toFile());
        }

    }
}
