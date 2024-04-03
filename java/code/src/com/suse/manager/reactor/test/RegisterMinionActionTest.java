/*
 * Copyright (c) 2015--2021 SUSE LLC
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
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerPath;
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

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessage;
import com.suse.manager.reactor.messaging.RegisterMinionEventMessageAction;
import com.suse.manager.reactor.utils.test.RhelUtilsTest;
import com.suse.manager.webui.controllers.channels.ChannelsUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.salt.custom.MinionStartupGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests for {@link RegisterMinionEventMessageAction}.
 */
public class RegisterMinionActionTest extends JMockBaseTestCaseWithUser {

    private static final String MINION_ID = "suma3pg.vagrant.local";
    private static final String MACHINE_ID = "003f13081ddd408684503111e066f921";
    private static final String SSH_PUSH_CONTACT_METHOD = "ssh-push";
    private static final String DEFAULT_CONTACT_METHOD = "default";

    private static final MinionStartupGrains DEFAULT_MINION_START_UP_GRAINS =
                         new MinionStartupGrains.MinionStartupGrainsBuilder()
                             .machineId(MACHINE_ID).saltbootInitrd(false)
                             .createMinionStartUpGrains();

    private Path metadataDirOfficial;
    private SaltService saltServiceMock;
    private SystemManager systemManager;
    private CloudPaygManager cloudManager4Test;

    @FunctionalInterface
    private interface ExpectationsFunction {

        Expectations apply(String key) throws Exception;

    }

    @FunctionalInterface
    private interface ActivationKeySupplier {

        String get(String contactMethod) throws Exception;
    }

    @FunctionalInterface
    private interface Assertions {

        void accept(Optional<MinionServer> minion, String machineId, String key) throws IOException;

    }

    private ExpectationsFunction SLES_EXPECTATIONS = (key) ->
            new Expectations() {{
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
                allowing(saltServiceMock).removeSaltSSHKnownHost(with(any(String.class)));
                will(returnValue(Optional.of(new MgrUtilRunner.RemoveKnowHostResult("removed", ""))));
            }};

    private ExpectationsFunction SLES_EXPECTATIONS_NO_STARTUPGRAINS = (key) ->
            new Expectations() {{
                allowing(saltServiceMock)
                        .getGrains(with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                will(returnValue(Optional.of(DEFAULT_MINION_START_UP_GRAINS)));
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
                allowing(saltServiceMock).removeSaltSSHKnownHost(with(any(String.class)));
                will(returnValue(Optional.of(new MgrUtilRunner.RemoveKnowHostResult("removed", ""))));
            }};

    private ExpectationsFunction SLES_EXPECTATIONS_ALREADY_REGISTERED = (key) ->
            new Expectations() {{
                allowing(saltServiceMock).updateSystemInfo(with(any(MinionList.class)));
            }};

    @SuppressWarnings("unchecked")
    private final ExpectationsFunction SLES_NO_AK_EXPECTATIONS = (key) ->
            new Expectations() {{
                allowing(saltServiceMock)
                        .getGrains(with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                will(returnValue(Optional.of(DEFAULT_MINION_START_UP_GRAINS)));
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
                allowing(saltServiceMock).getProducts(with(any(String.class)));
                will(returnValue(Optional.empty()));
            }};

    private ActivationKeySupplier ACTIVATION_KEY_SUPPLIER = (contactMethod) -> {
        Channel baseChannel = ChannelFactoryTest.createBaseChannel(user, "channel-x86_64");
        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
        key.setBaseChannel(baseChannel);
        key.setOrg(user.getOrg());
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
            assertTrue(ServerGroupFactory.listServers(testGroup).contains(minion),
                    "Server should have the testGroup ServerGroup");
            assertEquals(keyObj.getOrg(), minion.getOrg());
            Optional<Server> server = keyObj.getToken().getActivatedServers().stream()
                    .findFirst()
                    .filter(minion::equals);
            assertTrue(server.isPresent(), "Server should be a activated system on the activation key");
        }
    };

    private Consumer<Void> CLEANUP = (arg) -> MinionServerFactory.findByMachineId(MACHINE_ID)
            .ifPresent(ServerFactory::delete);

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        Config.get().setString("server.secret_key", "d8d796b3322d65928511769d180d284d2b15158165eb83083efa02c9024aa6cc");

       metadataDirOfficial = Files.createTempDirectory("meta");
       FormulaFactory.setMetadataDirOfficial(metadataDirOfficial.toString());
       Path testFormulaDir = metadataDirOfficial.resolve("testFormula");
       Files.createDirectories(testFormulaDir);
       Path testFormulaFile = Paths.get(testFormulaDir.toString(), "form.yml");
       Files.createFile(testFormulaFile);

       saltServiceMock = mock(SaltService.class);
       systemManager = new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltServiceMock);
       cloudManager4Test = new CloudPaygManager() {
           @Override
           public boolean isPaygInstance() {
               return false;
           }
       };

       context().checking(new Expectations() {{
           allowing(saltServiceMock).refreshPillar(with(any(MinionList.class)));
       }});
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(metadataDirOfficial.toFile());
    }

    /**
     * Test the minion registration.
     *
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    @Test
    public void testDoExecute() throws Exception {
        executeTest(
                SLES_EXPECTATIONS,
                ACTIVATION_KEY_SUPPLIER,
                SLES_ASSERTIONS,
                DEFAULT_CONTACT_METHOD);
    }
    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier,
                            Assertions assertions, String contactMethod) throws Exception {
        executeTest(expectations, keySupplier, assertions, CLEANUP, contactMethod);
    }

    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier,
                            Assertions assertions, Consumer<Void> cleanup, String contactMethod) throws Exception {
        executeTest(expectations, keySupplier, assertions, cleanup, contactMethod,
                            Optional.of(DEFAULT_MINION_START_UP_GRAINS));
    }

    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier,
                            Assertions assertions, Consumer<Void> cleanup, String contactMethod,
                            Optional<MinionStartupGrains> startupGrains) throws Exception {
        executeTest(expectations, keySupplier, assertions, cleanup, contactMethod, startupGrains, MACHINE_ID);
    }

    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier,
                            Assertions assertions, String contactMethod,
                            Optional<MinionStartupGrains> startupGrains) throws Exception {
        executeTest(expectations, keySupplier, assertions, CLEANUP, contactMethod, startupGrains, MACHINE_ID);
    }


    public void executeTest(ExpectationsFunction expectations, ActivationKeySupplier keySupplier,
                            Assertions assertions, Consumer<Void> cleanup, String contactMethod,
                            Optional<MinionStartupGrains> startupGrains, String machineId) throws Exception {
        // cleanup
        if (cleanup != null) {
            cleanup.accept(null);
        }

        String key = keySupplier != null ? keySupplier.get(contactMethod) : null;

        // Register a minion via RegisterMinionAction and mocked SaltService
        if (expectations != null) {
            Expectations exp = expectations.apply(key);
            context().checking(exp);

            TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
            ActionManager.setTaskomaticApi(taskomaticMock);

            context().checking(new Expectations() {{
                try {
                    allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
                }
                catch (TaskomaticApiException e) {
                    e.printStackTrace();
                }
            }});
        }

        RegisterMinionEventMessageAction action = new RegisterMinionEventMessageAction(saltServiceMock,
                saltServiceMock, cloudManager4Test);
        action.execute(new RegisterMinionEventMessage(MINION_ID, startupGrains));

        // Verify the resulting system entry
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(machineId);

        if (assertions != null) {
            assertions.accept(optMinion, machineId, key);
        }
    }

    @Test
    public void testReRegisterTraditionalAsMinion() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        Server server = ServerTestUtils.createTestSystem(user);
        server.setMachineId(MACHINE_ID);
        ServerFactory.save(server);
        SystemManager.giveCapability(server.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);
        SystemManager.lockServer(user, server, "manually locked");

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
            history.sort(Comparator.comparing(ServerHistoryEvent::getCreated));
            assertEquals(history.get(history.size() - 1).getSummary(), "Server reactivated as Salt minion");
            assertNull(minion.getLock());
        }, DEFAULT_CONTACT_METHOD);
    }

    /*
     * Test register a new system re-using existing minion id.
     * Case 2.1 : As somebody accepted the key by deleting the former key and accept the new
     * We migrate the existing system and change the machine id.
     */
    @Test
    public void testRegisterDuplicateMinionId() throws Exception {
        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT);
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        server.setHostname(MINION_ID);
        try {
            executeTest(SLES_EXPECTATIONS_ALREADY_REGISTERED, ACTIVATION_KEY_SUPPLIER, (minion, machineId, key) -> {
                assertTrue(MinionServerFactory.findByMachineId(MACHINE_ID).isPresent());
                MinionServerFactory.findByMachineId(MACHINE_ID).ifPresentOrElse(
                        m -> {
                            assertEquals(m.getCreated(), server.getCreated());
                            assertEquals(m.getId(), server.getId());
                        },
                        () -> fail("Machine ID not found"));
            }, null, DEFAULT_CONTACT_METHOD);
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /*
     * Test register a system where minion id and machine id are both known.
     * Case 2.2 - moved machine
     */
    @Test
    public void testAlreadyRegisteredMinionWithSameMachineId() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        server.setMachineId(MACHINE_ID);
        executeTest(SLES_EXPECTATIONS_ALREADY_REGISTERED, ACTIVATION_KEY_SUPPLIER,
                (minion, machineId, key) -> MinionServerFactory.findByMachineId(MACHINE_ID).ifPresentOrElse(
                m -> {
                    assertEquals(m.getCreated(), server.getCreated());
                    assertEquals(m.getId(), server.getId());
                },
                () -> fail("Machine ID not found")), null, DEFAULT_CONTACT_METHOD);
    }

    /*
     * Test register a system where minion id and machine id are both known.
     * Case 2.2 - fail with RegisterMinionException
     */
    @Test
    public void testAlreadyRegisteredMinionWithSameMachineId2() throws Exception {
        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT);
        MinionServer server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setMinionId(MINION_ID);
        server1.setHostname(MINION_ID);
        MinionServer server2 = MinionServerFactoryTest.createTestMinionServer(user);
        server2.setMachineId(MACHINE_ID);
        server2.setHostname(server2.getName());
        try {
            executeTest((key) -> new Expectations() {{
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
                allowing(saltServiceMock).removeSaltSSHKnownHost(with(any(String.class)));
                will(returnValue(Optional.of(new MgrUtilRunner.RemoveKnowHostResult("removed", ""))));
                allowing(saltServiceMock).deleteKey(server2.getMinionId());
            }}, ACTIVATION_KEY_SUPPLIER, (optMinion, machineId, key) -> assertFalse(optMinion.isPresent()),
                    null, DEFAULT_CONTACT_METHOD);
        }
        catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            assertContains(e.getMessage(), "Systems with conflicting minion ID and machine ID were found");
            assertContains(e.getMessage(), "Please remove conflicting systems first (" +
                    server1.getId() + ", " + server2.getId() + ")");
            return;
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
        fail("Expected Exception not thrown");
    }

    /*
     * Test register a new system with a new minion id, where the machine id is
     * already used by a different system (Case 1.2)
     */
    @Test
    public void testAlreadyRegisteredMinionWithNewMinionId() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMachineId(MACHINE_ID);
        executeTest((key) -> new Expectations() {{
            allowing(saltServiceMock).updateSystemInfo(with(any(MinionList.class)));
            exactly(1).of(saltServiceMock).deleteKey(server.getMinionId());
        }}, null, (minion, machineId, key) -> assertTrue(MinionServerFactory.findByMinionId(MINION_ID).isPresent()),
                null, DEFAULT_CONTACT_METHOD);
    }

    @Test
    public void testWithMissingMachineIdStartUpGrains() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId(MINION_ID);
        executeTest((key) -> new Expectations(),
                null,
                (minion, machineId, key) -> assertFalse(MinionServerFactory.findByMachineId(MACHINE_ID).isPresent()),
                null, DEFAULT_CONTACT_METHOD, Optional.of(new MinionStartupGrains()));
    }

    @Test
    public void testAlreadyRegisteredRetailMinion() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create(
                "TERMINALS", "All terminals group", user.getOrg());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.getManagedGroups().add(terminalsGroup);
        server.setMinionId(MINION_ID);
        server.setMachineId(MACHINE_ID);
        systemManager.addServerToServerGroup(server, terminalsGroup);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest((key) -> new Expectations() {{
            allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
            will(returnValue(getSystemInfo(MINION_ID, null, key)));
        }}, null, (minion, machineId, key) -> assertTrue(MinionServerFactory.findByMinionId(MINION_ID).isPresent()),
                null, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    @Test
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
            history.sort(Comparator.comparing(ServerHistoryEvent::getCreated));
            assertEquals(history.get(history.size() - 1).getSummary(), "Server reactivated as Salt minion");
        }, SSH_PUSH_CONTACT_METHOD);
    }

    @Test
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
        setupStubs(product);

        // Verify the resulting system entry
        Optional<MinionServer> optMinion = MinionServerFactory.findByMachineId(MACHINE_ID);
        assertTrue(optMinion.isPresent());
        MinionServer minion = optMinion.get();

        assertEquals(MINION_ID, minion.getName());
        // assigned channels are preserved
        Set<Channel> originalChannels = HibernateFactory.reload(server).getChannels();
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
        assertTrue(found, "Activation Key not set as invalid");
        assertEquals(otherOrg, minion.getOrg());
    }

    @Test
    public void testReRegisterMinionResetProxyPath() throws Exception {
        ServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        MinionServer proxy = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMachineId(MACHINE_ID);
        minion.setMinionId(MINION_ID);
        minion.setHostname(MINION_ID);
        minion.setName(MINION_ID);

        Set<ServerPath> paths = ServerFactory.createServerPaths(minion, proxy, "hostname");
        minion.getServerPaths().addAll(paths);

        ServerFactory.save(minion);

        executeTest(
                (key) -> new Expectations() {{
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                            new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                            .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                            .createMinionStartUpGrains();
                    allowing(saltServiceMock)
                            .getGrains(with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null, key)));
                    allowing(saltServiceMock).getProducts(with(any(String.class)));
                    will(returnValue(Optional.empty()));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    // setting a server makes it a re-activation key
                    key.setServer(minion);
                    key.setOrg(user.getOrg());
                    ActivationKeyFactory.save(key);
                    return key.getKey();
                },
                (optMinion, machineId, key) -> assertTrue(optMinion.get().getServerPaths().isEmpty()),
                null,
                DEFAULT_CONTACT_METHOD, Optional.empty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRegisterMinionWithoutActivationKeyNoSyncProducts() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock)
                            .getGrains(with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null)));
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
                (contactMethod) -> null,
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    // no base/required channels - e.g. we need an SCC sync
                    assertNull(minion.getBaseChannel());
                    assertTrue(minion.getChannels().isEmpty());
                }, DEFAULT_CONTACT_METHOD);
    }

    @Test
    public void testRegisterMinionWithoutActivationKey() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        HibernateFactory.getSession().flush();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null)));
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
                (contactMethod) -> null,
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
    @Test
    public void testRegisterMinionWithInvalidActivationKeyNoSyncProducts() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                new MinionStartupGrains.SuseManagerGrain(Optional.of("non-existent-key"));
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key")));
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
    @Test
    public void testRegisterMinionWithInvalidActivationKey()
        throws Exception {

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ConfigChannel cfgChannel = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Config channel 1", "config-channel-1");
        MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                new MinionStartupGrains.SuseManagerGrain(Optional.of("non-existent-key"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false).susemanagerGrain(suseManagerGrain)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key")));
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
                }, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    @Test
    public void testRegisterMinionWithActivationKey() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ConfigChannel cfgChannel = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Config channel 1", "config-channel-1");
        HibernateFactory.getSession().flush();
        executeTest(
                (key) -> new Expectations() {{
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                            new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                            .machineId(MACHINE_ID).saltbootInitrd(false).susemanagerGrain(suseManagerGrain)
                            .createMinionStartUpGrains();
                    allowing(saltServiceMock)
                            .getGrains(with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, key)));
                }},
                (contactMethod) -> {
                    ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                    key.setBaseChannel(baseChannelX8664);
                    baseChannelX8664.getAccessibleChildrenFor(user)
                            .forEach(key::addChannel);
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
                    .forEach(channels::add);
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
                }, DEFAULT_CONTACT_METHOD, Optional.empty());
    }

    @Test
    public void testRegisterMinionWithActivationKeySUSEManagerDefault() throws Exception {
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        Channel baseChannelX8664 = setupBaseAndRequiredChannels(channelFamily, product);
        ChannelFamily channelFamilyOther = createTestChannelFamily();
        SUSEProduct productOther =
                SUSEProductTestUtils.createTestSUSEProduct(channelFamilyOther);
        Channel baseChannelX8664Other =
                setupBaseAndRequiredChannels(channelFamilyOther, productOther);
        executeTest((key) -> new Expectations() {

            {
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
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
                    .forEach(key::addChannel);
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
            // --> base + mandatories + activation key selected channels
            channels.add(baseChannelX8664);
            ChannelsUtils.mandatoryChannelsByBaseChannel(baseChannelX8664)
                    .forEach(channels::add);
            ActivationKeyFactory.lookupByKey(key).getChannels().stream()
                    .filter(c -> c.getParentChannel().getId().equals(baseChannelX8664.getId()))
                    .forEach(channels::add);
            assertEquals(channels, minion.getChannels());
            assertTrue(minion.getFqdns().isEmpty());
        }, DEFAULT_CONTACT_METHOD);
    }

    private static Stream<RHELMinionTestCase> provideRHELMinionTestCase() throws IOException, ClassNotFoundException {
        var cases = getRHELMinionTestCases();
        return cases.orElse(List.of()).stream();
    }

    /*
     * Test register a RHEL Minion with different parameters.
     * Case 1: Register RHEL Minion With Multiple Release Packages
     * Case 2: Register RHEL Minion Without Activation Key
     * Case 3: Register RHEL Minion With RES Activation Key and One Base Channel
     * Case 4: Register RHEL Minion With RES Activation Key and Two Base Channels
     * Case 5: Register RHEL Minion With SLL Activation Key and One Base Channel
     * Case 6: Register RHEL Minion With SLL Activation Key and Two Base Channels
     * Case 7: Register RES Minion Without Activation Key
     * Case 8: Register SLL Minion Without Activation Key
     *
     * @param testCase RHEL Minion test case parameters
     * @throws Exception in executeTest
     */
    @ParameterizedTest(name = "{displayName}")
    @MethodSource("provideRHELMinionTestCase")
    void testRegisterRHELMinion(RHELMinionTestCase testCase) throws Exception {
        List<Channel> channels = testCase.channelParameters != null ?
                testCase.channelParameters
                        .stream()
                        .map(ch -> RhelUtilsTest.createExpandedSupportChannel(
                                user,
                                ch.get("version"),
                                ch.get("shortName"),
                                ch.get("friendlyName")
                        ))
                        .collect(Collectors.toList()) : null;
        Channel baseChannel = channels != null ? channels.get(0) : null;
        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT);
        HibernateFactory.getSession().flush();
        MinionStartupGrains minionStartUpGrains = new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        will(returnValue(getSystemInfo(MINION_ID, testCase.productName.toLowerCase(),
                                baseChannel != null ? key : null)));

                        allowing(saltServiceMock).runRemoteCommand(
                                with(any(MinionList.class)),
                                with("rpm -q --whatprovides --queryformat \"%{NAME}\\n\" redhat-release"));
                        will(returnValue(singletonMap(MINION_ID,
                                new Result<>(Xor.right(testCase.availableReleasePackages + "\n")))));

                        allowing(saltServiceMock).runRemoteCommand(
                                with(any(MinionList.class)),
                                with("rpm -q --queryformat \"VERSION=%{VERSION}\\nPROVIDENAME=[%{PROVIDENAME},]\\n" +
                                        "PROVIDEVERSION=[%{PROVIDEVERSION},]\\n\" " + testCase.releasePackage));
                        will(returnValue(singletonMap(MINION_ID, new Result<>(Xor.right(testCase.packageInfo)))));

                        allowing(saltServiceMock).redhatProductInfo(MINION_ID);
                        will(returnValue(Optional.of(new RedhatProductInfo(
                                Optional.empty(),
                                Optional.of(testCase.releaseFileContent),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.empty(),
                                Optional.of(testCase.resProvider),
                                Optional.of(testCase.sllProvider)
                        ))));
                    }},
                    baseChannel != null ? (contactMethod) -> {
                        ActivationKey key = ActivationKeyTest.createTestActivationKey(user);
                        key.setBaseChannel(baseChannel);
                        key.setOrg(user.getOrg());
                        ActivationKeyFactory.save(key);
                        return key.getKey();
                    } : null,
                    (optMinion, machineId, key) -> {
                        assertTrue(optMinion.isPresent());
                        MinionServer minion = optMinion.get();
                        assertEquals(testCase.osVersion, minion.getRelease());
                        assertTrue(minion.getFqdns().isEmpty());

                        Runnable baseChannelAssertions = baseChannel != null ? () -> {
                            assertNotNull(minion.getBaseChannel());
                            assertEquals(baseChannel, minion.getBaseChannel());
                            assertEquals(1, minion.getChannels().size());

                            SUSEProductFactory.getSession().flush();
                            // select from view should succeed
                            SUSEProductFactory.getSession()
                                    .createNativeQuery("select * from rhnServerOverview")
                                    .list();
                        } : () -> {
                            assertNull(minion.getBaseChannel());
                            assertEquals(0, minion.getChannels().size());
                        };
                        baseChannelAssertions.run();
                    }, DEFAULT_CONTACT_METHOD);
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Test that registration of a minion with no activation key and with a creator user (e.g.
     * user who accepts the salt key in the UI) will put that minion in the organization
     * of the creator user.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testRegisterSystemFromDifferentOrg() throws Exception {
        User creator = UserFactory.lookupById(UserTestUtils.createUser("chuck", "rangers"));
        MinionPendingRegistrationService.addMinion(creator, MINION_ID, ContactMethodUtil.DEFAULT);
        try {
            executeTest(
                    SLES_NO_AK_EXPECTATIONS,
                    (cm) -> null,
                    (minion, machineId, key) -> assertEquals(creator.getOrg(), minion.get().getOrg()),
                    DEFAULT_CONTACT_METHOD
                    );
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Test that registration of a minion with an activation key and a creator user (e.g.
     * user who accepts the salt key in the UI) will put that minion in the organization
     * of the activation key (the organization of the user is ignored in this case).
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testRegisterSystemWithAKAndCreator() throws Exception {
        User creator = UserFactory.lookupById(UserTestUtils.createUser("chuck", "rangers"));
        MinionPendingRegistrationService.addMinion(creator, MINION_ID, ContactMethodUtil.DEFAULT);
        try {
            executeTest(
                    SLES_EXPECTATIONS,
                    ACTIVATION_KEY_SUPPLIER,
                    (minion, machineId, key) -> assertEquals(ActivationKeyFactory.lookupByKey(key).getOrg(),
                            minion.get().getOrg()),
                    DEFAULT_CONTACT_METHOD
                    );
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Initial test of a registering a terminal machine
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
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
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    // Notice product name has spaces in the string. It is intentional to test hw string preprocessing
                    Map<String, Object> mods = new HashMap<>();
                    mods.put("saltboot_initrd", true);
                    mods.put("manufacturer", "QEMU");
                    mods.put("productname", "Cash Desk 01");
                    mods.put("minion_id_prefix", "Branch001");
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                }, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    /**
     * Test registering a terminal machine when a required group is missing.
     * In this case we want the minion NOT to be registered.
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailMinionTerminalGroupMissing() throws Exception {
        ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group", OrgFactory.getSatelliteOrg());
        ServerGroupFactory.create("Branch001", "Branch group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        Map<String, Object> mods = new HashMap<>();
                        mods.put("saltboot_initrd", true);
                        mods.put("manufacturer", "QEMU");
                        mods.put("productname", "CashDesk01");
                        mods.put("minion_id_prefix", "Branch001");
                        will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
                        allowing(saltServiceMock).callSync(
                                with(any(LocalCall.class)),
                                with(any(String.class)));
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> assertTrue(optMinion.isPresent()),
                    DEFAULT_CONTACT_METHOD,
                    Optional.of(minionStartUpGrains));
        }
        catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            fail("Unexpected Exception thrown");
        }
    }


    /**
     * Test registering a terminal machine when a required group is missing.
     * The terminal should be registered, but without group assignment and
     * without saltboot state applied
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailMinionBranchGroupMissing() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create(
                "HWTYPE:QEMU-CashDesk01", "HW group", OrgFactory.getSatelliteOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create(
                "TERMINALS", "All terminals group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        Map<String, Object> mods = new HashMap<>();
                        mods.put("saltboot_initrd", true);
                        mods.put("manufacturer", "QEMU");
                        mods.put("productname", "CashDesk01");
                        mods.put("minion_id_prefix", "Branch001");
                        will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
                        // no call to state.apply saltboot
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> {
                        assertTrue(optMinion.isPresent());
                        MinionServer minion = optMinion.get();
                        assertFalse(minion.getManagedGroups().contains(hwGroup));
                        assertFalse(minion.getManagedGroups().contains(terminalsGroup));
                    },
                    DEFAULT_CONTACT_METHOD,
                    Optional.of(minionStartUpGrains));
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Test registering a terminal machine when a non-required group (HW group) is missing
     * In this case we want the minion to be registered.
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailMinionHwGroupMissing() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create(
                "TERMINALS", "All terminals group", OrgFactory.getSatelliteOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create(
                "Branch001", "Branch group", OrgFactory.getSatelliteOrg());
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    Map<String, Object> mods = new HashMap<>();
                    mods.put("saltboot_initrd", true);
                    mods.put("manufacturer", "QEMU");
                    mods.put("productname", "CashDesk01");
                    mods.put("minion_id_prefix", "Branch001");
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                }, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    /**
     * When an empty profile is assigned to a HW type group (prefixed with "HWTYPE:") before registration,
     * don't assign it to (another) HW type group on registration.
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailMinionHwGroupAlreadyAssigned() throws Exception {
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create(
                "TERMINALS", "All terminals group", user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create(
                "Branch001", "Branch group", user.getOrg());
        ManagedServerGroup hwGroupMatching = ServerGroupFactory.create(
                "HWTYPE:QEMU-CashDesk01", "HW group", user.getOrg());
        ManagedServerGroup alreadyAssignedGroup = ServerGroupFactory.create("HWTYPE:idontmatch",
                "HW group - assigned to empty profile beforehand", user.getOrg());

        MinionServer emptyMinion = systemManager.createSystemProfile(user, "empty profile",
                singletonMap("hwAddress", "00:11:22:33:44:55"));
        ServerFactory.addServerToGroup(emptyMinion, alreadyAssignedGroup);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    Map<String, Object> mods = new HashMap<>();
                    mods.put("saltboot_initrd", true);
                    mods.put("manufacturer", "QEMU");
                    mods.put("productname", "CashDesk01");
                    mods.put("minion_id_prefix", "Branch001");
                    Map<String, String> interfaces = new HashMap<>();
                    interfaces.put("eth1", "00:11:22:33:44:55");
                    mods.put("hwaddr_interfaces", interfaces);
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    /**
     * Test registering retail machine in a specific org
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailTerminalNonDefaultOrg() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group",
                user.getOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group",
                user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group",
                user.getOrg());

        MinionPendingRegistrationService.addMinion(user, MINION_ID, ContactMethodUtil.DEFAULT);
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();

        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        Map<String, Object> mods = new HashMap<>();
                        mods.put("saltboot_initrd", true);
                        mods.put("manufacturer", "QEMU");
                        mods.put("productname", "CashDesk01");
                        mods.put("minion_id_prefix", "Branch001");
                        will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                    }, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Test failure in register of retail machine in a specific org when proxy is not present
     * The terminal should be registered, but without group assignment and
     * without saltboot state applied
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailTerminalNonDefaultOrgFailWithoutProxy() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group",
                user.getOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group",
                user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group",
                user.getOrg());

        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();

        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        Map<String, Object> mods = new HashMap<>();
                        mods.put("saltboot_initrd", true);
                        mods.put("manufacturer", "QEMU");
                        mods.put("productname", "CashDesk01");
                        mods.put("minion_id_prefix", "Branch001");
                        will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
                        // no call to state.apply saltboot
                    }},
                    (contactMethod) -> null, // no AK
                    (optMinion, machineId, key) -> {
                        assertTrue(optMinion.isPresent());
                        MinionServer minion = optMinion.get();
                        assertFalse(minion.getManagedGroups().contains(hwGroup));
                        assertFalse(minion.getManagedGroups().contains(terminalsGroup));
                        assertFalse(minion.getManagedGroups().contains(branchGroup));
                    },
                    DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
    }

    /**
     * Test registering retail machine in a specific org but without creating user (aka autoaccepted key)
     *
     * @throws Exception - if anything goes wrong
     */
    @Test
    public void testRegisterRetailTerminalNonDefaultOrgWithoutCreator() throws Exception {
        ManagedServerGroup hwGroup = ServerGroupFactory.create("HWTYPE:QEMU-CashDesk01", "HW group",
                user.getOrg());
        ManagedServerGroup terminalsGroup = ServerGroupFactory.create("TERMINALS", "All terminals group",
                user.getOrg());
        ManagedServerGroup branchGroup = ServerGroupFactory.create("Branch001", "Branch group",
                user.getOrg());

        // create a proxy for minion with correct organization
        MinionServer proxy = MinionServerFactoryTest.createTestMinionServer(user);
        // this proxy minion must have correct fqdn set equaling minions master
        String proxyFqdn = "proxy" + MINION_ID;
        proxy.addFqdn(proxyFqdn);

        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true)
                .createMinionStartUpGrains();

        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    Map<String, Object> mods = new HashMap<>();
                    mods.put("saltboot_initrd", true);
                    mods.put("manufacturer", "QEMU");
                    mods.put("productname", "CashDesk01");
                    mods.put("minion_id_prefix", "Branch001");
                    mods.put("master", proxyFqdn);
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                    assertEquals(minion.getOrg(), user.getOrg());
                }, DEFAULT_CONTACT_METHOD, Optional.of(minionStartUpGrains));
    }

    /**
     * Tests registration of an empty profile
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testEmptyProfileRegistration() throws Exception {
        MinionServer emptyMinion = systemManager.createSystemProfile(user, "empty profile",
                singletonMap("hwAddress", "00:11:22:33:44:55"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(false)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    Map<String, Object> mods = new HashMap<>();
                    mods.put("saltboot_initrd", false);
                    mods.put("manufacturer", "QEMU");
                    mods.put("productname", "CashDesk02");
                    mods.put("minion_id_prefix", "Branch001");
                    Map<String, String> interfaces = new HashMap<>();
                    interfaces.put("eth1", "00:11:22:33:44:55");
                    mods.put("hwaddr_interfaces", interfaces);
                    will(returnValue(getSystemInfo(MINION_ID, null, "non-existent-key", null, mods)));
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
                                    .map(Entitlement::getLabel)
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
                DEFAULT_CONTACT_METHOD,
                Optional.of(minionStartUpGrains));
    }

    /**
     * Test that a traditional -> salt migration respects the channels from the Activation Key (and overrides
     * the channels that have been assigned to the system)
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    @Test
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
        executeTest((key) -> new Expectations() {
            {
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
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
    @Test
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
        executeTest((key) -> new Expectations() {
            {
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
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
    @Test
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
        executeTest((key) -> new Expectations() {
            {
                allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                will(returnValue(getSystemInfo(MINION_ID, null, key)));
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

    @Test
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
                (key) -> new Expectations() {{
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                            new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                            .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                            .createMinionStartUpGrains();
                    allowing(saltServiceMock).getGrains(
                            with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null, key)));
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
                cleanup -> { },
                DEFAULT_CONTACT_METHOD,
                Optional.empty());
    }

    @Test
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
                (key) -> new Expectations() {{
                    MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                            new MinionStartupGrains.SuseManagerGrain(Optional.of(key));
                    MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                           .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                           .createMinionStartUpGrains();
                    allowing(saltServiceMock).getGrains(
                            with(any(String.class)), with(any(TypeToken.class)), with(any(String[].class)));
                    will(returnValue(Optional.of(minionStartUpGrains)));
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null, key)));
                }},
                (contactMethod) -> "1-re-already-used",
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    assertNotNull(minion.getBaseChannel());
                },
                cleanup -> { },
                DEFAULT_CONTACT_METHOD, Optional.empty());
    }

    @Test
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
        MinionStartupGrains.SuseManagerGrain suseManagerGrain =
                new MinionStartupGrains.SuseManagerGrain(Optional.of("1-re-already-used"));
        MinionStartupGrains minionStartUpGrains =  new MinionStartupGrains.MinionStartupGrainsBuilder()
                .machineId(MACHINE_ID).saltbootInitrd(true).susemanagerGrain(suseManagerGrain)
                .createMinionStartUpGrains();
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, null, key)));
                }},
                (contactMethod) -> "1-re-already-used",
                (optMinion, machineId, key) -> {
                    assertTrue(optMinion.isPresent());
                    MinionServer minion = optMinion.get();
                    assertEquals(MINION_ID, minion.getName());
                    assertNotNull(minion.getBaseChannel());
                },
                cleanup -> { },
                DEFAULT_CONTACT_METHOD,
                Optional.of(minionStartUpGrains));
    }

    /**
     * Test a registration of a non-free BYOS client at a PAYG SUMA Server.
     * @throws Exception
     */
    @Test
    public void testRegisterMinionBYOSonPAYG() throws Exception {
        cloudManager4Test = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setFree(false);
        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        will(returnValue(getSystemInfo(MINION_ID, "byos", key)));
                        allowing(saltServiceMock).getInstanceFlavor(MINION_ID);
                        will(returnValue(SumaUtil.PublicCloudInstanceFlavor.BYOS));
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
                    ACTIVATION_KEY_SUPPLIER,
                    (optMinion, machineId, key) -> assertTrue(optMinion.isEmpty()),
                    DEFAULT_CONTACT_METHOD);
        }
        catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            assertContains(e.getMessage(), MINION_ID);
            assertContains(e.getMessage(), "To manage BYOS (Bring-your-own-Subscription) clients " +
                    "you have to configure SCC Credentials");
            return;
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
        fail("Expected Exception not thrown");
    }

    /**
     * Test a registration of a non-free DC client at a PAYG SUMA Server.
     * @throws Exception
     */
    @Test
    public void testRegisterMinionDConPAYG() throws Exception {
        cloudManager4Test = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setFree(false);
        try {
            executeTest(
                    (key) -> new Expectations() {{
                        allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                        will(returnValue(getSystemInfo(MINION_ID, null, key)));
                        allowing(saltServiceMock).getInstanceFlavor(MINION_ID);
                        will(returnValue(SumaUtil.PublicCloudInstanceFlavor.UNKNOWN));
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
                    ACTIVATION_KEY_SUPPLIER,
                    (optMinion, machineId, key) -> assertTrue(optMinion.isEmpty()),
                    DEFAULT_CONTACT_METHOD);
        }
        catch (RegisterMinionEventMessageAction.RegisterMinionException e) {
            assertContains(e.getMessage(), MINION_ID);
            assertContains(e.getMessage(), "To manage Datacenter clients you have to configure SCC Credentials");
            return;
        }
        finally {
            MinionPendingRegistrationService.removeMinion(MINION_ID);
        }
        fail("Expected Exception not thrown");
    }

    /**
     * Test registration of a free BYOS client at a SUMA PAYG Server
     * @throws Exception
     */
    @Test
    public void testRegisterMinionFreeBYOSonPAYG() throws Exception {
        cloudManager4Test = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setFree(true);
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, key)));
                    allowing(saltServiceMock).getInstanceFlavor(MINION_ID);
                    will(returnValue(SumaUtil.PublicCloudInstanceFlavor.UNKNOWN));
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
                ACTIVATION_KEY_SUPPLIER,
                (optMinion, machineId, key) -> assertTrue(optMinion.isPresent()),
                DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test registration of a SUSE Manager Proxy at a SUMA PAYG Server
     * @throws Exception
     */
    @Test
    public void testRegisterMinionSumaProxyOnPAYG() throws Exception {
        cloudManager4Test = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        ChannelFamily channelFamily = createTestChannelFamily();
        channelFamily.setName("SUSE Manager Proxy");
        channelFamily.setLabel("SMP");

        SUSEProduct product = SUSEProductTestUtils.createTestSUSEProduct(channelFamily);
        product.setName("suse-manager-proxy");
        product.setVersion("4.3");
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setFree(false);
        SUSEProductFactory.save(product);

        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, null, key)));
                    allowing(saltServiceMock).getInstanceFlavor(MINION_ID);
                    will(returnValue(SumaUtil.PublicCloudInstanceFlavor.PAYG));
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
                ACTIVATION_KEY_SUPPLIER,
                (optMinion, machineId, key) -> assertTrue(optMinion.isPresent()),
                DEFAULT_CONTACT_METHOD);
    }

    /**
     * Test registration of a PAYG Client at a SUMA PAYG Server
     * @throws Exception
     */
    @Test
    public void testRegisterMinionPAYGonPAYG() throws Exception {
        cloudManager4Test = new CloudPaygManager() {
            @Override
            public boolean isPaygInstance() {
                return true;
            }
        };
        executeTest(
                (key) -> new Expectations() {{
                    allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
                    will(returnValue(getSystemInfo(MINION_ID, "slespayg", key)));
                    allowing(saltServiceMock).getInstanceFlavor(MINION_ID);
                    will(returnValue(SumaUtil.PublicCloudInstanceFlavor.PAYG));
                }},
                ACTIVATION_KEY_SUPPLIER,
                (optMinion, machineId, key) -> assertTrue(optMinion.isPresent()),
                DEFAULT_CONTACT_METHOD);
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
    private void setupStubs(SUSEProduct product)
        throws ClassNotFoundException, IOException {

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionServerFactory.findByMachineId(MACHINE_ID).ifPresent(ServerFactory::delete);
        context().checking(new Expectations() { {
            allowing(saltServiceMock).getSystemInfoFull(MINION_ID);
            will(returnValue(getSystemInfo(MINION_ID, null, "foo")));
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
            try {
                allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            }
            catch (TaskomaticApiException e) {
                e.printStackTrace();
            }
        } });

        RegisterMinionEventMessageAction action =
                new RegisterMinionEventMessageAction(saltServiceMock, saltServiceMock, cloudManager4Test);
        action.execute(new RegisterMinionEventMessage(MINION_ID, Optional.of(DEFAULT_MINION_START_UP_GRAINS)));
    }

    /*
    Encapsulates test cases for RHEL minion registration tests
     */
    private static class RHELMinionTestCase {

        private final String description;
        private final List<Map<String, String>> channelParameters;
        private final String productName;
        private final String availableReleasePackages;
        private final String releasePackage;
        private final String packageInfo;
        private final String releaseFileContent;
        private final String resProvider;
        private final String sllProvider;
        private final String osVersion;

        RHELMinionTestCase(
                String descriptionIn, List<Map<String, String>> channelParametersIn, String productNameIn,
                String availableReleasePackagesIn, String releasePackageIn, String packageInfoIn,
                String releaseFileContentIn, String resProviderIn, String sllProviderIn,
                String osVersionIn
        ) {
            this.description = descriptionIn;
            this.channelParameters = channelParametersIn;
            this.productName = productNameIn;
            this.availableReleasePackages = availableReleasePackagesIn;
            this.releasePackage = releasePackageIn;
            this.packageInfo = packageInfoIn;
            this.releaseFileContent = releaseFileContentIn;
            this.resProvider = resProviderIn;
            this.sllProvider = sllProviderIn;
            this.osVersion = osVersionIn;
        }
    }

    private static Optional<List<RHELMinionTestCase>> getRHELMinionTestCases()
            throws ClassNotFoundException, IOException {
        String json = readFile("rhel_minion_test_data.json");
        List<RHELMinionTestCase> testCases = new Gson()
                .fromJson(json, new TypeToken<List<RHELMinionTestCase>>() {
                }.getType());
        return Optional.of(testCases);
    }


    private Optional<SystemInfo> getSystemInfo(String minionId, String sufix, String akey)
            throws ClassNotFoundException, IOException {
        return getSystemInfo(minionId, sufix, akey, null);
    }


    private Optional<SystemInfo> getSystemInfo(String minionId, String sufix, String akey, String mkey)
            throws ClassNotFoundException, IOException {
        return getSystemInfo(minionId, sufix, akey, null, null);
    }

    private Optional<SystemInfo> getSystemInfo(String minionId, String sufix, String akey, String mkey,
            Map<String, Object> mods)
            throws ClassNotFoundException, IOException {

        Map<String, Object> infoMap = new JsonParser<>(Grains.items(false).getReturnType()).parse(
            readFile("dummy_systeminfo" + (sufix != null ? "_" + sufix : "") + ".json"));

        Map<String, String> susemanager = new HashMap<>();
        if (akey != null) {
            susemanager.put("activation_key", akey);
        }
        if (mkey != null) {
            susemanager.put("management_key", mkey);
        }

        Map<String, Object> applyResult = (Map<String, Object>)infoMap.get(
                "mgrcompat_|-grains_update_|-grains.items_|-module_run");
        Map<String, Object> changes = (Map<String, Object>)applyResult.get("changes");
        Map<String, Object> grains = (Map<String, Object>)changes.get("ret");
        grains.put("susemanager", susemanager);

        if (mods != null) {
            mods.forEach((key, value) -> grains.put(key, value));
        }

        SystemInfo info = Json.GSON.fromJson(Json.GSON.toJson(infoMap), SystemInfo.class);
        return Optional.of(info);
    }

    private static String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }
}
