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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
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

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
                if (key != null) {
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, key)));
                }
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
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));

                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).callSync(
                        with(any(LocalCall.class)),
                        with(any(String.class)));
                will(returnValue(Optional.empty()));
            }};

    private ActivationKeySupplier ACTIVATION_KEY_SUPPLIER = (contactMethod) -> {
        Channel baseChannel = ChannelFactoryTest.createBaseChannel(user);
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
        action.execute(new RegisterMinionEventMessage(MINION_ID));

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
        //SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

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

        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
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
                    allowing(saltServiceMock).callSync(
                             with(any(LocalCall.class)),
                             with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (DEFAULT_CONTACT_METHOD) -> {
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

        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
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
                    allowing(saltServiceMock).callSync(
                             with(any(LocalCall.class)),
                             with(any(String.class)));
                    will(returnValue(Optional.of(pil)));
                }},
                (DEFAULT_CONTACT_METHOD) -> {
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
                }, DEFAULT_CONTACT_METHOD);
    }


    @SuppressWarnings("unchecked")
    public void testRegisterMinionWithInvalidActivationKeyNoSyncProducts() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);

        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
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
                (DEFAULT_CONTACT_METHOD) -> {
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

        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
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
                (DEFAULT_CONTACT_METHOD) -> {
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

        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, null, key)));
                }},
                (DEFAULT_CONTACT_METHOD) -> {
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
                    assertEquals(channels, minion.getChannels());
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

        executeTest((saltServiceMock, key) -> new Expectations() {

            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
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
                allowing(saltServiceMock).callSync(with(any(LocalCall.class)),
                        with(any(String.class)));
                will(returnValue(Optional.of(pil)));
            }
        }, (DEFAULT_CONTACT_METHOD) -> {
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
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=config(redhat-release-server),redhat-release,redhat-release-server,redhat-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7Server,\n")))));

                    allowing(saltServiceMock).applyState(MINION_ID, "packages.redhatproductinfo");
                    will(returnValue(Optional.of(new JsonParser<>(State.apply(Collections.emptyList()).getReturnType()).parse(
                            readFile("dummy_packages_redhatprodinfo_rhel.json")))));

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

    public void testRegisterRHELMinionWithRESActivationKey() throws Exception {
        Channel resChannel = RhelUtilsTest.createResChannel(user, "7");
        HibernateFactory.getSession().flush();
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", key)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=config(redhat-release-server),redhat-release,redhat-release-server,redhat-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7.2-9.el7,7Server,\n")))));

                    allowing(saltServiceMock).applyState(MINION_ID, "packages.redhatproductinfo");
                    will(returnValue(Optional.of(new JsonParser<>(State.apply(Collections.emptyList()).getReturnType()).parse(
                            readFile("dummy_packages_redhatprodinfo_rhel.json")))));

                }},
                (DEFAULT_CONTACT_METHOD) -> {
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
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRESMinionWithoutActivationKey() throws Exception {
        RhelUtilsTest.createResChannel(user, "7");
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "res", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("sles_es-release-server")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" sles_es-release-server"));
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
                            "PROVIDENAME=centos-release,config(sles_es-release-server),redhat-release,redhat-release-server,sles_es-release-server,sles_es-release-server(x86-64),system-release,system-release(releasever),\n" +
                            "PROVIDEVERSION=,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7.2-9.el7.2.1,7Server,\n")))));

                    allowing(saltServiceMock).applyState(MINION_ID, "packages.redhatproductinfo");
                    will(returnValue(Optional.of(new JsonParser<>(State.apply(Collections.emptyList()).getReturnType()).parse(
                            readFile("dummy_packages_redhatprodinfo_res.json")))));
                }},
                null,
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals("7Server", minion.getRelease());

                    // base channel check
                    assertNotNull(minion.getBaseChannel());
                    assertEquals("RES", minion.getBaseChannel().getProductName().getName());
                }, DEFAULT_CONTACT_METHOD);
    }

    public void testRegisterRHELMinionWithMultipleReleasePackages() throws Exception {
        executeTest(
                (saltServiceMock, key) -> new Expectations() {{
                    allowing(saltServiceMock).getMasterHostname(MINION_ID);
                    will(returnValue(Optional.of(MINION_ID)));
                    allowing(saltServiceMock).getMachineId(MINION_ID);
                    will(returnValue(Optional.of(MACHINE_ID)));
                    allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                    allowing(saltServiceMock).syncModules(with(any(MinionList.class)));

                    allowing(saltServiceMock).getGrains(MINION_ID);
                    will(returnValue(getGrains(MINION_ID, "rhel", null)));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                    // Minion returns multiple release packages/versions
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("redhat-release-server\nsles_es-release-server\nsles_es-release-server\nredhat-release-server\n")))));

                    allowing(saltServiceMock).runRemoteCommand(with(any(MinionList.class)), with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\nPROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" redhat-release-server"));
                    // Minion returns data for multiple release packages/versions
                    will(returnValue(Collections.singletonMap(MINION_ID, new Result<>(Xor.right("VERSION=7.2\n" +
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

                    allowing(saltServiceMock).applyState(MINION_ID, "packages.redhatproductinfo");
                    will(returnValue(Optional.of(new JsonParser<>(State.apply(Collections.emptyList()).getReturnType()).parse(
                            readFile("dummy_packages_redhatprodinfo_rhel.json")))));

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
     * Test that a traditional -> salt migration respects the channels from the Activation Key (and overrides
     * the channels that have been assigned to the system)
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testMigrationSystemWithChannelsAndAK() throws Exception {
        Channel akBaseChannel = ChannelTestUtils.createBaseChannel(user);
        Channel akChildChannel = ChannelTestUtils.createChildChannel(user, akBaseChannel);

        Channel assignedChannel = ChannelTestUtils.createBaseChannel(user);
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        server.addChannel(assignedChannel);
        ServerFactory.save(server);

        executeTest((saltServiceMock, key) -> new Expectations() {
            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
            }
        }, (DEFAULT_CONTACT_METHOD) -> {
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
        Channel assignedChannel = ChannelTestUtils.createBaseChannel(user);
        Channel assignedChildChannel = ChannelTestUtils.createChildChannel(user, assignedChannel);
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        server.getChannels().clear();
        server.addChannel(assignedChannel);
        server.addChannel(assignedChildChannel);
        ServerFactory.save(server);

        executeTest((saltServiceMock, key) -> new Expectations() {
            {
                allowing(saltServiceMock).getMasterHostname(MINION_ID);
                will(returnValue(Optional.of(MINION_ID)));
                allowing(saltServiceMock).getMachineId(MINION_ID);
                will(returnValue(Optional.of(MACHINE_ID)));
                allowing(saltServiceMock).syncGrains(with(any(MinionList.class)));
                allowing(saltServiceMock).syncModules(with(any(MinionList.class)));
                allowing(saltServiceMock).getGrains(MINION_ID);
                will(returnValue(getGrains(MINION_ID, null, key)));
            }
        }, (DEFAULT_CONTACT_METHOD) -> null,
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

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        context().checking(new Expectations() { {
            allowing(saltService).getMasterHostname(MINION_ID);
            will(returnValue(Optional.of(MINION_ID)));
            allowing(saltService).getMachineId(MINION_ID);
            will(returnValue(Optional.of(MACHINE_ID)));
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
        action.execute(new RegisterMinionEventMessage(MINION_ID));
        return saltService;
    }

    private Optional<Map<String, Object>> getGrains(String minionId, String sufix, String akey) throws ClassNotFoundException, IOException {
        Map<String, Object> grains = new JsonParser<>(Grains.items(false).getReturnType()).parse(
                readFile("dummy_grains" + (sufix != null ? "_" + sufix : "") + ".json"));
        Map<String, String> susemanager = new HashMap<>();
        if (akey != null) {
            susemanager.put("activation_key", akey);
            grains.put("susemanager", susemanager);
        }
        return Optional.of(grains);
    }

    private String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }

}
