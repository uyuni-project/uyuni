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
package com.suse.manager.reactor.messaging.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ImageTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.reactor.utils.test.RhelUtilsTest;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.salt.custom.Openscap;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class JobReturnEventMessageActionTest extends JMockBaseTestCaseWithUser {

    // JsonParser for parsing events from files
    public static final JsonParser<Event> EVENTS =
            new JsonParser<>(new TypeToken<Event>(){});

    private TaskomaticApi taskomaticApi;
    private SaltService saltServiceMock;
    private SystemEntitlementManager systemEntitlementManager;
    private Path metadataDirOfficial;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
        saltServiceMock = context().mock(SaltService.class);
        SaltService saltService = new SaltService();
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(saltService, new VirtManagerSalt(saltService))
        );

        metadataDirOfficial = Files.createTempDirectory("meta");
        FormulaFactory.setDataDir(tmpSaltRoot.toString());
        FormulaFactory.setMetadataDirOfficial(metadataDirOfficial.toString() + File.separator);
    }

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdate() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify the results
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("4.2", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("timezone-java")) {
                assertEquals("2016c", pkg.getEvr().getVersion());
                assertEquals("0.37.1", pkg.getEvr().getRelease());
                assertEquals("noarch", pkg.getArch().getName());
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());

        minion.getInstalledProducts().stream().forEach(product -> {
            assertEquals("sles", product.getName().toLowerCase());
            assertEquals("sles",  product.getSUSEProduct().getName());
            assertEquals("12.1", product.getVersion());
            assertEquals("12.1",  product.getSUSEProduct().getVersion());
            assertEquals("0", product.getRelease());
            assertEquals(null, product.getSUSEProduct().getRelease());
            assertEquals("x86_64", product.getArch().getName());
            assertEquals("x86_64", product.getSUSEProduct().getArch().getName());
            assertEquals(true, product.isBaseproduct());
        });
        assertEquals(1, minion.getInstalledProducts().size());

        // Verify OS family
        assertEquals("Suse", minion.getOsFamily());

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

    public void testApplyPackageDelta() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        assertEquals(0, minion.getPackages().size());

        Map<String, Change<Xor<String, List<Pkg.Info>>>> install = Json.GSON.fromJson(new InputStreamReader(getClass()
                .getResourceAsStream("/com/suse/manager/reactor/messaging/test/pkg_install.new_format.json")),
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>(){}.getType());
        SaltUtils.applyChangesFromStateModule(install, minion);
        assertEquals(1, minion.getPackages().size());
        List<InstalledPackage> packages = new ArrayList<>(minion.getPackages());
        assertEquals("vim", packages.get(0).getName().getName());
        assertEquals("x86_64", packages.get(0).getArch().getLabel());
        assertEquals("1.42.11", packages.get(0).getEvr().getVersion());
        assertEquals("7.1", packages.get(0).getEvr().getRelease());
        assertEquals(new Date(1498636531000L), packages.get(0).getInstallTime());


        Map<String, Change<Xor<String, List<Pkg.Info>>>> update = Json.GSON.fromJson(new InputStreamReader(getClass()
                        .getResourceAsStream("/com/suse/manager/reactor/messaging/test/pkg_update.new_format.json")),
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>(){}.getType());

        SaltUtils.applyChangesFromStateModule(update, minion);
        assertEquals(1, minion.getPackages().size());
        List<InstalledPackage> packages1 = new ArrayList<>(minion.getPackages());
        assertEquals("vim", packages1.get(0).getName().getName());
        assertEquals("x86_64", packages1.get(0).getArch().getLabel());
        assertEquals("1.42.12", packages1.get(0).getEvr().getVersion());
        assertEquals("7.2", packages1.get(0).getEvr().getRelease());
        assertEquals(new Date(1498636553000L), packages1.get(0).getInstallTime());


        Map<String, Change<Xor<String, List<Pkg.Info>>>> remove = Json.GSON.fromJson(new InputStreamReader(getClass()
                .getResourceAsStream("/com/suse/manager/reactor/messaging/test/pkg_remove.new_format.json")),
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>(){}.getType());


        SaltUtils.applyChangesFromStateModule(remove, minion);
        assertEquals(0, minion.getPackages().size());
    }

    public void testsPackageDeltaFromStateApply() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        assertEquals(0, minion.getPackages().size());

        Map<String, JsonElement> apply = Json.GSON.fromJson(new InputStreamReader(getClass()
                        .getResourceAsStream("/com/suse/manager/reactor/messaging/test/apply_pkg.new_format.json")),
                new TypeToken<Map<String, JsonElement>>(){}.getType());
        SaltUtils.applyChangesFromStateApply(apply, minion);

        assertEquals(1, minion.getPackages().size());
        List<InstalledPackage> packages = new ArrayList<>(minion.getPackages());
        assertEquals("vim", packages.get(0).getName().getName());
        assertEquals("x86_64", packages.get(0).getArch().getLabel());
        assertEquals("1.42.11", packages.get(0).getEvr().getVersion());
        assertEquals("7.1", packages.get(0).getEvr().getRelease());
        assertEquals(new Date(1498636531000L), packages.get(0).getInstallTime());
    }


    /**
     * Test the processing of packages.profileupdate job return event on an existing
     * minion which already has installed packages using the new return format which
     * includes all package versions installed on the minion.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateAllVersions() throws Exception {
        // set up minion, action and response: 6 packages installed
        // aaa_base, java, bash (x86_64 and i686), kernel-default (4.4.73-5.1 and 4.4.126-94.22.1)
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        JobReturnEventMessage message = new JobReturnEventMessage(JobReturnEvent
                .parse(getJobReturnEvent("packages.profileupdate.allversions.json", action.getId()))
                .get());
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify names and versions
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                if (pkg.getArch().getName().equals("x86_64")) {
                    assertEquals("500", pkg.getEvr().getVersion());
                    assertEquals("75.2", pkg.getEvr().getRelease());
                }
                else if (pkg.getArch().getName().equals("i686")) {
                    assertEquals("555", pkg.getEvr().getVersion());
                    assertEquals("75.8", pkg.getEvr().getRelease());
                }
                else {
                    fail("Pkg arch:" + pkg.getArch());
                }
            }
            else if (pkg.getName().getName().equals("java")) {
                assertEquals("1.6", pkg.getEvr().getVersion());
                assertEquals("0", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("kernel-default")) {
                assertEquals("x86_64", pkg.getArch().getName());
                if (pkg.getEvr().getVersion().equals("4.4.126")) {
                    assertEquals("94.22.1", pkg.getEvr().getRelease());
                }
                else if (pkg.getEvr().getVersion().equals("4.4.73")) {
                    assertEquals("5.1", pkg.getEvr().getRelease());
                }
                else {
                    fail();
                }
            }
            else {
                fail();
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(6, minion.getPackages().size());
    }

    /**
     * Test the processing of packages.profileupdate job return event on an existing
     * minion which already has installed packages.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateMultiple() throws Exception {
        // set up minion, action and response: 3 packages installed
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        JobReturnEventMessage message = new JobReturnEventMessage(JobReturnEvent
                .parse(getJobReturnEvent("packages.profileupdate.json", action.getId()))
                .get());
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify names and versions
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("4.2", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("timezone-java")) {
                assertEquals("2016c", pkg.getEvr().getVersion());
                assertEquals("0.37.1", pkg.getEvr().getRelease());
                assertEquals("noarch", pkg.getArch().getName());
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());


        // set up different response: aaa_base is identical, bash was updated to
        // version 500, timezone-java is gone and java is new
        message = new JobReturnEventMessage(JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.updated.json", action.getId()))
                .get());
        messageAction.execute(message);

        // Verify names and versions
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("500", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("java")) {
                assertEquals("1.6", pkg.getEvr().getVersion());
                assertEquals("0", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else {
                fail();
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());
    }

    public void testPackagesProfileUpdateLivePatching() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        JobReturnEventMessage message = new JobReturnEventMessage(JobReturnEvent
                .parse(getJobReturnEvent("packages.profileupdate.json", action.getId()))
                .get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify no live patching version is returned
        assertNull(minion.getKernelLiveVersion());

        //Switch to live patching
        message = new JobReturnEventMessage(JobReturnEvent
                .parse(getJobReturnEvent("packages.profileupdate.livepatching.json",
                        action.getId()))
                .get());
        messageAction.execute(message);

        // Verify live patching version
        assertEquals("kgraft_patch_2_2_1", minion.getKernelLiveVersion());

        //Switch back from live patching
        message = new JobReturnEventMessage(JobReturnEvent
                .parse(getJobReturnEvent("packages.profileupdate.json",
                        action.getId()))
                .get());
        messageAction.execute(message);

        // Verify no live patching version is returned again
        assertNull(minion.getKernelLiveVersion());
    }

    /**
     * Test the processing of packages.profileupdate job return event
     * for RHEL7 with RES.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateRhel7RES() throws Exception {
        RhelUtilsTest.createResChannel(user, "7");
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.rhel7res.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify the results
        for (InstalledPackage pkg : minion.getPackages()) {
            if (pkg.getName().getName().equals("aaa_base")) {
                assertEquals("13.2+git20140911.61c1681", pkg.getEvr().getVersion());
                assertEquals("12.1", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("bash")) {
                assertEquals("4.2", pkg.getEvr().getVersion());
                assertEquals("75.2", pkg.getEvr().getRelease());
                assertEquals("x86_64", pkg.getArch().getName());
            }
            else if (pkg.getName().getName().equals("timezone-java")) {
                assertEquals("2016c", pkg.getEvr().getVersion());
                assertEquals("0.37.1", pkg.getEvr().getRelease());
                assertEquals("noarch", pkg.getArch().getName());
            }

            // All packages have epoch null
            assertNull(pkg.getEvr().getEpoch());
        }
        assertEquals(3, minion.getPackages().size());

        assertEquals(1, minion.getInstalledProducts().size());
        minion.getInstalledProducts().stream().forEach(product -> {
            assertEquals("res", product.getName());
            assertEquals("7", product.getVersion());
            assertEquals(null, product.getRelease());
            // in the case of RES the product arch is taken from the server arch
            assertEquals("x86_64", product.getArch().getName());
        });

        // Verify OS family
        assertEquals("RedHat", minion.getOsFamily());

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

    /**
     * Test the processing of packages.profileupdate job return event
     * for Ubuntu 18.04.
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateUbuntu() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("amd64-debian-linux"));
        minion.setMinionId("minion.ubuntu.local");
        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.ubuntu.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // Verify the results
        assertEquals(3, minion.getPackages().size());

        // 'libgcc1' package
        InstalledPackage pkg =
                minion.getPackages().stream().filter(p -> "libgcc1".equals(p.getName().getName())).findFirst().get();
        assertEquals("8.2.0", pkg.getEvr().getVersion());
        assertEquals("1ubuntu2~18.04", pkg.getEvr().getRelease());
        assertEquals("1", pkg.getEvr().getEpoch());
        assertEquals("amd64-deb", pkg.getArch().getLabel());

        // 'libefiboot1' package
        pkg = minion.getPackages().stream().filter(p -> "libefiboot1".equals(p.getName().getName())).findFirst().get();
        assertEquals("34", pkg.getEvr().getVersion());
        assertEquals("1", pkg.getEvr().getRelease());
        assertNull(pkg.getEvr().getEpoch());
        assertEquals("amd64-deb", pkg.getArch().getLabel());

        // 'init' package
        pkg = minion.getPackages().stream().filter(p -> "init".equals(p.getName().getName())).findFirst().get();
        assertEquals("1.51", pkg.getEvr().getVersion());
        assertEquals("X", pkg.getEvr().getRelease());
        assertNull(pkg.getEvr().getEpoch());
        assertEquals("amd64-deb", pkg.getArch().getLabel());

        // Verify OS family
        assertEquals("Debian", minion.getOsFamily());

        // Verify the action status
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

    /**
     * Test the processing of packages.profileupdate job return event in the case where the system has installed CaaSP
     * and it should be locked via Salt formula
     *
     * @throws Exception in case of an error
     */
    public void testPackagesProfileUpdateWithCaaSPSystemLocked() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).refreshPillar(with(any(MinionList.class)));
        }});
        SaltUtils.INSTANCE.setSystemQuery(saltServiceMock);

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_PACKAGES_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        HibernateFactory.getSession().flush();
        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("packages.profileupdate.caasp.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertTrue(minion.getInstalledProducts().stream().anyMatch(
                p -> p.getName().equalsIgnoreCase(SaltUtils.CAASP_PRODUCT_IDENTIFIER)));
        assertTrue(action.getServerActions().stream()
                .filter(serverAction -> serverAction.getServer().equals(minion))
                .findAny().get().getStatus().equals(ActionFactory.STATUS_COMPLETED));
    }

    public void testHardwareProfileUpdateX86NoDmi()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.nodmi.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());
            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getDmi());
            assertNull(server.getDmi().getSystem());
            assertNull(server.getDmi().getProduct());
            assertNull(server.getDmi().getBios());
            assertNull(server.getDmi().getVendor());
            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());
        });
    }

    public void testHardwareProfileUpdateDockerNoDmiUdev()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.docker.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());
            assertNull(server.getVirtualInstance());
            assertNotNull(server.getDmi());
            assertNull(server.getDmi().getSystem());
            assertNull(server.getDmi().getProduct());
            assertNull(server.getDmi().getBios());
            assertNull(server.getDmi().getVendor());
            assertTrue(server.getDevices().isEmpty());
        });
    }

    public void testHardwareProfileUpdateX86() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());
            assertEquals(Long.valueOf(1), server.getCpu().getNrsocket());
            assertEquals(Long.valueOf(1), server.getCpu().getNrCPU());
            assertEquals("Intel Xeon E312xx (Sandy Bridge)", server.getCpu().getModel());
            assertEquals("3492.164", server.getCpu().getMHz());
            assertEquals("GenuineIntel", server.getCpu().getVendor());
            assertEquals("1", server.getCpu().getStepping());
            assertEquals("6", server.getCpu().getFamily());
            assertEquals("4096 KB", server.getCpu().getCache());
            assertEquals("6984.32", server.getCpu().getBogomips());
            assertEquals("fpu vme de pse tsc msr pae mce cx8 apic sep mtrr " +
                            "pge mca cmov pat pse36 clflush mmx fxsr sse sse2 ss " +
                            "syscall nx pdpe1gb rdtscp lm constant_tsc rep_good " +
                            "nopl eagerfpu pni pclmulqdq vmx ssse3 fma cx16 pcid " +
                            "sse4_1 sse4_2 x2apic movbe popcnt tsc_deadline_timer " +
                            "aes xsave avx f16c rdrand hypervisor lahf_lm abm xsaveopt " +
                            "vnmi ept fsgsbase bmi1 avx2 smep bmi2 erms invpcid",
                    server.getCpu().getFlags());
            assertEquals("42", server.getCpu().getVersion());
            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getDmi());
            assertNotNull(server.getDmi().getSystem());
            assertNotNull(server.getDmi().getProduct());
            assertNotNull(server.getDmi().getBios());
            assertNotNull(server.getDmi().getVendor());
            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("52:54:00:af:7f:30", ethNames.get("eth0").getHwaddr());
            assertEquals("52:54:00:eb:51:3d", ethNames.get("eth1").getHwaddr());

            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::5054:ff:fed0:91", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::5054:ff:fefc:19a4", ethNames.get("eth1").getIPv6Addresses().get(0).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth1").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth1").getIPv6Addresses().get(0).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIPv4Addresses().get(0).getAddress());
            assertEquals("192.168.121.155", ethNames.get("eth0").getIPv4Addresses().get(0).getAddress());
            assertEquals("172.24.108.98", ethNames.get("eth1").getIPv4Addresses().get(0).getAddress());

            assertEquals("255.0.0.0", ethNames.get("lo").getIPv4Addresses().get(0).getNetmask());
            assertEquals("255.255.255.0", ethNames.get("eth0").getIPv4Addresses().get(0).getNetmask());
            assertEquals("255.240.0.0", ethNames.get("eth1").getIPv4Addresses().get(0).getNetmask());

            assertEquals("127.255.255.255", ethNames.get("lo").getIPv4Addresses().get(0).getBroadcast());
            assertEquals("192.168.121.255", ethNames.get("eth0").getIPv4Addresses().get(0).getBroadcast());
            assertEquals("172.31.255.255", ethNames.get("eth1").getIPv4Addresses().get(0).getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("virtio_net", ethNames.get("eth0").getModule());
            assertEquals("virtio_net", ethNames.get("eth1").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });
    }

    public void testHardwareProfileUpdateGrainsFqdns() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.ppc64.json", (server) -> {
            assertNotNull(server);
            assertEquals(2, server.getFqdns().size());
        });
    }

    public void testHardwareProfileUpdateCustomFqdns() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.x86.custom.fqdns.json", (server) -> {
            assertNotNull(server);
            assertEquals(5, server.getFqdns().size());
            List<String> collect = server.getFqdns().stream().map(ServerFQDN::getName).collect(Collectors.toList());
            assertTrue(collect.contains("custom.fqdns.name.one"));
            assertTrue(collect.contains("custom.fqdns.name.two"));
            assertTrue(collect.contains("custom.fqdns.name.three"));
        });
    }

    public void testHardwareProfileUpdateNetworkModuleFqdns() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.x86.json", (server) -> {
            assertNotNull(server);
            assertEquals(2, server.getFqdns().size());
        });
    }

    public void testHardwareProfileUpdateX86LongCPUValues()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.cpulongval.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertEquals(32, server.getCpu().getModel().length());
            assertEquals(16, server.getCpu().getMHz().length());
            assertEquals(32, server.getCpu().getVendor().length());
            assertEquals(16, server.getCpu().getStepping().length());
            assertEquals(32, server.getCpu().getFamily().length());
            assertEquals(16, server.getCpu().getCache().length());
            assertEquals(16, server.getCpu().getBogomips().length());
            assertEquals(32, server.getCpu().getVersion().length());
            assertEquals(2048, server.getCpu().getFlags().length());
        });
    }

    public void testHardwareProfileUpdatePPC64()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.ppc64.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            server = MinionServerFactory.findByMinionId(server.getMinionId()).orElse(null);
            assertEquals("CHRP IBM pSeries (emulated by qe", server.getCpu().getVendor());
            assertEquals("POWER8E (raw), altivec supported", server.getCpu().getModel());
            assertEquals(null, server.getCpu().getBogomips());
            assertEquals("3425.000000", server.getCpu().getMHz());

            assertNull(server.getDmi());
            assertTrue(!server.getDevices().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("52:54:00:d7:4f:20", ethNames.get("eth0").getHwaddr());

            assertEquals(2, ethNames.get("eth0").getIPv6Addresses().size());
            assertEquals(1, ethNames.get("eth0").getGlobalIpv6Addresses().size());
            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("2620:113:80c0:8000:10:161:25:49", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::5054:ff:fed7:4f20", ethNames.get("eth0").getIPv6Addresses().get(1).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("global", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth0").getIPv6Addresses().get(1).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIPv4Addresses().get(0).getAddress());
            assertEquals("10.161.25.49", ethNames.get("eth0").getIPv4Addresses().get(0).getAddress());

            assertEquals("255.0.0.0", ethNames.get("lo").getIPv4Addresses().get(0).getNetmask());
            assertEquals("255.255.192.0", ethNames.get("eth0").getIPv4Addresses().get(0).getNetmask());

            assertEquals(null, ethNames.get("lo").getIPv4Addresses().get(0).getBroadcast());
            assertEquals("10.161.63.255", ethNames.get("eth0").getIPv4Addresses().get(0).getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("ibmveth", ethNames.get("eth0").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());
        });
    }


    public void testHardwareProfileUpdateScsiDevices()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.scsi.x86.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertEquals(1, server.getDevices().stream().filter(d -> "HD".equals(d.getDeviceClass()) && "scsi".equals(d.getBus())).count());
            assertEquals(1, server.getDevices().stream().filter(d -> "CDROM".equals(d.getDeviceClass()) && "ata".equals(d.getBus())).count());
            assertEquals(1, server.getDevices().stream().filter(d -> "scsi".equals(d.getBus())).count());
        });
    }

    public void testHardwareProfileUpdatePrimaryIPv4Only()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4only.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
            assertEquals("172.24.108.98", server.getIpAddress());
            assertEquals("fe80::5054:ff:fefc:19a4", server.getIp6Address());
        });
    }

    public void testHardwareProfileUpdatePrimaryIPv4OnlyLocalhost()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4onlylocalhost.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals(null, ethNames.get("eth1").getPrimary());
            assertEquals("192.168.121.155", server.getIpAddress());
            assertEquals("fe80::1234:ff:fed0:91", server.getIp6Address());
        });
    }

    public void testHardwareProfileUpdatetPrimaryIPv6Only()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv6only.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());
            assertEquals(null, ethNames.get("eth1").getPrimary());

        });
    }

    public void testHardwareProfileUpdatetPrimaryIPV4IPv6()  throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4ipv6.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });
    }

    public void testHardwareProfileChangeNetworkIP()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4ipv6.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });

        HibernateFactory.getSession().flush();

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("hardware.profileupdate.ip_change_ipv4ipv6.x86.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        HibernateFactory.getSession().flush();
    }

    public void testHardwareProfileMultiIP()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.multi-ipv4.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("br0").getPrimary());
            assertEquals(null, ethNames.get("virbr0").getPrimary());
            assertEquals("10.160.5.165,192.168.103.42", ethNames.get("br0").getIPv4AddressesAsString());
            assertEquals("2620:113:80c0:8080:10:160:5:165,2620:113:80c0:8080:1ec:34d9:996:79bf," +
                    "2620:113:80c0:8080:88ac:f1ab:8b:6735,2620:113:80c0:8080:f64d:30ff:fe67:6333," +
                    "fe80::f64d:30ff:fe67:6333",
                    ethNames.get("br0").getIPv6AddressesAsString());
        });
    }

    public void testHardwareProfileNoNetworkIPChange()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.primary_ips_ipv4ipv6.x86.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals(null, ethNames.get("eth0").getPrimary());
            assertEquals("Y", ethNames.get("eth1").getPrimary());
        });

        HibernateFactory.getSession().flush();

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));
        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("hardware.profileupdate.primary_ips_ipv4ipv6.x86.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);
    }

    public void testHardwareProfileUpdatePrimaryIPsEmptySSH()  throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        ServerAction sa = ActionFactoryTest.createServerAction(server, action);
        action.addServerAction(sa);

        JsonObject obj = getJsonElement("hardware.profileupdate.primary_ips_empty_ssh.x86.json");
        JsonElement element = obj.get("suma-ref31-min-centos7.mgr.suse.de");

        Set<NetworkInterface> oldIfs = new HashSet<>();
        oldIfs.addAll(server.getNetworkInterfaces());

        SaltUtils.INSTANCE.updateServerAction(sa, 0L, true, "n/a", element, "state.apply");

        Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                eth -> eth.getName(),
                Function.identity()
        ));
        assertEquals(null, ethNames.get("lo").getPrimary());
        assertEquals(null, ethNames.get("eth0").getPrimary());
        assertEquals("10.162.210.36", server.getIpAddress());
        assertEquals("fe80::a8b2:93ff:fe00:14", server.getIp6Address());
        assertFalse(server.getNetworkInterfaces().containsAll(oldIfs));
        assertFalse(server.getFqdns().isEmpty());
        assertEquals(2, server.getFqdns().size());
    }

    public void testHardwareProfileUpdateS390() throws Exception {
        testHardwareProfileUpdate("hardware.profileupdate.s390.json", (server) -> {
            assertNotNull(server);
            assertNotNull(server.getCpu());

            assertNull(server.getCpu().getNrsocket());
            assertEquals(Long.valueOf(0), server.getCpu().getNrCPU());
            assertEquals("s390x", server.getCpu().getModel());
            assertEquals("0", server.getCpu().getMHz());
            assertEquals("IBM/S390", server.getCpu().getVendor());
            assertNull(server.getCpu().getStepping());
            assertNull(server.getCpu().getFamily());
            assertNull(server.getCpu().getCache());
            assertEquals("2913.00", server.getCpu().getBogomips());
            assertEquals("esan3 zarch stfle msa ldisp eimm dfp etf3eh highgprs",
                    server.getCpu().getFlags());
            assertNull(server.getCpu().getVersion());

            assertNotNull(server.getVirtualInstance());
            assertNotNull(server.getVirtualInstance().getHostSystem());
            assertEquals("z/OS", server.getVirtualInstance().getHostSystem().getOs());
            assertEquals("IBM Mainframe 2827 0000000000069A27", server.getVirtualInstance().getHostSystem().getName());
            assertEquals(Long.valueOf(45), server.getVirtualInstance().getHostSystem().getCpu().getNrCPU());
            assertEquals(Long.valueOf(45), server.getVirtualInstance().getHostSystem().getCpu().getNrsocket());

            assertEquals(VirtualInstanceFactory.getInstance().getFullyVirtType(),
                    server.getVirtualInstance().getType());
            assertNotNull(server.getVirtualInstance().getUuid());
            assertEquals(VirtualInstanceFactory.getInstance().getUnknownState(),
                    server.getVirtualInstance().getState());
            assertEquals(Long.valueOf(1),
                    server.getVirtualInstance().getConfirmed());
            assertNull(server.getDmi());

            assertTrue(!server.getDevices().isEmpty());
            assertTrue(!server.getNetworkInterfaces().isEmpty());

            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));

            assertEquals("00:00:00:00:00:00", ethNames.get("lo").getHwaddr());
            assertEquals("02:00:00:00:42:8e", ethNames.get("eth0").getHwaddr());

            assertEquals("::1", ethNames.get("lo").getIPv6Addresses().get(0).getAddress());
            assertEquals("fe80::ff:fe00:428e", ethNames.get("eth0").getIPv6Addresses().get(0).getAddress());

            assertEquals("128", ethNames.get("lo").getIPv6Addresses().get(0).getNetmask());
            assertEquals("64", ethNames.get("eth0").getIPv6Addresses().get(0).getNetmask());

            assertEquals("host", ethNames.get("lo").getIPv6Addresses().get(0).getScope());
            assertEquals("link", ethNames.get("eth0").getIPv6Addresses().get(0).getScope());

            assertEquals("127.0.0.1", ethNames.get("lo").getIPv4Addresses().get(0).getAddress());
            assertEquals("10.161.155.142", ethNames.get("eth0").getIPv4Addresses().get(0).getAddress());

            assertEquals("255.0.0.0", ethNames.get("lo").getIPv4Addresses().get(0).getNetmask());
            assertEquals("255.255.240.0", ethNames.get("eth0").getIPv4Addresses().get(0).getNetmask());

            assertEquals("127.255.255.255", ethNames.get("lo").getIPv4Addresses().get(0).getBroadcast());
            assertEquals(null, ethNames.get("eth0").getIPv4Addresses().get(0).getBroadcast());

            assertEquals(null, ethNames.get("lo").getModule());
            assertEquals("qeth", ethNames.get("eth0").getModule());

            assertEquals(null, ethNames.get("lo").getPrimary());
            assertEquals("Y", ethNames.get("eth0").getPrimary());

        });
    }


    private MinionServer testHardwareProfileUpdate(String jsonFile, Consumer<MinionServer> assertions) throws Exception{
        // Prepare test objects: minion server and action
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");

        Action action = ActionFactoryTest.createAction(
                user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        action.addServerAction(ActionFactoryTest.createServerAction(server, action));

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent(jsonFile, action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertions.accept(server);
        return server;
    }

    public void testHardwareProfileInfiniband()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.infiniband.json", (server) -> {
            Map<String, NetworkInterface> ethNames = server.getNetworkInterfaces().stream().collect(Collectors.toMap(
                    eth -> eth.getName(),
                    Function.identity()
            ));
            assertEquals(59, ethNames.get("ib0.8001").getHwaddr().length());
        });
    }

    public void testHardwareProfileXenHost()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.xen-host.json", (server) -> {
            assertFalse("system should not be a virtual guest", server.isVirtualGuest());
        });
    }

    public void testHardwareProfilePublicCloud()  throws Exception {
        MinionServer minion = testHardwareProfileUpdate("hardware.profileupdate.public_cloud.json", (server) -> {
            assertNotNull(server);
            assertEquals("iabcdef1234567890", server.getVirtualInstance().getUuid());
        });
    }

    /**
     * Read a Salt job return event while substituting the corresponding action id.
     *
     * @param filename the filename to read from
     * @param actionId the id of the action to correlate this Salt job with
     * @return event object parsed from the json file
     */
    private Event getJobReturnEvent(String filename, long actionId) throws Exception {
        return getJobReturnEvent(filename, actionId, null);
    }

    /**
     * Read a Salt job return event while substituting the corresponding action id
     * and placeholders.
     *
     * @param filename the filename to read from
     * @param actionId the id of the action to correlate this Salt job with
     * @param placeholders map of placeholders to substitute
     * @return event object parsed from the json file
     */
    private Event getJobReturnEvent(String filename, long actionId, Map<String, String> placeholders) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        if (placeholders != null) {
            for (Map.Entry<String, String> entries : placeholders.entrySet()) {
                String placeholder = entries.getKey();
                String value = entries.getValue();
                eventString = StringUtils.replace(eventString, placeholder, value);
            }
        }
        return EVENTS.parse(eventString);
    }

    private JsonObject getJsonElement(String filename) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String jsonString = Files.lines(path)
                .collect(Collectors.joining("\n"));
        return JsonParser.GSON.fromJson(jsonString, JsonObject.class);
    }


    public void testUpdateServerAction() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("abcdefg.vagrant.local");
        SUSEProductTestUtils.createVendorSUSEProducts();

        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(minion.getId()),
                Arrays.asList(ApplyStatesEventMessage.CHANNELS),
                new Date());

        ServerAction sa = ActionFactoryTest.createServerAction(minion, action);

        action.addServerAction(sa);

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("state.apply.with.failures.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });


        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertEquals(
            Arrays.asList(sa),
            action.getServerActions().stream().filter(
                serverAction -> serverAction.getServer().equals(minion)
            ).collect(java.util.stream.Collectors.toList()));

        // Verify the action status
        assertTrue(sa.getStatus().equals(ActionFactory.STATUS_FAILED));
        context().assertIsSatisfied();
    }

    public void testOpenscap() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });


        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12sp1.test.local");
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCAP, 1L);
        ScapAction action = ActionManager.scheduleXccdfEval(user,
                minion, "/usr/share/openscap/scap-yast2sec-xccdf.xml", "--profile Default", new Date());

        ServerAction sa = ActionFactoryTest.createServerAction(minion, action);

        action.addServerAction(sa);

        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("openscap.xccdf.success.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();

        File scapFile = new File(TestUtils.findTestDataInDir(
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/results.xml").getPath());
        String resumeXsl = new File(TestUtils.findTestData(
                "/com/redhat/rhn/manager/audit/test/openscap/minionsles12sp1.test.local/xccdf-resume.xslt.in").getPath())
                .getPath();

        JsonElement jsonElement = message.getJobReturnEvent().getData().getResult(JsonElement.class);


        TypeToken<Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>>> typeToken =
                new TypeToken<Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>>>() { };
        Map<String, StateApplyResult<Ret<Openscap.OpenscapResult>>> stateResult = Json.GSON.fromJson(
                jsonElement, typeToken.getType());
        Openscap.OpenscapResult openscapResult = stateResult.entrySet().stream().findFirst().map(e -> e.getValue().getChanges().getRet())
                .orElseThrow(() -> new RuntimeException("missiong scap result"));

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).storeMinionScapFiles(
                    with(any(MinionServer.class)),
                    with(openscapResult.getUploadDir()),
                    with(action.getId()));
            Map<Boolean, String> result = new HashMap<>();
            result.put(true, scapFile.getParent());
            will(returnValue(result));
        }});

        SaltUtils.INSTANCE.setXccdfResumeXsl(resumeXsl);
        SaltUtils.INSTANCE.setSystemQuery(saltServiceMock);
        messageAction.execute(message);

        assertEquals(ActionFactory.STATUS_COMPLETED, sa.getStatus());
    }

    /**
     * Build and inspect the same profile twice.
     * Check that the same ImageInfo instance is kept during successive runs and that the build history and revision
     * number are filled correctly.
     * @throws Exception
     */
    public void testContainerImageBuild()  throws Exception {
        String digest1 = "1111111111111111111111111111111111111111111111111111111111111111";
        String digest2 = "2222222222222222222222222222222222222222222222222222222222222222";
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST);

        String imageName = "matei-apache-python" + TestUtils.randomString(5);
        String imageVersion = "latest";

        ImageStore store = ImageTestUtils.createImageStore("test-docker-registry:5000", user);
        ImageProfile profile = ImageTestUtils.createImageProfile(imageName, store, user);

        ImageInfo imgInfoBuild1 = doTestContainerImageBuild(server, imageName, imageVersion, profile,
                (imgInfo) -> {
                    // assert initial revision number
                    assertEquals(1, imgInfo.getRevisionNumber());
                } );
        doTestContainerImageInspect(server, imageName, imageVersion, profile, imgInfoBuild1,
                digest1,
                (imgInfo) -> {
            // assertions after inspect
            // reload imgInfo to get the build history
            imgInfo = TestUtils.reload(imgInfo);
            assertNotNull(imgInfo);
            // test that the history of the build was updated correctly
            assertEquals(1, imgInfo.getBuildHistory().size());
            assertEquals(1, imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).count());
            assertEquals(
                    "docker-registry:5000/" + imageName + "@sha256:" + digest1,
                    imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).findFirst().get().getRepoDigest());
            assertEquals(1,
                    imgInfo.getBuildHistory().stream().findFirst().get().getRevisionNumber());
        });

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        doTestContainerImageBuild(server, imageName, imageVersion, profile,
                (imgInfo) -> {
                    // assert revision number incremented
                    assertEquals(2, imgInfo.getRevisionNumber());
                    // assert ImageInfo instance didn't change after second build
                    assertEquals(imgInfoBuild1.getId(), imgInfo.getId());
                } );
        doTestContainerImageInspect(server, imageName, imageVersion, profile, imgInfoBuild1,
                digest2,
                (imgInfo) -> {
            // reload imgInfo to get the build history
            imgInfo = TestUtils.reload(imgInfo);
            assertNotNull(imgInfo);
            // test that history for the second build is present
            assertEquals(2, imgInfo.getBuildHistory().size());
            assertEquals(2, imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).count());
            assertEquals(
                    "docker-registry:5000/" + imageName + "@sha256:" + digest1,
                    imgInfo.getBuildHistory().stream()
                            .filter(hist -> hist.getRevisionNumber() == 1)
                            .flatMap(h -> h.getRepoDigests().stream())
                            .findFirst().get().getRepoDigest());
            assertEquals(
                    "docker-registry:5000/" + imageName + "@sha256:" + digest2,
                    imgInfo.getBuildHistory().stream()
                            .filter(hist -> hist.getRevisionNumber() == 2)
                            .flatMap(h -> h.getRepoDigests().stream())
                            .findFirst().get().getRepoDigest());
            assertEquals(2, imgInfo.getBuildHistory().size());
            assertTrue(
                    imgInfo.getBuildHistory().stream().anyMatch(h -> h.getRevisionNumber() == 1));
            assertTrue(
                    imgInfo.getBuildHistory().stream().anyMatch(h -> h.getRevisionNumber() == 2));

        });
    }

    /**
     * Build and inspect two versions of the same image.
     * @throws Exception
     */
    public void testContainerImageBuildMultipleVersions()  throws Exception {
        String digest = "1111111111111111111111111111111111111111111111111111111111111111";
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST);

        String imageName = "meaksh-apache-python" + TestUtils.randomString(5);
        String imageVersion1 = "latest";
        String imageVersion2 = "anotherversion";

        ImageStore store = ImageTestUtils.createImageStore("test-docker-registry:5000", user);
        ImageProfile profile = ImageTestUtils.createImageProfile(imageName, store, user);

        ImageInfo imgInfoBuild1 = doTestContainerImageBuild(server, imageName, imageVersion1, profile,
                (imgInfo) -> {
                    // assert initial revision number
                    assertEquals(1, imgInfo.getRevisionNumber());
                } );
        doTestContainerImageInspect(server, imageName, imageVersion1, profile, imgInfoBuild1,
                digest,
                (imgInfo) -> {
            // assertions after inspect
            // reload imgInfo to get the build history
            imgInfo = TestUtils.reload(imgInfo);
            assertNotNull(imgInfo);
            // test that the history of the build was updated correctly
            assertEquals(1, imgInfo.getBuildHistory().size());
            assertEquals(1, imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).count());
            assertEquals(
                    "docker-registry:5000/" + imageName + "@sha256:" + digest,
                    imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).findFirst().get().getRepoDigest());
            assertEquals(1,
                    imgInfo.getBuildHistory().stream().findFirst().get().getRevisionNumber());
        });

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ImageInfo imgInfoBuild2 = doTestContainerImageBuild(server, imageName, imageVersion2, profile,
                (imgInfo) -> {
                    // assert revision number incremented
                    assertEquals(1, imgInfo.getRevisionNumber());
                    assertFalse(imgInfoBuild1.getId().equals(imgInfo.getId()));
                } );
        doTestContainerImageInspect(server, imageName, imageVersion2, profile, imgInfoBuild2,
                digest,
                (imgInfo) -> {
            // reload imgInfo to get the build history
            imgInfo = TestUtils.reload(imgInfo);
            assertNotNull(imgInfo);
            // test that history for the second build is present
            assertEquals(1, imgInfo.getBuildHistory().size());
            assertEquals(1, imgInfo.getBuildHistory().stream().flatMap(h -> h.getRepoDigests().stream()).count());
            assertEquals(
                    "docker-registry:5000/" + imageName + "@sha256:" + digest,
                    imgInfo.getBuildHistory().stream()
                            .filter(hist -> hist.getRevisionNumber() == 1)
                            .flatMap(h -> h.getRepoDigests().stream())
                            .findFirst().get().getRepoDigest());
            assertEquals(1, imgInfo.getBuildHistory().size());
            assertTrue(
                    imgInfo.getBuildHistory().stream().anyMatch(h -> h.getRevisionNumber() == 1));

        });
    }

    /* Test container image import feature: imports an image and checks if image info has been populated.
     * @throws Exception
     */
    public void testContainerImageImport()  throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minionsles12-suma3pg.vagrant.local");
        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.CONTAINER_BUILD_HOST);

        String imageName = "mbologna-apache-python" + TestUtils.randomString(5);
        String imageVersion = "latest";

        ImageStore store = ImageTestUtils.createImageStore("test-docker-registry:5000", user);

        doTestContainerImageImport(server, imageName, imageVersion, store,
                (imgInfo) -> {
                    // reload imgInfo to get the build history
                    imgInfo = TestUtils.reload(imgInfo);
                    assertNotNull(imgInfo);
                    // test that history for the second build is present
                    assertEquals(imageName, imgInfo.getName());
                    assertNull(imgInfo.getBuildAction());
                    assertNotNull(imgInfo.getInspectAction());
                });
    }

    private void doTestContainerImageInspect(MinionServer server, String imageName, String imageVersion, ImageProfile profile, ImageInfo imgInfo,
                                             String digest,
                                             Consumer<ImageInfo> assertions) throws Exception {
        // schedule an inspect action
        ImageInspectAction inspectAction = ActionManager.scheduleImageInspect(
                user,
                Collections.singletonList(server.getId()),
                Optional.empty(),
                imageVersion,
                profile.getLabel(),
                profile.getTargetStore(),
                Date.from(Instant.now()));
        TestUtils.reload(inspectAction);
        // Process the image inspect return event
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("$IMAGE$", imageName);
        placeholders.put("$DIGEST$", digest);

        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("image.profileupdate.json",
                        inspectAction.getId(),
                        placeholders
                        ));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // assertions after inspect
        assertions.accept(imgInfo);
    }

    private ImageInfo doTestContainerImageBuild(MinionServer server, String imageName, String imageVersion, ImageProfile profile, Consumer<ImageInfo> assertions) throws Exception {
        // schedule the build
        long actionId = ImageInfoFactory.scheduleBuild(server.getId(), imageVersion, profile, new Date(), user);
        ImageBuildAction buildAction = (ImageBuildAction) ActionFactory.lookupById(actionId);
        TestUtils.reload(buildAction);
        Optional<ImageInfo> imgInfoBuild = ImageInfoFactory.lookupByBuildAction(buildAction);
        assertTrue(imgInfoBuild.isPresent());

        // Process the image build return event
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("image.build.dockerfile.json",
                        actionId,
                        Collections.singletonMap("$IMAGE$", imageName)));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // assert we have the same initial ImageInfo even after processing the event
        assertTrue(ImageInfoFactory.lookupById(imgInfoBuild.get().getId()).isPresent());
        ImageInfo imgInfo = TestUtils.reload(imgInfoBuild.get());
        assertNotNull(imgInfo);

        // other assertions after build
        assertions.accept(imgInfoBuild.get());

        return imgInfoBuild.get();
    }

    private ImageInfo doTestContainerImageImport(MinionServer server, String imageName, String imageVersion, ImageStore store, Consumer<ImageInfo> assertions) throws Exception {
        Long actionId = ImageInfoFactory.scheduleImport(server.getId(), imageName, imageVersion, store, Optional.empty(), new Date(), user);
        Action action = ActionFactory.lookupById(actionId);
        TestUtils.reload(action);

        // Process the image inspect return event
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("$IMAGE$", imageName);
        placeholders.put("$DIGEST$", "ffff");

        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("image.profileupdate.json",
                        actionId,
                        placeholders
                ));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertTrue(ImageInfoFactory.lookupByName(imageName, imageVersion, store.getId()).isPresent());
        ImageInfo imgInfo = ImageInfoFactory.lookupByName(imageName, imageVersion, store.getId()).get();
        assertNotNull(imgInfo);

        // other assertions after inspection
        assertions.accept(imgInfo);

        return imgInfo;
    }

    public void testKiwiImageBuild() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minion.local");
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        ServerFactory.save(server);

        MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.ExecResult();
        context().checking(new Expectations() {{
            allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)));
            allowing(saltServiceMock).collectKiwiImage(with(equal(server)),
                    with(equal("/var/lib/Kiwi/build06/images/POS_Image_JeOS6.x86_64-6.0.0-build06.tgz")),
                    with(equal(String.format("/srv/www/os-images/%d/", user.getOrg().getId()))));
            will(returnValue(Optional.of(mockResult)));
        }});
        SaltUtils.INSTANCE.setSystemQuery(saltServiceMock);

        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.OSIMAGE_BUILD_HOST);

        ActivationKey key = ImageTestUtils.createActivationKey(user);
        ImageProfile profile = ImageTestUtils.createKiwiImageProfile("my-kiwi-image", key, user);

        doTestKiwiImageBuild(server, "my-kiwi-image", profile, (info) -> {
            assertEquals(1, info.getRevisionNumber());
            assertEquals("foo123", info.getVersion());
            assertNotNull(info.getChecksum());
            assertEquals("a46cbaad0679e40ea53d0907ed42e00030142b0b9372c9ebc0ba6b9dde5df6b",
                    info.getChecksum().getChecksum());
        });
    }

    public void testKiwiImageInspect() throws Exception {
        ImageInfoFactory.setTaskomaticApi(getTaskomaticApi());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setMinionId("minion.local");
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        ServerFactory.save(server);

        MgrUtilRunner.ExecResult mockResult = new MgrUtilRunner.ExecResult();
        context().checking(new Expectations() {{
            allowing(saltServiceMock).generateSSHKey(with(equal(SaltSSHService.SSH_KEY_PATH)));
            allowing(saltServiceMock).collectKiwiImage(with(equal(server)),
                    with(equal("/var/lib/Kiwi/build06/images/POS_Image_JeOS6.x86_64-6.0.0-build06.tgz")),
                    with(equal(String.format("/srv/www/os-images/%d/", user.getOrg().getId()))));
            will(returnValue(Optional.of(mockResult)));
        }});
        SaltUtils.INSTANCE.setSystemQuery(saltServiceMock);

        systemEntitlementManager.addEntitlementToServer(server, EntitlementManager.OSIMAGE_BUILD_HOST);

        new File("/srv/susemanager/pillar_data/images").mkdirs();

        ActivationKey key = ImageTestUtils.createActivationKey(user);
        ImageProfile profile = ImageTestUtils.createKiwiImageProfile("my-kiwi-image", key, user);

        doTestKiwiImageInspect(server, "my-kiwi-image", profile, (info) -> {
            assertNotNull(info.getInspectAction().getId());
            assertEquals(286, info.getPackages().size());
            File generatedPillar = new File("/srv/susemanager/pillar_data/images/image-POS_Image_JeOS6.x86_64-6.0.0-build24.sls");

            assertTrue(generatedPillar.exists());
            Map<String, Object> map;

            try (FileInputStream fi = new FileInputStream(generatedPillar)) {
                map = new Yaml().loadAs(fi, Map.class);
                assertTrue(map.containsKey("boot_images"));
                Map<String, Map<String, Map<String, Object>>> bootImages = (Map<String, Map<String, Map<String, Object>>>) map.get("boot_images");
                assertEquals("tftp://tftp/boot/POS_Image_JeOS6-6.0.0/initrd-netboot-suse-SLES12.x86_64-2.1.1.gz", bootImages.get("POS_Image_JeOS6-6.0.0").get("initrd").get("url"));
                Map<String, Map<String, Map<String, Object>>> images = (Map<String, Map<String, Map<String, Object>>>) map.get("images");
                assertEquals("ftp://ftp/image/POS_Image_JeOS6.x86_64-6.0.0-build24/POS_Image_JeOS6.x86_64-6.0.0", images.get("POS_Image_JeOS6").get("6.0.0").get("url"));
                assertEquals(1490026496, images.get("POS_Image_JeOS6").get("6.0.0").get("size"));
                assertEquals("a64dbc025c748bde968b888db6b7b9e3", images.get("POS_Image_JeOS6").get("6.0.0").get("hash"));
            } catch (FileNotFoundException e) {
                fail("Cannot find OS Image generated pillar");
            } catch (IOException e) {
                fail("Cannot find OS Image generated pillar");
            }
        });
    }

    private ImageInfo doTestKiwiImageBuild(MinionServer server, String imageName,
                                           ImageProfile profile, Consumer<ImageInfo> assertions)
            throws Exception {
        // schedule the build
        long actionId = ImageInfoFactory.scheduleBuild(server.getId(), "foo123", profile, new Date(), user);
        ImageBuildAction buildAction = (ImageBuildAction) ActionFactory.lookupById(actionId);
        TestUtils.reload(buildAction);
        Optional<ImageInfo> imgInfoBuild = ImageInfoFactory.lookupByBuildAction(buildAction);
        assertTrue(imgInfoBuild.isPresent());

        // Process the image build return event
        Optional<JobReturnEvent> event =
                JobReturnEvent.parse(getJobReturnEvent("image.build.kiwi.json", actionId));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // assert we have the same initial ImageInfo even after processing the event
        assertTrue(ImageInfoFactory.lookupById(imgInfoBuild.get().getId()).isPresent());
        ImageInfo imgInfo = TestUtils.reload(imgInfoBuild.get());
        assertNotNull(imgInfo);

        // other assertions after build
        assertions.accept(imgInfoBuild.get());

        return imgInfoBuild.get();
    }

    private ImageInfo doTestKiwiImageInspect(MinionServer server, String imageName,
                                             ImageProfile profile, Consumer<ImageInfo> assertions)
            throws Exception {
        // schedule the build
        long actionId = ImageInfoFactory.scheduleBuild(server.getId(), null, profile,
                new Date(), user);
        ImageBuildAction buildAction =
                (ImageBuildAction) ActionFactory.lookupById(actionId);
        TestUtils.reload(buildAction);
        Optional<ImageInfo> imgInfoBuild = ImageInfoFactory.lookupByBuildAction(buildAction);
        assertTrue(imgInfoBuild.isPresent());

        actionId = ImageInfoFactory.scheduleInspect(imgInfoBuild.get(), new Date(), user);

        // schedule an inspect action
        ImageInspectAction inspectAction = (ImageInspectAction) ActionFactory.lookupById(actionId);
        TestUtils.reload(inspectAction);
        // Process the image inspect return event
        Optional<JobReturnEvent> event = JobReturnEvent
                .parse(getJobReturnEvent("image.inspect.kiwi.json", actionId));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertTrue(ImageInfoFactory.lookupById(imgInfoBuild.get().getId()).isPresent());
        ImageInfo imgInfo = TestUtils.reload(imgInfoBuild.get());

        assertions.accept(imgInfo);

        return imgInfo;
    }

    public TaskomaticApi getTaskomaticApi() throws TaskomaticApiException {
        if (taskomaticApi == null) {
            taskomaticApi = context().mock(TaskomaticApi.class);
            context().checking(new Expectations() {
                {
                    allowing(taskomaticApi)
                            .scheduleActionExecution(with(any(Action.class)));
                }
            });
        }

        return taskomaticApi;
    }

    public void testNoRegisterOnInexistentMinionReturnEvent() throws Exception {
        int initialMessageCount = MessageQueue.getMessageCount();
        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("openscap.xccdf.success.json", 123));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertEquals(initialMessageCount, MessageQueue.getMessageCount());
    }

    public void testSubscribeChannelsActionSuccess() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        SaltServerActionService saltServerActionService = new SaltServerActionService(new SaltService());

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("dev-minsles12sp2.test.local");

        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch1.setParentChannel(base);
        ch2.setParentChannel(base);

        Optional<Channel> baseChannel = Optional.of(base);
        Set<Channel> channels = new HashSet<>();
        channels.add(ch1);
        channels.add(ch2);

        Set<Action> actions = ActionChainManager.scheduleSubscribeChannelsAction(user,
                Collections.singleton(minion.getId()),
                baseChannel,
                channels,
                new Date(),
                null);

        Action action = actions.stream().findFirst().get();

        ServerAction sa = ActionFactoryTest.createServerAction(minion, action);
        action.addServerAction(sa);

        saltServerActionService.setCommitTransaction(false);
        Map<LocalCall<?>, List<MinionSummary>> calls = saltServerActionService.callsForAction(action);

        HibernateFactory.getSession().flush();

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("subscribe.channels.success.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        assertEquals(ActionFactory.STATUS_COMPLETED, sa.getStatus());
        assertEquals(0L, (long)sa.getResultCode());
        assertEquals(baseChannel.get().getId(), minion.getBaseChannel().getId());
        assertEquals(2, minion.getChildChannels().size());
        assertTrue(minion.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch1.getId())));
        assertTrue(minion.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch2.getId())));

        assertEquals(3, minion.getAccessTokens().size());
        assertTokenChannel(minion, base);
        assertTokenChannel(minion, ch1);
        assertTokenChannel(minion, ch2);
    }

    public void testSubscribeChannelsActionNullTokens() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)),
                    with(any(SubscribeChannelsAction.class)));
        } });
        SaltServerActionService saltServerActionService = new SaltServerActionService(new SaltService());

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("dev-minsles12sp2.test.local");

        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch1.setParentChannel(base);
        ch2.setParentChannel(base);

        Optional<Channel> baseChannel = Optional.of(base);
        Set<Channel> channels = new HashSet<>();
        channels.add(ch1);
        channels.add(ch2);

        Set<Action> actions = ActionChainManager.scheduleSubscribeChannelsAction(user,
                Collections.singleton(minion.getId()),
                baseChannel,
                channels,
                new Date(),
                null);

        SubscribeChannelsAction action = (SubscribeChannelsAction) actions.stream().findFirst().get();

        ServerAction sa = ActionFactoryTest.createServerAction(minion, action);
        action.addServerAction(sa);

        saltServerActionService.setCommitTransaction(false);
        Map<LocalCall<?>, List<MinionSummary>> calls = saltServerActionService.callsForAction(action);

        // artifically expire tokens
        action.getDetails().getAccessTokens().stream().forEach(t -> t.setMinion(null));
        HibernateFactory.getSession().flush();

        // Setup an event message from file contents
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("subscribe.channels.success.json", action.getId()));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        // check that tokens are really gone
        assertEquals(0, minion.getAccessTokens().size());
    }

    private void assertTokenChannel(MinionServer minion, Channel channel) {
        assertTrue(channel.getLabel(), minion.getAccessTokens().stream()
                .filter(token -> token.getChannels().size() == 1 && token.getChannels().contains(channel))
                .findFirst().isPresent());
    }

    public void testFailDependentServerActions() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        Action applyStateAction = ActionFactoryTest.createAction(user, ActionFactory.TYPE_APPLY_STATES);
        Action rebootAction = ActionFactoryTest.createAction(user,ActionFactory.TYPE_REBOOT);
        Action runScriptAction = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        //Add dependency
        rebootAction.setPrerequisite(applyStateAction);
        runScriptAction.setPrerequisite(rebootAction);

        ActionFactory.addServerToAction(minion.getId(), applyStateAction);
        ActionFactory.addServerToAction(minion.getId(), rebootAction);
        ActionFactory.addServerToAction(minion.getId(), runScriptAction);

        //need to flush to get the correct dates in database
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        applyStateAction.getServerActions().stream().findFirst().get().setStatus(ActionFactory.STATUS_FAILED);

        JobReturnEventMessageAction.failDependentServerActions(applyStateAction.getId(),
                minion.getMinionId(), Optional.empty());

        applyStateAction =  ActionFactory.lookupById(applyStateAction.getId());
        rebootAction = ActionFactory.lookupById(rebootAction.getId());
        runScriptAction =  ActionFactory.lookupById(runScriptAction.getId());

        ServerAction applyStateServerAction = applyStateAction.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId())).findFirst().get();
        ServerAction rebootSeverAction = rebootAction.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId())).findFirst().get();
        ServerAction runScriptSeverAction = runScriptAction.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId())).findFirst().get();

        assertEquals(ActionFactory.STATUS_FAILED, applyStateServerAction.getStatus());
        assertEquals(ActionFactory.STATUS_FAILED, rebootSeverAction.getStatus());
        assertEquals(ActionFactory.STATUS_FAILED, runScriptSeverAction.getStatus());

        //Check the output of dependent actions
        assertEquals("Prerequisite failed", rebootSeverAction.getResultMsg());
        assertEquals("Prerequisite failed", runScriptSeverAction.getResultMsg());
    }

    public void testActionChainResponse() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)),
                    with(any(SubscribeChannelsAction.class)));
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        Date earliestAction = new Date();
        ApplyStatesAction applyHighstate = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(minion.getId()),
                new ArrayList<>(),
                earliestAction);

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", 10L, "#!/bin/csh\necho hello");
        ScriptRunAction runScript = ActionManager.scheduleScriptRun(
                user, Arrays.asList(minion.getId()), "Run script test", sad, earliestAction);

        ServerAction saHighstate = ActionFactoryTest.createServerAction(minion, applyHighstate);
        applyHighstate.addServerAction(saHighstate);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${minion-id}", minion.getMinionId());
        placeholders.put("${action1-id}", applyHighstate.getId() + "");
        placeholders.put("${action2-id}", runScript.getId() + "");

        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("action.chain.one.chunk.json", applyHighstate.getId(),
                        placeholders));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);

        applyHighstate = (ApplyStatesAction)ActionFactory.lookupById(applyHighstate.getId());
        saHighstate = applyHighstate.getServerActions().stream().findFirst().get();

        assertEquals(ActionFactory.STATUS_COMPLETED, saHighstate.getStatus());
        assertEquals(0L, (long)saHighstate.getResultCode());
        assertEquals(minion.getId(), saHighstate.getServer().getId());
        assertEquals("Successfully applied state(s): highstate", saHighstate.getResultMsg());

        runScript = (ScriptRunAction)ActionFactory.lookupById(runScript.getId());
        ServerAction saScript = runScript.getServerActions().stream().findFirst().get();

        assertEquals(ActionFactory.STATUS_COMPLETED, saScript.getStatus());
        assertEquals(0L, (long)saScript.getResultCode());
        assertEquals(minion.getId(), saScript.getServer().getId());
        ScriptResult scriptResult = runScript.getScriptActionDetails().getResults()
                .stream()
                .filter(res -> saScript.getServerId().equals(res.getServerId()))
                .findFirst().get();
        assertEquals(
                "total 12\n" +
                "drwxr-xr-x  2 root root 4096 Sep 21  2014 bin\n" +
                "-rwxr-xr-x  1 root root 1636 Sep 12 17:07 netcat.py\n" +
                "drwxr-xr-x 14 root root 4096 Jul 25  2017 salt\n",
                new String(scriptResult.getOutput()));
    }

    public void testActionChainPackageRefreshNeeded() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {
            {
                allowing(taskomaticMock).scheduleActionExecution(with(any(PackageAction.class)));
            }
        });

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        ApplyStatesAction applyHighstate = ActionManager.scheduleApplyStates(user, Arrays.asList(minion.getId()),
                new ArrayList<>(), new Date());

        ServerAction saHighstate = ActionFactoryTest.createServerAction(minion, applyHighstate);
        applyHighstate.addServerAction(saHighstate);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${minion-id}", minion.getMinionId());
        placeholders.put("${action1-id}", applyHighstate.getId() + "");

        Optional<JobReturnEvent> event = JobReturnEvent.parse(getJobReturnEvent("action.chain.refresh.needed.json", applyHighstate.getId(),
                        placeholders));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);
        List<Action> serversActions = ActionFactory.listActionsForServer(user, minion);
        //Verify that there are 2 actions scheduled, one apply state that we scheduled above and
        //2nd was because full package refresh was needed.
        assertEquals("2 actions have been scheduled for server 1", 2, serversActions.size());
    }

    public void testActionChainPackageRefreshNotNeeded() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {
            {
                allowing(taskomaticMock).scheduleActionExecution(with(any(PackageAction.class)));
            }
        });

        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        ApplyStatesAction applyHighstate = ActionManager.scheduleApplyStates(user, Arrays.asList(minion.getId()),
                new ArrayList<>(), new Date());

        ServerAction saHighstate = ActionFactoryTest.createServerAction(minion, applyHighstate);
        applyHighstate.addServerAction(saHighstate);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${minion-id}", minion.getMinionId());
        placeholders.put("${action1-id}", applyHighstate.getId() + "");

        Optional<JobReturnEvent> event = JobReturnEvent.parse(getJobReturnEvent("action.chain.refresh.not.needed.json", applyHighstate.getId(),
                placeholders));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);
        List<Action> serversActions = ActionFactory.listActionsForServer(user, minion);
        //Verify that there is only one action scheduled, the apply state one that we scheduled above
        assertEquals("2 actions have been scheduled for server 1", 1, serversActions.size());
    }

    public void testMinionStartupResponse() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        String runningKernel = minion.getRunningKernel();
        Long lastBoot = minion.getLastBoot();
        String name = minion.getName();
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${minion-id}", minion.getMinionId());
        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("minion.startup.applied.state.response.json", 0,
                        placeholders));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        // Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction();
        messageAction.execute(message);
        assertEquals(name, minion.getName());
        assertNotSame(runningKernel, minion.getRunningKernel());
        assertNotSame(lastBoot, minion.getLastBoot());
    }
}
