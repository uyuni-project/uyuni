/**
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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.test.PackageCapabilityTest;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.task.TaskManager;
import com.redhat.rhn.taskomatic.task.repomd.RpmRepositoryWriter;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

public class RpmRepositoryWriterTest extends BaseTestCaseWithUser {

    private Path mountPointDir;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mountPointDir = Files.createTempDirectory("rpmrepotest");
    }

    public void testPagination() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));

        PackageManager.createRepoEntrys(channel.getId());

        for (int i = 0; i < 25; i++) {
            com.redhat.rhn.domain.rhnpackage.Package pkg = PackageManagerTest.addPackageToChannel("pkgpg" + i, channel);
        }

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        DataResult<PackageDto> pkgs = TaskManager.getChannelPackageDtos(channel, 0, 10);
        assertEquals(10, pkgs.size());

        pkgs = TaskManager.getChannelPackageDtos(channel, 0, 30);
        assertEquals(25, pkgs.size());
    }


    public void testWriteRepomdFiles() throws Exception {
        RpmRepositoryWriter writer = new RpmRepositoryWriter("rhn/repodata", mountPointDir.toAbsolutePath().toString());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        com.redhat.rhn.domain.rhnpackage.Package pkg1 = PackageManagerTest.addPackageToChannel("pkg1", channel);

        PackageProvides prov1 = new PackageProvides();
        PackageCapability provCap = PackageCapabilityTest.createTestCapability("capProv1");
        prov1.setCapability(provCap);
        prov1.setPack(pkg1);
        prov1.setSense(0L);
        pkg1.getProvides().add(prov1);

        PackageRequires req1 = new PackageRequires();
        PackageCapability reqCap = PackageCapabilityTest.createTestCapability("capReq1");
        req1.setCapability(reqCap);
        req1.setPack(pkg1);
        req1.setSense(0L);
        pkg1.getRequires().add(req1);

        PackageManager.createRepoEntrys(channel.getId());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        writer.writeRepomdFiles(channel);

        String repomdExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<repomd xmlns=\"http://linux.duke.edu/metadata/repo\"><data type=\"primary\"><location href=\"repodata/primary.xml.gz\"/>" +
                "<checksum type=\"sha256\">61d08b336c520bdfade052b808616e5a80e5eec29d82c2e7dbbd58bc7ee93864</checksum>" +
                "<open-checksum type=\"sha256\">d515cc563a9cf6dfe2f0fbe999bbcec0d495f4c7e1cd09286d7df74c58300e1b</open-checksum><timestamp>1544711985</timestamp>" +
                "</data><data type=\"filelists\"><location href=\"repodata/filelists.xml.gz\"/>" +
                "<checksum type=\"sha256\">b6528b97a62a107e9357d406e1d22be971ca55d4332abbc1bb0e9b84c3139d99</checksum>" +
                "<open-checksum type=\"sha256\">9e1aca5fd324f66bbedd27f6dfc722da83354374a6808010886e93e09795bb30</open-checksum>" +
                "<timestamp>1544711985</timestamp></data><data type=\"other\"><location href=\"repodata/other.xml.gz\"/>" +
                "<checksum type=\"sha256\">4fa5ac10cf38edc497f18e41f537e1ef24a2fb4d0b537c96248433ca3a51b975</checksum>" +
                "<open-checksum type=\"sha256\">a50ffc2416654509bb720263a0ab090c3c53739305e8f637422f81937fabd700</open-checksum>" +
                "<timestamp>1544711985</timestamp></data><data type=\"susedata\"><location href=\"repodata/susedata.xml.gz\"/>" +
                "<checksum type=\"sha256\">2818aff4276b64a24abb2e60fc97d152abee478c35a5f4b04015c7704e8b7cfe</checksum>" +
                "<open-checksum type=\"sha256\">20c9750a06ddcc9b783504433cbc1eaea14c08889770f19274fa3f91bcbbd37f</open-checksum>" +
                "<timestamp>1544711985</timestamp></data></repomd>";
        repomdExpected = cleanupRepomd(repomdExpected);

        String primaryXmlExpected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<metadata packages=\"1\" xmlns=\"http://linux.duke.edu/metadata/common\" xmlns:rpm=\"http://linux.duke.edu/metadata/rpm\"><package type=\"rpm\">" +
                "<name>pkg1</name><arch>noarch</arch><version ver=\"1.0.0\" rel=\"1\" epoch=\"1\"/>" +
                "<checksum type=\"md5\" pkgid=\"YES\">$1$jrBDaf62$URlwK4AAEVCdcWhcZdcSK1</checksum>" +
                "<summary>Created by RHN-JAVA unit tests. Please disregard.</summary><description>RHN-JAVA Package Test</description><packager/><url/>" +
                "<time file=\"1544723761\" build=\"1544723761\"/><size package=\"42\" archive=\"42\" installed=\"42\"/>" +
                "<location href=\"getPackage/$1$RAiHwOUL$bcHCUHDdGcmVfklrxaRVC1\"/><format><rpm:license>Red Hat - RHN - 2005</rpm:license>" +
                "<rpm:vendor>Rhn-Java</rpm:vendor><rpm:group>4XwfQEUNzkDr3</rpm:group><rpm:buildhost>foo2</rpm:buildhost>" +
                "<rpm:sourcerpm>ZL4ke7jOwX6Tz</rpm:sourcerpm><rpm:header-range start=\"-1\" end=\"-1\"/><rpm:provides>" +
                "<rpm:entry name=\"capProv1\" flags=\"GE\" epoch=\"0\" ver=\"\" rel=\"1.0\"/></rpm:provides><rpm:requires>" +
                "<rpm:entry name=\"capReq1\" flags=\"GE\" epoch=\"0\" ver=\"\" rel=\"1.0\"/></rpm:requires><rpm:conflicts/>" +
                "<rpm:obsoletes/><rpm:recommends/><rpm:suggests/><rpm:supplements/><rpm:enhances/></format></package></metadata>";

        primaryXmlExpected = cleanupPrimaryXml(primaryXmlExpected);
        Path channelDir = mountPointDir.resolve("rhn").resolve("repodata").resolve(channel.getLabel());


        File repomdXml = channelDir.resolve("repomd.xml").toFile();

        try (FileInputStream fin = new FileInputStream(repomdXml)) {
            String xml = TestUtils.readAll(fin);

            assertEquals(repomdExpected, cleanupRepomd(xml));

        }
        catch (IOException e) {
            fail(e.getMessage());
        }

        File primaryXmlGz = channelDir.resolve("primary.xml.gz").toFile();

        try (FileInputStream fin = new FileInputStream(primaryXmlGz); InputStream gzipStream = new GZIPInputStream(fin)) {
            String primaryXmlStr = TestUtils.readAll(gzipStream);

            primaryXmlStr = cleanupPrimaryXml(primaryXmlStr);


            assertEquals(primaryXmlExpected, primaryXmlStr);
        }
        catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private String cleanupRepomd(String str) {
        String ret = str.trim().replaceFirst("<checksum type=\"sha256\">.*</checksum>", "<checksum type=\"sha256\">xxx</checksum>");
        ret = ret.replaceFirst("<open-checksum type=\"sha256\">.*</open-checksum>", "<open-checksum type=\"sha256\">xxx</open-checksum>");
        ret = ret.replaceFirst("<timestamp>.*</timestamp>", "<timestamp>123</timestamp>");
        return ret;
    }

    private String cleanupPrimaryXml(String str) {
        String ret = str.trim().replaceFirst(">.*?</checksum>", ">xxx</checksum>");
        ret = ret.replaceFirst("<time file=\"\\d+\" build=\"\\d+\"/>", "<time file=\"123\" build=\"123\"/>");
        ret = ret.replaceFirst("<location href=\"getPackage/.*?\"/>", "<location href=\"getPackage/xxx\"/>");
        ret = ret.replaceFirst("<rpm:sourcerpm>.*?</rpm:sourcerpm>", "<rpm:sourcerpm>xxx</rpm:sourcerpm>");
        ret = ret.replaceFirst("<rpm:group>.*?</rpm:group>", "<rpm:group>xxx</rpm:group>");
        return ret;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(mountPointDir.toFile());
    }
}
