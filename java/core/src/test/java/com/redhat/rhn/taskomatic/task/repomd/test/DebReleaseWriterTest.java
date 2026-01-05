/*
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.taskomatic.task.repomd.DebPackageWriter;
import com.redhat.rhn.taskomatic.task.repomd.DebReleaseWriter;
import com.redhat.rhn.taskomatic.task.repomd.DebRepositoryWriter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.time.ZonedDateTime;

public class DebReleaseWriterTest extends BaseTestCaseWithUser {

    private String prefix;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        prefix = Files.createTempDirectory("debreleasewriter").toAbsolutePath() + File.separator;
    }

    @Test
    public void testDateFormat() {
        ZonedDateTime time = ZonedDateTime.parse("2018-11-22T12:35:40+01:00[Europe/Madrid]");
        assertEquals("Thu, 22 Nov 2018 11:35:40 UTC", DebReleaseWriter.RFC822_DATE_FORMAT.format(time));
    }

    @Test
    public void testGenerateRelease() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setChannelArch(ChannelFactory.findArchByLabel("channel-ia64-deb"));

        PackageArch pa = PackageFactory.lookupPackageArchByLabel("amd64-deb");

        com.redhat.rhn.domain.rhnpackage.Package pkg1 = PackageManagerTest.addPackageToChannel("pkg1", channel);
        pkg1.setPackageArch(pa);

        DebPackageWriter pkgWriter = new DebPackageWriter(channel, prefix);
        pkgWriter.close();

        DebRepositoryWriter repoWriter = new DebRepositoryWriter("", prefix);
        repoWriter.setCommitTransaction(false);
        repoWriter.gzipCompress(pkgWriter.getFilenamePackages());

        DebReleaseWriter releaseWriter = new DebReleaseWriter(channel, prefix);
        releaseWriter.generateRelease();
        ZonedDateTime now = ZonedDateTime.now();
        String releaseDatetime = DebReleaseWriter.RFC822_DATE_FORMAT.format(now);

        String releaseContent = FileUtils.readStringFromFile(prefix + "Release");

        if (!releaseContent.contains(releaseDatetime)) {
            // We have a possible race condition of 1 second
            releaseDatetime = DebReleaseWriter.RFC822_DATE_FORMAT.format(now.minusSeconds(1));
        }
        String rel = "Archive: " + channel.getLabel() + "\n" +
                "Label: " + channel.getLabel() + "\n" +
                "Suite: " + channel.getLabel() + "\n" +
                "Architectures: i386 ia64\n" +
                "Date: " + releaseDatetime + "\n" +
                "Description: TestChannel description\n" +
                "MD5Sum:\n" +
                " d41d8cd98f00b204e9800998ecf8427e 0 Packages\n" +
                "SHA1:\n" +
                " da39a3ee5e6b4b0d3255bfef95601890afd80709 0 Packages\n" +
                "SHA256:\n" +
                " e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 0 Packages\n";
        // Remove the compressed file checksums as those may vary
        assertEquals(rel, releaseContent.replaceAll(" [^ ]+ [^ ]+ Packages.gz\n", ""));
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        org.apache.commons.io.FileUtils.deleteDirectory(new File(prefix));
    }
}
