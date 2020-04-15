/**
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

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
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
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
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
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * ChannelSoftwareHandlerTest
 */
@SuppressWarnings("deprecation")
public class ChannelSoftwareHandlerTest extends BaseHandlerTestCase {

    private TaskomaticApi taskomaticApi = new TaskomaticApi();
    private SystemQuery systemQuery = new SaltService();
    private RegularMinionBootstrapper regularMinionBootstrapper = RegularMinionBootstrapper.getInstance(systemQuery);
    private SSHMinionBootstrapper sshMinionBootstrapper = SSHMinionBootstrapper.getInstance(systemQuery);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private ChannelSoftwareHandler handler = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
    private ErrataHandler errataHandler = new ErrataHandler();
    private final Mockery MOCK_CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};
    /**
     * {@inheritDoc}
     */
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

        List<Long> packages2add = new ArrayList<Long>();
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

        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        List<String> labels = new ArrayList<String>();
        labels.add(child.getLabel());
        // adding base last to make sure the handler does the right
        // thing regardless of where the base channel is.
        labels.add(base.getLabel());

        Integer sid = server.getId().intValue();
        int rc = csh.setSystemChannels(admin, sid, labels);

        server = (Server) reload(server);

        // now verify
        assertEquals(1, rc);
        assertEquals(2, server.getChannels().size());
        Channel newBase = server.getBaseChannel();
        assertNotNull(newBase);
        assertEquals(newBase.getLabel(), base.getLabel());

        List<String> nobase = new ArrayList<String>();
        nobase.add(child.getLabel());
        nobase.add(child2.getLabel());

        try {
            rc = csh.setSystemChannels(admin, sid, nobase);
            fail("setSystemChannels didn't complain when given no base channel");
        }
        catch (InvalidChannelException ice) {
            // ice ice baby
        }

    }

    public void testSetSystemChannels() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);

        Channel c1 = ChannelFactoryTest.createTestChannel(admin);
        Server server = ServerFactoryTest.createTestServer(admin, true);

        List<String> channelsToSubscribe = new ArrayList<String>();
        channelsToSubscribe.add(c1.getLabel());

        assertEquals(0, server.getChannels().size());
        int result = csh.setSystemChannels(admin,
                server.getId().intValue(), channelsToSubscribe);

        server = (Server) reload(server);

        assertEquals(1, result);
        assertEquals(1, server.getChannels().size());

        Channel c2 = ChannelFactoryTest.createTestChannel(admin);
        assertFalse(c1.getLabel().equals(c2.getLabel()));
        channelsToSubscribe = new ArrayList<String>();
        channelsToSubscribe.add(c2.getLabel());
        assertEquals(1, channelsToSubscribe.size());
        result = csh.setSystemChannels(admin,
                server.getId().intValue(), channelsToSubscribe);

        server = (Server) reload(server);

        assertEquals(1, result);
        Channel subscribed = server.getChannels().iterator().next();
        assertTrue(server.getChannels().contains(c2));

        //try to make it break
        channelsToSubscribe = new ArrayList<String>();
        channelsToSubscribe.add(TestUtils.randomString());
        try {
            csh.setSystemChannels(admin,
                    server.getId().intValue(), channelsToSubscribe);
            fail("subscribed system to invalid channel.");
        }
        catch (Exception e) {
            //success
        }

        server = (Server) reload(server);
        //make sure servers channel subscriptions weren't changed
        assertEquals(1, result);
        subscribed = server.getChannels().iterator().next();
        assertEquals(c2.getLabel(), subscribed.getLabel());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {

            Channel c3 = ChannelFactoryTest.createTestChannel(admin);
            List<String> channels = new ArrayList<String>();
            channels.add(c3.getLabel());
            assertEquals(1, channels.size());

            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);

            int rc = csh.setSystemChannels(admin,
                    server.getId().intValue(), channels);

            fail("allowed incompatible channel arch to be set, returned: " + rc);
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

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

    public void testListArches() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        List<ChannelArch> arches = csh.listArches(admin);
        assertNotNull(arches);
        assertTrue(arches.size() > 0);
    }

    public void testListArchesPermissionError() {
        try {
            ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
            List<ChannelArch> arches = csh.listArches(admin);
            assertNotNull(arches);
            assertTrue(arches.size() > 0);
        }
        catch (PermissionCheckFailureException e) {
            assertTrue(true);
        }
    }

    public void testDeleteChannel() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        String label = c.getLabel();
        c = (Channel) reload(c);

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

    public void testDeleteClonedChannel() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Channel cClone1 = ChannelFactoryTest.createTestClonedChannel(c, admin);
        Channel cClone2 = ChannelFactoryTest.createTestClonedChannel(cClone1, admin);
        cClone2 = (Channel) reload(cClone2);

        assertEquals(1, csh.delete(admin, cClone2.getLabel()));
        assertNotNull(reload(c));
        assertNotNull(reload(cClone1));
        assertNull(reload(cClone2));
    }

    public void testDeleteChannelWithClones() throws Exception {
        ChannelSoftwareHandler csh = getMockedHandler();
        addRole(admin, RoleFactory.CHANNEL_ADMIN);

        Channel c = ChannelFactoryTest.createTestChannel(admin);
        Channel cClone1 = ChannelFactoryTest.createTestClonedChannel(c, admin);
        Channel cClone2 = ChannelFactoryTest.createTestClonedChannel(cClone1, admin);
        cClone1 = (Channel) reload(cClone1);
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

    public void testIsGloballySubscribable() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        assertEquals(1, csh.isGloballySubscribable(admin, c.getLabel()));
        // should be assertTrue
    }

    public void testIsGloballySubscribableNoSuchChannel() throws Exception {
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

    public void testSetDetails() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        Channel c = ChannelFactoryTest.createTestChannel(admin);
        assertNotNull(c);
        assertNull(c.getParentChannel());

        Channel  result = csh.getDetails(admin, c.getLabel());

        Map<String, String> details = new HashMap<>();
        details.put("checksum_label", "sha256");
        details.put("name","new-name");
        details.put("summary", "new-summary");
        details.put("description", "new-dsc");
        details.put("maintainer_name","foo");
        details.put("maintainer_email","foo@bar.com");
        details.put("maintainer_phone","+18098098");
        details.put("gpg_key_url","http://gpg.url");
        details.put("gpg_key_id","AE1234BC");
        details.put("gpg_key_fp"," CA20 8686 2BD6 9DFC 65F6 ECC4 2191 80CD DB42 A60E");
        details.put("gpg_check", "True");
        csh.setDetails(admin,c.getLabel(),details);

        channelDetailsEquality(c, result);

        result = csh.getDetails(admin, c.getId().intValue());
        channelDetailsEquality(c, result);
    }

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
            assertEquals(null, result.getEndOfLife());
        }

        assertEquals(null, result.getParentChannel());
    }

    public void testCreate() throws Exception {
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

    public void testCreateWithGPGCheckDisabled() throws Exception {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        int i = csh.create(admin, "api-test-chan-label",
                "apiTestChanName", "apiTestSummary", "channel-x86_64", null, "sha1", new HashMap<String, String>(),false);
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

    public void testCreateWithChecksum() throws Exception {
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
        catch (InvalidChannelLabelException e) {
            fail("Wasn't expecting this in this test.");
        }
        catch (InvalidChannelNameException e) {
            fail("Wasn't expecting this in this test.");
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }
    }

    public void testCreateNullRequiredParams() {
        ChannelSoftwareHandler csh = new ChannelSoftwareHandler(taskomaticApi, xmlRpcSystemHelper);
        addRole(admin, RoleFactory.CHANNEL_ADMIN);
        // null label
        try {
            csh.create(admin, null, "api-test-nonnull", "api test summary",
                    "channel-x86_64", null);
            fail("create did not throw exception when given a null label");
        }
        catch (IllegalArgumentException iae) {
            fail("Wasn't expecting this in this test.");
        }
        catch (PermissionCheckFailureException e) {
            fail("We're not looking for this exception right now");
        }
        catch (InvalidChannelLabelException expected) {
            // expected
        }
        catch (InvalidChannelNameException e) {
            fail("Wasn't expecting this in this test.");
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }

        try {
            csh.create(admin, "api-test-nonnull", null, "api test summary",
                    "channel-x86_64", null);
            fail("create did not throw exception when given a null label");
        }
        catch (IllegalArgumentException iae) {
            fail("Wasn't expecting this in this test.");
        }
        catch (PermissionCheckFailureException e) {
            fail("We're not looking for this exception right now");
        }
        catch (InvalidChannelLabelException e) {
            fail("Wasn't expecting this in this test.");
        }
        catch (InvalidChannelNameException expected) {
            // expected
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }
    }

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
        catch (PermissionCheckFailureException e) {
            fail("Not looking for this");
        }
        catch (InvalidChannelLabelException e) {
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
        catch (PermissionCheckFailureException e) {
            fail("Not looking for this");
        }
        catch (InvalidChannelLabelException e) {
            // do nothing, this we expect
        }
        catch (InvalidChannelNameException e) {
            fail("Not looking for this");
        }
        catch (InvalidParentChannelException e) {
            fail("Wasn't expecting this in this test.");
        }
    }

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

    public void testSubscribeSystem() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        Channel baseChan = ChannelFactoryTest.createBaseChannel(admin);
        Channel childChan = ChannelFactoryTest.createTestChannel(admin);
        childChan.setParentChannel(baseChan);


        List<String> labels = new ArrayList<String>();
        labels.add(baseChan.getLabel());
        labels.add(childChan.getLabel());

        int returned = handler.subscribeSystem(admin,
                server.getId().intValue(), labels);

        assertEquals(1, returned);
        server = (Server)HibernateFactory.reload(server);
        assertEquals(2, server.getChannels().size());
        assertTrue(server.getChannels().contains(baseChan));
        assertTrue(server.getChannels().contains(childChan));

        labels.clear();
        returned = handler.subscribeSystem(admin,
                server.getId().intValue(), labels);
        assertEquals(1, returned);
        server = (Server)HibernateFactory.reload(server);
        assertEquals(0, server.getChannels().size());
    }


    public void testCloneAll() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin, false);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestPublishedErrata(
                admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label";
        Map<String, String> details = new HashMap<String, String>();
        details.put("name", "test-clone");
        details.put("summary", "summary");
        details.put("label", label);
        details.put("checksum", "sha256");

        int id = handler.clone(admin, original.getLabel(), details, false);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = (Channel) TestUtils.reload(chan);
        assertNotNull(chan);
        assertEquals(label, chan.getLabel());
        assertEquals(1, chan.getPackages().size());
        assertEquals(original.isGPGCheck(), chan.isGPGCheck());
        assertFalse(chan.isGPGCheck());

        // errata cloning is tested in CloneErrataActionTest

        // Test that we're actually creating a cloned channel:
        assertTrue(chan.isCloned());
    }

    public void testCloneWithOverrideGPGCheck() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin, false);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestPublishedErrata(
                admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label";
        Map<String, String> details = new HashMap<String, String>();
        details.put("name", "test-clone");
        details.put("summary", "summary");
        details.put("label", label);
        details.put("checksum", "sha256");
        details.put("gpg_check", "True");

        int id = handler.clone(admin, original.getLabel(), details, false);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = (Channel) TestUtils.reload(chan);
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
    public void testCloneOriginal() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(admin);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        Errata errata = ErrataFactoryTest.createTestPublishedErrata(
                admin.getOrg().getId());
        original.addPackage(pack);
        original.addErrata(errata);

        String label = "test-clone-label-2";
        Map<String, String> details = new HashMap<String, String>();
        details.put("name", "test-clone2");
        details.put("summary", "summary2");
        details.put("label", label);
        details.put("checksum", "sha256");

        int id = handler.clone(admin, original.getLabel(), details, true);
        Channel chan = ChannelFactory.lookupById((long) id);
        chan = (Channel) TestUtils.reload(chan);


        assertNotNull(chan);
        assertEquals(label, chan.getLabel());
        assertEquals(1, chan.getPackages().size());
        assertEquals(0, chan.getErratas().size());
    }


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

        mergeFrom = (Channel) TestUtils.saveAndReload(mergeFrom);
        mergeTo = (Channel) TestUtils.saveAndReload(mergeTo);

        Object[] list =  handler.mergePackages(admin, mergeFrom.getLabel(),
                mergeTo.getLabel());

        assertEquals(1, list.length);
        assertEquals(packTwo, list[0]);
    }

    public void testMergeErrata() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        List<Map<String, Object>> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 0);
        List<Map<String, Object>> toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(toList.size(), 0);

        Map<String, Object> errataInfo = new HashMap<String, Object>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<Integer>();
        List<Map<String, Object>> bugs = new ArrayList<Map<String, Object>>();
        List<String> keywords = new ArrayList<String>();
        List<String> channels = new ArrayList<String>();
        channels.add(mergeFrom.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
                bugs, keywords, packages, true, channels);
        TestUtils.flushAndEvict(errata);

        fromList = handler.listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 1);

        Object[] mergeResult = handler.mergeErrata(admin, mergeFrom.getLabel(),
                mergeTo.getLabel());
        assertEquals(mergeResult.length, fromList.size());

        toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(mergeResult.length, fromList.size());
    }

    public void testMergeAlreadyMergedErrata() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        Map<String, Object> errataInfo = new HashMap<String, Object>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<Integer>();
        List<Map<String, Object>> bugs = new ArrayList<Map<String, Object>>();
        List<String> keywords = new ArrayList<String>();
        List<String> channels = new ArrayList<String>();
        channels.add(mergeFrom.getLabel());
        channels.add(mergeTo.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
            bugs, keywords, packages, true, channels);
        TestUtils.flushAndEvict(errata);

        List<Map<String, Object>> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(1, fromList.size());

        Object[] mergeResult = handler.mergeErrata(
            admin, mergeFrom.getLabel(), mergeTo.getLabel());

        fromList = handler.listErrata(admin, mergeFrom.getLabel());
        assertEquals(1, fromList.size());
        assertEquals(0, mergeResult.length);
    }

    public void testMergeErrataByDate() throws Exception {
        Channel mergeFrom = ChannelFactoryTest.createTestChannel(admin);
        Channel mergeTo = ChannelFactoryTest.createTestChannel(admin);

        List<Map<String, Object>> fromList = handler
                .listErrata(admin, mergeFrom.getLabel());
        assertEquals(fromList.size(), 0);
        List<Map<String, Object>> toList = handler.listErrata(admin, mergeTo.getLabel());
        assertEquals(toList.size(), 0);

        Map<String, Object> errataInfo = new HashMap<String, Object>();
        String advisoryName = TestUtils.randomString();
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "unspecified");

        List<Integer> packages = new ArrayList<Integer>();
        List<Map<String, Object>> bugs = new ArrayList<Map<String, Object>>();
        List<String> keywords = new ArrayList<String>();
        List<String> channels = new ArrayList<String>();
        channels.add(mergeFrom.getLabel());

        Errata errata = errataHandler.create(admin, errataInfo,
                bugs, keywords, packages, true, channels);
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


    public void testListLatestPackages() throws Exception {
        Channel chan = ChannelFactoryTest.createTestChannel(admin);
        Package pack = PackageTest.createTestPackage(admin.getOrg());
        chan.addPackage(pack);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -5);

        String startDateStr = "2004-08-20 08:00:00";
        String endDateStr = "3004-08-20 08:00:00";

        List<PackageDto> list = handler.listAllPackages(admin, chan.getLabel(),
                startDateStr);
        assertTrue(list.size() == 1);

        list = handler.listAllPackages(admin, chan.getLabel(), startDateStr,
                endDateStr);
        assertTrue(list.size() == 1);

        list = handler.listAllPackages(admin, chan.getLabel());
        assertTrue(list.size() == 1);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);

        list = handler.listAllPackages(admin, chan.getLabel(), startDate);
        assertTrue(list.size() == 1);

        list = handler.listAllPackages(admin, chan.getLabel(), startDate,
                endDate);
        assertTrue(list.size() == 1);
    }

    public void testUnsubscribeChildChannels() throws Exception {
        ChannelSoftwareHandler handler = getMockedHandler();
        ActionChainManager.setTaskomaticApi(handler.getTaskomaticApi());

        Server server = MinionServerFactoryTest.createTestMinionServer(admin);
        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        Channel base = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base);
        child2.setParentChannel(base);
        base.setParentChannel(null);

        server.addChannel(base);
        server.addChannel(child1);
        server.addChannel(child2);

        Integer sid = server.getId().intValue();

        Date earliest = new Date();
        SystemHandler systemHandler = new SystemHandler(taskomaticApi, xmlRpcSystemHelper);
        long actionId = systemHandler.scheduleChangeChannels(admin, sid, base.getLabel(),
                Arrays.asList(child1.getLabel(),child2.getLabel()), earliest);

        Action action = ActionFactory.lookupById(actionId);
        SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
        assertEquals(base.getId(), sca.getDetails().getBaseChannel().getId());
        assertEquals(2, sca.getDetails().getChannels().size());


        //unsubscribe one child channel
        long[] actionIds = handler.unsubscribeChannels(admin, Collections.singletonList(sid), "",
                Arrays.asList(child2.getLabel()));

        action = ActionFactory.lookupById(actionIds[0]);
        assertTrue(action instanceof SubscribeChannelsAction);
        sca = (SubscribeChannelsAction)action;
        assertEquals(base.getId(), sca.getDetails().getBaseChannel().getId());
        assertEquals(1, sca.getDetails().getChannels().size());
    }

    public void testUnsubscribeBaseChannel() throws Exception {
        ChannelSoftwareHandler handler = getMockedHandler();
        ActionChainManager.setTaskomaticApi(handler.getTaskomaticApi());
        Server server = MinionServerFactoryTest.createTestMinionServer(admin);
        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        Channel base = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base);
        child2.setParentChannel(base);
        base.setParentChannel(null);

        server.addChannel(base);
        server.addChannel(child1);
        server.addChannel(child2);

        Integer sid = server.getId().intValue();

        Date earliest = new Date();
        SystemHandler systemHandler = new SystemHandler(taskomaticApi, xmlRpcSystemHelper);
        long actionId = systemHandler.scheduleChangeChannels(admin, sid, base.getLabel(),
                Arrays.asList(child1.getLabel(),child2.getLabel()), earliest);
        Action action = ActionFactory.lookupById(actionId);
        assertTrue(action instanceof SubscribeChannelsAction);
        SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
        assertEquals(base.getId(), sca.getDetails().getBaseChannel().getId());
        assertEquals(2, sca.getDetails().getChannels().size());

        //unsubscribe base channel (all the child channels will be removed too)
        long [] actionIds= handler.unsubscribeChannels(admin, Collections.singletonList(sid), base.getLabel(),
                Collections.emptyList());

        action = ActionFactory.lookupById(actionIds[0]);
        assertTrue(action instanceof SubscribeChannelsAction);
        sca = (SubscribeChannelsAction)action;
        assertNull(sca.getDetails().getBaseChannel());
        assertTrue(sca.getDetails().getChannels().isEmpty());
        assertEquals(0, sca.getDetails().getChannels().size());
    }

    private ChannelSoftwareHandler getMockedHandler() throws Exception {
        TaskomaticApi taskomaticMock = MOCK_CONTEXT.mock(TaskomaticApi.class);
        ChannelSoftwareHandler systemHandler = new ChannelSoftwareHandler(taskomaticMock, xmlRpcSystemHelper);
        ChannelManager.setTaskomaticApi(taskomaticMock);


        MOCK_CONTEXT.checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock)
                    .scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
            allowing(taskomaticMock)
                    .getRepoSyncSchedule(with(any(Channel.class)), with(any(User.class)));
            allowing(taskomaticMock).isRunning();
            will(returnValue(true));
        }});


        return systemHandler;
    }
}
