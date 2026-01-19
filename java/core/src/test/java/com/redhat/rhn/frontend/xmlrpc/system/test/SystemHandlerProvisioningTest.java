/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.system.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Date;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(JUnit5Mockery.class)
public class SystemHandlerProvisioningTest extends BaseHandlerTestCase {

        private final TaskomaticApi taskomaticApi = new TaskomaticApi();
        private final SystemQuery systemQuery = new TestSystemQuery();
        private final SaltApi saltApi = new TestSaltApi();
        private final CloudPaygManager paygManager = new TestCloudPaygManagerBuilder().build();
        private final AttestationManager attestationManager = new AttestationManager();
        private final RegularMinionBootstrapper regularMinionBootstrapper =
                new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
        private final SSHMinionBootstrapper sshMinionBootstrapper =
                new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
        private final XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
                regularMinionBootstrapper,
                sshMinionBootstrapper
        );
        private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
        );
        private final SystemManager systemManager =
                new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);
        private final SystemHandler handler =
                new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager, systemManager,
                        serverGroupManager, new TestCloudPaygManagerBuilder().build(), new AttestationManager());
        private HttpServletRequest mockRequest;

        @RegisterExtension
        protected final JUnit5Mockery mockContext = new JUnit5Mockery() {{
                setThreadingPolicy(new Synchroniser());
                setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        }};

        @Override
        @BeforeEach
        public void setUp() throws Exception {
                super.setUp();
                mockRequest = mockContext.mock(HttpServletRequest.class);

                TaskomaticApi testApi = new TaskomaticApi() {
                        @Override
                        public void scheduleActionExecution(Action action) {
                                // do not call API in a test
                        }
                };
                KickstartScheduleCommand.setTaskomaticApi(testApi);
        }

        @AfterEach
        public void tearDown() throws Exception {
                KickstartScheduleCommand.setTaskomaticApi(new TaskomaticApi());
        }

        @Test
        public void testProvisionSystem() throws Exception {
                Server server = ServerTestUtils.createTestSystem(admin);
                systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.SALT);
                TestUtils.saveAndFlush(server);
                server = reload(server);

                // salt-minion package has to be added to pass Kickstart validator
                Package testPackage = PackageTest.createTestPackage(admin.getOrg(), "salt-minion");
                PackageManagerTest.associateSystemToPackage(server, testPackage);

                KickstartDataTest.setupTestConfiguration(admin);
                KickstartData k = KickstartDataTest.createKickstartWithProfile(admin);
                KickstartDataTest.addCommand(admin, k,
                        "custom", "echo test-command");
                k.getKickstartDefaults().getKstree().setChannel(server.getBaseChannel());
                String profileName = k.getLabel();
                Map<String, String> advancedOptions = Map.of(
                        "kernel_options", "console=tty0", "post_kernel_options", "console=tty1"
                );
                RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();

                int result = 0;
                result = handler.provisionSystem(admin, request, server.getId().intValue(), null, profileName,
                        new Date(), advancedOptions);

                // something was scheduled
                assertNotEquals(0, result);

                // KS action is what we scheduled
                List<KickstartSession> res = KickstartFactory.lookupAllKickstartSessionsByServer(server.getId());
                assertFalse(res.isEmpty());
                KickstartSession ks = res.get(0);
                assertNotNull(ks);
                assertEquals(server.getId(), ks.getNewServer().getId());
                assertEquals("echo test-command", ks.getKsdata().getCommand("custom").getArguments());

                // Action details to check correct host
                List<Action> actions = ActionFactory.listActionsForServer(admin, server);
                assertNotNull(actions);
                KickstartInitiateAction kia = (KickstartInitiateAction) actions.get(0);
                KickstartActionDetails kad = kia.getKickstartActionDetails();
                assertEquals(" console=tty0", kad.getAppendString());
                assertEquals(ConfigDefaults.get().getHostname(), kad.getKickstartHost());

                // Cobbler kernel and post kernel options
                CobblerConnection con = CobblerXMLRPCHelper.getConnection(admin);
                SystemRecord rec =  SystemRecord.lookupById(con, server.getCobblerId());
                assertEquals(" console=tty0", rec.getKernelOptions().get());
                assertEquals("console=tty1", rec.getKernelOptionsPost().get());
        }

        @Test
        public void testProvisionSystemFromProxy() throws Exception {
                Server server = ServerTestUtils.createTestSystem(admin);
                systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.SALT);
                TestUtils.saveAndFlush(server);
                server = reload(server);

                // salt-minion package has to be added to pass Kickstart validator
                Package testPackage = PackageTest.createTestPackage(admin.getOrg(), "salt-minion");
                PackageManagerTest.associateSystemToPackage(server, testPackage);

                Server proxy = ServerTestUtils.createTestSystem(admin);
                systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.SALT);
                proxy.setHostname("proxy.example.com");
                SystemManager.activateProxy(proxy, "4.3");
                TestUtils.saveAndFlush(proxy);
                proxy = reload(proxy);

                KickstartDataTest.setupTestConfiguration(admin);
                KickstartData k = KickstartDataTest.createKickstartWithProfile(admin);
                KickstartDataTest.addCommand(admin, k,
                        "custom", "echo test-command 2");
                k.getKickstartDefaults().getKstree().setChannel(server.getBaseChannel());
                String profileName = k.getLabel();
                RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();

                int result = 0;
                result = handler.provisionSystem(admin, request, server.getId().intValue(),
                        proxy.getId().intValue(), profileName);

                // something was scheduled
                assertNotEquals(0, result);

                // KS action is what we scheduled
                List<KickstartSession> res = KickstartFactory.lookupAllKickstartSessionsByServer(server.getId());
                assertFalse(res.isEmpty());
                KickstartSession ks = res.get(0);
                assertNotNull(ks);
                assertEquals(server.getId(), ks.getNewServer().getId());
                assertEquals("echo test-command 2", ks.getKsdata().getCommand("custom").getArguments());

                // Action details to check correct host
                List<Action> actions = ActionFactory.listActionsForServer(admin, server);
                assertNotNull(actions);
                KickstartInitiateAction kia = (KickstartInitiateAction) actions.get(0);
                KickstartActionDetails kad = kia.getKickstartActionDetails();
                assertEquals(proxy.getHostname(), kad.getKickstartHost());
        }

        @Test
        public void testProvisionSystemFromProxyAutoDetected() throws Exception {
                Server server = ServerTestUtils.createTestSystem(admin);
                systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.SALT);
                TestUtils.saveAndFlush(server);
                server = reload(server);

                // salt-minion package has to be added to pass Kickstart validator
                Package testPackage = PackageTest.createTestPackage(admin.getOrg(), "salt-minion");
                PackageManagerTest.associateSystemToPackage(server, testPackage);

                Server proxy = ServerTestUtils.createTestSystem(admin);
                systemEntitlementManager.setBaseEntitlement(server, EntitlementManager.SALT);
                proxy.setHostname("proxy.example.com");
                SystemManager.activateProxy(proxy, "4.3");
                TestUtils.saveAndFlush(proxy);
                proxy = reload(proxy);

                KickstartDataTest.setupTestConfiguration(admin);
                KickstartData k = KickstartDataTest.createKickstartWithProfile(admin);
                KickstartDataTest.addCommand(admin, k,
                        "custom", "echo test-command 3");
                k.getKickstartDefaults().getKstree().setChannel(server.getBaseChannel());
                String profileName = k.getLabel();
                RhnMockHttpServletRequest request = new RhnMockHttpServletRequest();

                String headerValue = "1006681409::1151513167.96:21600.0:VV/xF" +
                "NEmCYOuHxEBAs7BEw==:myproxy,1006681408" +
                "::1151513034.3:21600.0:w2lm+XWSFJMVCGBK1dZXXQ==:fjs-0-11." +
                "uyunidev.suse.com,1006678487::1152567362.02:21600.0:t15l" +
                "gsaTRKpX6AxkUFQ11A==:fjs-0-12.rhndev.redhat.com";
                headerValue = headerValue.replaceFirst("myproxy", proxy.getHostname());
                request.setHeader("X-RHN-Proxy-Auth", headerValue);

                int result = 0;
                result = handler.provisionSystem(admin, request, server.getId().intValue(),
                        profileName);

                // something was scheduled
                assertNotEquals(0, result);

                // KS action is what we scheduled
                List<KickstartSession> res = KickstartFactory.lookupAllKickstartSessionsByServer(server.getId());
                assertFalse(res.isEmpty());
                KickstartSession ks = res.get(0);
                assertNotNull(ks);
                assertEquals(server.getId(), ks.getNewServer().getId());
                assertEquals("echo test-command 3", ks.getKsdata().getCommand("custom").getArguments());

                // Action details to check correct host
                List<Action> actions = ActionFactory.listActionsForServer(admin, server);
                assertNotNull(actions);
                KickstartInitiateAction kia = (KickstartInitiateAction) actions.get(0);
                KickstartActionDetails kad = kia.getKickstartActionDetails();
                assertEquals(proxy.getHostname(), kad.getKickstartHost());
        }
}
