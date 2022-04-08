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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageExtraTagsKeys;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.test.PackageCapabilityTest;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.task.TaskManager;
import com.redhat.rhn.taskomatic.task.repomd.DebPackageWriter;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

public class DebPackageWriterTest extends JMockBaseTestCaseWithUser {

    private Path tmpDir;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        tmpDir = Files.createTempDirectory("debPkgWriterTest");
    }

    @Test
    public void testWritePackages() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(user);

        PackageExtraTagsKeys tag1 = PackageManagerTest.createExtraTagKey("Tag1");
        PackageExtraTagsKeys tag2 = PackageManagerTest.createExtraTagKey("Tag2");
        PackageExtraTagsKeys tag3 = PackageManagerTest.createExtraTagKey("Tag3");

        Package pkg1 = PackageManagerTest.addPackageToChannel("pkg_1", channel);
        pkg1.setVendor(null);
        pkg1.getExtraTags().put(tag1, "value1");
        pkg1.getExtraTags().put(tag2, "value2");

        PackageCapability cap1 = PackageCapabilityTest.createTestCapability("python:any _0");
        cap1.setVersion(" 2.4~");
        PackageRequires req1 = new PackageRequires();
        req1.setCapability(cap1);
        req1.setPack(pkg1);
        req1.setSense(12L);

        PackageCapability cap2 = PackageCapabilityTest.createTestCapability("python-crypto _1");
        cap2.setVersion(" 2.5.0");
        PackageRequires req2 = new PackageRequires();
        req2.setCapability(cap2);
        req2.setPack(pkg1);
        req2.setSense(12L);

        pkg1.getRequires().add(req1);
        pkg1.getRequires().add(req2);

        Package pkg2 = PackageManagerTest.addPackageToChannel("pkg_2", channel);
        pkg2.getExtraTags().put(tag2, "value2");
        pkg2.getExtraTags().put(tag3, "value3");

        Package pkg3 = PackageManagerTest.addPackageToChannel("pkg_3", channel);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        DataResult<PackageDto> packageBatch = TaskManager.getChannelPackageDtos(channel, 0, 100);
        packageBatch.elaborate();
        Map<Long, Map<String, String>> extraTags = TaskManager.getChannelPackageExtraTags(
                Arrays.asList(pkg1.getId(), pkg2.getId()));
        for (PackageDto pkgDto : packageBatch) {
            pkgDto.setExtraTags(extraTags.get(pkgDto.getId()));
        }

        try (DebPackageWriter writer = new DebPackageWriter(channel, tmpDir.normalize().toString() + File.separator)) {
            for (PackageDto pkgDto: packageBatch) {
                writer.addPackage(pkgDto);
            }
        }

        String packagesContent = FileUtils.readFileToString(tmpDir.resolve("Packages").toFile());
        packagesContent = cleanupContent(packagesContent);

        assertEquals("Package: pkg_1\n" +
                        "Version: 1:1.0.0-1\n" +
                        "Architecture: noarch\n" +
                        "Maintainer: Debian\n" +
                        "Installed-Size: 42\n" +
                        "Depends: python:any (>= 2.4~), python-crypto (>= 2.5.0)\n" +
                        "Filename: channel/getPackage/pkg_1_1:1.0.0-1.noarch.deb\n" +
                        "Size: 42\n" +
                        "MD5sum: some-md5sum\n" +
                        "Section: some-section\n" +
                        "Tag1: value1\n" +
                        "Tag2: value2\n" +
                        "Description: RHN-JAVA Package Test\n" +
                        "\n" +
                        "Package: pkg_2\n" +
                        "Version: 1:1.0.0-1\n" +
                        "Architecture: noarch\n" +
                        "Maintainer: Rhn-Java\n" +
                        "Installed-Size: 42\n" +
                        "Filename: channel/getPackage/pkg_2_1:1.0.0-1.noarch.deb\n" +
                        "Size: 42\n" +
                        "MD5sum: some-md5sum\n" +
                        "Section: some-section\n" +
                        "Tag3: value3\n" +
                        "Tag2: value2\n" +
                        "Description: RHN-JAVA Package Test\n" +
                        "\n" +
                        "Package: pkg_3\n" +
                        "Version: 1:1.0.0-1\n" +
                        "Architecture: noarch\n" +
                        "Maintainer: Rhn-Java\n" +
                        "Installed-Size: 42\n" +
                        "Filename: channel/getPackage/pkg_3_1:1.0.0-1.noarch.deb\n" +
                        "Size: 42\n" +
                        "MD5sum: some-md5sum\n" +
                        "Section: some-section\n" +
                        "Description: RHN-JAVA Package Test",
                packagesContent.trim());
    }

    public static String cleanupContent(String packagesContent) {
        packagesContent = packagesContent.replaceAll("Filename: channel.*/getPackage/",
                "Filename: channel/getPackage/");
        packagesContent = packagesContent.replaceAll("MD5sum: .*\\s", "MD5sum: some-md5sum\n");
        packagesContent = packagesContent.replaceAll("Section: .*\\s", "Section: some-section\n");
        return packagesContent;
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tmpDir.toFile());
    }

}
