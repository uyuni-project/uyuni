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
import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelListProcessor;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.distupgrade.test.DistUpgradeManagerTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.salt.MinionStartupGrains;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.reactor.utils.test.RhelUtilsTest;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Opt;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
    private static final String SSH_PUSH_CONTACT_METHOD = "ssh-push";
    private static final String DEFAULT_CONTACT_METHOD = "default";

    @FunctionalInterface
    private interface ExpectationsFunction {

        Expectations apply(SaltService saltServiceMock, String key) throws Exception;

    }

    @FunctionalInterface
    private interface ActivationKeySupplier {

        String get(String contactMethod) throws Exception;
    }

    @FunctionalInterface
    private interface Assertions {

        void accept(Optional<MinionServer> minion, String machineId, String key) throws IOException;

    }

    private ExpectationsFunction SLES_EXPECTATIONS = (saltServiceMock, key) ->
            new Expectations(){ {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                        .machineId(MACHINE_ID).saltbootInitrd(false)
                        .createMinionStartUpGrains();
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
            } };

    @SuppressWarnings("unchecked")
    private final ExpectationsFunction SLES_NO_AK_EXPECTATIONS = (saltServiceMock, key) ->
            new Expectations() {{
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                         .machineId(MACHINE_ID).saltbootInitrd(false)
                        .createMinionStartUpGrains();
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getProducts(with(any(String.class)));
                will(returnValue(Optional.empty()));
            }};

    private ActivationKeySupplier ACTIVATION_KEY_SUPPLIER = (contactMethod) -> {
        Channel baseChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setBaseChannel(baseChannel);
        key.setOrg(user.getOrg());
        Arrays.asList(
                "rhncfg", "rhncfg-actions", "rhncfg-client", "rhn-virtualization-host", "osad"
        ).forEach(blacklisted ->
                key.addPackage(PackageFactory.lookupOrCreatePackageByName(blacklisted), null)
        );
        key.setContactMethod(ServerFactory.findContactMethodByLabel(contactMethod));
        key.addPackage(PackageFactory.lookupOrCreatePackageByName("vim"), null);
        ManagedServerGroup testGroup = ServerGroupFactory.create(
                "TestGroup", "group for tests", user.getOrg());
        key.setServerGroups(Collections.singleton(testGroup));
        ActivationKeyFactory.save(key);
        return key.getKey();
    };

    private Assertions SLES_ASSERTIONS = (optMinion, machineId, key) -> {
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
        if (key != null) {
            ActivationKey keyObj = ActivationKeyFactory.lookupByKey(key);
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
            assertEquals(keyObj.getBaseChannel(), minion.getBaseChannel());
            ServerGroup testGroup = keyObj.getServerGroups().stream().findFirst().get();
            assertTrue("Server should have the testGroup ServerGroup",
                    ServerGroupFactory.listServers(testGroup).contains(minion));
            assertEquals(keyObj.getOrg(), minion.getOrg());
            Optional<Server> server = keyObj.getToken().getActivatedServers().stream()
                    .findFirst()
                    .filter(minion::equals);
            assertTrue("Server should be a activated system on the activation key", server.isPresent());
        }
    };

    private Consumer<Void> CLEANUP = (arg) -> {
        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
    };

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        Config.get().setString("server.secret_key", "d8d796b3322d65928511769d180d284d2b15158165eb83083efa02c9024aa6cc");
        FormulaFactory.setDataDir(tmpSaltRoot.resolve("formulas/").toString() + "/");
    }

    /**
     * Test the minion registration.
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public void testDoExecute() throws Exception {
        executeTest(
                SLES_EXPECTATIONS,
                ACTIVATION_KEY_SUPPLIER,
                SLES_ASSERTIONS,
                DEFAULT_CONTACT_METHOD);
    }
    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier, Assertions assertions, String contactMethod) throws Exception {
        executeTest(expectations, keySupplier, assertions, CLEANUP, contactMethod);
    }

    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier, Assertions assertions, Consumer<Void> cleanup, String contactMethod) throws Exception {
        executeTest(expectations, keySupplier, assertions, cleanup, contactMethod, Optional.empty());
    }

    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier, Assertions assertions, Consumer<Void> cleanup, String contactMethod, Optional<MinionStartupGrains> startupGrains) throws Exception {

        SaltService saltServiceMock = mock(SaltService.class);
        // cleanup
        if (cleanup != null) {
            cleanup.accept(null);
        }

        String key = keySupplier != null ? keySupplier.get(contactMethod) : null;

        // Register a minion via RegisterMinionAction and mocked SaltService
        if (expectations != null) {
            Expectations exp = expectations.apply(saltServiceMock, key);
            context().checking(exp);

            TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
            ActionManager.setTaskomaticApi(taskomaticMock);

            context().checking(new Expectations() {{
                try {
                    allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
                } catch (TaskomaticApiException e) {
                    e.printStackTrace();
                }
            }});
        }

        RegisterMinionEventMessageAction action = new RegisterMinionEventMessageAction(saltServiceMock);
        action.execute(new RegisterMinionEventMessage(MINION_ID, startupGrains));

        // Verify the resulting system entry
        String machineId = saltServiceMock.getMachineId(MINION_ID).get();
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);

        if (assertions != null) {
            assertions.accept(optMinion, machineId, key);
        }
    }

    public void testReRegisterTraditionalAsMinion() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        ServerFactory.save(server);
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);
        SystemManager.lockServer(user,server,"manually locked");

        // Activation key used for the traditional registration
        ActivationKey activationKey = ActivationKeyTest.createTestActivationKey(user);
        activationKey.getToken().getActivatedServers().add(server);

        executeTest(SLES_EXPECTATIONS, ACTIVATION_KEY_SUPPLIER, (optMinion, machineId, key) -> {
            SLES_ASSERTIONS.accept(optMinion, machineId, key);
            MinionServer minion = optMinion.get();
            assertEquals(server.getId(), minion.getId());
            assertEquals(1, ActivationKeyFactory.lookupByActivatedServer(minion).size());
            assertEquals(key, ActivationKeyFactory.lookupByActivatedServer(minion).get(0).getKey());
            List<ServerHistoryEvent> history = new ArrayList<>();
            history.addAll(minion.getHistory());
            Collections.sort(history, (h1, h2) -> h1.getCreated().compareTo(h2.getCreated()));
            assertEquals(history.get(history.size()-1).getSummary(), "Server reactivated as Salt minion");
            assertNull(minion.getLock());
        }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterDuplicateMinionId() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        executeTest(SLES_EXPECTATIONS, null, (minion, machineId, key) -> {
            assertFalse(MinionServerFactory.findByMachineId(MACHINE_ID).isPresent());
        }, null, DEFAULT_CONTACT_METHOD);
    }

    public void testAlreadyRegisteredMinionWithSameMachineId() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        server.setMachineId(MACHINE_ID);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {{
            allowing(saltServiceMock).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));

        }}, null, (minion, machineId, key) -> {
            assertTrue(MinionServerFactory.findByMinionId(MINION_ID).isPresent());
        }, null, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    public void testAlreadyRegisteredMinionWithNewMinionId() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMachineId(MACHINE_ID);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {{
            allowing(saltServiceMock).getMasterHostname(MINION_ID);
            will(returnValue(Optional.of(MINION_ID)));
            allowing(saltServiceMock).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));
            allowing(saltServiceMock).getGrains(MINION_ID);
            will(returnValue(getGrains(MINION_ID, null, key)));
            allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
            allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
            exactly(1).of(saltServiceMock).deleteKey(server.getMinionId());
        }}, null, (minion, machineId, key) -> {
            assertTrue(MinionServerFactory.findByMinionId(MINION_ID).isPresent());
        }, null, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    public void testWithMissingMachineIdStartUpGrains() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        executeTest((saltServiceMock, key) -> new Expectations() {{
            allowing(saltServiceMock).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));

        }}, null, (minion, machineId, key) -> {
            assertFalse(MinionServerFactory.findByMachineId(MACHINE_ID).isPresent());
        }, null, DEFAULT_CONTACT_METHOD, Optional.of(new MinionStartupGrains()));
    }

    public void testAlreadyRegisteredRetailMinion() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group", user.getOrg());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.getManagedGroups().add(terminalsGroup);
        server.setMinionId(MINION_ID);
        server.setMachineId(MACHINE_ID);
        SystemManager.addServerToServerGroup(server, terminalsGroup);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {{
            allowing(saltServiceMock).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));

        }}, null, (minion, machineId, key) -> {
            assertTrue(MinionServerFactory.findByMinionId(MINION_ID).isPresent());
        }, null, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    public void testChangeContactMethodRegisterMinion() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        ServerFactory.save(server);
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        executeTest(SLES_EXPECTATIONS, ACTIVATION_KEY_SUPPLIER, (optMinion, machineId, key) -> {
            SLES_ASSERTIONS.accept(optMinion, machineId, key);
            MinionServer minion = optMinion.get();
            assertEquals(server.getId(), minion.getId());
            assertEquals(minion.getContactMethod().getLabel(), DEFAULT_CONTACT_METHOD);
            List<ServerHistoryEvent> history = new ArrayList<>();
            history.addAll(minion.getHistory());
            Collections.sort(history, (h1, h2) -> h1.getCreated().compareTo(h2.getCreated()));
            assertEquals(history.get(history.size()-1).getSummary(), "Server reactivated as Salt minion");
        }, SSH_PUSH_CONTACT_METHOD);
    }

    public void testReRegisterTraditionalAsMinionInvalidActKey() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);

        // create machine in different organization
        Org otherOrg = UserTestUtils.createNewOrgFull("otherOrg");
        User otherUser = UserTestUtils.createUser("otheruser", otherOrg.getId());
        Server server = ServerTestUtils.createTestSystem(otherUser);
        server.setMachineId(MACHINE_ID);
        ServerFactory.save(server);

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        SaltService saltService = setupStubs(product);

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(MINION_ID).get();
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();

        assertEquals(MINION_ID, minion.getName());
        // assigned channels are preserved
        Set<Channel> originalChannels = ((Server) HibernateFactory.reload(server)).getChannels();
        assertEquals(originalChannels, minion.getChannels());

        // Invalid Activation Key should be reported because
        // org of server does not match org of activation key
        boolean found = false;
        for (ServerHistoryEvent h : minion.getHistory()) {
            if (h.getSummary().equals("Invalid Activation Key")) {
                found = true;
                break;
            }
        }
        assertTrue("Activation Key not set as invalid", found);
        assertEquals(otherOrg, minion.getOrg());
    }

    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithoutActivationKeyNoSyncProducts() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, null)));
                    List<ProductInfo> pil = new ArrayList<>();
                    ProductInfo pi = new ProductInfo(
                                product.getName(),
                                product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                                true, true, "productline", Optional.of("registerrelease"),
                                "test", "repo", "shortname", "summary", "vendor",
                                product.getVersion());
                    pil.add(pi);
                    allowing(saltServiceMock).getProducts(with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (contactMethod) -> {
                    return null;
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    // no base/required channels - e.g. we need an SCC sync
                    assertNull(minion.getBaseChannel());
                    assertTrue(minion.getChannels().isEmpty());
                }, DEFAULT_CONTACT_METHOD);
    }

    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithoutActivationKey() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        HibernateFactory.getSession().flush();
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, null)));
                    List<ProductInfo> pil = new ArrayList<>();
                    ProductInfo pi = new ProductInfo(
                                product.getName(),
                                product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                                true, true, "productline", Optional.of("registerrelease"),
                                "test", "repo", "shortname", "summary", "vendor",
                                product.getVersion());
                    pil.add(pi);
                    allowing(saltServiceMock).getProducts(with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (contactMethod) -> {
                    return null;
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());

                    assertNotNull(minion.getBaseChannel());
                    assertEquals(baseChannelX8664, minion.getBaseChannel());
                    assertFalse(minion.getChannels().isEmpty());
                    assertTrue(minion.getChannels().size() > 1);

                    // Check if the state assignment file is generated
                    assertTrue(tmpSaltRoot.resolve("custom")
                            .resolve("custom_" + minion.getMachineId() + ".sls").toFile()
                            .exists());
                }, DEFAULT_CONTACT_METHOD);
    }

    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithInvalidActivationKeyNoSyncProducts() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of("non-existent-key"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false).susemanagerGrain(suseManagerGrain)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")));
                    List<ProductInfo> pil = new ArrayList<>();
                    ProductInfo pi = new ProductInfo(
                                product.getName(),
                                product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                                true, true, "productline", Optional.of("registerrelease"),
                                "test", "repo", "shortname", "summary", "vendor",
                                product.getVersion());
                    pil.add(pi);
                    allowing(saltServiceMock).callSync(
                             with(any(LocalCall.class)),
                             with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setOrg(user.getOrg());
                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());

                    // base channel check
                    assertNull(minion.getBaseChannel());
                    assertTrue(minion.getChannels().isEmpty());

                }, DEFAULT_CONTACT_METHOD);
    }

    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithInvalidActivationKey()
        throws Exception {

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ConfigChannel cfgChannel = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Config channel 1", "config-channel-1");
        MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of("non-existent-key"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false).susemanagerGrain(suseManagerGrain)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")));
                    List<ProductInfo> pil = new ArrayList<>();
                    ProductInfo pi = new ProductInfo(
                                product.getName(),
                                product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                                true, true, "productline", Optional.of("registerrelease"),
                                "test", "repo", "shortname", "summary", "vendor",
                                product.getVersion());
                    pil.add(pi);
                    allowing(saltServiceMock).callSync(
                             with(any(LocalCall.class)),
                             with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setBaseChannel(baseChannelX8664);
                    key.setOrg(user.getOrg());

                    ConfigChannelListProcessor proc = new ConfigChannelListProcessor();
                    proc.add(key.getAllConfigChannels(), cfgChannel);

                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());

                    // base channel check
                    assertNull(minion.getBaseChannel());
                    assertTrue(minion.getChannels().isEmpty());

                    // State assignment file check
                    Path slsPath = tmpSaltRoot.resolve("custom").resolve("custom_" + minion.getMachineId() + ".sls");
                    assertTrue(slsPath.toFile().exists());
                    assertFalse(new String(Files.readAllBytes(slsPath)).contains(
                            ConfigChannelSaltManager.getInstance().getChannelStateName(cfgChannel)));
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterMinionWithActivationKey() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ConfigChannel cfgChannel = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Config channel 1", "config-channel-1");
        HibernateFactory.getSession().flush();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                            .machineId(MACHINE_ID).saltbootInitrd(false).susemanagerGrain(suseManagerGrain)
                            .createMinionStartUpGrains();
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, key)));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setBaseChannel(baseChannelX8664);
                    baseChannelX8664.getAccessibleChildrenFor(user)
                            .forEach(channel -> key.addChannel(channel));
                    key.setOrg(user.getOrg());

                    ConfigChannelListProcessor proc = new ConfigChannelListProcessor();
                    proc.add(key.getAllConfigChannels(), cfgChannel);

                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());

                    // only channel associated with Activation Key and child
                    // channels associated with it must be present
                    assertNotNull(minion.getBaseChannel());
                    HashSet<Channel> channels = new HashSet<>();
                    channels.add(baseChannelX8664);
                    baseChannelX8664.getAccessibleChildrenFor(user)
                    .forEach(channel -> channels.add(channel));
                    assertEquals(baseChannelX8664, minion.getBaseChannel());
                    assertEquals(channels.size(), minion.getChannels().size());
                    assertTrue(minion.getChannels().containsAll(channels));

                    assertTrue(minion.getFqdns().isEmpty());

                    // Config channel check
                    assertEquals(1, minion.getConfigChannelCount());
                    assertTrue(minion.getConfigChannelStream().anyMatch(c -> "config-channel-1".equals(c.getLabel())));

                    // State assignment file check
                    Path slsPath = tmpSaltRoot.resolve("custom").resolve("custom_" + minion.getMachineId() + ".sls");
                    assertTrue(slsPath.toFile().exists());
                    assertContains(new String(Files.readAllBytes(slsPath)),
                            ConfigChannelSaltManager.getInstance().getChannelStateName(cfgChannel));
                }, DEFAULT_CONTACT_METHOD);
    }

    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithActivationKeySUSEManagerDefault() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ChannelFamily channelFamilyOther = createTestChannelFamily();
        SUSEProduct productOther =
                SUSEProductTestUtils.createTestSUSEProduct(channelFamilyOther);
        Channel baseChannelX8664Other =
                setupBaseAndRequiredChannels(channelFamilyOther, productOther);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {

            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
                List<ProductInfo> pil = new ArrayList<>();
                ProductInfo pi = new ProductInfo(product.getName(),
                        product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                        true, true, "productline", Optional.of("registerrelease"), "test",
                        "repo", "shortname", "summary", "vendor", product.getVersion());
                pil.add(pi);
                allowing(saltServiceMock).getProducts(with(any(String.class)));
                will(returnValue(Optional.of(pil)));
            }
        }, (contactMethod) -> {
            ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
            key.setBaseChannel(null);
            // Channels unrelated to the product added as child channels in the
            // Activation Key
            baseChannelX8664Other.getAccessibleChildrenFor(user)
                    .forEach(channel -> key.addChannel(channel));
            key.setOrg(user.getOrg());
            ActivationKeyFactory.save(key);
            return key.getKey();
        }, (optMinion, machineId, key) -> {
            assertTrue(optMinion.isPresent());
            MinionServer minion = optMinion.get();
            assertEquals(MINION_ID, minion.getName());

            assertNotNull(minion.getBaseChannel());
            assertEquals(baseChannelX8664, minion.getBaseChannel());
            HashSet<Channel> channels = new HashSet<>();

            // Child channels for auto-selected product must be added
            channels.add(baseChannelX8664);
            baseChannelX8664.getAccessibleChildrenFor(user)
                    .forEach(channel -> channels.add(channel));
            assertEquals(channels, minion.getChannels());
            assertTrue(minion.getFqdns().isEmpty());
        }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRHELMinionWithoutActivationKey() throws Exception {
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=config(redhat-release-server),redhat-release,redhat-release-server,redhat-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7Server,\n")))));

                    allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                    will(returnValue(Optional.of(new RedhatProductInfo(
                            Optional.empty(),
                            Optional.of("Red Hat Enterprise Linux Server release 7.2 (Maipo)"),
                            Optional.empty()
                    ))));

                }},
                null,
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());
                    assertTrue(minion.getFqdns().isEmpty());

                    assertNull(minion.getBaseChannel());
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRHELMinionWithRESActivationKeyOneBaseChannel() throws Exception {
        Channel resChannel = RhelUtilsTest.createResChannel(user, "7");
        HibernateFactory.getSession().flush();
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", key)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=config(redhat-release-server),redhat-release,redhat-release-server,redhat-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7Server,\n")))));

                    allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                    will(returnValue(Optional.of(new RedhatProductInfo(
                            Optional.empty(),
                            Optional.of("Red Hat Enterprise Linux Server release 7.2 (Maipo)"),
                            Optional.empty()
                    ))));

                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setBaseChannel(resChannel);
                    key.setOrg(user.getOrg());
                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());

                    assertNotNull(minion.getBaseChannel());
                    assertEquals("RES", minion.getBaseChannel().getProductName().getName());
                    assertEquals(resChannel, minion.getBaseChannel());

                    assertEquals(1, minion.getChannels().size());
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRHELMinionWithRESActivationKeyTwoBaseChannels() throws Exception {
        Channel resChannel_i386 = RhelUtilsTest.createResChannel(user, "7", "ia32", "res-i386");
        Channel resChannel_x86_64 = RhelUtilsTest.createResChannel(user, "7", "x86_64", "res-x86_64");
        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT, Optional.empty());
        HibernateFactory.getSession().flush();
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", key)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=config(redhat-release-server),redhat-release,redhat-release-server,redhat-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7Server,\n")))));

                    allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                    will(returnValue(Optional.of(new RedhatProductInfo(
                            Optional.empty(),
                            Optional.of("Red Hat Enterprise Linux Server release 7.2 (Maipo)"),
                            Optional.empty()
                    ))));

                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setBaseChannel(resChannel_x86_64);
                    key.setOrg(user.getOrg());
                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());

                    assertNotNull(minion.getBaseChannel());
                    assertEquals("RES", minion.getBaseChannel().getProductName().getName());
                    assertEquals(resChannel_x86_64, minion.getBaseChannel());

                    assertEquals(1, minion.getChannels().size());

                    SUSEProductFactory.getSession().flush();
                    // select from view should succeed
                    SUSEProductFactory.getSession().createNativeQuery("select * from rhnServerOverview").list();

                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRESMinionWithoutActivationKey() throws Exception {
        Channel resChannel_x86_64 = RhelUtilsTest.createResChannel(user, "7", "x86_64", "res-x86_64");
        Channel resChannel_i386 = RhelUtilsTest.createResChannel(user, "7", "ia32", "res-i386");
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "res", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("sles_es-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" sles_es-release-server"));
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7Server,\n")))));

                    allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                    will(returnValue(Optional.of(new RedhatProductInfo(
                            Optional.empty(),
                            Optional.of("Red Hat Enterprise Linux Server release 7.2 (Maipo)\n" +
                                    "# This is a \"SLES Expanded Support platform Server release 7.2\"\n" +
                                    "# The above \"Red Hat Enterprise Linux \" string is only used to \n" +
                                    "# keep software compatibility."),

                            Optional.of("sles_es-release-server-7.2-9.el7.2.1.x86_64")
                    ))));
                }},
                null,
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());

                    // base channel check
                    assertNotNull(minion.getBaseChannel());
                    assertEquals("RES", minion.getBaseChannel().getProductName().getName());
                    assertEquals(resChannel_x86_64, minion.getBaseChannel());

                    assertEquals(1, minion.getChannels().size());
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRHELMinionWithMultipleReleasePackages() throws Exception {
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    // Minion returns multiple release packages/versions
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\nsles_es-release-server\nsles_es-release-server\nredhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    // Minion returns data for multiple release packages/versions
                    will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7Server,\n" +
                            "VERSION=7.3\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7Server,\n" +
                            "VERSION=7.2\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7Server,\n" +
                            "VERSION=7.3\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7.3-7.el7,7Server,\n")))));

                    allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                    will(returnValue(Optional.of(new RedhatProductInfo(
                            Optional.empty(),
                            Optional.of("Red Hat Enterprise Linux Server release 7.2 (Maipo)"),
                            Optional.empty()
                    ))));

                }},
                null,
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());
                    assertTrue(minion.getFqdns().isEmpty());

                    assertNull(minion.getBaseChannel());
                    assertEquals(0, minion.getChannels().size());
                }, DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test that registration of a minion with no activation key and no creator user will
     * put that minion in the default ("Satellite") organization
     *
     * @throws Exception if anything goes wrong
     */
    public void testRegisterSystemNoUser() throws Exception {
        executeTest(
                SLES_NO_AK_EXPECTATIONS,
                (cm) -> null,
                (minion, machineId, key) -> {
                    assertEquals(OrgFactory.getSatelliteOrg(), minion.get().getOrg());
                },
                DEFAULT_CONTACT_METHOD
        );
    }

    /**
     * Test that registration of a minion with no activation key and with a creator user (e.g.
     * user who accepts the salt key in the UI) will put that minion in the organization
     * of the creator user.
     *
     * @throws Exception if anything goes wrong
     */
    public void testRegisterSystemFromDifferentOrg() throws Exception {
        User creator = UserFactory.lookupById(UserTestUtils.createUser("chuck", "rangers"));
        MinionPendingRegistrationService.addMinion(creator, MINION_ID, ContactMethodUtil.DEFAULT, Optional.empty());

        executeTest(
                SLES_NO_AK_EXPECTATIONS,
                (cm) -> null,
                (minion, machineId, key) -> {
                    assertEquals(creator.getOrg(), minion.get().getOrg());
                },
                DEFAULT_CONTACT_METHOD
        );
    }

    /**
     * Test that registration of a minion with an activation key and a creator user (e.g.
     * user who accepts the salt key in the UI) will put that minion in the organization
     * of the activation key (the organization of the user is ignored in this case).
     *
     * @throws Exception if anything goes wrong
     */
    public void testRegisterSystemWithAKAndCreator() throws Exception {
        User creator = UserFactory.lookupById(UserTestUtils.createUser("chuck", "rangers"));
        MinionPendingRegistrationService.addMinion(creator, MINION_ID, ContactMethodUtil.DEFAULT, Optional.empty());

        executeTest(
                SLES_EXPECTATIONS,
                ACTIVATION_KEY_SUPPLIER,
                (minion, machineId, key) -> {
                    assertEquals(ActivationKeyFactory.lookupByKey(key).getOrg(), minion.get().getOrg());
                },
                DEFAULT_CONTACT_METHOD
        );
    }

    /**
     * Initial test of a registering a terminal machine
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailTerminal() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group",
                OrgFactory.getSatelliteOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group",
                OrgFactory.getSatelliteOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group",
                OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    // Notice product name has spaces in the string. It is intentional to test hw string preprocessing
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                            .map(map -> {
                                map.put("saltboot_initrd", true);
                                map.put("manufacturer", "QEMU");
                                map.put("productname", "Cash Desk 01");
                                map.put("minion_id_prefix", "Branch001");
                                return map;
                            })));
                    allowing(saltServiceMock).callSync(
                            with(any(LocalCall.class)),
                            with(any(String.class)));
                }},
                (contactMethod) -> null, // no AK
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertTrue(minion.getManagedGroups().contains(hwGroup));
                    assertTrue(minion.getManagedGroups().contains(terminalsGroup));
                    assertTrue(minion.getManagedGroups().contains(branchGroup));
                }, DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test registering a terminal machine when a required group is missing.
     * In this case we want the minion NOT to be registered.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailMinionTerminalGroupMissing() throws Exception {
        ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group", OrgFactory.getSatelliteOrg());
        ServerGroupFactory.create("Branch001", "Branch group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        try {
            executeTest(
                    (saltServiceMock, key) -> new Expectations() {{
                        allowing(saltServiceMock).getMasterHostname(MINION_ID);
                        will(returnValue(Optional.of(MINION_ID)));
                        allowing(saltServiceMock).getMachineId(MINION_ID);
                        will(returnValue(Optional.of(MACHINE_ID)));
                        allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                        will(returnValue(Optional.of(minionStartUpGrains)));
                        allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                        allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                        allowing(saltServiceMock).getGrains(MINION_ID);
                        will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                                .map(map -> {
                                    map.put("saltboot_initrd", true);
                                    map.put("manufacturer", "QEMU");
                                    map.put("productname", "CashDesk01");
                                    map.put("minion_id_prefix", "Branch001");
                                    return map;
                                })));
                        allowing(saltServiceMock).callSync(
                                with(any(LocalCall.class)),
                                with(any(String.class)));
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> {
                        assertFalse(optMinion.isPresent());
                    }, DEFAULT_CONTACT_METHOD);
        } catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            return;
        }
        fail("Expected Exception not thrown");
    }


    /**
     * Test registering a terminal machine when a required group is missing.
     * In this case we want the minion NOT to be registered.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailMinionBranchGroupMissing() throws Exception {
        ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group", OrgFactory.getSatelliteOrg());
        ServerGroupFactory.create("TERMINALS", "All terminals group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        try {
            executeTest(
                    (saltServiceMock, key) -> new Expectations() {{
                        allowing(saltServiceMock).getMasterHostname(MINION_ID);
                        will(returnValue(Optional.of(MINION_ID)));
                        allowing(saltServiceMock).getMachineId(MINION_ID);
                        will(returnValue(Optional.of(MACHINE_ID)));
                        allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                        will(returnValue(Optional.of(minionStartUpGrains)));
                        allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                        allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                        allowing(saltServiceMock).getGrains(MINION_ID);
                        will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                                .map(map -> {
                                    map.put("saltboot_initrd", true);
                                    map.put("manufacturer", "QEMU");
                                    map.put("productname", "CashDesk01");
                                    map.put("minion_id_prefix", "Branch001");
                                    return map;
                                })));
                        allowing(saltServiceMock).callSync(
                                with(any(LocalCall.class)),
                                with(any(String.class)));
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> {
                        assertFalse(optMinion.isPresent());
                    }, DEFAULT_CONTACT_METHOD);
        } catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            return;
        }
        fail("Expected Exception not thrown");
    }

    /**
     * Test registering a terminal machine when a non-required group (HW group) is missing
     * In this case we want the minion to be registered.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailMinionHwGroupMissing() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group", OrgFactory.getSatelliteOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                            .map(map -> {
                                map.put("saltboot_initrd", true);
                                map.put("manufacturer", "QEMU");
                                map.put("productname", "CashDesk01");
                                map.put("minion_id_prefix", "Branch001");
                                return map;
                            })));
                    allowing(saltServiceMock).callSync(
                            with(any(LocalCall.class)),
                            with(any(String.class)));
                }},
                (contactMethod) -> null, // no AK
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertTrue(minion.getManagedGroups().contains(terminalsGroup));
                    assertTrue(minion.getManagedGroups().contains(branchGroup));
                }, DEFAULT_CONTACT_METHOD);
    }

    /**
     * When an empty profile is assigned to a HW type group (prefixed with "HWTYPE:") before registration,
     * don't assign it to (another) HW type group on registration.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailMinionHwGroupAlreadyAssigned() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group", user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group", user.getOrg());
        ManagedServerGroup hwGroupMatching = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group", user.getOrg());
        ManagedServerGroup alreadyAssignedGroup = ServerGroupFactory.create("HWTYPE:idontmatch",
                "HW group - assigned to empty profile beforehand", user.getOrg());

        MinionServer emptyMinion = SystemManager.createSystemProfile(user, "empty profile",
                singletonMap("hwAddress", "00:11:22:33:44:55"));
        ServerFactory.addServerToGroup(emptyMinion, alreadyAssignedGroup);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                            .map(map -> {
                                map.put("saltboot_initrd", true);
                                map.put("manufacturer", "QEMU");
                                map.put("productname", "CashDesk01");
                                map.put("minion_id_prefix", "Branch001");
                                Map<String, String> interfaces = new HashMap<>();
                                interfaces.put("eth1", "00:11:22:33:44:55");
                                map.put("hwaddr_interfaces", interfaces);
                                return map;
                            })));
                    allowing(saltServiceMock).callSync(
                            with(any(LocalCall.class)),
                            with(any(String.class)));
                }},
                (contactMethod) -> null, // no AK
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    // minion will stay assigned to its original HW type group after registration
                    assertTrue(minion.getManagedGroups().contains(alreadyAssignedGroup));
                    assertTrue(minion.getManagedGroups().contains(terminalsGroup));
                    assertTrue(minion.getManagedGroups().contains(branchGroup));
                    // minion won't be assigned to its matching HW type group in this case
                    // (it's already assigned to the HW type group above)
                    assertFalse(minion.getManagedGroups().contains(hwGroupMatching));
                },
                null,
                DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test registering retail machine in a specific org
     *
     * @throws Exception - if anything goes wrong
     */
    public void testRegisterRetailTerminalNonDefaultOrg() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group",
                user.getOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group",
                user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group",
                user.getOrg());

        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT, Optional.empty());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();

        try {
            executeTest(
                    (saltServiceMock, key) -> new Expectations() {{
                        allowing(saltServiceMock).getMasterHostname(MINION_ID);
                        will(returnValue(Optional.of(MINION_ID)));
                        allowing(saltServiceMock).getMachineId(MINION_ID);
                        will(returnValue(Optional.of(MACHINE_ID)));
                        allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                        will(returnValue(Optional.of(minionStartUpGrains)));
                        allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                        allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                        allowing(saltServiceMock).getGrains(MINION_ID);
                        will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                                .map(map -> {
                                    map.put("saltboot_initrd", true);
                                    map.put("manufacturer", "QEMU");
                                    map.put("productname", "CashDesk01");
                                    map.put("minion_id_prefix", "Branch001");
                                    return map;
                                })));
                        allowing(saltServiceMock).callSync(
                                with(any(LocalCall.class)),
                                with(any(String.class)));
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> {
                        assertTrue(optMinion.isPresent());
                        MinionServer minion = optMinion.get();
                        assertTrue(minion.getManagedGroups().contains(hwGroup));
                        assertTrue(minion.getManagedGroups().contains(terminalsGroup));
                        assertTrue(minion.getManagedGroups().contains(branchGroup));
                    }, DEFAULT_CONTACT_METHOD);
        } finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Tests registration of an empty profile
     * @throws Exception if anything goes wrong
     */
    public void testEmptyProfileRegistration() throws Exception {
        MinionServer emptyMinion = SystemManager.createSystemProfile(user, "empty profile",
               singletonMap("hwAddress", "00:11:22:33:44:55"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                            .map(map -> {
                                map.put("saltboot_initrd", false);
                                map.put("manufacturer", "QEMU");
                                map.put("productname", "CashDesk02");
                                map.put("minion_id_prefix", "Branch001");
                                Map<String, String> interfaces = new HashMap<>();
                                interfaces.put("eth1", "00:11:22:33:44:55");
                                map.put("hwaddr_interfaces", interfaces);
                                return map;
                            })));
                    allowing(saltServiceMock).callSync(
                            with(any(LocalCall.class)),
                            with(any(String.class)));
                }},
                (contactMethod) -> null, // no AK
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    Set<String> expectedEntitlements = Collections.singleton(EntitlementManager.SALT_ENTITLED);
                    assertEquals(
                            expectedEntitlements,
                            minion.getEntitlements().stream()
                                    .map(e -> e.getLabel())
                                    .collect(Collectors.toSet()));
                    assertEquals(emptyMinion.getId(), minion.getId());
                    assertEquals(MINION_ID, minion.getMinionId());
                    assertEquals(MACHINE_ID, minion.getMachineId());
                    assertEquals(MACHINE_ID, minion.getDigitalServerId());
                    HibernateFactory.getSession().refresh(minion); // refresh minions to populate network interfaces
                    HibernateFactory.getSession().refresh(emptyMinion);
                    assertEquals(emptyMinion.getNetworkInterfaces(), minion.getNetworkInterfaces());
                },
                null,
                DEFAULT_CONTACT_METHOD);
    }


    /**
     * Tests migration of formula assignment and data for empty profile during registration
     * @throws Exception if anything goes wrong
     */
    public void testMigrateFormulaDataForEmptyProfile() throws Exception {
        final String testFormula = "testFormula";
        final String hwAddress = "00:11:22:33:44:55";

        // assign some formula
        MinionServer emptyMinion = SystemManager.createSystemProfile(user, "empty profile",
                singletonMap("hwAddress", "00:11:22:33:44:55"));
        String minionId = "_" + hwAddress;
        FormulaFactory.saveServerFormulas(minionId, Collections.singletonList(testFormula));
        Map<String, Object> formulaContent = singletonMap("testKey", "testVal");
        FormulaFactory.saveServerFormulaData(formulaContent, minionId, testFormula);

        assertTrue(Paths.get(FormulaFactory.getPillarDir(), "_" + hwAddress + "_" + testFormula + ".json").toFile().exists());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, "non-existent-key")
                            .map(map -> {
                                map.put("saltboot_initrd", false);
                                map.put("manufacturer", "QEMU");
                                map.put("productname", "CashDesk02");
                                map.put("minion_id_prefix", "Branch001");
                                Map<String, String> interfaces = new HashMap<>();
                                interfaces.put("eth1", hwAddress);
                                map.put("hwaddr_interfaces", interfaces);
                                return map;
                            })));
                    allowing(saltServiceMock).callSync(
                            with(any(LocalCall.class)),
                            with(any(String.class)));
                }},
                (contactMethod) -> null, // no AK
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    Set<String> expectedEntitlements = Collections.singleton(EntitlementManager.SALT_ENTITLED);
                    assertEquals(
                            expectedEntitlements,
                            minion.getEntitlements().stream()
                                    .map(e -> e.getLabel())
                                    .collect(Collectors.toSet()));
                    assertEquals(emptyMinion.getId(), minion.getId());
                    assertEquals(MINION_ID, minion.getMinionId());
                    assertEquals(MACHINE_ID, minion.getMachineId());
                    assertEquals(MACHINE_ID, minion.getDigitalServerId());
                    HibernateFactory.getSession().refresh(minion); // refresh minions to populate network interfaces
                    HibernateFactory.getSession().refresh(emptyMinion);
                    assertEquals(emptyMinion.getNetworkInterfaces(), minion.getNetworkInterfaces());

                    assertTrue(Paths.get(FormulaFactory.getPillarDir(), MINION_ID + "_" + testFormula + ".json").toFile().exists());
                    assertFalse(Paths.get(FormulaFactory.getPillarDir(), hwAddress + "_" + testFormula + ".json").toFile().exists());
                },
                null,
                DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test that a traditional -> salt migration respects the channels from the Activation Key (and overrides
     * the channels that have been assigned to the system)
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testMigrationSystemWithChannelsAndAK() throws Exception {
        Channel akBaseChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel akChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        akChildChannel.setParentChannel(akBaseChannel);
        TestUtils.saveAndFlush(akChildChannel);

        Channel assignedChannel = ChannelTestUtils.createBaseChannel(user);
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        server.addChannel(assignedChannel);
        ServerFactory.save(server);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {
            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
            }
        }, (contactMethod) -> {
            ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
            key.setBaseChannel(akBaseChannel);
            key.addChannel(akChildChannel);
            key.setOrg(user.getOrg());
            ActivationKeyFactory.save(key);
            return key.getKey();
        }, (optMinion, machineId, key) -> {
            assertTrue(optMinion.isPresent());
            MinionServer minion = optMinion.get();
            assertEquals(MINION_ID, minion.getName());

            assertNotNull(minion.getBaseChannel());
            assertEquals(akBaseChannel, minion.getBaseChannel());

            HashSet<Channel> channels = new HashSet<>();
            channels.add(akBaseChannel);
            channels.add(akChildChannel);
            assertEquals(channels, minion.getChannels());
            assertTrue(minion.getFqdns().isEmpty());
        }, DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test that a traditional -> salt migration respects the channels from the Activation Key (and overrides
     * the channels that have been assigned to the system)
     * In this case, the already assigned base channel was same the one in the AK, but the child channels differ.
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testMigrationSystemWithChannelsAndAKSameBase() throws Exception {
        Channel akBaseChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel akChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        akChildChannel.setParentChannel(akBaseChannel);
        TestUtils.saveAndFlush(akChildChannel);
        Channel assignedChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        assignedChildChannel.setParentChannel(akBaseChannel);
        TestUtils.saveAndFlush(assignedChildChannel);

        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        server.addChannel(akBaseChannel);
        server.addChannel(assignedChildChannel);
        ServerFactory.save(server);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {
            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
            }
        }, (contactMethod) -> {
            ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
            key.setBaseChannel(akBaseChannel);
            key.addChannel(akChildChannel);
            key.setOrg(user.getOrg());
            ActivationKeyFactory.save(key);
            return key.getKey();
        }, (optMinion, machineId, key) -> {
            assertTrue(optMinion.isPresent());
            MinionServer minion = optMinion.get();
            assertEquals(MINION_ID, minion.getName());

            assertNotNull(minion.getBaseChannel());
            assertEquals(akBaseChannel, minion.getBaseChannel());

            HashSet<Channel> channels = new HashSet<>();
            channels.add(akBaseChannel);
            channels.add(akChildChannel);
            assertEquals(channels, minion.getChannels());
            assertTrue(minion.getFqdns().isEmpty());
        }, DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test that a traditional -> salt migration preserves assigned channels when no AK is used
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testMigrationSystemWithChannelsNoAK() throws Exception {
        Channel assignedChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel assignedChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        assignedChildChannel.setParentChannel(assignedChannel);
        TestUtils.saveAndFlush(assignedChildChannel);

        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        server.getChannels().clear();
        server.addChannel(assignedChannel);
        server.addChannel(assignedChildChannel);
        ServerFactory.save(server);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest((saltServiceMock, key) -> new Expectations() {
            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                will(returnValue(Optional.of(minionStartUpGrains)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
            }
        }, (contactMethod) -> null,
        (optMinion, machineId, key) -> {
            assertTrue(optMinion.isPresent());
            MinionServer minion = optMinion.get();
            assertEquals(MINION_ID, minion.getName());

            assertNotNull(minion.getBaseChannel());
            assertEquals(assignedChannel, minion.getBaseChannel());

            HashSet<Channel> channels = new HashSet<>();
            channels.add(assignedChannel);
            channels.add(assignedChildChannel);
            assertEquals(channels, minion.getChannels());
            assertTrue(minion.getFqdns().isEmpty());
        }, DEFAULT_CONTACT_METHOD);
    }

    public void testMigrationMinionWithReActivationKey() throws Exception {
        Channel assignedChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel assignedChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        assignedChildChannel.setParentChannel(assignedChannel);
        TestUtils.saveAndFlush(assignedChildChannel);

        MinionServer oldMinion = MinionServerFactoryTest.createTestMinionServer(user);
        oldMinion.addChannel(assignedChannel);
        oldMinion.addChannel(assignedChildChannel);
        ServerFactory.save(oldMinion);

        ChannelFamily channelFamily = createTestChannelFamily();
        HibernateFactory.getSession().flush();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                            .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                            .createMinionStartUpGrains();
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, null, key)));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    // setting a server makes it a re-activation key
                    key.setServer(oldMinion);
                    key.setOrg(user.getOrg());
                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    assertNotNull(minion.getBaseChannel());

                    assertTrue(minion.getFqdns().isEmpty());

                    // State assignment file check
                    Path slsPath = tmpSaltRoot.resolve("custom").resolve("custom_" + minion.getMachineId() + ".sls");
                    assertTrue(slsPath.toFile().exists());
                },
                cleanup -> {},
                DEFAULT_CONTACT_METHOD);
    }

    public void testMinionWithUsedReActivationKey() throws Exception {
        Channel assignedChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel assignedChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        assignedChildChannel.setParentChannel(assignedChannel);

        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        MinionServer oldMinion = MinionServerFactoryTest.createTestMinionServer(user);
        oldMinion.setMachineId(MACHINE_ID);
        oldMinion.setMinionId(MINION_ID);
        oldMinion.setHostname(MINION_ID);
        oldMinion.setName(MINION_ID);
        oldMinion.getChannels().clear();
        oldMinion.addChannel(assignedChannel);
        oldMinion.addChannel(assignedChildChannel);
        ServerFactory.save(oldMinion);
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                           .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                           .createMinionStartUpGrains();
                    allowing(saltServiceMock).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, null, key)));
                }},
                (contactMethod) -> {
                    return "1-re-already-used";
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    assertNotNull(minion.getBaseChannel());
                },
                cleanup -> {},
                DEFAULT_CONTACT_METHOD);
    }

    public void testMinionWithUsedReActivationKeyWithStartUpGrains() throws Exception {
        Channel assignedChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        Channel assignedChildChannel = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        assignedChildChannel.setParentChannel(assignedChannel);

        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        MinionServer oldMinion = MinionServerFactoryTest.createTestMinionServer(user);
        oldMinion.setMachineId(MACHINE_ID);
        oldMinion.setMinionId(MINION_ID);
        oldMinion.setHostname(MINION_ID);
        oldMinion.setName(MINION_ID);
        oldMinion.getChannels().clear();
        oldMinion.addChannel(assignedChannel);
        oldMinion.addChannel(assignedChildChannel);
        ServerFactory.save(oldMinion);
        MinionStartupGrains.SuseManagerGrain suseManagerGrain = new MinionStartupGrains.SuseManagerGrain(Optional.of("1-re-already-used"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                .createMinionStartUpGrains();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, null, key)));
                }},
                (contactMethod) -> {
                    return "1-re-already-used";
                },
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    assertNotNull(minion.getBaseChannel());
                },
                cleanup -> {},
                DEFAULT_CONTACT_METHOD,
                Optional.of(minionStartUpGrains));
    }

    private Channel setupBaseAndRequiredChannels(ChannelFamily channelFamily,
            SUSEProduct product)
        throws Exception {
        ChannelProduct channelProduct = createTestChannelProduct();
        ChannelArch channelArch = ChannelFactory.findArchByLabel("channel-x86_64");
        Channel baseChannelX8664 = DistUpgradeManagerTest
                .createTestBaseChannel(channelFamily, channelProduct, channelArch);
        SUSEProductTestUtils.createTestSUSEProductChannel(baseChannelX8664, product, true);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        Channel channel3 = ChannelFactoryTest.createTestChannel(user, "channel-x86_64");
        channel2.setChannelArch(channelArch);
        channel3.setChannelArch(channelArch);
        channel2.setParentChannel(baseChannelX8664);
        channel3.setParentChannel(baseChannelX8664);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel2, product, true);
        SUSEProductTestUtils.createTestSUSEProductChannel(channel3, product, true);
        return baseChannelX8664;
    }

    @SuppressWarnings("unchecked")
    private SaltService setupStubs(SUSEProduct product)
        throws ClassNotFoundException, IOException {
        SaltService saltService = mock(SaltService.class);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        context().checking(new Expectations() { {
            allowing(saltService).getMasterHostname(MINION_ID);
            will(returnValue(Optional.of(MINION_ID)));
            allowing(saltService).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));
            allowing(saltService).getGrains(with(any(String.class)), with(any(TypeToken.class)),with(any(String[].class)));
            will(returnValue(Optional.of(minionStartUpGrains)));;
            allowing(saltService).getGrains(MINION_ID);
            will(returnValue(getGrains(MINION_ID, null, "foo")));
            allowing(saltService).syncGrains(with(any(MinionList.class)));
            allowing(saltService).syncModules(with(any(MinionList.class)));
            List<ProductInfo> pil = new ArrayList<>();
            ProductInfo pi = new ProductInfo(
                        product.getName(),
                        product.getArch().getLabel(), "descr", "eol", "epoch", "flavor",
                        true, true, "productline", Optional.of("registerrelease"),
                        "test", "repo", "shortname", "summary", "vendor",
                        product.getVersion());
            pil.add(pi);
            allowing(saltService).callSync(
                     with(any(LocalCall.class)),
                     with(any(String.class)));
            will(returnValue(Optional.of(pil)));
            try {
                allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            } catch (TaskomaticApiException e) {
                e.printStackTrace();
            }
        } });

        RegisterMinionEventMessageAction action =
                new RegisterMinionEventMessageAction(saltService);
        action.execute(new RegisterMinionEventMessage(MINION_ID, Optional.empty()));
        return saltService;
    }

    private Optional<Map<String, Object>> getGrains(String minionId, String sufix, String akey)
            throws ClassNotFoundException, IOException {
        return getGrains(minionId, sufix, akey, null);
    }

    private Optional<Map<String, Object>> getGrains(String minionId, String sufix, String akey, String mkey)
            throws ClassNotFoundException, IOException {
        Map<String, Object> grains = new JsonParser<>(Grains.items(false).getReturnType()).parse(
                readFile("dummy_grains" + (sufix != null ? "_" + sufix : "") + ".json"));
        Map<String, String> susemanager = new HashMap<>();
        if (akey != null) {
            susemanager.put("activation_key", akey);
        }
        if (mkey != null) {
            susemanager.put("management_key", mkey);
        }
        grains.put("susemanager", susemanager);
        return Optional.of(grains);
    }

    private String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }

}
