/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.channel.software.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.PackageDto;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelNameException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParentChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchUserException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.channel.software.ChannelSoftwareHandler;
import com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

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

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

/**
 * ChannelSoftwareHandlerTest
 */
@SuppressWarnings("deprecation")
@ExtendWith(JUnit5Mockery.class)
public class ChannelSoftwareHandlerTest extends BaseHandlerTestCase {

    private TaskomaticApi taskomaticApi = new TaskomaticApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final CloudPaygManager paygManager = new CloudPaygManager();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager);
    private SSHMinionBootstrapper sshMinionBootstrapper = new SSHMinionBootstrapper(systemQuery, saltApi, paygManager);
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
    private SystemHandler systemHandler = new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager,
            systemManager, serverGroupManager, new CloudPaygManager(), new AttestationManager());
    private ChannelSoftwareHandler handler = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
    private ErrataHandler errataHandler = new ErrataHandler();

    @RegisterExtension
    protected final Mockery mockContext = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        Context ctx = Context.getCurrentContext();
        ctx.setLocale(Locale.getDefault());
        ctx.setTimezone(TimeZone.getDefault());

    }

    public void ignoredtestAddRemovePackages() throws Exception {

        // TODO : GET THIS WORKING
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        Package pkg1 = PackageTest.createTestPackage(admin.getOrg());
        Package pkg2 = PackageTest.createTestPackage(admin.getOrg());

        List<Long> packages2add = new ArrayList<>();
        packages2add.add(pkg1.getId());
        packages2add.add(pkg2.getId());

        assertEquals(0, channel.getPackageCount());
        handler.addPackages(admin, channel.getLabel(), packages2add);
        assertEquals(2, channel.getPackageCount());

        Long bogusId = System.currentTimeMillis();
        packages2add.add(bogusId);

        try {
            handler.addPackages(admin, channel.getLabel(), packages2add);
            fail("should have gotten a permission check failure since admin wouldn't " +
                 "have access to a package that doesn't exist.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //Test remove packages
        assertEquals(2, channel.getPackageCount());
        try {
            handler.removePackages(admin, channel.getLabel(), packages2add);
            fail("should have gotten a permission check failure.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        packages2add.remove(bogusId);
        packages2add.remove(pkg2.getId());
        packages2add.add(pkg1.getId());
        assertEquals(2, packages2add.size()); // should have 2 entries for pkg1
        handler.removePackages(admin, channel.getLabel(), packages2add);
        assertEquals(1, channel.getPackageCount());


        // test for invalid package arches
        packages2add.clear();
        assertEquals(0, packages2add.size());

        PackageArch pa = PackageFactory.lookupPackageArchByLabel("x86_64");
        assertNotNull(pa);
        pkg1.setPackageArch(pa);
        TestUtils.saveAndFlush(pkg1);
        packages2add.add(pkg1.getId());

        try {
            handler.addPackages(admin, channel.getLabel(), packages2add);
            fail("incompatible package was added to channel");
        }
        catch (FaultException e) {
            assertEquals(1202, e.getErrorCode());
        }
    }

    @Test
    public void testSetGloballySubscribable() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        assertTrue(channel.isGloballySubscribable(admin.getOrg()));
        handler.setGloballySubscribable(admin, channel.getLabel(), false);
        assertFalse(channel.isGloballySubscribable(admin.getOrg()));
        handler.setGloballySubscribable(admin, channel.getLabel(), true);
        assertTrue(channel.isGloballySubscribable(admin.getOrg()));

        assertFalse(regular.hasRole(RoleFactory.CHANNEL_ADMIN));
        try {
            handler.setGloballySubscribable(regular, channel.getLabel(), false);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }
        assertTrue(channel.isGloballySubscribable(admin.getOrg()));

        try {
            handler.setGloballySubscribable(admin, TestUtils.randomString(),
                                            false);
            fail();
        }
        catch (NoSuchChannelException e) {
            //success
        }
        assertTrue(channel.isGloballySubscribable(admin.getOrg()));
    }

    @Test
    public void testSetUserSubscribable() throws Exception {
        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        c1.setGloballySubscribable(false, admin.getOrg());
        User user = UserTestUtils.createUser("foouser", admin.getOrg().getId());

        assertFalse(ChannelManager.verifyChannelSubscribe(user, c1.getId()));
        handler.setUserSubscribable(admin, c1.getLabel(), user.getLogin(), true);
        assertTrue(ChannelManager.verifyChannelSubscribe(user, c1.getId()));

        handler.setUserSubscribable(admin, c1.getLabel(), user.getLogin(), false);
        assertFalse(ChannelManager.verifyChannelSubscribe(user, c1.getId()));

        try {
            handler.setUserSubscribable(
                    regular, c1.getLabel(), user.getLogin(), true);
            fail("should have gotten a permission exception.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        try {
            handler.setUserSubscribable(admin, c1.getLabel(),
                        "asfd" + TestUtils.randomString(), true);
            fail("should have gotten a permission exception.");
        }
        catch (NoSuchUserException e) {
            //success
        }
    }

    @Test
    public void testIsUserSubscribable() throws Exception {
        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        c1.setGloballySubscribable(false, admin.getOrg());
        User user = UserTestUtils.createUser("foouser", admin.getOrg().getId());

        assertEquals(0, handler.isUserSubscribable(
                admin, c1.getLabel(), user.getLogin()));
        handler.setUserSubscribable(
                admin, c1.getLabel(), user.getLogin(), true);
        assertEquals(1, handler.isUserSubscribable(
                admin, c1.getLabel(), user.getLogin()));

        handler.setUserSubscribable(
                admin, c1.getLabel(), user.getLogin(), false);
        assertEquals(0, handler.isUserSubscribable(
                admin, c1.getLabel(), user.getLogin()));
    }

    @Test
    public void testIsExisting() throws Exception {
        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        assertTrue(handler.isExisting(admin, c1.getLabel()));
        assertFalse(handler.isExisting(admin, c1.getLabel() + UUID.randomUUID()));
    }

    @Test
    public void testSetSystemChannelsBaseChannel() throws Exception {

        Channel base = ChannelFactoryTest.createTestChannel(admin);
        assertTrue(base.isBaseChannel());
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel child = ChannelFactoryTest.createTestChannel(admin);
        child.setParentChannel(base);
        ChannelFactory.save(child);
        assertFalse(child.isBaseChannel());

        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        child2.setParentChannel(base);
        ChannelFactory.save(child2);
        assertFalse(child2.isBaseChannel());

        SystemHandler sh = new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager, systemManager,
                serverGroupManager, new CloudPaygManager(), new AttestationManager());

        int sid = server.getId().intValue();
        int rc1 = sh.setBaseChannel(admin, sid, base.getLabel());
        int rc2 = sh.setChildChannels(admin, sid, List.of(child.getLabel()));

        server = reload(server);

        // now verify
        assertEquals(1, rc1);
        assertEquals(1, rc2);
        assertEquals(2, server.getChannels().size());
        Channel newBase = server.getBaseChannel();
        assertNotNull(newBase);
        assertEquals(newBase.getLabel(), base.getLabel());

        try {
            sh.setBaseChannel(admin, sid, child.getLabel());
            fail("setBaseChannel didn't complain when given no base channel");
        }
        catch (InvalidChannelException ice) {
            // ice ice baby
        }

    }

    @Test
    public void testSetBaseChannel() throws Exception {
        SystemHandler sh = new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager, systemManager,
                serverGroupManager, new CloudPaygManager(), new AttestationManager());

        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        Server server = ServerFactoryTest.createTestServer(admin, true);

        assertEquals(0, server.getChannels().size());
        int result = sh.setBaseChannel(admin, server.getId().intValue(), c1.getLabel());

        server = reload(server);

        assertEquals(1, result);
        assertEquals(1, server.getChannels().size());

        Channel c2 = ChannelFactoryTest.createTestChannel(admin);
        assertNotEquals(c1.getLabel(), c2.getLabel());
        result = sh.setBaseChannel(admin, server.getId().intValue(), c2.getLabel());

        server = reload(server);

        assertEquals(1, result);
        assertTrue(server.getChannels().contains(c2));

        //try to make it break
        try {
            sh.setBaseChannel(admin, server.getId().intValue(), TestUtils.randomString());
            fail("subscribed system to invalid channel.");
        }
        catch (Exception e) {
            //success
        }

        server = reload(server);
        //make sure servers channel subscriptions weren't changed
        assertEquals(1, result);
        Channel subscribed = server.getChannels().iterator().next();
        assertEquals(c2.getLabel(), subscribed.getLabel());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {

            Channel c3 = ChannelFactoryTest.createTestChannel(admin);

            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);

            int rc = sh.setBaseChannel(admin, server.getId().intValue(), c3.getLabel());

            fail("allowed incompatible channel arch to be set, returned: " + rc);
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

    @Test
    public void testListSystemChannels() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Server s = ServerFactoryTest.createTestServer(admin, true);

        //Server shouldn't have any channels yet
        Object[] result = csh.listSystemChannels(admin,
                s.getId().intValue());
        assertEquals(0, result.length);

        SystemManager.subscribeServerToChannel(admin, s, c);

        //should be subscribed to 1 channel
        result = csh.listSystemChannels(admin,
                s.getId().intValue());
        assertEquals(1, result.length);

        //try no_such_system fault exception
        try {
            csh.listSystemChannels(admin, -2390);
            fail("ChannelSoftwareHandler.listSystemChannels didn't throw an exception " +
                 "for invalid system id");
        }
        catch (FaultException e) {
            //success
        }
    }

    @Test
    public void testListSubscribedSystems() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Server s = ServerFactoryTest.createTestServer(admin);
        SystemManager.subscribeServerToChannel(admin, s, c);
        flushAndEvict(c);
        flushAndEvict(s);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);

        Object[] result = csh.listSubscribedSystems(admin, c.getLabel());
        assertTrue(result.length > 0);

        //NoSuchChannel
        try {
            result = csh.listSubscribedSystems(admin, TestUtils.randomString());
            fail("ChannelSoftwareHandler.listSubscribedSystemd didn't throw " +
                 "NoSuchChannelException.");
        }
        catch (NoSuchChannelException e) {
            //success
        }

        //Permission
        try {
            result = csh.listSubscribedSystems(regular, c.getLabel());
            fail("Regular user allowed access to channel system list.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }
    }

    @Test
    public void testListArches() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        List<ChannelArch> arches = csh.listArches(admin);
        assertNotNull(arches);
        assertFalse(arches.isEmpty());
    }

    @Test
    public void testListArchesPermissionError() {
        try {
            ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
            List<ChannelArch> arches = csh.listArches(admin);
            assertNotNull(arches);
            assertFalse(arches.isEmpty());
        }
        catch (PermissionCheckFailureException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testDeleteChannel() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        String label = c.getLabel();
        c = reload(c);

        try {
            assertEquals(1, csh.delete(admin, label));
        }
        catch (ValidatorException e) {
            // taskomatic is down
            assertEquals("message.channel.cannot-be-deleted.no-taskomatic", e.getResult()
                .getErrors().get(0).getKey());
            return;
        }
        // taskomatic is up
        assertNull(reload(c));
    }

    @Test
    public void testDeleteClonedChannel() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Channel cClone1 = ChannelFactoryTest.createTestClonedChannel(c, admin);
        Channel cClone2 = ChannelFactoryTest.createTestClonedChannel(cClone1, admin);
        cClone2 = reload(cClone2);

        assertEquals(1, csh.delete(admin, cClone2.getLabel()));
        assertNotNull(reload(c));
        assertNotNull(reload(cClone1));
        assertNull(reload(cClone2));
    }

    @Test
    public void testDeleteChannelWithClones() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Channel cClone1 = ChannelFactoryTest.createTestClonedChannel(c, admin);
        Channel cClone2 = ChannelFactoryTest.createTestClonedChannel(cClone1, admin);
        cClone1 = reload(cClone1);
        try {
            csh.delete(admin, cClone1.getLabel());
            fail();
        }
        catch (ValidationException exc) {
            assertEquals(exc.getErrorCode(), 2800);
            assertContains(
                    exc.getMessage(),
                    "Unable to delete channel. The channel you have tried to delete has been cloned. " +
                    "You must delete the clones before you can delete this channel.");
            assertNotNull(reload(c));
            assertNotNull(reload(cClone1));
            assertNotNull(reload(cClone2));
        }
    }

    @Test
    public void testIsGloballySubscribable() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        assertEquals(1, csh.isGloballySubscribable(admin, c.getLabel()));
        // should be assertTrue
    }

    @Test
    public void testIsGloballySubscribableNoSuchChannel() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        try {
            csh.isGloballySubscribable(admin, "notareallabel");
            fail();
        }
        catch (NoSuchChannelException e) {
            // expected
        }
    }

    @Test
    public void testGetDetails() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        assertNotNull(c);
        assertNull(c.getParentChannel());

        Channel  result = csh.getDetails(admin, c.getLabel());
        channelDetailsEquality(c, result);

        result = csh.getDetails(admin, c.getId().intValue());
        channelDetailsEquality(c, result);
    }

    @Test
    public void testSetDetails() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        assertNotNull(c);
        assertNull(c.getParentChannel());

        Channel  result = csh.getDetails(admin, c.getLabel());

        Map<String, String> details = new HashMap<>();
        details.put("checksum_label", "sha256");
        details.put("name", "new-name");
        details.put("summary", "new-summary");
        details.put("description", "new-dsc");
        details.put("maintainer_name", "foo");
        details.put("maintainer_email", "foo@bar.com");
        details.put("maintainer_phone", "+18098098");
        details.put("gpg_key_url", "http://gpg.url");
        details.put("gpg_key_id", "AE1234BC");
        details.put("gpg_key_fp", " CA20 8686 2BD6 9DFC 65F6 ECC4 2191 80CD DB42 A60E");
        details.put("gpg_check", "True");
        csh.setDetails(admin, c.getLabel(), details);

        channelDetailsEquality(c, result);

        result = csh.getDetails(admin, c.getId().intValue());
        channelDetailsEquality(c, result);
    }

    @Test
   public void testGetChannelLastBuildById() throws Exception {
       ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
       addRole(admin, RoleFactory.CHANNEL_ADMIN);
       Channel c = ChannelFactoryTest.createTestChannel(admin);
       assertNotNull(c);
       String lastRepoBuild = csh.getChannelLastBuildById(admin, c.getId().intValue());
       assertEquals(lastRepoBuild, "");
    }

    private void channelDetailsEquality(Channel original, Channel result) {
        assertNotNull(result);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getLabel(), result.getLabel());
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getChannelArch().getName(),
                result.getChannelArch().getName());
        assertEquals(original.getSummary(), result.getSummary());
        assertEquals(original.getDescription(), result.getDescription());

        assertEquals(original.getMaintainerName(), result.getMaintainerName());
        assertEquals(original.getMaintainerEmail(), result.getMaintainerEmail());
        assertEquals(original.getMaintainerPhone(), result.getMaintainerPhone());
        assertEquals(original.getSupportPolicy(), result.getSupportPolicy());

        assertEquals(original.getGPGKeyUrl(), result.getGPGKeyUrl());
        assertEquals(original.getGPGKeyId(), result.getGPGKeyId());
        assertEquals(original.getGPGKeyFp(), result.getGPGKeyFp());
        assertEquals(original.isGPGCheck(), result.isGPGCheck());
        if (original.getEndOfLife() != null) {
            assertEquals(original.getEndOfLife().toString(),
                    result.getEndOfLife().toString());
        }
        else {
            assertNull(result.getEndOfLife());
        }

        assertNull(result.getParentChannel());
    }

    @Test
    public void testCreate() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        int i = csh.create(admin, "api-test-chan-label",
                "apiTestChanName", "apiTestSummary", "channel-x86_64", null);
        assertEquals(1, i);
        Channel c = ChannelFactory.lookupByLabel(admin.getOrg(), "api-test-chan-label");
        assertNotNull(c);
        assertEquals("apiTestChanName", c.getName());
        assertEquals("apiTestSummary", c.getSummary());
        ChannelArch ca = ChannelFactory.findArchByLabel("channel-x86_64");
        assertNotNull(ca);
        assertNotNull(c.getChannelArch());
        assertEquals(ca.getLabel(), c.getChannelArch().getLabel());
        assertEquals(c.getChecksumTypeLabel(), "sha1");
        assertTrue(c.isGPGCheck());
    }

    @Test
    public void testCreateWithGPGCheckDisabled() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        int i = csh.create(admin, "api-test-chan-label",
                "apiTestChanName", "apiTestSummary", "channel-x86_64", null,
                "sha1", new HashMap<>(), false);
        assertEquals(1, i);
        Channel c = ChannelFactory.lookupByLabel(admin.getOrg(), "api-test-chan-label");
        assertNotNull(c);
        assertEquals("apiTestChanName", c.getName());
        assertEquals("apiTestSummary", c.getSummary());
        ChannelArch ca = ChannelFactory.findArchByLabel("channel-x86_64");
        assertNotNull(ca);
        assertNotNull(c.getChannelArch());
        assertEquals(ca.getLabel(), c.getChannelArch().getLabel());
        assertEquals(c.getChecksumTypeLabel(), "sha1");
        assertFalse(c.isGPGCheck());
    }

    @Test
    public void testCreateWithChecksum() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        int i = csh.create(admin, "api-test-checksum-chan-label",
                "apiTestCSChanName", "apiTestSummary", "channel-ia32", null, "sha256");
        assertEquals(1, i);
        Channel c = ChannelFactory.lookupByLabel(admin.getOrg(),
                                   "api-test-checksum-chan-label");
        assertNotNull(c);
        assertEquals("apiTestCSChanName", c.getName());
        assertEquals("apiTestSummary", c.getSummary());
        ChannelArch ca = ChannelFactory.findArchByLabel("channel-ia32");
        assertNotNull(ca);
        assertNotNull(c.getChannelArch());
        assertEquals(ca.getLabel(), c.getChannelArch().getLabel());
        assertEquals(c.getChecksumTypeLabel(), "sha256");
    }

    @Test
    public void testCreateUnauthUser() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        try {
            csh.create(regular, "api-test-chan-label",
                   "apiTestChanName", "apiTestSummary", "channel-x86_64", null);
            fail("create did NOT throw an exception");

        }
        catch (PermissionCheckFailureException e) {
            // expected
        }
        catch (InvalidChannelLabelException | InvalidParentChannelException | InvalidChannelNameException e) {
            fail("Wasn't expecting this in this test.");
        }
    }

    @Test
    public void testCreateNullRequiredParams() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        // null label
        try {
            csh.create(admin, null, "api-test-nonnull", "api test summary",
                    "channel-x86_64", null);
            fail("create did not throw exception when given a null label");
        }
        catch (IllegalArgumentException | InvalidParentChannelException | InvalidChannelNameException iae) {
            fail("Wasn't expecting this in this test.");
        }
        catch (PermissionCheckFailureException e) {
            fail("We're not looking for this exception right now");
        }
        catch (InvalidChannelLabelException expected) {
            // expected
        }

        try {
            csh.create(admin, "api-test-nonnull", null, "api test summary",
                    "channel-x86_64", null);
            fail("create did not throw exception when given a null label");
        }
        catch (IllegalArgumentException | InvalidParentChannelException | InvalidChannelLabelException iae) {
            fail("Wasn't expecting this in this test.");
        }
        catch (PermissionCheckFailureException e) {
            fail("We're not looking for this exception right now");
        }
        catch (InvalidChannelNameException expected) {
            // expected
        }
    }

    @Test
    public void testInvalidChannelNameAndLabel() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        int i;
        try {
            i = csh.create(admin, "api-test-chan-label",
                    "apiTestChanName", "apiTestSummary", "channel-x86_64", null);
            assertEquals(1, i);
        }
        catch (Exception e) {
            fail("Not looking for this");
        }

        // ok now for the real test.

        try {
            csh.create(admin, "api-test-chan-label",
                    "apiTestChanName", "apiTestSummary", "channel-x86_64", null);
        }
        catch (PermissionCheckFailureException | InvalidChannelLabelException e) {
            fail("Not looking for this");
        }
        catch (InvalidChannelNameException e) {
            // do nothing, this we expect
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }

        try {
            csh.create(admin, "api-test-chan-label",
                    "apiTestChanName1010101", "apiTestSummary", "channel-x86_64", null);
        }
        catch (PermissionCheckFailureException | InvalidChannelNameException e) {
            fail("Not looking for this");
        }
        catch (InvalidChannelLabelException e) {
            // do nothing, this we expect
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }
    }

    @Test
    public void testSetContactDetails() throws Exception {

        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        assertNull(channel.getMaintainerName());
        assertNull(channel.getMaintainerEmail());
        assertNull(channel.getMaintainerPhone());
        assertNull(channel.getSupportPolicy());

        // execute
        int result = handler.setContactDetails(admin, channel.getLabel(),
                "John Doe", "jdoe@somewhere.com", "9765551212", "No Policy");

        // verify
        assertEquals(1, result);

        channel = ChannelFactory.lookupByLabelAndUser(channel.getLabel(), admin);

        assertEquals("John Doe", channel.getMaintainerName());
        assertEquals("jdoe@somewhere.com", channel.getMaintainerEmail());
        assertEquals("9765551212", channel.getMaintainerPhone());
        assertEquals("No Policy", channel.getSupportPolicy());
    }

    public void xxxtestListPackagesWithoutChannel() throws Exception {
        // Disable this test till we find out why listPackagesWithoutChannel takes
        // *forever*, but only running under this test!
        Object[] iniailList = handler.listPackagesWithoutChannel(admin);

        PackageTest.createTestPackage(admin.getOrg());
        Package nonOrphan = PackageTest.createTestPackage(admin.getOrg());
        Channel testChan = ChannelFactoryTest.createTestChannel(admin);
        testChan.addPackage(nonOrphan);

        Object[] secondList = handler.listPackagesWithoutChannel(admin);

        assertEquals(1, secondList.length - iniailList.length);
    }

    @Test
    public void testChannelSubscription() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        Channel baseChan = ChannelFactoryTest.createBaseChannel(admin);
        Channel childChan = ChannelFactoryTest.createTestChannel(admin);
        childChan.setParentChannel(baseChan);

        SystemHandler sh = new SystemHandler(taskomaticApi, xmlRpcSystemHelper, systemEntitlementManager, systemManager,
                serverGroupManager, new CloudPaygManager(), new AttestationManager());

        int return1 = sh.setBaseChannel(admin, server.getId().intValue(), baseChan.getLabel());
        int return2 = sh.setChildChannels(admin, server.getId().intValue(), List.of(childChan.getLabel()));

        assertEquals(1, return1);
        assertEquals(1, return2);
        server = HibernateFactory.reload(server);
        assertEquals(2, server.getChannels().size());
        assertTrue(server.getChannels().contains(baseChan));
        assertTrue(server.getChannels().contains(childChan));

        return1 = sh.setBaseChannel(admin, server.getId().intValue(), "");
        return2 = sh.setChildChannels(admin, server.getId().intValue(), List.of());
        assertEquals(1, return1);
        assertEquals(1, return2);
        server = HibernateFactory.reload(server);
        assertEquals(0, server.getChannels().size());
    }


    @Test
    public void testCloneAll() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin, false);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label";
        Map<String, String> details = new HashMap<>();
        details.put("name", "test-clone");
        details.put("summary", "summary");
        details.put("label", label);
        details.put("checksum", "sha256");

        int id = handler.clone(admin, original.getLabel(), details, false);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = TestUtils.reload(chan);
        assertNotNull(chan);
        assertEquals(label, chan.getLabel());
        assertEquals(1, chan.getPackages().size());
        assertEquals(original.isGPGCheck(), chan.isGPGCheck());
        assertFalse(chan.isGPGCheck());

        // errata cloning is tested in CloneErrataActionTest

        // Test that we're actually creating a cloned channel:
        assertTrue(chan.isCloned());
    }

    @Test
    public void testCloneWithOverrideGPGCheck() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin, false);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestErrata(
                admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label";
        Map<String, String> details = new HashMap<>();
        details.put("name", "test-clone");
        details.put("summary", "summary");
        details.put("label", label);
        details.put("checksum", "sha256");
        details.put("gpg_check", "True");

        int id = handler.clone(admin, original.getLabel(), details, false);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = TestUtils.reload(chan);
        assertNotNull(chan);
        assertEquals(label, chan.getLabel());
        assertEquals(1, chan.getPackages().size());
        assertTrue(chan.isGPGCheck());

        assertTrue(chan.isCloned());
    }

    /*
     * Had to make 2 testClone methods because of some hibernate oddities.
     * (The 2nd time it looks up the roles within handler.clone, it gets a
     *  shared resource error).
     */
    @Test
    public void testCloneOriginal() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestErrata(
                admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label-2";
        Map<String, String> details = new HashMap<>();
        details.put("name", "test-clone2");
        details.put("summary", "summary2");
        details.put("label", label);
        details.put("checksum", "sha256");

        int id = handler.clone(admin, original.getLabel(), details, true);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = TestUtils.reload(chan);


        assertNotNull(chan);
        assertEquals(label, chan.getLabel());
        assertEquals(1, chan.getPackages().size());
        assertEquals(0, chan.getErratas().size());
    }


    @Test
    public void testMergeTo() throws Exception {

        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        Package packOne = PackageTest.createTestPackage(admin.getOrg());
        Package packTwo = PackageTest.createTestPackage(admin.getOrg());

        mergeFrom.setOrg(null);
        mergeTo.setOrg(admin.getOrg());

        mergeFrom.addPackage(packOne);
        mergeFrom.addPackage(packTwo);
        mergeTo.addPackage(packOne);

        mergeFrom = TestUtils.saveAndReload(mergeFrom);
        mergeTo = TestUtils.saveAndReload(mergeTo);

        Object[] list =  handler.mergePackages(admin, mergeFrom.getLabel(),
                mergeTo.getLabel());

        assertEquals(1, list.length);
        assertEquals(packTwo, list[0]);
    }

    @Test
    public void testMergeErrata() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        List<ErrataOverview> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 0);
        List<ErrataOverview> toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(toList.size(), 0);

        Map<String, Object> errataInfo = new HashMap<>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("advisory_status", AdvisoryStatus.FINAL.getMetadataValue());
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = new ArrayList<>();
        channels.add(mergeFrom.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
                bugs, keywords, packages, channels);
        TestUtils.flushAndEvict(errata);

        fromList = handler.listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 1);

        Object[] mergeResult = handler.mergeErrata(admin, mergeFrom.getLabel(),
                mergeTo.getLabel());
        assertEquals(mergeResult.length, fromList.size());

        toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(mergeResult.length, fromList.size());
    }

    @Test
    public void testMergeAlreadyMergedErrata() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        Map<String, Object> errataInfo = new HashMap<>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_status", AdvisoryStatus.FINAL.getMetadataValue());
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = new ArrayList<>();
        channels.add(mergeFrom.getLabel());
        channels.add(mergeTo.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
            bugs, keywords, packages, channels);
        TestUtils.flushAndEvict(errata);

        List<ErrataOverview> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(1, fromList.size());

        Object[] mergeResult = handler.mergeErrata(
            admin, mergeFrom.getLabel(), mergeTo.getLabel());

        fromList = handler.listErrata(admin, mergeFrom.getLabel());
        assertEquals(1, fromList.size());
        assertEquals(0, mergeResult.length);
    }

    @Test
    public void testMergeErrataByDate() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        List<ErrataOverview> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 0);
        List<ErrataOverview> toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(toList.size(), 0);

        Map<String, Object> errataInfo = new HashMap<>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("advisory_status", AdvisoryStatus.FINAL.getMetadataValue());
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = new ArrayList<>();
        channels.add(mergeFrom.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
                bugs, keywords, packages, channels);
        TestUtils.flushAndEvict(errata);

        fromList = handler.listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 1);

        Object[] mergeResult = handler.mergeErrata(admin, mergeFrom.getLabel(),
                mergeTo.getLabel(), "2008-09-30", "2030-09-30");
        assertEquals(mergeResult.length, fromList.size());

        toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(mergeResult.length, fromList.size());

        // perform a second merge on an interval where we know we don't have any
        // errata and verify the result
        mergeResult = handler.mergeErrata(admin, mergeFrom.getLabel(),
                mergeTo.getLabel(), "2006-09-30", "2007-10-30");
        assertEquals(mergeResult.length, 0);
    }


    @Test
    public void testListLatestPackages() throws Exception {
        Channel chan = ChannelFactoryTest.createTestChannel(admin);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        chan.addPackage(pack);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -5);

        List<PackageDto> list = handler.listAllPackages(admin, chan.getLabel());
        assertEquals(1, list.size());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = sdf.parse("2004-08-20 08:00:00");
        Date endDate = sdf.parse("3004-08-20 08:00:00");

        list = handler.listAllPackages(admin, chan.getLabel(), startDate);
        assertEquals(1, list.size());

        list = handler.listAllPackages(admin, chan.getLabel(), startDate,
                endDate);
        assertEquals(1, list.size());
    }

    private ChannelSoftwareHandler getMockedHandler() throws Exception {
        TaskomaticApi taskomaticMock = mockContext.mock(TaskomaticApi.class);
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticMock, xmlRpcSystemHelper);
        ChannelManager.setTaskomaticApi(taskomaticMock);

        mockContext.checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock)
                    .scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
            allowing(taskomaticMock)
                    .getRepoSyncSchedule(with(any(Channel.class)), with(any(User.class)));
            allowing(taskomaticMock).isRunning();
            will(returnValue(true));
        }});


        return csh;
    }
}
