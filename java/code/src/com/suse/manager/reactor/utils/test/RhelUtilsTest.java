/**
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.CmdExecCodeAll;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Test for {@link com.suse.manager.reactor.utils.RhelUtils}
 */
public class RhelUtilsTest extends JMockBaseTestCaseWithUser {

    private static String PLAIN_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)";
    private static String RES_REDHAT_RELEASE =
            "Red Hat Enterprise Linux Server release 6.8 (Santiago)\n" +
            "# This is a \"SLES Expanded Support platform release 6.8\"\n" +
            "# The above \"Red Hat Enterprise Linux Server\" string is only used to \n" +
            "# keep software compatibility.";
    private static String CENTOS_REDHAT_RELEASE =
            "CentOS Linux release 7.2.1511 (Core)";

    @FunctionalInterface
    private interface SetupMinionConsumer {

        void accept(MinionServer minion) throws Exception;

    }

    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public void testParseReleaseFileRedHat() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(PLAIN_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    public void testParseReleaseFileRES() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(RES_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("RedHatEnterpriseServer", os.get().getName());
        assertEquals("6", os.get().getMajorVersion());
        assertEquals("8", os.get().getMinorVersion());
        assertEquals("Santiago", os.get().getRelease());
    }

    public void testParseReleaseFileCentos() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile(CENTOS_REDHAT_RELEASE);
        assertTrue(os.isPresent());
        assertEquals("CentOS", os.get().getName());
        assertEquals("7", os.get().getMajorVersion());
        assertEquals("2.1511", os.get().getMinorVersion());
        assertEquals("Core", os.get().getRelease());
    }

    public void testParseReleaseFileNonMatching() {
        Optional<RhelUtils.ReleaseFile> os = RhelUtils.parseReleaseFile("GarbageOS 1.0 (Trash can)");
        assertFalse(os.isPresent());
    }

    private void doTestDetectRhelProduct(String json, SetupMinionConsumer setupMinion, Consumer<Optional<RhelUtils.RhelProduct>> response)
            throws Exception {
        Map<String, State.ApplyResult> map = new JsonParser<>(State.apply(Collections.emptyList()).getReturnType()).parse(
                TestUtils.readAll(TestUtils.findTestData(json)));
        String centosReleaseContent = map.get("cmd_|-centosrelease_|-cat /etc/centos-release_|-run")
                .getChanges(CmdExecCodeAll.class)
                .getStdout();
        String rhelReleaseContent = map.get("cmd_|-rhelrelease_|-cat /etc/redhat-release_|-run")
                .getChanges(CmdExecCodeAll.class)
                .getStdout();
        String whatProvidesRes = map.get("cmd_|-respkgquery_|-rpm -q --whatprovides 'sles_es-release-server'_|-run")
                .getChanges(CmdExecCodeAll.class)
                .getStdout();
        MinionServer minionServer = MinionServerFactoryTest.createTestMinionServer(user);
        if (setupMinion != null) {
            setupMinion.accept(minionServer);
        }
        Optional<RhelUtils.RhelProduct> prod = RhelUtils.detectRhelProduct(minionServer,
                Optional.ofNullable(whatProvidesRes),
                Optional.ofNullable(rhelReleaseContent),
                Optional.ofNullable(centosReleaseContent));
        assertTrue(prod.isPresent());
        response.accept(prod);

    }

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

    public void testDetectRhelProductRHEL() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_rhel.json",
                null,
                prod -> {
                    assertFalse(prod.get().getSuseProduct().isPresent());
                    assertEquals("RedHatEnterpriseServer", prod.get().getName());
                    assertEquals("Maipo", prod.get().getRelease());
                    assertEquals("7", prod.get().getVersion());
        });
    }

    public void testDetectRhelProductCentos() throws Exception {
        doTestDetectRhelProduct("dummy_packages_redhatprodinfo_centos.json",
                null,
                prod -> {
                    assertFalse(prod.get().getSuseProduct().isPresent());
                    assertEquals("CentOS", prod.get().getName());
                    assertEquals("Core", prod.get().getRelease());
                    assertEquals("7", prod.get().getVersion());
        });
    }

    public static Channel createResChannel(User user, String version) throws Exception {
        return createResChannel(user, version, "x86_64", "channel-x86_64");
    }

    public static Channel createResChannel(User user, String version, String archLabel, String channelLabel) throws Exception {
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
