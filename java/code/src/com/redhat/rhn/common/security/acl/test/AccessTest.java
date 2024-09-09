/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.common.security.acl.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.salt.netapi.calls.LocalCall;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * AccessTest
 */
public class AccessTest extends BaseTestCaseWithUser {

    private Acl acl;
    private final SaltApi saltApi = new TestSaltApi() {
        @Override
        public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
            return Optional.empty();
        }
    };
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        acl = new Acl();
        acl.registerHandler(new Access());
    }

    @Test
    public void testAccessNotFoundEntry() {
        Access access = new Access();
        String[] foo = {"FOO"};
        boolean rc = access.aclIs(null, foo);
        assertFalse(rc);
    }

    @Test
    public void testAccessValidEntry() {
        Config c = Config.get();
        c.setBoolean("test.true", "true");
        c.setBoolean("test.TrUe", "TrUe");
        c.setBoolean("test.one", "1");
        c.setBoolean("test.yes", "y");
        c.setBoolean("test.YES", "Y");
        c.setBoolean("test.on", "on");
        c.setBoolean("test.ON", "ON");

        Access access = new Access();
        String[] foo = new String[1];

        foo[0] = "test.true";
        assertTrue(access.aclIs(null, foo), "test.true is false");
        foo[0] = "test.TrUe";
        assertTrue(access.aclIs(null, foo), "test.TrUe is false");
        foo[0] = "test.one";
        assertTrue(access.aclIs(null, foo), "test.one is false");
        foo[0] = "test.yes";
        assertTrue(access.aclIs(null, foo), "test.yes is false");
        foo[0] = "test.YES";
        assertTrue(access.aclIs(null, foo), "test.YES is false");
        foo[0] = "test.on";
        assertTrue(access.aclIs(null, foo), "test.on is false");
        foo[0] = "test.ON";
        assertTrue(access.aclIs(null, foo), "test.ON is false");
    }

    @Test
    public void testAccessWithInvalidAcl() {
        boolean rc = acl.evalAcl(new HashMap<>(), "is(foo)");
        assertFalse(rc);
    }

    @Test
    public void testAccessWithValidAcl() {
        Config c = Config.get();
        c.setBoolean("test.true", "true");
        c.setBoolean("test.TrUe", "TrUe");
        c.setBoolean("test.one", "1");
        c.setBoolean("test.yes", "y");
        c.setBoolean("test.YES", "Y");
        c.setBoolean("test.on", "on");
        c.setBoolean("test.ON", "ON");

        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.true)"), "test.true is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.TrUe)"), "test.TrUe is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.one)"), "test.one is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.yes)"), "test.yes is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.YES)"), "test.YES is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.on)"), "test.on is false");
        assertTrue(acl.evalAcl(new HashMap<>(), "is(test.ON)"), "test.ON is false");
    }

    @Test
    public void testForFalse() {
        Config c = Config.get();
        c.setBoolean("test.false", "false");
        c.setBoolean("test.FaLse", "FaLse");
        c.setBoolean("test.zero", "0");
        c.setBoolean("test.no", "n");
        c.setBoolean("test.NO", "N");
        c.setBoolean("test.off", "off");
        c.setBoolean("test.OFF", "OFF");

        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.false)"), "test.false is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.FaLse)"), "test.FaLse is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.zero)"), "test.zero is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.no)"), "test.no is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.NO)"), "test.NO is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.off)"), "test.off is true");
        assertFalse(acl.evalAcl(new HashMap<>(), "is(test.OFF)"), "test.OFF is true");
    }

    @Test
    public void testUserRoleAcl() {
        Map<String, Object> context = new HashMap<>();
        User user = new MockUser();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        context.put("user", user);
        boolean rc = acl.evalAcl(context, "user_role(org_admin)");
        assertTrue(rc);
    }

    @Test
    public void testUserCanManageChannelAcl() {
        Map<String, Object> context = new HashMap<>();
        User user =  UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.CHANNEL_ADMIN);
        context.put("user", user);
        boolean rc = acl.evalAcl(context, "user_can_manage_channels()");
        assertTrue(rc);
    }

    @Test
    public void testUserRoleAclFalse() {
        Map<String, Object> context = new HashMap<>();
        User user = new MockUser();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        context.put("user", user);
        boolean rc = acl.evalAcl(context, "user_role(channel_admin)");
        assertFalse(rc);
    }

    @Test
    public void testNeedsFirstUser() {
        boolean rc = acl.evalAcl(new HashMap<>(), "need_first_user()");
        assertFalse(rc);
    }

    @Test
    public void testSystemFeature() {
        Map<String, Object> context = new HashMap<>();
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        context.put("user", user);
        Server s = ServerFactoryTest.createTestServer(user, false);
        context.put("sid", new String[] {s.getId().toString()});
        boolean rc = acl.evalAcl(context, "system_feature(ftr_kickstart)");
        assertTrue(rc);
        rc = acl.evalAcl(context, "not system_feature(ftr_kickstart)");
        assertFalse(rc);
    }

    @Test
    public void testAclSystemHasManagementEntitlement() {
        Map<String, Object> context = new HashMap<>();

        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        context.put("sid", new String[] {s.getId().toString()});
        context.put("user", user);
        assertTrue(acl.evalAcl(context, "system_has_management_entitlement()"));

        s = ServerFactoryTest.createUnentitledTestServer(user, true,
                ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        context.put("sid", new String[] {s.getId().toString()});
        assertFalse(acl.evalAcl(context, "system_has_management_entitlement()"));

        // Intentionally invalid sysid
        context.put("sid", new String[] { "999900099900" });
        assertFalse(acl.evalAcl(context, "system_has_management_entitlement()"));

    }

    /**
     * Test ACL: system_has_salt_entitlement()
     */
    @Test
    public void testAclSystemHasSaltEntitlement() {
        Map<String, Object> context = new HashMap<>();
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        // Check with a salt entitled system
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled());
        context.put("sid", new String[] {s.getId().toString()});
        context.put("user", user);
        assertTrue(acl.evalAcl(context, "system_has_salt_entitlement()"));

        // Change the base entitlement to MANAGEMENT
        systemEntitlementManager.setBaseEntitlement(s, EntitlementManager.MANAGEMENT);
        context.put("sid", new String[] {s.getId().toString()});
        context.put("user", user);
        assertFalse(acl.evalAcl(context, "system_has_salt_entitlement()"));

        // Check with an unentitled system
        s = ServerFactoryTest.createUnentitledTestServer(user, true,
                ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        context.put("sid", new String[] {s.getId().toString()});
        assertFalse(acl.evalAcl(context, "system_has_salt_entitlement()"));

        // Intentionally invalid sysid
        context.put("sid", new String[] { "999900099900" });
        assertFalse(acl.evalAcl(context, "system_has_salt_entitlement()"));
    }

    /**
     * Test ACL: acl_system_is_bootstrap_minion_server()
     * @throws Exception in case of an error
     */
    @Test
    public void testAclSystemIsBootstrapMinionServer() throws Exception {
        Map<String, Object> context = new HashMap<>();
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        // Check with a minion system w/o bootstrap entitlement
        Server s = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeBootstrapEntitled());
        s = TestUtils.saveAndReload(s);

        context.put("sid", new String[] {s.getId().toString()});
        context.put("user", user);
        assertFalse(acl.evalAcl(context, "system_is_bootstrap_minion_server()"));

        // Check with a minion system with bootstrap entitlement
        SystemManager.addMinionInfoToServer(s.getId(), "testMid");
        TestUtils.saveAndReload(s);

        assertTrue(acl.evalAcl(context, "system_is_bootstrap_minion_server()"));

        // Check with a traditional system system
        s = ServerFactoryTest.createUnentitledTestServer(user, true,
                ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        context.put("sid", new String[] {s.getId().toString()});
        assertFalse(acl.evalAcl(context, "system_is_bootstrap_minion_server()"));
    }

    @Test
    public void testUnimplementedMethods() {

        String[] methods = { "user_authenticated()" };

        for (String methodIn : methods) {
            evalAclAssertFalse(acl, methodIn);
        }
    }

    @Test
    public void testGlobalConfigIsGone() {
        Map<String, Object> context = new HashMap<>();
        try {
            acl.evalAcl(context, "global_config(foo)");
            fail("global_config is back, what moron undid my change!");
        }
        catch (IllegalArgumentException e) {
            // doo nothing
        }
    }

    @Test
    public void testCanAccessChannel() {
        try {
            Map<String, Object> context = new HashMap<>();
            User user =  UserTestUtils.findNewUser("testUser",
                    "testOrg" + this.getClass().getSimpleName());
            context.put("user", user);
            user.addPermanentRole(RoleFactory.CHANNEL_ADMIN);

            Channel chan = ChannelFactoryTest.createBaseChannel(user);
            assertTrue(acl.evalAcl(context, "can_access_channel(" + chan.getId() + ")"));
        }
        catch (Exception e) {
            fail("channel validation failed");
        }
    }

    @Test
    public void testIsModularChannel() {
        Map<String, Object> context = new HashMap<>();
        User user = UserTestUtils.findNewUser("testUser", "testOrg" + this.getClass().getSimpleName());
        context.put("user", user);

        user.addPermanentRole(RoleFactory.CHANNEL_ADMIN);
        Channel chan = null;

        try {
            chan = ChannelFactoryTest.createTestChannel(user);
        }
        catch (Exception e) {
            fail("channel validation failed");
        }

        context.put("cid", chan.getId());

        assertFalse(acl.evalAcl(context, "is_modular_channel()"));

        Modules mod = new Modules();
        mod.setRelativeFilename("filename");
        chan.setModules(mod);
        mod.setChannel(chan);

        assertTrue(acl.evalAcl(context, "is_modular_channel()"));

    }

    @Test
    public void testFormvarExists() {
        Map<String, Object> context = new HashMap<>();
        assertFalse(acl.evalAcl(context, "formvar_exists(cid)"));
        context.put("cid", "161");
        assertTrue(acl.evalAcl(context, "formvar_exists(cid)"));
        assertFalse(acl.evalAcl(context, "formvar_exists(pid)"));
        assertFalse(acl.evalAcl(context, "formvar_exists()"));
    }

    private void evalAclAssertFalse(Acl aclIn, String aclStr) {
        Map<String, Object> context = new HashMap<>();
        // acl methods must be in the following form
        // aclXxxYyy(Object context, String[] params) and invoked
        // xxx_yyy(param)
        boolean rc = aclIn.evalAcl(context, aclStr);
        assertFalse(rc);
    }

    @Test
    public void testIsVirtual() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuests(user, 1, systemEntitlementManager);
        Server guest = host.getGuests().iterator().next().getGuestSystem();

        Access a = new Access();
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("sid", guest.getId());
        ctx.put("user", user);
        assertTrue(a.aclSystemIsVirtual(ctx, null));
    }

    @Test
    public void testCanEvalIfServerHasPtfRepositories() throws Exception {
        Server serverNoSupport = ServerFactoryTest.createTestServer(user, true);
        serverNoSupport.setOs(ServerConstants.ALMA);

        Server serverWithSupport = ServerFactoryTest.createTestServer(user, true);
        serverWithSupport.setOs(ServerConstants.SLES);

        Server serverWithSupportAndChannel = ServerFactoryTest.createTestServer(user, true);
        serverWithSupportAndChannel.setOs(ServerConstants.SLES);

        Channel normalChannel = ChannelFactoryTest.createTestChannel(user, List.of("http://example.com/REPO/RPMS"));
        Channel ptfChannel = ChannelFactoryTest.createTestChannel(user, List.of("http://example.com/PTF/RPMS"));

        SystemManager.subscribeServerToChannel(user, serverNoSupport, normalChannel);
        SystemManager.subscribeServerToChannel(user, serverWithSupport, normalChannel);
        SystemManager.subscribeServerToChannel(user, serverWithSupportAndChannel, ptfChannel);

        serverNoSupport = TestUtils.saveAndReload(serverNoSupport);
        serverWithSupport = TestUtils.saveAndReload(serverWithSupport);
        serverWithSupportAndChannel = TestUtils.saveAndReload(serverWithSupportAndChannel);

        Access access = new Access();
        Map<String, Object> context = new HashMap<>();
        context.put("user", user);

        context.put("sid", serverNoSupport.getId());
        assertFalse(access.aclHasPtfRepositories(context, null));

        context.put("sid", serverWithSupport.getId());
        assertFalse(access.aclHasPtfRepositories(context, null));

        context.put("sid", serverWithSupportAndChannel.getId());
        assertTrue(access.aclHasPtfRepositories(context, null));
    }

    @Test
    public void testCanEvalIfServerSupportsPtfRemoval() {
        Package zypperNoSupport = PackageTestUtils.createZypperPackage("1.14.50", user);
        Package zypperWithSupport = PackageTestUtils.createZypperPackage("1.14.59", user);

        Server serverNoSupport = ServerFactoryTest.createTestServer(user, true);
        serverNoSupport.setOs(ServerConstants.ALMA);

        Server serverOnlyPtf = ServerFactoryTest.createTestServer(user, true);
        serverOnlyPtf.setOs(ServerConstants.SLES);
        serverOnlyPtf.setRelease("15");

        Server serverPtfAndRemoval = ServerFactoryTest.createTestServer(user, true);
        serverPtfAndRemoval.setOs(ServerConstants.SLES);
        serverPtfAndRemoval.setRelease("15");

        PackageTestUtils.installPackagesOnServer(List.of(zypperNoSupport), serverOnlyPtf);
        PackageTestUtils.installPackagesOnServer(List.of(zypperWithSupport), serverPtfAndRemoval);

        serverNoSupport = TestUtils.saveAndReload(serverNoSupport);
        serverOnlyPtf = TestUtils.saveAndReload(serverOnlyPtf);
        serverPtfAndRemoval = TestUtils.saveAndReload(serverPtfAndRemoval);

        Access access = new Access();
        Map<String, Object> context = new HashMap<>();
        context.put("user", user);

        context.put("sid", serverNoSupport.getId());
        assertFalse(access.aclSystemSupportsPtfRemoval(context, null));

        context.put("sid", serverOnlyPtf.getId());
        assertFalse(access.aclSystemSupportsPtfRemoval(context, null));

        context.put("sid", serverPtfAndRemoval.getId());
        assertTrue(access.aclSystemSupportsPtfRemoval(context, null));
    }

    /**
     * Override the methods in User that talk to the database
     */
    class MockUser extends UserImpl {
        private final Set<Role> mockRoles;

        MockUser() {
            mockRoles = new HashSet<>();
        }

        /**
         * This is the key method that needs to be overriden
         * There is a check in User that looks up the Org
         * that isn't necessary for this Unit Test
         */
        @Override
        public void addPermanentRole(Role label) {
            mockRoles.add(label);
        }

        /** @see com.redhat.rhn.domain.user.User#hasRole */
        @Override
        public boolean hasRole(Role label) {
            return mockRoles.contains(label);
        }
    }


}
