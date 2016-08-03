/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.manager.distupgrade.test.DistUpgradeManagerTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.Zypper.ProductInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.parser.JsonParser;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Tests for {@link RegisterMinionEventMessageAction}.
 */
public class RegisterMinionActionTest extends JMockBaseTestCaseWithUser {

    private static final String MINION_ID = "suma3pg.vagrant.local";
    private static final String MACHINE_ID = "003f13081ddd408684503111e066f921";

    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Test the minion registration.
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void testDoExecute()
            throws Exception {
        executeTest(null);
    }

    public void executeTest(Consumer<MinionServer> assertions) throws Exception {

        // cleanup
        SaltService saltServiceMock = mock(SaltService.class);

        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Channel baseChannel = ChannelFactoryTest.createBaseChannel(user);
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setBaseChannel(baseChannel);
        key.setOrg(user.getOrg());
        Arrays.asList(
            "rhncfg", "rhncfg-actions", "rhncfg-client", "rhn-virtualization-host", "osad"
        ).forEach(blacklisted ->
            key.addPackage(PackageFactory.lookupOrCreatePackageByName(blacklisted), null)
        );
        key.addPackage(PackageFactory.lookupOrCreatePackageByName("vim"), null);
        ManagedServerGroup testGroup = ServerGroupFactory.create(
                "TestGroup", "group for tests", user.getOrg());
        key.setServerGroups(Collections.singleton(testGroup));
        ActivationKeyFactory.save(key);

        // Register a minion via RegisterMinionAction and mocked SaltService

        context().checking(new Expectations() { {
            allowing(saltServiceMock).getMasterHostname(MINION_ID);
            will(returnValue(Optional.of(MINION_ID)));
            allowing(saltServiceMock).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));
            allowing(saltServiceMock).getGrains(MINION_ID);
            will(returnValue(getGrains(MINION_ID, key.getKey())));
            allowing(saltServiceMock).getCpuInfo(MINION_ID);
            will(returnValue(getCpuInfo(MINION_ID)));
            allowing(saltServiceMock).syncGrains(with(any(String.class)));
            allowing(saltServiceMock).syncModules(with(any(String.class)));
        } });

        RegisterMinionEventMessageAction action = new RegisterMinionEventMessageAction(saltServiceMock);
        action.doExecute(new RegisterMinionEventMessage(MINION_ID));

        // Verify the resulting system entry
        String machineId = saltServiceMock.getMachineId(MINION_ID).get();
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();
        assertEquals(MINION_ID, minion.getName());
        assertEquals(machineId, minion.getDigitalServerId());
        assertEquals(machineId, minion.getMachineId());
        assertEquals("3.12.48-52.27-default", minion.getRunningKernel());
        assertEquals("SLES", minion.getOs());
        assertEquals("12", minion.getRelease());
        assertEquals("N", minion.getAutoUpdate());
        assertEquals(489, minion.getRam());

        assertEquals(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"),
                minion.getServerArch());
        assertEquals(ServerFactory.findContactMethodByLabel("default"),
                minion.getContactMethod());

        // Verify the entitlement
        assertEquals(EntitlementManager.SALT, minion.getBaseEntitlement());

        // Verify activation key
        Optional<Set<PackageState>> packageStates = StateFactory.latestPackageStates(minion);
        assertTrue(packageStates.isPresent());
        packageStates.ifPresent(states -> {
            assertEquals(1, states.size());
            states.stream().forEach(state -> {
                assertEquals(state.getName().getName(), "vim");
                assertEquals(state.getPackageState(), PackageStates.INSTALLED);
                assertEquals(state.getVersionConstraint(), VersionConstraints.ANY);
            });
        });
        assertEquals(baseChannel, minion.getBaseChannel());
        assertTrue("Server should have the testGroup ServerGroup",
                ServerGroupFactory.listServers(testGroup).contains(minion));
        assertEquals(key.getOrg(), minion.getOrg());
        Optional<Server> server = key.getToken().getActivatedServers().stream()
                .findFirst()
                .filter(minion::equals);
        assertTrue("Server should be a activated system on the activation key", server.isPresent());

        if (assertions != null) {
            assertions.accept(minion);
        }

    }

    public void testReRegisterTraditionalAsMinion() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        ServerFactory.save(server);

        executeTest(minion -> {
            assertEquals(server.getId(), minion.getId());
            List<ServerHistoryEvent> history = new ArrayList<>();
            history.addAll(minion.getHistory());
            Collections.sort(history, (h1, h2) -> h1.getCreated().compareTo(h2.getCreated()));
            assertEquals(history.get(history.size()-1).getSummary(), "Server reactivated as Salt minion");
        });
    }

    public void testRegisterMinionWithoutActivationKeyNoProductChannel() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = setupChannelAndProduct(channelFamily);
        SaltService saltService = setupStubs(product);

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(MINION_ID).get();
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();

        // no base/required channels - e.g. we need an SCC sync
        assertEquals(MINION_ID, minion.getName());
        assertNull(minion.getBaseChannel());
        assertTrue(minion.getChannels().isEmpty());
    }

    public void testRegisterMinionWithoutActivationKeyWithProductChannel()
        throws Exception {

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = setupChannelAndProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);

        SaltService saltService = setupStubs(product);

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(MINION_ID).get();
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();
        assertEquals(MINION_ID, minion.getName());

        // base channel check
        assertNotNull(minion.getBaseChannel());
        assertEquals(baseChannelX8664, minion.getBaseChannel());

        // required channels checks
        assertFalse(minion.getChannels().isEmpty());
        assertTrue(minion.getChannels().size() > 1);
    }

    private SUSEProduct setupChannelAndProduct(ChannelFamily channelFamily)
        throws Exception {
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setRelease(null);
        return product;
    }

    private Channel setupBaseAndRequiredChannels(ChannelFamily channelFamily,
            SUSEProduct product)
        throws Exception {
        ChannelProduct channelProduct = createTestChannelProduct();
        ChannelArch channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel baseChannelX8664 = DistUpgradeManagerTest
                .createTestBaseChannel(channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelX8664, product);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);
        channel2.setParentChannel(baseChannelX8664);
        channel3.setParentChannel(baseChannelX8664);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel2, product);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel3, product);
        return baseChannelX8664;
    }

    @SuppressWarnings("unchecked")
    private SaltService setupStubs(SUSEProduct product)
        throws ClassNotFoundException, IOException {
        SaltService saltService = mock(SaltService.class);

        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        context().checking(new Expectations() { {
            allowing(saltService).getMasterHostname(MINION_ID);
            will(returnValue(Optional.of(MINION_ID)));
            allowing(saltService).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));
            allowing(saltService).getGrains(MINION_ID);
            will(returnValue(getGrains(MINION_ID, "foo")));
            allowing(saltService).getCpuInfo(MINION_ID);
            will(returnValue(getCpuInfo(MINION_ID)));
            allowing(saltService).syncGrains(with(any(String.class)));
            allowing(saltService).syncModules(with(any(String.class)));
            List<ProductInfo> pil = new ArrayList<>();
            ProductInfo pi = new ProductInfo(
                        product.getName(),
                        product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                        true, true, "productline", Optional.of("registerrelease"),
                        "release", "repo", "shortname", "summary", "vendor",
                        product.getVersion());
            pil.add(pi);
            allowing(saltService).callSync(
                     with(any(LocalCall.class)),
                     with(any(String.class)));
            will(returnValue(Optional.of(pil)));
        } });

        RegisterMinionEventMessageAction action =
                new RegisterMinionEventMessageAction(saltService);
        action.doExecute(new RegisterMinionEventMessage(MINION_ID));
        return saltService;
    }


    private Optional<Map<String, Object>> getCpuInfo(String minionId) throws IOException, ClassNotFoundException {
        return Optional.of(new JsonParser<>(Status.cpuinfo().getReturnType()).parse(
                readFile("dummy_cpuinfo.json")));
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> getGrains(String minionId, String akey) throws ClassNotFoundException, IOException {
        Map<String, Object> grains = new JsonParser<>(Grains.items(false).getReturnType()).parse(
                readFile("dummy_grains.json"));
        Map<String, String> susemanager = new HashMap<>();
        susemanager.put("activation_key", akey);
        grains.put("susemanager", susemanager);
        return Optional.of(grains);
    }

    private String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }

}
