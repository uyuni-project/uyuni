/**
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

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.taskomatic.task.repomd.DebPackageWriter;
import com.redhat.rhn.taskomatic.task.repomd.DebReleaseWriter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.io.File;
import java.nio.file.Files;
import java.time.ZonedDateTime;

public class DebReleaseWriterTest extends BaseTestCaseWithUser {

    private String prefix;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        prefix = Files.createTempDirectory("debreleasewriter").toAbsolutePath().toString() + File.separator;
    }

    public void testDateFormat() {
        ZonedDateTime time = ZonedDateTime.parse("2018-11-22T12:35:40+01:00[Europe/Madrid]");
        assertEquals("Thu, 22 Nov 2018 11:35:40 UTC", DebReleaseWriter.RFC822_DATE_FORMAT.format(time));
    }

    public void testGenerateRelease() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setChannelArch(ChannelFactory.findArchByLabel("channel-ia64-deb"));

        PackageArch pa = PackageFactory.lookupPackageArchByLabel("amd64-deb");

        com.redhat.rhn.domain.rhnpackage.Package pkg1 = PackageManagerTest.addPackageToChannel("pkg1", channel);
        pkg1.setPackageArch(pa);

        DebPackageWriter pkgWriter = new DebPackageWriter(channel, prefix);
        pkgWriter.generatePackagesGz();

        DebReleaseWriter writer = new DebReleaseWriter(channel, prefix);
        writer.generateRelease();

        String releaseContent = FileUtils.readStringFromFile(prefix + "Release");

        String rel = "Archive: " + channel.getLabel() + "\n" +
                "Label: " + channel.getLabel() + "\n" +
                "Suite: " + channel.getLabel() + "\n" +
                "Architectures: ia64\n" +
                "Date: " + DebReleaseWriter.RFC822_DATE_FORMAT.format(ZonedDateTime.now()) + "\n" +
                "Description: TestChannel description\n" +
                "MD5Sum:\n" +
                " d41d8cd98f00b204e9800998ecf8427e 0 main/binary-ia64/Packages\n" +
                " 3970e82605c7d109bb348fc94e9eecc0 20 main/binary-ia64/Packages.gz\n" +
                "SHA1:\n" +
                " da39a3ee5e6b4b0d3255bfef95601890afd80709 0 main/binary-ia64/Packages\n" +
                " e03849ea786b9f7b28a35c17949e85a93eb1cff1 20 main/binary-ia64/Packages.gz\n" +
                "SHA256:\n" +
                " e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855 0 main/binary-ia64/Packages\n" +
                " f5d031af01f137ae07fa71720fab94d16cc8a2a59868766002918b7c240f3967 20 main/binary-ia64/Packages.gz\n";
        assertEquals(rel, releaseContent);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        org.apache.commons.io.FileUtils.deleteDirectory(new File(prefix));
    }
}
