/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.repomd.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.taskomatic.task.repomd.DebRepositoryWriter;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

public class DebRepositoryWriterTest extends JMockBaseTestCaseWithUser {

    private Path tmpDir;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tmpDir = Files.createTempDirectory("debPkgWriterTest");
    }

    @Test
    public void testWriteRepoMetadata() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(user);

        PackageExtraTagsKeys multiArchTag = PackageManagerTest.createExtraTagKey("Multi-Arch");

        Package pkg1 = PackageManagerTest.addPackageToChannel("pkg_1", channel);
        pkg1.getExtraTags().put(multiArchTag, "same");
        Package pkg2 = PackageManagerTest.addPackageToChannel("pkg_2", channel);
        Package pkg3 = PackageManagerTest.addPackageToChannel("pkg_3", channel);

        PackageManager.createRepoEntrys(channel.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        DebRepositoryWriter writer = new DebRepositoryWriter("rhn/repodata", tmpDir.normalize().toString());
        writer.writeRepomdFiles(channel);
        Path channelRepodataDir = tmpDir.resolve("rhn/repodata/" + channel.getLabel());
        List<String> fileNames = Files.list(channelRepodataDir)
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        assertTrue(fileNames.contains("Release"));
        assertTrue(fileNames.contains("Packages"));
        assertTrue(fileNames.contains("Packages.gz"));

        String packagesContent = FileUtils.readFileToString(
                tmpDir.resolve("rhn/repodata/" + channel.getLabel() + "/Packages").toFile());
        packagesContent = DebPackageWriterTest.cleanupContent(packagesContent);

        try (FileInputStream fin = new FileInputStream(
                tmpDir.resolve("rhn/repodata/" + channel.getLabel() + "/Packages.gz").toFile());
             InputStream gzipStream = new GZIPInputStream(fin)) {

            String packagesGzContent = TestUtils.readAll(gzipStream);
            packagesGzContent = DebPackageWriterTest.cleanupContent(packagesGzContent);

            assertEquals(packagesContent, packagesGzContent);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }

//        String releaseContent = FileUtils.readFileToString(channelRepodataDir.resolve("Release").toFile());
//        System.out.println(releaseContent);
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

}
