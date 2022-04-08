/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.suse.manager.reactor.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.CmdResult;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Test for {@link com.suse.manager.reactor.utils.RhelUtils}
 */
public class RhelUtilsTest extends JMockBaseTestCaseWithUser {

    private static final String PLAIN_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)";
    private static final String RES_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)\n" +
            "# This is a \"SLES Expanded Support platform release 6.8\"\n" +
            "# The above \"Red Hat Enterprise Linux Server\" string is only used to \n" +
            "# keep software compatibility.";
    private static final String CENTOS_REDHAT_RELEASE =
            "CentOS Linux release 7.2.1511 (Core)";
    private static final String ORACLE_RELEASE =
            "Oracle Linux Server release 8.2";
    private static final String ALIBABA_RELEASE =
            "Alibaba Cloud Linux (Aliyun Linux) release 2.1903 LTS (Hunting Beagle)";
    private static final String ALMALINUX_RELEASE =
            "AlmaLinux release 8.3 (Purple Manul)";
    private static final String AMAZON_RELEASE =
            "Amazon Linux release 2 (Karoo)";
    private static final String ROCKY_RELEASE =
            "Rocky Linux release 8.4 (Green Obsidian)";

    @FunctionalInterface
    private interface SetupMinionConsumer {

        void accept(MinionServer minion) throws Exception;

    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Test
    public void testParseReleaseFileRedHat() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(PLAIN_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileRES() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(RES_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileCentos() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(CENTOS_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("CentOS", os.get().getName());
        assertEquals("7", os.get().getMajorVersion());
        assertEquals("2.1511", os.get().getMinorVersion());
        assertEquals("Core", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileOracle() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(ORACLE_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("OracleLinux", os.get().getName());
        assertEquals("8", os.get().getMajorVersion());
        assertEquals("2", os.get().getMinorVersion());
        assertEquals("", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileAlibaba() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(ALIBABA_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("AlibabaCloud(Aliyun)", os.get().getName());
        assertEquals("2", os.get().getMajorVersion());
        assertEquals("1903", os.get().getMinorVersion());
        assertEquals("Hunting Beagle", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileAlma() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(ALMALINUX_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("AlmaLinux", os.get().getName());
        assertEquals("8", os.get().getMajorVersion());
        assertEquals("3", os.get().getMinorVersion());
        assertEquals("Purple Manul", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileAmazon() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(AMAZON_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("AmazonLinux", os.get().getName());
        assertEquals("2", os.get().getMajorVersion());
        assertEquals("Karoo", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileRocky() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(ROCKY_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RockyLinux", os.get().getName());
        assertEquals("8", os.get().getMajorVersion());
        assertEquals("4", os.get().getMinorVersion());
        assertEquals("Green Obsidian", os.get().getRelease());
    }

    @Test
    public void testParseReleaseFileNonMatching() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile("GarbageOS 1.0 (Trash can)");
        assertFalse(os.isPresent());
    }

    private void doTestDetectRhelProduct(String json, SetupMinionConsumer setupMinion,
                                         Consumer<Optional<RhelUtils.RhelProduct>> response)
            throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(
                user, "/com/suse/manager/reactor/utils/test/productdata", false, false);
        Map<String, State.ApplyResult> map = new JsonParser<>(
                State.apply(Collections.emptyList()).getReturnType()).parse(
                        TestUtils.readAll(TestUtils.findTestData(json)));
        String centosReleaseContent = map.get("cmd_|-centosrelease_|-cat /etc/centos-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String alibabaReleaseContent = map.get("cmd_|-alibabarelease_|-cat /etc/alinux-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String oracleReleaseContent = map.get("cmd_|-oraclerelease_|-cat /etc/oracle-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String almaReleaseContent = map.get("cmd_|-almarelease_|-cat /etc/almalinux-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String amazonReleaseContent = map.get("cmd_|-amazonrelease_|-cat /etc/system-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String rockyReleaseContent = map.get("cmd_|-rockyrelease_|-cat /etc/rocky-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String rhelReleaseContent = map.get("cmd_|-rhelrelease_|-cat /etc/redhat-release_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        String whatProvidesRes = map.get("cmd_|-respkgquery_|-rpm -q --whatprovides 'sles_es-release-server'_|-run")
                .getChanges(CmdResult.class)
                .getStdout();
        MinionServer minionServer = MinionServerFactoryTest.createTestMinionServer(user);
        minionServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        if (setupMinion != null) {
            setupMinion.accept(minionServer);
        }
        Optional<RhelUtils.RhelProduct> prod = RhelUtils.detectRhelProduct(minionServer,
                Optional.ofNullable(whatProvidesRes),
                Optional.ofNullable(rhelReleaseContent),
                Optional.ofNullable(centosReleaseContent),
                Optional.ofNullable(oracleReleaseContent),
                Optional.ofNullable(alibabaReleaseContent),
                Optional.ofNullable(almaReleaseContent),
                Optional.ofNullable(amazonReleaseContent),
                Optional.ofNullable(rockyReleaseContent));
        assertTrue(prod.isPresent());
        response.accept(prod);

    }

    @Test
    public void testDetectRhelProductRES() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_res.json",
                minionServer -> {
                    Channel resChannel = createResChannel(user, "7");
                    minionServer.addChannel(resChannel);
                    minionServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
                },
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent());
                    assertEquals("res", prod.get().getSuseProduct().get().getName());
                    assertEquals("RedHatEnterpriseServer", prod.get().getName());
                    assertEquals("Maipo", prod.get().getRelease());
                    assertEquals("7", prod.get().getVersion());
        });
    }

    @Test
    public void testDetectCentOSProductRES() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_centos2.json",
                minionServer -> {
                    Channel resChannel = createResChannel(user, "6");
                    minionServer.addChannel(resChannel);
                    minionServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
                },
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("res", prod.get().getSuseProduct().get().getName());
                    assertEquals("CentOS", prod.get().getName());
                    assertEquals("Final", prod.get().getRelease());
                    assertEquals("6", prod.get().getVersion());
        });
    }

    @Test
    public void testDetectRhelProductRHEL() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_rhel.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("RedHatEnterpriseServer", prod.get().getName());
                    assertEquals("Maipo", prod.get().getRelease());
                    assertEquals("7", prod.get().getVersion());
        });
    }

    @Test
    public void testDetectRhelProductCentos() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_centos.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("CentOS", prod.get().getName());
                    assertEquals("Core", prod.get().getRelease());
                    assertEquals("7", prod.get().getVersion());
        });
    }

    @Test
    public void testDetectRhelProductOracle() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_oracle.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("OracleLinux", prod.get().getName());
                    assertEquals("", prod.get().getRelease());
                    assertEquals("8", prod.get().getVersion());
        });
    }

    @Test
    public void testDetectRhelProductAlibaba() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_alibaba.json",
                null,
                prod -> {
                    assertFalse(prod.get().getSuseProduct().isPresent());
                    assertEquals("AlibabaCloud(Aliyun)", prod.get().getName());
                    assertEquals("Hunting Beagle", prod.get().getRelease());
                    assertEquals("2", prod.get().getVersion());
                });
    }

    @Test
    public void testDetectRhelProductAlma() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_almalinux.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("AlmaLinux", prod.get().getName());
                    assertEquals("Purple Manul", prod.get().getRelease());
                    assertEquals("8", prod.get().getVersion());
                });
    }

    @Test
    public void testDetectRhelProductAmazon() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_amazon.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("AmazonLinux", prod.get().getName());
                    assertEquals("Karoo", prod.get().getRelease());
                    assertEquals("2", prod.get().getVersion());
                });
    }

    @Test
    public void testDetectRhelProductRocky() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_rockylinux.json",
                null,
                prod -> {
                    assertTrue(prod.get().getSuseProduct().isPresent(), "SUSE Product not found");
                    assertEquals("RockyLinux", prod.get().getName());
                    assertEquals("Green Obsidian", prod.get().getRelease());
                    assertEquals("8", prod.get().getVersion());
                });
    }

    public static Channel createResChannel(User user, String version) throws Exception {
        return createResChannel(user, version, "x86_64", "channel-x86_64");
    }

    public static Channel createResChannel(User user, String version, String archLabel, String channelLabel)
            throws Exception {
        Channel c = ChannelFactoryTest.createTestChannel(user, "channel-" + archLabel);
        c.setLabel(channelLabel);
        c.setOrg(null); // vendor channels don't have org

        SUSEProduct suseProduct = SUSEProductFactory.findAllSUSEProducts().stream()
                .filter(p -> p.getName().equalsIgnoreCase("res") && p.getVersion().equals(version))
                .findFirst().orElseGet(() -> {
                    SUSEProduct suseProd = new SUSEProduct();
                    suseProd.setBase(true);
                    suseProd.setName("res");
                    suseProd.setVersion(version);
                    suseProd.setRelease(null);
                    suseProd.setReleaseStage(ReleaseStage.released);
                    suseProd.setFriendlyName("RHEL Expanded Support  " + version);
                    suseProd.setProductId(new Random().nextInt(999999));
                    suseProd.setArch(null); // RES products can contain channels with different archs
                    SUSEProductFactory.save(suseProd);
                    SUSEProductFactory.getSession().flush();
                    return suseProd;
        });

        ProductName pn = ChannelFactoryTest.lookupOrCreateProductName("RES");
        c.setProductName(pn);

        SUSEProductChannel spc = new SUSEProductChannel();
        spc.setChannel(c);
        spc.setProduct(suseProduct);
        spc.setMandatory(true);

        suseProduct.getSuseProductChannels().add(spc);

        TestUtils.saveAndFlush(spc);

        ChannelFactory.save(c);
        return c;
    }

}
