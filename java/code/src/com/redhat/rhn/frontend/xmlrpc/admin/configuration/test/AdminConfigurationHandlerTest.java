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
package com.redhat.rhn.frontend.xmlrpc.admin.configuration.test;

import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProductChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorBaseChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorChildChannel;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.xmlrpc.activationkey.ActivationKeyHandler;
import com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.systemgroup.ServerGroupHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.user.UserHandler;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.TestUtils;


import com.suse.cloud.CloudPaygManager;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminConfigurationHandlerTest extends BaseHandlerTestCase {

    private static final Logger LOG = LogManager.getLogger(AdminConfigurationHandlerTest.class);
    private TaskomaticApi taskomaticApi = new TaskomaticApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final CloudPaygManager paygManager = new CloudPaygManager();
    private final AttestationManager attestationManager = new AttestationManager();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private SSHMinionBootstrapper sshMinionBootstrapper =
            new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );
    private SystemManager systemManager =
            new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);

    private SystemHandler systemHandler = new SystemHandler(taskomaticApi, xmlRpcSystemHelper,
            systemEntitlementManager, systemManager, serverGroupManager, GlobalInstanceHolder.PAYG_MANAGER,
            GlobalInstanceHolder.ATTESTATION_MANAGER);
    private MigrationManager migrationManager = new MigrationManager(serverGroupManager);
    private OrgHandler orgHandler = new OrgHandler(migrationManager);
    private ServerGroupHandler serverGroupHandler = new ServerGroupHandler(xmlRpcSystemHelper, serverGroupManager);
    private UserHandler userHandler = new UserHandler(serverGroupManager);
    private ActivationKeyHandler activationKeyHandler = new ActivationKeyHandler(serverGroupManager);
    private ChannelHandler channelHandler = new ChannelHandler();
    private ChannelSoftwareHandler channelSoftwareHandler =
            new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
    private AdminConfigurationHandler adminConfigurationHandler;
    private SaltApi testSaltApi;

    @RegisterExtension
    protected final Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        testSaltApi = context.mock(SaltApi.class);
        adminConfigurationHandler = new AdminConfigurationHandler(
                                  orgHandler, serverGroupHandler, userHandler, activationKeyHandler,
                                  systemHandler, channelHandler, channelSoftwareHandler, testSaltApi);
    }

    @Test
    public void testAdminConfigurationHandler() throws Exception {

        ChannelFamily channelFamily = createTestChannelFamily();
        SUSEProduct product = createTestSUSEProduct(channelFamily);
        ChannelProduct channelProduct1 = createTestChannelProduct();
        // Create channels
        Channel baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct1);
        baseChannel.setUpdateTag("SLE-SERVER");
        createTestSUSEProductChannel(baseChannel, product, true);

        Channel childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct1);
        Channel childChannel2 = createTestVendorChildChannel(baseChannel, channelProduct1);

        createTestSUSEProductChannel(childChannel1, product, true);
        createTestSUSEProductChannel(childChannel2, product, true);



        String config = TestUtils.readAll(TestUtils.findTestData("config1.yaml"));
        config = config.replace("BASE_CHANNEL", baseChannel.getLabel());
        config = config.replace("CHILD_CHANNEL", childChannel1.getLabel());

        config = config.replace("MANAGEABLE_CHANNEL", "");
        config = config.replace("SUBSCRIBABLE_CHANNEL", "");
        config = config.replace("CONFIGURATION_CHANNEL", "");

        Map<String, Object> configYaml = new Yaml().load(config);

        int nOrgsBefore = OrgFactory.lookupAllOrgs().size();
        int nUsersBefore = UserFactory.getInstance().findAllUsers(Optional.empty()).size();

        context.checking(new Expectations() {
            {
                allowing(testSaltApi).selectMinions(with("*httpd*"), with("glob"));
                will(returnValue(Arrays.asList("httpd_server_1")));
            }
        });


        // call the API for the first time
        adminConfigurationHandler.configure(satAdmin, configYaml);

        int nOrgs = OrgFactory.lookupAllOrgs().size();
        assertEquals(nOrgs, nOrgsBefore + 2);

        Org org1 = OrgFactory.lookupByName("my_org1");
        Org org2 = OrgFactory.lookupByName("my_org2");

        int nUsers = UserFactory.getInstance().findAllUsers(Optional.empty()).size();
        assertEquals(nUsers, nUsersBefore + 4); // 2 orgs, 1 admin and 1 user each -> 4 users.

        int nUsers1 = UserFactory.getInstance().findAllUsers(Optional.of(org1)).size();
        int nUsers2 = UserFactory.getInstance().findAllUsers(Optional.of(org2)).size();
        assertEquals(nUsers1, 2);
        assertEquals(nUsers2, 2);


        int nGroups1 = ServerGroupFactory.listManagedGroups(org1).size();
        int nGroups2 = ServerGroupFactory.listManagedGroups(org2).size();

        assertEquals(nGroups1, 1);
        assertEquals(nGroups2, 1);

        User admin1 = UserFactory.getInstance().findAllOrgAdmins(org1).get(0);
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(admin1);
        minion.setMinionId("httpd_server_1");

        ConfigChannelHandler cch = new ConfigChannelHandler();
        ConfigChannel configChannel = cch.create(admin1, TestUtils.randomString(),
                TestUtils.randomString(), TestUtils.randomString());

        Channel channel = ChannelFactoryTest.createTestChannel(admin1);
        HibernateFactory.getSession().flush();

        // the MinionServerFactoryTest.createTestMinionServer call above creates also a new group, update the number
        nGroups1 = ServerGroupFactory.listManagedGroups(org1).size();

        // call the API again
        config = TestUtils.readAll(TestUtils.findTestData("config1.yaml"));
        config = config.replace("BASE_CHANNEL", baseChannel.getLabel());
        config = config.replace("CHILD_CHANNEL", childChannel1.getLabel());

        config = config.replace("MANAGEABLE_CHANNEL", channel.getLabel());
        config = config.replace("SUBSCRIBABLE_CHANNEL", channel.getLabel());

        config = config.replace("CONFIGURATION_CHANNEL", configChannel.getLabel());
        LOG.error("{}", config);
        configYaml = new Yaml().load(config);
        adminConfigurationHandler.configure(satAdmin, configYaml);

        assertEquals(nOrgs, OrgFactory.lookupAllOrgs().size());

        assertEquals(nUsers, UserFactory.getInstance().findAllUsers(Optional.empty()).size());

        assertEquals(nUsers1, UserFactory.getInstance().findAllUsers(Optional.of(org1)).size());
        assertEquals(nUsers2, UserFactory.getInstance().findAllUsers(Optional.of(org2)).size());

        assertEquals(nGroups1, ServerGroupFactory.listManagedGroups(org1).size());
        assertEquals(nGroups2, ServerGroupFactory.listManagedGroups(org2).size());

        ManagedServerGroup group1 = ServerGroupFactory.lookupByNameAndOrg("httpd_servers", org1);
        List<Server> servers1 = ServerGroupFactory.listServers(group1);
        assertEquals(1, servers1.size());
        assertEquals(minion.getName(), servers1.get(0).getName());

        ManagedServerGroup group2 = ServerGroupFactory.lookupByNameAndOrg("httpd_servers", org2);
        List<Server> servers2 = ServerGroupFactory.listServers(group2);
        assertEquals(0, servers2.size());

        // call the API again
        config = TestUtils.readAll(TestUtils.findTestData("config1.yaml"));
        config = config.replace("BASE_CHANNEL", baseChannel.getLabel());
        config = config.replace("CHILD_CHANNEL", childChannel1.getLabel());

        config = config.replace("MANAGEABLE_CHANNEL", "");
        config = config.replace("SUBSCRIBABLE_CHANNEL", "");
        config = config.replace("CONFIGURATION_CHANNEL", "");
        LOG.error("{}", config);
        configYaml = new Yaml().load(config);
        adminConfigurationHandler.configure(satAdmin, configYaml);
    }

    @Test
    public void testMinimal() throws Exception {
        String config = TestUtils.readAll(TestUtils.findTestData("minimal.yaml"));
        Map<String, Object> configYaml = new Yaml().load(config);


        adminConfigurationHandler.configure(satAdmin, configYaml);
    }
}
