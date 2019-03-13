/**
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
package com.redhat.rhn.frontend.xmlrpc.system.test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.redhat.rhn.domain.server.NetworkInterfaceFactory;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.manager.action.ActionChainManager;
import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit3.JUnit3Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.jmock.lib.legacy.ClassImposteriser;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptResult;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CustomDataValue;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.test.CPUTest;
import com.redhat.rhn.domain.server.test.GuestBuilder;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.HistoryEvent;
import com.redhat.rhn.frontend.dto.OperationDetailsDto;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.dto.ServerPath;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.events.SsmDeleteServersAction;
import com.redhat.rhn.frontend.xmlrpc.ChannelSubscriptionException;
import com.redhat.rhn.frontend.xmlrpc.InvalidActionTypeException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidEntitlementException;
import com.redhat.rhn.frontend.xmlrpc.InvalidErrataException;
import com.redhat.rhn.frontend.xmlrpc.InvalidPackageException;
import com.redhat.rhn.frontend.xmlrpc.MissingCapabilityException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchActionException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchPackageException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.UndefinedCustomFieldsException;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;
import com.redhat.rhn.frontend.xmlrpc.system.SUSEInstalledProduct;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.ssm.SsmOperationManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SystemHandlerTest extends BaseHandlerTestCase {

    private SystemHandler handler = new SystemHandler();

    private final Mockery MOCK_CONTEXT = new JUnit3Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ClassImposteriser.INSTANCE);
    }};

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testGetNetworkDevices() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.getNetworkInterfaces().clear();

        NetworkInterface device = NetworkInterfaceTest.createTestNetworkInterface(server);
        server.addNetworkInterface(device);
        assertEquals(1, server.getNetworkInterfaces().size());
        TestUtils.saveAndFlush(server);

        List<NetworkInterface> results = handler.getNetworkDevices(admin, server.getId().intValue());
        assertEquals(1, results.size());

        NetworkInterface dev = results.get(0);
        assertEquals(device.getName(), dev.getName());
        assertEquals(device.getIPv4Addresses(), dev.getIPv4Addresses());
    }

    public void testObtainReactivationKey() throws Exception {
        Server server = ServerFactoryTest.createUnentitledTestServer(admin, true,
                ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        //since we can't really test this without giving the server entitlements, just
        //make sure we get a permission exception
        try {
            handler.obtainReactivationKey(admin, server.getId().intValue());
            fail("SystemHandler.obtainReactivationKey allowed key to be generated for " +
            "system without agent smith feature.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

    }

    public void testObtainReactivationKeyWithCert() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());
        assertFalse(StringUtils.isBlank(handler.obtainReactivationKey(cert.toString())));
    }

    public void xxxtestUpgradeEntitlement() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Entitlement ent = EntitlementManager.VIRTUALIZATION;

        assertTrue(SystemManager.canEntitleServer(server, ent));

        int result = handler.upgradeEntitlement(admin,
                server.getId().intValue(),
                ent.getLabel());

        assertEquals(1, result);
        assertFalse(SystemManager.canEntitleServer(server, ent));

        //Cause PermissionCheckFailureException
        try {
            result = handler.upgradeEntitlement(admin,
                    server.getId().intValue(),
                    ent.getLabel());
            fail("SystemHandler.upgradeEntitlement allowed upgrade when canEntitleServer " +
            "evalueated to false.");
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        //Cause InvalidEntitlementException
        try {
            result = handler.upgradeEntitlement(admin,
                    server.getId().intValue(),
                    TestUtils.randomString());
            fail("SystemHandler.upgradeEntitlement allowed upgrade to phoney entitlement");
        }
        catch (InvalidEntitlementException e) {
            //success
        }
    }

    public void testSetChildChannelsDeprecated() throws Exception {
        // the usage of setChildChannels API as tested by this junit where
        // channel ids are passed as arguments is being deprecated...

        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNull(server.getBaseChannel());
        Integer sid = server.getId().intValue();

        Channel base = ChannelFactoryTest.createTestChannel(admin);
        base.setParentChannel(null);

        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base);

        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        child2.setParentChannel(base);

        //subscribe to base channel.
        SystemManager.subscribeServerToChannel(admin, server, base);
        server = reload(server);
        assertNotNull(server.getBaseChannel());

        List cids = new ArrayList();
        cids.add(child1.getId().intValue());
        cids.add(child2.getId().intValue());

        int result = handler.setChildChannels(admin, sid, cids);
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertEquals(3, server.getChannels().size());

        //Try 'unsubscribing' from child1...
        cids = new ArrayList();
        cids.add(child2.getId().intValue());
        assertEquals(1, cids.size());

        result = handler.setChildChannels(admin, sid, cids);
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertEquals(2, server.getChannels().size());

        //Try putting an invalid channel in there
        cids = new ArrayList();
        cids.add(-32339);
        assertEquals(1, cids.size());

        try {
            result = handler.setChildChannels(admin, sid, cids);
            fail("SystemHandler.setChildChannels allowed invalid child channel to be set.");
        }
        catch (InvalidChannelException e) {
            //success
        }
        assertEquals(2, server.getChannels().size());

        Channel base2 = ChannelFactoryTest.createTestChannel(admin);
        base2.setParentChannel(null);
        cids = new ArrayList();
        cids.add(base2.getId().intValue());
        assertEquals(1, cids.size());

        try {
            result = handler.setChildChannels(admin, sid, cids);
            fail("SystemHandler.setChildChannels allowed invalid child channel to be set.");
        }
        catch (InvalidChannelException e) {
            //success
        }
        catch (ChannelSubscriptionException e) {
            //success
        }

        server = reload(server);
        assertEquals(2, server.getChannels().size());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {
            List ia32Children = new ArrayList();
            ia32Children.add(child1.getId().intValue());
            ia32Children.add(child2.getId().intValue());

            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);


            result = handler.setChildChannels(admin, sid, ia32Children);
            fail("allowed invalid child channel to be set.");
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

    public void testSetChildChannels() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNull(server.getBaseChannel());
        Integer sid = server.getId().intValue();

        Channel base = ChannelFactoryTest.createTestChannel(admin);
        base.setParentChannel(null);

        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base);

        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        child2.setParentChannel(base);

        //subscribe to base channel.
        SystemManager.subscribeServerToChannel(admin, server, base);
        server = reload(server);
        assertNotNull(server.getBaseChannel());

        List channelLabels = new ArrayList();
        channelLabels.add(new String(child1.getLabel()));
        channelLabels.add(new String(child2.getLabel()));

        int result = handler.setChildChannels(admin, sid, channelLabels);
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertEquals(3, server.getChannels().size());

        //Try 'unsubscribing' from child1...
        channelLabels = new ArrayList();
        channelLabels.add(new String(child2.getLabel()));
        assertEquals(1, channelLabels.size());

        result = handler.setChildChannels(admin, sid, channelLabels);
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertEquals(2, server.getChannels().size());

        //Try putting an invalid channel in there
        channelLabels = new ArrayList();
        channelLabels.add(new String("invalid-unknown-channel-label"));
        assertEquals(1, channelLabels.size());

        try {
            result = handler.setChildChannels(admin, sid, channelLabels);
            fail("SystemHandler.setChildChannels allowed invalid child channel to be set.");
        }
        catch (InvalidChannelLabelException e) {
            //success
        }
        assertEquals(2, server.getChannels().size());

        Channel base2 = ChannelFactoryTest.createTestChannel(admin);
        base2.setParentChannel(null);
        channelLabels = new ArrayList();
        channelLabels.add(new String(base2.getLabel()));
        assertEquals(1, channelLabels.size());

        try {
            result = handler.setChildChannels(admin, sid, channelLabels);
            fail("SystemHandler.setChildChannels allowed invalid child channel to be set.");
        }
        catch (InvalidChannelException e) {
            //success
        }
        catch (ChannelSubscriptionException e) {
            //success
        }

        server = reload(server);
        assertEquals(2, server.getChannels().size());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {
            List ia32Children = new ArrayList();
            ia32Children.add(new String(child1.getLabel()));
            ia32Children.add(new String(child2.getLabel()));

            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);


            result = handler.setChildChannels(admin, sid, ia32Children);
            fail("allowed invalid child channel to be set.");
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

    public void testSetBaseChannelDeprecated() throws Exception {
        // the setBaseChannel API tested by this junit is being deprecated

        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNull(server.getBaseChannel());
        Integer sid = server.getId().intValue();

        Channel base1 = ChannelFactoryTest.createTestChannel(admin);
        base1.setParentChannel(null);

        Channel base2 = ChannelFactoryTest.createTestChannel(admin);
        base2.setParentChannel(null);

        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base1);

        // Set base channel to base1
        int result = handler.setBaseChannel(admin, sid,
                base1.getId().intValue());
        server = reload(server);
        assertEquals(1, result);
        assertNotNull(server.getBaseChannel());
        assertEquals(server.getBaseChannel().getLabel(), base1.getLabel());

        // Set base channel to base2
        result = handler.setBaseChannel(admin, sid,
                base2.getId().intValue());
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertNotNull(server.getBaseChannel());
        assertEquals(server.getBaseChannel().getLabel(), base2.getLabel());

        // Try setting base channel to child
        try {
            result = handler.setBaseChannel(admin, sid,
                    child1.getId().intValue());
            fail("SystemHandler.setBaseChannel allowed invalid base channel to be set.");
        }
        catch (InvalidChannelException e) {
            // success
        }
        assertEquals(server.getBaseChannel().getLabel(), base2.getLabel());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {
            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);


            result = handler.setBaseChannel(admin, sid,
                    base1.getId().intValue());
            fail("allowed channel with incompatible arch to be set");
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

    public void testSetBaseChannel() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNull(server.getBaseChannel());
        Integer sid = server.getId().intValue();

        Channel base1 = ChannelFactoryTest.createTestChannel(admin);
        base1.setParentChannel(null);

        Channel base2 = ChannelFactoryTest.createTestChannel(admin);
        base2.setParentChannel(null);

        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(base1);

        // Set base channel to base1
        int result = handler.setBaseChannel(admin, sid, base1.getLabel());
        server = reload(server);
        assertEquals(1, result);
        assertNotNull(server.getBaseChannel());
        assertEquals(server.getBaseChannel().getLabel(), base1.getLabel());

        // Set base channel to base2
        result = handler.setBaseChannel(admin, sid, base2.getLabel());
        server = TestUtils.reload(server);
        assertEquals(1, result);
        assertNotNull(server.getBaseChannel());
        assertEquals(server.getBaseChannel().getLabel(), base2.getLabel());

        // Try setting base channel to child
        try {
            result = handler.setBaseChannel(admin, sid, child1.getLabel());
            fail("SystemHandler.setBaseChannel allowed invalid base channel to be set.");
        }
        catch (InvalidChannelException e) {
            // success
        }
        assertEquals(server.getBaseChannel().getLabel(), base2.getLabel());

        // try setting the base channel of an s390 server to
        // IA-32.
        try {
            // change the arch of the server
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("s390-redhat-linux"));
            ServerFactory.save(server);


            result = handler.setBaseChannel(admin, sid, base1.getLabel());
            fail("allowed channel with incompatible arch to be set");
        }
        catch (InvalidChannelException e) {
            // success
        }
    }

    public void testScheduleChangeChannels() throws Exception {
        SystemHandler handler = getMockedHandler();
        ActionChainManager.setTaskomaticApi(handler.getTaskomaticApi());

        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        Channel parent = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(parent);
        child2.setParentChannel(parent);

        server.addChannel(parent);
        server.addChannel(child1);
        server.addChannel(child2);

        Integer sid = server.getId().intValue();

        Channel base1 = ChannelFactoryTest.createTestChannel(admin);
        base1.setParentChannel(null);
        Channel child3 = ChannelFactoryTest.createTestChannel(admin);
        child3.setParentChannel(base1);
        Date earliest = new Date();
        long actionId = handler.scheduleChangeChannels(admin, sid, base1.getLabel(),
                    Arrays.asList(child3.getLabel()), earliest);

        Action action = ActionFactory.lookupById(actionId);
        assertEquals(earliest, action.getEarliestAction());
        assertTrue(action instanceof SubscribeChannelsAction);
        SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
        assertEquals(base1.getId(), sca.getDetails().getBaseChannel().getId());
        assertEquals(1, sca.getDetails().getChannels().size());
        assertTrue(sca.getDetails().getChannels()
                .stream().filter(cc -> cc.getId().equals(child3.getId())).findFirst().isPresent());
    }

    public void testScheduleChangeChannelsNoChildren() throws Exception {
        SystemHandler handler = getMockedHandler();
        ActionChainManager.setTaskomaticApi(handler.getTaskomaticApi());

        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel child1 = ChannelFactoryTest.createTestChannel(admin);
        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        Channel parent = ChannelFactoryTest.createTestChannel(admin);
        child1.setParentChannel(parent);
        child2.setParentChannel(parent);

        server.addChannel(parent);
        server.addChannel(child1);
        server.addChannel(child2);

        Integer sid = server.getId().intValue();

        Channel base1 = ChannelFactoryTest.createTestChannel(admin);
        base1.setParentChannel(null);
        Date earliest = new Date();
        long actionId = handler.scheduleChangeChannels(admin, sid, base1.getLabel(),
                Collections.emptyList(), earliest);

        Action action = ActionFactory.lookupById(actionId);
        assertEquals(earliest, action.getEarliestAction());
        assertTrue(action instanceof SubscribeChannelsAction);
        SubscribeChannelsAction sca = (SubscribeChannelsAction)action;
        assertEquals(base1.getId(), sca.getDetails().getBaseChannel().getId());
        assertTrue(sca.getDetails().getChannels().isEmpty());
    }


    public void testListSubscribableBaseChannels() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Channel base = ChannelFactoryTest.createTestChannel(admin);
        base.setParentChannel(null);
        SystemManager.subscribeServerToChannel(admin, server, base);

        Object[] results = handler.listSubscribableBaseChannels(admin,
                server.getId().intValue());

        assertTrue(results.length > 0);
        //make sure that every channel returned has null for parent_channel
        for (int i = 0; i < results.length; i++) {
            Map map = (Map) results[i];
            Number id = (Number) map.get("id");
            Long cid = id.longValue();
            Channel c = ChannelManager.lookupByIdAndUser(cid, admin);
            assertNull(c.getParentChannel());
        }
    }

    public void testListSubscribableChildChannels() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel parent = ChannelFactoryTest.createTestChannel(admin);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(admin);
        child.setParentChannel(parent);

        Object[] result = handler.listSubscribableChildChannels(admin,
                server.getId().intValue());
        //server shouldn't have any channels yet
        assertEquals(0, result.length);
        SystemManager.subscribeServerToChannel(admin, server, parent);
        server = reload(server);
        result = handler.listSubscribableChildChannels(admin,
                server.getId().intValue());

        //server should have 1 child channel
        assertEquals(1, result.length);
    }

    public void testListBaseChannels() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Channel base = ChannelFactoryTest.createTestChannel(admin);
        base.setParentChannel(null);
        SystemManager.subscribeServerToChannel(admin, server, base);

        Object[] results = handler.listBaseChannels(admin,
                server.getId().intValue());

        assertTrue(results.length > 0);
        //make sure that every channel returned has null for parent_channel
        for (int i = 0; i < results.length; i++) {
            Map map = (Map) results[i];
            Number id = (Number) map.get("id");
            Long cid = id.longValue();
            Channel c = ChannelManager.lookupByIdAndUser(cid, admin);
            assertNull(c.getParentChannel());
        }
    }

    public void testListChildChannels() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel parent = ChannelFactoryTest.createTestChannel(admin);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(admin);
        child.setParentChannel(parent);

        Object[] result = handler.listChildChannels(admin,
                server.getId().intValue());
        //server shouldn't have any channels yet
        assertEquals(0, result.length);
        SystemManager.subscribeServerToChannel(admin, server, parent);
        server = reload(server);
        result = handler.listChildChannels(admin,
                server.getId().intValue());

        //server should have 1 child channel
        assertEquals(1, result.length);
    }

    public void testListNewerOlderInstalledPackages() throws Exception {
        //TODO: not really sure how to test this guy. For now, send in a foobared nvre and
        //make sure we get back a fault exception
        Server server = ServerFactoryTest.createTestServer(admin, true);
        try {
            handler.listNewerInstalledPackages(admin,
                    server.getId().intValue(),
                    TestUtils.randomString(), "3", "4", "");
            fail("listNewerInstalledPackages did not throw fault exception.");
        }
        catch (NoSuchPackageException e) {
            //success
        }

        try {
            handler.listOlderInstalledPackages(admin,
                    server.getId().intValue(),
                    "" + System.currentTimeMillis(), "3", "4", "");
            fail("listOlderInstalledPackages did not throw fault exception.");
        }
        catch (NoSuchPackageException e) {
            //success
        }
    }

    public void testListLatestUpgradablePackages() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int numPackages = SystemManager.latestUpgradablePackages(server.getId()).size();

        List<Map<String, Object>> results =
                handler.listLatestUpgradablePackages(admin,
                        server.getId().intValue());

        //make sure the handler returns the same number of packages as systemmanger...
        assertEquals(numPackages, results.size());
    }

    public void testListLatestInstallablePackages() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int numPackages = SystemManager.latestInstallablePackages(server.getId()).size();

        List<Map<String, Object>> results =
                handler.listLatestInstallablePackages(admin,
                        server.getId().intValue());

        //make sure the handler returns the same number of packages as systemmanger...
        assertEquals(numPackages, results.size());
    }


    public void testGetEntitlements() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int numEntitlements = server.getEntitlements().size();

        Object[] results = handler.getEntitlements(admin,
                server.getId().intValue());
        assertEquals(numEntitlements, results.length);
        assertTrue(results.length > 0);
        String entLabel = (String) results[0];
        Entitlement e = EntitlementManager.getByName(entLabel);
        assertTrue(server.hasEntitlement(e));
    }

    public void testDownloadSystemId() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        String sysid = handler.downloadSystemId(admin,
                server.getId().intValue());
        assertNotNull(sysid);
    }

    public void testListPackages() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int numPackages = SystemManager.installedPackages(server.getId(), false).size();

        List<Map<String, Object>> result =
                handler.listPackages(admin,
                        server.getId().intValue());

        int numPackages2 = result.size();

        assertEquals(numPackages, numPackages2);

        //TODO: when we can add packages to servers via a test method, make revisit this.
    }


    public void testListExtraPackages() throws Exception {
        Package testPackage = PackageTest.createTestPackage(admin.getOrg());
        Server server = ServerFactoryTest.createTestServer(admin, true);
        PackageManagerTest.associateSystemToPackage(server, testPackage);

        int numPackages = SystemManager.listExtraPackages(server.getId()).size();

        List<Map<String, Object>> result =
                handler.listExtraPackages(admin,
                        server.getId().intValue());

        int numPackages2 = result.size();

        assertTrue(result.stream().anyMatch(m -> m.get("name").equals(testPackage.getPackageName().getName())));
        assertEquals(numPackages, numPackages2);
    }

    public void testIsNvreInstalled() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int result = handler.isNvreInstalled(admin,
                server.getId().intValue(),
                "foo", "-4", "1.b2", "bar");

        assertEquals(0, result);

        result = handler.isNvreInstalled(admin,
                server.getId().intValue(),
                "foo", "-4", "1.b2");

        assertEquals(0, result);

    }

    public void testDeleteSystemWithCert() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Long sid = server.getId();
        ClientCertificate cert = SystemManager.createClientCertificate(server);
        cert.validate(server.getSecret());
        KickstartDataTest.setupTestConfiguration(admin);
        assertEquals(1, handler.deleteSystem(cert.toString()));
        assertNull(ServerFactory.lookupById(sid));
    }

    public void testDeleteSystems() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Long sid = server.getId();
        Integer id = sid.intValue();

        Server test = SystemManager.lookupByIdAndUser(sid, admin);
        assertNotNull(test);
        //ok, we have an admin with a server he has access to
        List sids = new ArrayList();
        sids.add(id);
        try {
            handler.deleteSystems(regular, sids);
            fail("SystemHandler.deleteSystems allowed unauthorized deletion");
        }
        catch (FaultException e) {
            //success
        }
        assertEquals(1, handler.deleteSystems(admin, sids));

        List<OperationDetailsDto> ops = SsmOperationManager.allOperations(admin);
        for (OperationDetailsDto op : ops) {
            assertEquals(op.getDescription(), LocalizationService.getInstance().
                    getMessage(SsmDeleteServersAction.OPERATION_NAME));
        }
    }

    public void testScheduleVirtProvision() throws Exception {
        Server server = ServerTestUtils.createTestSystem(admin);
        server.setBaseEntitlement(EntitlementManager.MANAGEMENT);
        TestUtils.saveAndFlush(server);
        server = reload(server);
        KickstartDataTest.setupTestConfiguration(admin);
        KickstartData k = KickstartDataTest.createKickstartWithProfile(admin);
        KickstartDataTest.addCommand(admin, k, "url", "--url http://cascade.sfbay.redhat." +
        "com/rhn/kickstart/ks-rhel-i386-server-5");

        k.getKickstartDefaults().getKstree().setChannel(server.getBaseChannel());
        ChannelTestUtils.setupBaseChannelForVirtualization(admin, server.getBaseChannel());


        String guestName = "vguest-" + TestUtils.randomString();
        String profileName = k.getLabel();

        int result = handler.provisionVirtualGuest(admin,
                server.getId().intValue(),
                guestName, profileName,
                256, 1, 2048);
        assertEquals(1, result);
        assertNotNull(KickstartFactory.lookupAllKickstartSessionsByServer(server.getId()));
    }

    public void testAddNote() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        int sizeBefore = server.getNotes().size();
        int result = handler.addNote(admin, server.getId().intValue(),
                "TestNote", "TestNote body");
        int sizeAfter = server.getNotes().size();
        assertEquals(1, result);
        assertTrue(sizeAfter > sizeBefore);
        assertEquals(1, sizeAfter - sizeBefore);
    }

    public void testDeleteNote() throws Exception {
        // Setup
        Server server = ServerFactoryTest.createTestServer(admin);
        int sizeBefore = server.getNotes().size();
        int result = handler.addNote(admin, server.getId().intValue(),
                "TestNote", "TestNote body");
        int sizeAfter = server.getNotes().size();
        assertEquals(1, result);
        assertTrue(sizeAfter > sizeBefore);

        // Test
        Note deleteMe = server.getNotes().iterator().next();
        result = handler.deleteNote(admin, server.getId().intValue(),
                deleteMe.getId().intValue());

        // Verify
        assertEquals(1, result);
    }

    public void testDeleteAllNotes() throws Exception {
        // Setup
        Server server = ServerFactoryTest.createTestServer(admin);
        int sizeBefore = server.getNotes().size();
        int result = handler.addNote(admin, server.getId().intValue(),
                "TestNote", "TestNote body");
        int sizeAfter = server.getNotes().size();
        assertEquals(1, result);
        assertTrue(sizeAfter > sizeBefore);

        // Test
        result = handler.deleteNotes(admin, server.getId().intValue());

        // Verify
        assertEquals(1, result);

        server = ServerFactory.lookupById(server.getId());
        assertEquals(0, server.getNotes().size());
    }

    public void testListAllEvents() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        List<Map<String, Object>>results = handler.listSystemEvents(admin,
                server.getId().intValue());
        assertEquals(0, results.size());

        Action a = ActionManager.scheduleHardwareRefreshAction(admin, server, new Date());

        ActionFactory.save(a);
        a = reload(a);

        results = handler.listSystemEvents(admin,
                server.getId().intValue());

        assertEquals(1, results.size());
    }

    public void testListSystems() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        List<Object> results = Arrays.asList(handler.listSystems(admin));
        assertTrue(results.size() >= 1);
        assertTrue(results.stream().anyMatch(so -> ((SystemOverview) so).getId().equals(server.getId())));

        for (Object o : results) {
            SystemOverview so = (SystemOverview) o;
            assertNotNull(so.getLastBootAsDate());
        }
    }

    public void testSetProfileName() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        int result;
        //try empty string
        try {
            result = handler.setProfileName(admin,
                    server.getId().intValue(), "    ");
            fail("SystemHandler.setProfileName allowed an invalid profile name to be set.");
        }
        catch (FaultException e) {
            //success
        }
        //try name that is too short
        try {
            result = handler.setProfileName(admin,
                    server.getId().intValue(), "   f   ");
            fail("SystemHandler.setProfileName allowed an invalid profile name to be set.");
        }
        catch (FaultException e) {
            //success
        }

        //try name that is too long
        try {
            result = handler.setProfileName(admin,
                    server.getId().intValue(), getLongTestString());
            fail("SystemHandler.setProfileName allowed an invalid profile name to be set.");
        }
        catch (FaultException e) {
            //success
        }

        String validProfileName = "XmlRpcTest - " + TestUtils.randomString();
        result = handler.setProfileName(admin,
                server.getId().intValue(), validProfileName);
        assertEquals(1, result);
        assertEquals(validProfileName, server.getName());
    }

    private String getLongTestString() {
        StringBuffer longString = new StringBuffer();
        //run 15 times for good measure
        for (int i = 0; i < 15; i++) {
            longString.append(TestUtils.randomString());
        }
        return longString.toString();
    }

    public void testCustomDataValues() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        CustomDataKey testKey = CustomDataKeyTest.createTestCustomDataKey(admin);

        // setCustomValues
        Org org = admin.getOrg();
        org.addCustomDataKey(testKey);

        String keyLabel = testKey.getLabel();

        String val1 = TestUtils.randomString();
        String val2 = "foo" + TestUtils.randomString();
        String fooKey = "foo" + TestUtils.randomString();

        server.addCustomDataValue(testKey, val1, admin);

        CustomDataValue val = server.getCustomDataValue(testKey);
        //make sure the value was set properly
        assertEquals(val1, val.getValue());

        Map valuesToSet = new HashMap();
        valuesToSet.put(keyLabel, val2);

        int setResult = handler.setCustomValues(admin,
                server.getId().intValue(),
                valuesToSet);

        //make sure the val was updated
        val = server.getCustomDataValue(testKey);
        assertEquals(val2, val.getValue());
        assertEquals(1, setResult);

        // try to set custom values with some undefined keys
        valuesToSet.put(fooKey, val1);
        try {
            setResult = handler.setCustomValues(admin,
                    server.getId().intValue(),
                    valuesToSet);
            fail("Didn't get exception for undefined keys.");
        }
        catch (UndefinedCustomFieldsException e) {
            //success
        }

        //getCustomValues
        Map result = handler.getCustomValues(admin,
                server.getId().intValue());

        assertEquals(1, result.size());

        // try to delete custom values using undefined keys
        List<String> valuesToDelete = new ArrayList<String>();
        valuesToDelete.add(fooKey);
        try {
            setResult = handler.deleteCustomValues(admin,
                    server.getId().intValue(),
                    valuesToDelete);
            fail("Didn't get exception for undefined keys.");
        }
        catch (UndefinedCustomFieldsException e) {
            //success
        }
        val = server.getCustomDataValue(testKey);
        assertNotNull(val);

        result = handler.getCustomValues(admin, server.getId().intValue());
        assertEquals(1, result.size());

        // now delete the custom value that was previously added
        valuesToDelete.clear();
        valuesToDelete.add(testKey.getLabel());
        setResult = handler.deleteCustomValues(admin,
                server.getId().intValue(),
                valuesToDelete);

        assertEquals(1, setResult);
        val = server.getCustomDataValue(testKey);
        assertNull(val);
    }

    public void testListUserSystems() throws Exception {

        DataResult adminSystems = UserManager.visibleSystems(admin);
        int numAdminSystems = adminSystems.size();

        List results = handler.listUserSystems(admin, admin.getLogin());
        assertEquals(numAdminSystems, results.size());

        ServerFactoryTest.createTestServer(admin, true);
        results = handler.listUserSystems(admin, admin.getLogin());
        assertTrue(results.size() > numAdminSystems);
    }

    public void testListGroups() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        DataResult dr = SystemManager.availableSystemGroups(server, admin);
        Object[] results = handler.listGroups(admin,
                server.getId().intValue());
        assertEquals(dr.size(), results.length);
    }

    public void testSetMembership() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        ServerGroup group = ServerGroupTestUtils.createManaged(admin);

        assertEquals(0, group.getServers().size());

        handler.setGroupMembership(admin,
                server.getId().intValue(),
                group.getId().intValue(),
                true);

        assertEquals(1, group.getServers().size());


        handler.setGroupMembership(admin,
                server.getId().intValue(),
                group.getId().intValue(),
                false);

        assertEquals(0, group.getServers().size());


        try {
            handler.setGroupMembership(admin,
                    server.getId().intValue(),
                    -10,
                    true);
            fail();
        }
        catch (FaultException e) {
            //success
        }
    }

    public void testGetNetwork() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNull(server.getIpAddress());
        assertNull(server.getHostname());

        Map result = handler.getNetwork(admin, server.getId().intValue());
        assertNotNull(result.get("ip"));
        assertNotNull(result.get("hostname"));
    }

    public void testGetId() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        List<SystemOverview> idList = handler.getId(admin, server.getName());
        assertTrue(1 == idList.size());
        SystemOverview smap = idList.get(0);
        assertEquals(server.getId(), smap.getId());
        assertEquals(server.getName(), smap.getName());
        assertNotNull(smap.getLastCheckin());
    }

    public void testGetName() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Map name = handler.getName(admin, server.getId().intValue());

        assertTrue(null != name);
        assertEquals(server.getId(), name.get("id"));
        assertEquals(server.getName(), (String)name.get("name"));
        assertNotNull(name.get("last_checkin"));

        try {
            Map invalid = handler.getName(admin, 10001234);
            assertTrue(null != invalid);
            assertNull(invalid.get("id"));
            assertNull(invalid.get("name"));
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }

    public void testGetRegistrationDate() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertEquals(server.getCreated(), handler.getRegistrationDate(admin,
                server.getId().intValue()));

    }

    public void testListSubscribedChildChannels() throws Exception {

        Server server = ServerFactoryTest.createTestServer(admin, true);
        Channel child = ChannelFactoryTest.createTestChannel(admin);
        Channel child2 = ChannelFactoryTest.createTestChannel(admin);
        Channel parent = ChannelFactoryTest.createTestChannel(admin);
        child.setParentChannel(parent);
        child2.setParentChannel(parent);

        server.addChannel(parent);
        server.addChannel(child);
        server.addChannel(child2);

        List<Channel> list = handler.listSubscribedChildChannels(admin,
                server.getId().intValue());

        assertEquals(2, list.size());

        Channel childMap =  list.get(0);
        Channel childMap2 = list.get(1);
        String id1 =  childMap.getId().toString();
        String id2 =  childMap2.getId().toString();

        //make sure the two aren't equal
        assertFalse(id1.equals(id2));
        //make sure each one is in the list
        assertTrue(id1.equals(child.getId().toString()) ||
                id2.equals(child.getId().toString()));
        assertTrue(id1.equals(child2.getId().toString()) ||
                id2.equals(child2.getId().toString()));

    }


    public void testSearchForIds() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.setName("ydjdk1234");
        Server server2 = ServerFactoryTest.createTestServer(admin, true);
        server2.setName("ydjdk-test1234");

        List<SystemOverview> sysList = handler.searchByName(admin, "ydjdk1");
        assertTrue(sysList.size() == 1);
        SystemOverview result = sysList.get(0);
        assertEquals(server.getId().intValue(), (result.getId().intValue()));
        assertEquals("ydjdk1234", result.getName());
        assertTrue(null != result.getLastCheckin());
    }

    public void testListAdministrators() throws Exception {
        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Set servers = new HashSet();
        servers.add(server);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        manager.addServers(group, servers, admin);

        Set admins = new HashSet();
        admins.add(regular);
        manager.associateAdmins(group, admins, admin);


        User nonGroupAdminUser = UserTestUtils.createUser(
                "testUser3", admin.getOrg().getId());
        nonGroupAdminUser.removePermanentRole(RoleFactory.ORG_ADMIN);

        List users = ServerFactory.listAdministrators(server);

        boolean containsAdmin = false;
        boolean containsRegular = false;
        boolean containsNonGroupAdmin = false;  //we want this to be false to pass

        for (Iterator itr = users.iterator(); itr.hasNext();) {

            User user = (User) itr.next();
            if (user.getLogin().equals(admin.getLogin())) {
                containsAdmin = true;
            }
            if (user.getLogin().equals(regular.getLogin())) {
                containsRegular = true;
            }
            if (user.getLogin().equals(nonGroupAdminUser.getLogin())) {
                containsNonGroupAdmin = true;
            }
        }
        assertTrue(containsAdmin);
        assertTrue(containsRegular);
        assertFalse(containsNonGroupAdmin);
    }

    public void testGetRunningKernel() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertEquals(ServerFactoryTest.RUNNING_KERNEL, handler.getRunningKernel(admin,
                server.getId().intValue()));
    }

    public void testUserCantSeeRunningKernel() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        try {
            handler.getRunningKernel(regular, server.getId().intValue());
            fail();
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }

    public void testGetEventHistory() throws Exception {

        Server server = ServerFactoryTest.createTestServer(admin, true);

        ServerHistoryEvent event = new ServerHistoryEvent();
        event.setServer(server);
        event.setDetails("details");
        event.setSummary("summary");

        Set history = server.getHistory();
        server.setHistory(history);
        TestUtils.saveAndFlush(event);
        TestUtils.saveAndFlush(server);

        Object[] supposedHistory = handler.getEventHistory(admin,
                server.getId().intValue());

        assertEquals(((HistoryEvent) supposedHistory[0]).getId().longValue(),
                event.getId().longValue());
    }

    public void testGetRelevantErrata() throws Exception {

        Errata e = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_BUG);
        TestUtils.flushAndEvict(e);
        Server s = ServerFactoryTest.createTestServer(admin);
        ServerFactory.save(s);
        TestUtils.flushAndEvict(s);

        UserFactory.save(admin);
        TestUtils.flushAndEvict(admin);
        Package p = e.getPackages().iterator().next();
        ErrataCacheManager.insertNeededErrataCache(
                s.getId(), e.getId(), p.getId());

        Object[] array = handler.getRelevantErrata(admin,
                s.getId().intValue());
        assertEquals(array.length, 1);
        ErrataOverview errata = (ErrataOverview) array[0];
        assertEquals(e.getId().intValue(), errata.getId().intValue());
    }

    public void testGetRelevantErrataByType() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        int numErrata = SystemManager.relevantErrataByType(admin, server.getId(),
        "Bug Fix Advisory").size();

        Object[] result = handler.getRelevantErrataByType(admin,
                server.getId().intValue(), "Bug Fix Advisory");

        int numErrata2 = result.length;

        assertEquals(numErrata, numErrata2);
    }

    public void testGetDmi() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Dmi dmi = new Dmi();
        dmi.setAsset("asset_string");
        dmi.setBoard("board_string");
        dmi.setProduct("product_string");
        dmi.setVendor("vendor_string");
        dmi.setSystem("system_string");
        dmi.setBios("release_string", "version_string", "release_string");
        dmi.setServer(server);
        server.setDmi(dmi);

        Dmi dmiMap = (Dmi) handler.getDmi(admin,
                server.getId().intValue());

        assertEquals(dmi.getAsset(), dmiMap.getAsset());
        assertEquals(dmi.getBoard(), dmiMap.getBoard());
        assertEquals(dmi.getProduct(), dmiMap.getProduct());
        assertEquals(dmi.getVendor(), dmiMap.getVendor());
        assertEquals(dmi.getSystem(), dmiMap.getSystem());
        assertEquals(dmi.getBios().getVendor(), dmiMap.getBios().getVendor());
        assertEquals(dmi.getBios().getVersion(), dmiMap.getBios().getVersion());
        assertEquals(dmi.getBios().getRelease(), dmiMap.getBios().getRelease());
    }

    public void testGetCpu() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        CPU cpu = new CPU();
        cpu.setCache("cache_string");
        cpu.setFamily("family_string");
        cpu.setMHz("MHz_string");
        cpu.setVendor("vendor_string");
        cpu.setFlags("flags_string");
        cpu.setModel("model_string");
        cpu.setStepping("stepping_string");

        cpu.setServer(server);
        cpu.setArch(ServerFactory.lookupCPUArchByName(CPUTest.ARCH_NAME));
        server.setCpu(cpu);

        CPU cpuMap = (CPU) handler.getCpu(admin,
                server.getId().intValue());
        assertEquals(cpu.getCache(), cpuMap.getCache());
        assertEquals(cpu.getFamily(), cpuMap.getFamily());
        assertEquals(cpu.getMHz(), cpuMap.getMHz());
        assertEquals(cpu.getVendor(), cpuMap.getVendor());
        assertEquals(cpu.getFlags(), cpuMap.getFlags());
        assertEquals(cpu.getModel(), cpuMap.getModel());
        assertEquals(cpu.getStepping(), cpuMap.getStepping());
        assertEquals(cpu.getArchName(), cpuMap.getArchName());
    }

    public void testGetMemory() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.setRam(1024);
        server.setSwap(1025);

        Map memory = handler.getMemory(admin,
                server.getId().intValue());
        assertEquals(server.getRam(), memory.get("ram"));
        assertEquals(server.getSwap(), memory.get("swap"));
    }

    public void testGetDevices() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Device device = new Device();
        device.setBus(Device.BUS_ADB);
        device.setDeviceClass(Device.CLASS_FLOPPY);
        device.setDescription("description_string");
        device.setDriver("mighty_mouse");
        device.setPcitype((long) -3);
        device.setDevice("device_string");

        device.setServer(server);
        server.addDevice(device);

        Object[] dev =  handler.getDevices(admin,
                server.getId().intValue());

        Device fetchedDev = (Device) dev[0];
        assertEquals(device.getBus(), fetchedDev.getBus());
        assertEquals(device.getDeviceClass(), fetchedDev.getDeviceClass());
        assertEquals(device.getDescription(), fetchedDev.getDescription());
        assertEquals(device.getDriver(), fetchedDev.getDriver());
        assertEquals(device.getPcitype(), fetchedDev.getPcitype());
        assertEquals(device.getDevice(), fetchedDev.getDevice());
    }


    public void testScheduleNonExistentPackageInstall() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        List packageIds = new LinkedList();
        packageIds.add(-1);

        try {
            handler.schedulePackageInstall(admin, server.getId().intValue(), packageIds, new Date());
            fail();
        }
        catch (InvalidPackageException e) {
            // expected
        }
    }

    public void testScheduleScript() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        SystemManagerTest.giveCapability(server.getId(), "script.run", 1L);

        List serverIds = new ArrayList();
        serverIds.add(server.getId().intValue());

        Integer actionId = handler.scheduleScriptRun(admin,
                serverIds, "root", "root",
                600, "", new Date());

        ScriptRunAction newAction = (ScriptRunAction)ActionManager.lookupAction(
                admin, actionId.longValue());
        assertNotNull(newAction);
        ScriptActionDetails newActionDetails = newAction.getScriptActionDetails();
        assertNotNull(newActionDetails);

        // Results not yet available:
        Object [] result = handler.getScriptResults(admin, actionId);
        assertEquals(0, result.length);
    }

    public void testScheduleScriptMissingCapability() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        List serverIds = new ArrayList();
        serverIds.add(server.getId().intValue());

        try {
            handler.scheduleScriptRun(admin,
                    serverIds, "root", "root",
                    600, "", new Date());
            fail();
        }
        catch (MissingCapabilityException e) {
            // expected
        }
    }

    public void testScheduleScriptAsUnentitledUser() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        List serverIds = new ArrayList();
        serverIds.add(server.getId().intValue());

        try {
            handler.scheduleScriptRun(regular,
                    serverIds, "root", "root",
                    600, "", new Date());
            fail();
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }

    public void testScheduleScriptNoSuchServer() throws Exception {
        List serverIds = new ArrayList();
        serverIds.add(-1);

        try {
            handler.scheduleScriptRun(admin, serverIds, "root", "root",
                    600, "", new Date());
            fail();
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }

    public void testGetScriptResult() throws Exception {
        Calendar futureCal = GregorianCalendar.getInstance();
        futureCal.set(2050, 12, 14);

        Server server = ServerFactoryTest.createTestServer(admin, true);
        SystemManagerTest.giveCapability(server.getId(), "script.run", 1L);

        List serverIds = new ArrayList();
        serverIds.add(server.getId().intValue());

        Integer actionId = handler.scheduleScriptRun(admin,
                serverIds, "root", "root",
                600, "", new Date());

        ScriptRunAction newAction = (ScriptRunAction)ActionManager.lookupAction(
                admin, actionId.longValue());

        ScriptActionDetails newActionDetails = newAction.getScriptActionDetails();
        newActionDetails.setParentAction(newAction);

        ScriptResult result = new ScriptResult();
        result.setServerId(server.getId());
        result.setReturnCode(1L);
        result.setStartDate(new Date());
        result.setStopDate(futureCal.getTime());
        result.setParentScriptActionDetails(newActionDetails);
        result.setActionScriptId(newActionDetails.getId());

        newActionDetails.addResult(result);
        newAction.setScriptActionDetails(newActionDetails);

        ActionFactory.save(newAction);
        flushAndEvict(newAction);

        Object [] scriptResult = handler.getScriptResults(admin,
                newAction.getId().intValue());
        assertEquals(1, scriptResult.length);
    }

    public void testGetScriptResultForNonExistentAction() throws Exception {
        try {
            handler.getScriptResults(admin, -1);
            fail();
        }
        catch (NoSuchActionException e) {
            // expected
        }
    }

    public void testGetScriptResultForWrongActionType() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Action a = ActionManager.scheduleHardwareRefreshAction(admin, server, new Date());
        ActionFactory.save(a);

        try {
            handler.getScriptResults(admin, a.getId().intValue());
            fail();
        }
        catch (InvalidActionTypeException e) {
            // expected
        }
    }

    public void testApplyIrrelevantErrata() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        List serverIds = new ArrayList();
        serverIds.add(server.getId().intValue());

        Errata irrelevantErrata = ErrataFactoryTest.createTestPublishedErrata(
                admin.getOrg().getId());
        assertEquals(0, SystemManager.relevantErrata(admin, server.getId()).size());
        List errataIds = new LinkedList();
        errataIds.add(irrelevantErrata.getId().intValue());
        try {
            handler.applyErrata(admin, server.getId().intValue(),
                    errataIds);
            fail();
        }
        catch (InvalidErrataException e) {
            // expected
        }
        try {
            handler.scheduleApplyErrata(admin, serverIds, errataIds);
            fail();
        }
        catch (InvalidErrataException e) {
            // expected
        }
    }

    public void testSchedulePackageInstall() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Package pkg = PackageTest.createTestPackage(admin.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();
        handler.schedulePackageInstall(admin, server.getId().intValue(),
                packageIds, new Date());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public void testSchedulePackageRemove() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        // add a package to the server
        Package pkg = PackageManagerTest.addPackageToSystemAndChannel(
                "test-package-name" + TestUtils.randomString(), server,
                ChannelFactoryTest.createTestChannel(admin));
        server = TestUtils.reload(server);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();
        server.getPackages().size();

        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        handler.schedulePackageRemove(admin, server.getId().intValue(),
                packageIds, new Date());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Removal", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public void testHardwareRefresh() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();
        handler.scheduleHardwareRefresh(admin, server.getId().intValue(),
                new Date());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Hardware List Refresh", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public void testPackageRefresh() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();
        handler.schedulePackageRefresh(admin, server.getId().intValue(),
                new Date());

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package List Refresh", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    public void testGetDetails() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        Server lookupServer = (Server)handler.getDetails(admin,
                server.getId().intValue());
        assertNotNull(lookupServer);
    }

    public void testGetDetailsNoSuchServer() throws Exception {
        try {
            handler.getDetails(admin, -1);
            fail();
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }

    public void testSetDetails() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        SystemManager.removeAllServerEntitlements(server.getId());

        Map details = new HashMap();
        String profileName = "blah";
        String description = "some description";
        String address1 = "address1";
        String address2 = "address2";
        String city = "Halifax";
        String state = "Nova Scotia";
        String country = "CA";
        String building = "building";
        String room = "room";
        String rack = "rack";
        String contactMethod = "ssh-push";

        details.put("profile_name", profileName);
        details.put("base_entitlement", "enterprise_entitled");
        details.put("description", description);
        details.put("auto_errata_update", Boolean.TRUE);
        details.put("address1", address1);
        details.put("address2", address2);
        details.put("city", city);
        details.put("state", state);
        details.put("country", country);
        details.put("building", building);
        details.put("room", room);
        details.put("rack", rack);
        details.put("contact_method", contactMethod);

        handler.setDetails(admin, server.getId().intValue(), details);
        TestUtils.saveAndFlush(server);
        server = reload(server);

        assertEquals(profileName, server.getName());
        assertEquals(description, server.getDescription());
        assertEquals("Y", server.getAutoUpdate());
        assertEquals("enterprise_entitled", server.getBaseEntitlement().getLabel());
        assertEquals(address1, server.getLocation().getAddress1());
        assertEquals(address2, server.getLocation().getAddress2());
        assertEquals(city, server.getLocation().getCity());
        assertEquals(state, server.getLocation().getState());
        assertEquals(country, server.getLocation().getCountry());
        assertEquals(building, server.getLocation().getBuilding());
        assertEquals(room, server.getLocation().getRoom());
        assertEquals(rack, server.getLocation().getRack());
        assertEquals(contactMethod, server.getContactMethod().getLabel());
    }

    public void testSetDetailsContactMethodInvalid() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        SystemManager.removeAllServerEntitlements(server.getId());

        Map details = new HashMap();
        details.put("contact_method", "foobar");
        try {
            handler.setDetails(admin, server.getId().intValue(), details);
            fail("Setting invalid contact method should throw exception!");
        }
        catch (FaultException e) {
            // expected
        }
    }

    public void testSetDetailsContactMethodForSalt() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);

        Map details = new HashMap();
        details.put("contact_method", "ssh-push");
        try {
            handler.setDetails(admin, server.getId().intValue(), details);
            fail("Modifying contact method on salt system should throw exception!");
        }
        catch (FaultException e) {
            assertEquals("contactMethodChangeNotAllowed", e.getLabel());
            // expected
        }
    }

    public void testSetLockStatus() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        //server unlocked by default
        assertNull(server.getLock());

        //lock the server
        handler.setLockStatus(admin, server.getId().intValue(),
                true);

        TestUtils.saveAndFlush(server);
        server = reload(server);
        assertNotNull(server.getLock());

        //unlock the server
        handler.setLockStatus(admin, server.getId().intValue(),
                false);

        TestUtils.saveAndFlush(server);
        server = reload(server);
        assertNull(server.getLock());
    }

    public void testSetDetailsUnentitleServer() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);
        SystemManager.removeAllServerEntitlements(server.getId());
        Map details = new HashMap();
        details.put("base_entitlement", "unentitle");

        handler.setDetails(admin, server.getId().intValue(), details);
        TestUtils.saveAndFlush(server);
        server = reload(server);

        assertNull(server.getBaseEntitlement());
    }

    public void testSetDetailsBaseEntitlementAsNonOrgAdmin() throws Exception {
        Server server = ServerFactoryTest.createTestServer(regular, true);
        SystemManager.removeAllServerEntitlements(server.getId());
        Map details = new HashMap();
        details.put("base_entitlement", "unentitle");

        try {
            handler.setDetails(regular, server.getId().intValue(), details);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            // expected
        }
    }

    public void testAddEntitlements() throws Exception {
        Server server = ServerTestUtils.createVirtHostWithGuests(admin, 0);

        Integer serverId = server.getId().intValue();
        List<String> entitlements = new LinkedList<String>() { {
            add(EntitlementManager.VIRTUALIZATION_ENTITLED);
        } };

        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        int result = handler.removeEntitlements(admin, serverId, entitlements);
        assertEquals(1, result);

        TestUtils.flushAndEvict(server);
        server = SystemManager.lookupByIdAndUser(server.getId(), admin);
        assertFalse(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        result = handler.addEntitlements(admin, serverId, entitlements);
        assertEquals(1, result);

        TestUtils.flushAndEvict(server);
        server = SystemManager.lookupByIdAndUser(server.getId(), admin);
        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    public void testAddEntitlementSystemAlreadyHas() throws Exception {
        Server server = ServerTestUtils.createVirtHostWithGuests(admin, 0);

        Integer serverId = server.getId().intValue();
        List<String> entitlements = new LinkedList<String>() { {
            add(EntitlementManager.VIRTUALIZATION_ENTITLED);
        } };

        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));

        // Shouldn't fail:
        int result = handler.addEntitlements(admin, serverId, entitlements);
        assertEquals(1, result);

        TestUtils.flushAndEvict(server);
        server = SystemManager.lookupByIdAndUser(server.getId(), admin);
        assertTrue(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    public void testRemoveEntitlements() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        SystemManager.entitleServer(server, EntitlementManager.VIRTUALIZATION);
        List entitlements = new LinkedList();
        entitlements.add(EntitlementManager.VIRTUALIZATION_ENTITLED);

        handler.removeEntitlements(admin, server.getId().intValue(),
                entitlements);
    }

    public void testRemoveEntitlementsServerDoesNotHave() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        assertFalse(server.hasEntitlement(EntitlementManager.VIRTUALIZATION));
        List entitlements = new LinkedList();
        entitlements.add(EntitlementManager.VIRTUALIZATION_ENTITLED);

        handler.removeEntitlements(admin, server.getId().intValue(),
                entitlements);
    }

    public void testListPackagesFromChannel() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(admin, true);
        Channel testChannel = ChannelFactoryTest.createTestChannel(admin);

        Package testPackage = PackageTest.createTestPackage(admin.getOrg());

        testChannel.addPackage(testPackage);

        Set<InstalledPackage> instPackages = testServer.getPackages();
        InstalledPackage testInstPack = new InstalledPackage();
        testInstPack.setArch(testPackage.getPackageArch());
        testInstPack.setEvr(testPackage.getPackageEvr());
        testInstPack.setName(testPackage.getPackageName());
        testInstPack.setServer(testServer);

        instPackages.add(testInstPack);
        Integer serverId = testServer.getId().intValue();
        Map<String, Object> returned = handler
            .listPackagesFromChannel(admin, serverId,
                        testChannel.getLabel()).get(0);

        assertEquals(testPackage.getPackageName().getName(), returned.get("name"));
        assertEquals(testPackage.getPackageEvr().getVersion(), returned.get("version"));
        assertEquals(testPackage.getPackageEvr().getRelease(), returned.get("release"));
        assertEquals(testPackage.getPackageEvr().getEpoch(), returned.get("epoch"));
        assertEquals(testPackage.getId(), returned.get("id"));
        assertEquals(testPackage.getPackageArch().getLabel(), returned.get("arch_label"));

        // reload object as modified data is changed by a trigger
        HibernateFactory.getSession().refresh(testPackage);
        assertEquals(testPackage.getModified().getTime(),
                ((Date) returned.get("last_modified")).getTime());

        assertEquals(testPackage.getPath(), returned.get("path"));
    }

    public void testListFqdns() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(admin, true);
        testServer.addFqdn("foo.bar.baz");
        testServer.addFqdn("foo.bat.xyz");
        ServerFactory.save(testServer);

        List<String> returned = handler.listFqdns(admin, testServer.getId().intValue());

        assertEquals(2, returned.size());
        assertTrue(returned.contains("foo.bar.baz"));
        assertTrue(returned.contains("foo.bat.xyz"));
    }

    public void testScheduleSyncPackagesWithSystem() throws Exception {

        Channel testChannel = ChannelFactoryTest.createTestChannel(admin);

        Package p1 = PackageTest.createTestPackage(admin.getOrg());
        Package p2 = PackageTest.createTestPackage(admin.getOrg());

        testChannel.addPackage(p1);
        testChannel.addPackage(p2);
        ChannelFactory.save(testChannel);

        Server s1 = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Server s2 = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        s1.addChannel(testChannel);
        s2.addChannel(testChannel);

        PackageManagerTest.associateSystemToPackageWithArch(s1, p1);
        PackageManagerTest.associateSystemToPackageWithArch(s2, p2);

        ServerFactory.save(s1);
        ServerFactory.save(s2);

        List packagesToSync = new LinkedList();
        packagesToSync.add(p2.getId().intValue());

        // This call has an embedded transaction in the stored procedure:
        // lookup_transaction_package(:operation, :n, :e, :v, :r, :a)
        // which can cause deadlocks.  We are forced to call commitAndCloseTransaction()
        commitAndCloseSession();
        handler.scheduleSyncPackagesWithSystem(admin, s1.getId().
                        intValue(), s2.getId().intValue(), packagesToSync,
                new Date());
    }

    public void testScheduleReboot() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(admin, true);

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        dr = ActionManager.recentlyScheduledActions(admin, null, 30);

        int preScheduleSize = dr.size();

        Long returnInt = handler.scheduleReboot(admin,
                testServer.getId().intValue(), new Date());
        assertNotNull(returnInt);

        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("System reboot", ((ScheduledAction)dr.get(0)).getTypeName());

    }


    public void testCreatePackageProfile() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(admin, true);
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        testServer.addChannel(channel);

        Package testPackage = PackageTest.createTestPackage(admin.getOrg());

        //Test a package the satellite knows about
        InstalledPackage testInstPack = new InstalledPackage();
        testInstPack.setArch(testPackage.getPackageArch());
        testInstPack.setEvr(testPackage.getPackageEvr());
        testInstPack.setName(testPackage.getPackageName());
        testInstPack.setServer(testServer);

        Set<InstalledPackage> serverPackages = testServer.getPackages();
        serverPackages.add(testInstPack);

        String profileLabel = TestUtils.randomString();

        Profile newProfile = ProfileFactory.findByNameAndOrgId(profileLabel,
                admin.getOrg().getId());
        assertNull(newProfile);

        Integer returned = handler.createPackageProfile(admin,
                testServer.getId().intValue(),
                profileLabel, TestUtils.randomString());

        assertEquals(Integer.valueOf(1), returned);

        newProfile = ProfileFactory.findByNameAndOrgId(profileLabel,
                admin.getOrg().getId());
        assertNotNull(newProfile);

        DataResult profilePackages = ProfileManager.listProfilePackages(newProfile.getId());
        assertEquals(1, profilePackages.size());
    }

    public void testComparePackageProfile() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(admin, true);
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        testServer.addChannel(channel);

        Package testPackage = PackageTest.createTestPackage(admin.getOrg());

        //Test a package the satellite knows about
        InstalledPackage testInstPack = new InstalledPackage();
        testInstPack.setArch(testPackage.getPackageArch());
        testInstPack.setEvr(testPackage.getPackageEvr());
        testInstPack.setName(testPackage.getPackageName());
        testInstPack.setServer(testServer);

        Set<InstalledPackage> serverPackages = testServer.getPackages();
        serverPackages.add(testInstPack);

        String profileLabel = TestUtils.randomString();

        handler.createPackageProfile(admin,
                testServer.getId().intValue(),
                profileLabel, TestUtils.randomString());

        // create another test server... this is the server that we will
        // compare the newly created profile against.
        Server testServer2 = ServerFactoryTest.createTestServer(admin, true);

        Object[] compareResults = handler.comparePackageProfile(admin,
                testServer2.getId().intValue(), profileLabel);

        assertEquals(1, compareResults.length);

        PackageMetadata metadata = (PackageMetadata) compareResults[0];

        // verify that the package found existed only in the profile
        assertEquals(3, metadata.getComparisonAsInt());
    }

    public void testListOutOfDateSystems() throws Exception {
        Server testServer = ServerFactoryTest.createTestServer(regular, true);

        Long sid = testServer.getId().longValue();
        Package pack = PackageTest.createTestPackage(admin.getOrg());

        ErrataCacheManager.insertNeededErrataCache(sid, null,
                pack.getId());

        Object [] array =  handler.listOutOfDateSystems(regular);

        assertTrue(array.length > 0);
        boolean sidExists  = false;
        for (int i = 0; i < array.length; i++) {
            SystemOverview s = (SystemOverview)array[i];
            if (testServer.getId().equals(s.getId().longValue())) {
                sidExists = true;
                break;
            }
        }
        assertTrue(sidExists);
    }


    public void testListUngroupedSystems() throws Exception {

        Server testServer = ServerFactoryTest.createTestServer(admin, false);
        ServerFactoryTest.createTestServer(admin, true);

        List<SystemOverview> servers = handler.listUngroupedSystems(admin);
        assertTrue(servers.size() > 0);
        boolean sidExists  = false;
        for (int i = 0; i < servers.size(); i++) {
            SystemOverview s = servers.get(i);
            if (testServer.getId().equals(s.getId())) {
                sidExists = true;
                break;
            }
        }
        assertTrue(sidExists);
    }

    public void testGetSubscribedBaseChannel() throws Exception {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);
        if (srv1.getBaseChannel() != null) {
            Channel base = (Channel) handler.getSubscribedBaseChannel(admin,
                    srv1.getId().intValue());
            assertEquals(srv1.getBaseChannel(), base);
        }
    }

    public void testListInactiveSystems() throws Exception {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);
        Calendar cal = Calendar.getInstance();
        srv1.getServerInfo().setCheckin(cal.getTime());

        List list = handler.listInactiveSystems(admin);
        assertFalse(systemInList(srv1.getId(), list));

        cal.add(Calendar.DAY_OF_YEAR, -5);

        srv1.getServerInfo().setCheckin(cal.getTime());
        list = handler.listInactiveSystems(admin);
        System.out.println(list);
        assertTrue(systemInList(srv1.getId(), list));

        list = handler.listInactiveSystems(admin, 2);
        assertTrue(systemInList(srv1.getId(), list));

        list = handler.listInactiveSystems(admin, 10);
        assertFalse(systemInList(srv1.getId(), list));

    }

    private boolean systemInList(Long id, List<SystemOverview> list) {
        for (SystemOverview server : list) {
            if (server.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }


    public void testWhoCreated() throws Exception {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);
        srv1.setCreator(admin);
        assertEquals(admin, handler.whoRegistered(admin, srv1.getId().intValue()));
    }


    public void testListSystemsWithPackage() throws Exception {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);


        Package pack = PackageTest.createTestPackage(admin.getOrg());
        pack.setOrg(admin.getOrg());
        InstalledPackage iPack = new InstalledPackage();
        iPack.setName(pack.getPackageName());
        iPack.setEvr(pack.getPackageEvr());
        iPack.setArch(pack.getPackageArch());
        iPack.setServer(srv1);
        Set<InstalledPackage> set = srv1.getPackages();
        set.add(iPack);

        PackageFactory.getSession().save(pack);

        List list = handler.listSystemsWithPackage(admin, iPack.getName().getName(),
                iPack.getEvr().getVersion(), iPack.getEvr().getRelease());

        assertTrue(systemInList(srv1.getId(), list));

        list = handler.listSystemsWithPackage(admin, pack.getId().intValue());
        assertTrue(systemInList(srv1.getId(), list));


    }

    public void testScheduleGuestAction() throws Exception {
        Server host = ServerFactoryTest.createTestServer(admin, true);
        GuestBuilder build = new GuestBuilder(admin);
        VirtualInstance guest = build.createGuest().withVirtHost().build();
        guest.setHostSystem(host);

        VirtualInstanceFactory.getInstance().saveVirtualInstance(guest);

        int id = handler.scheduleGuestAction(admin,
                guest.getGuestSystem().getId().intValue(), "restart");

        List<Action> actions = ActionFactory.listActionsForServer(admin, host);

        boolean contains = false;
        for (Action act : actions) {
            if (act.getId() == id) {
                contains = true;
                assertEquals(act.getActionType(), ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
            }
        }
        assertTrue(contains);
    }

    public void testSetGuestMemory() throws Exception {
        Server host = ServerFactoryTest.createTestServer(admin, true);
        GuestBuilder build = new GuestBuilder(admin);
        VirtualInstance guest = build.createGuest().withVirtHost().build();
        guest.setHostSystem(host);

        VirtualInstanceFactory.getInstance().saveVirtualInstance(guest);

        int id = handler.setGuestMemory(admin,
                guest.getGuestSystem().getId().intValue(), 512);

        List<Action> actions = ActionFactory.listActionsForServer(admin, host);

        boolean contains = false;
        for (Action act : actions) {
            if (act.getId() == id) {
                contains = true;
                assertEquals(act.getActionType(),
                        ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY);
                VirtualizationSetMemoryAction action = HibernateFactory.getSession().load(
                        VirtualizationSetMemoryAction.class,  (long) id);
                assertEquals(action.getMemory(), Integer.valueOf(512 * 1024));
            }
        }
        assertTrue(contains);
    }


    public void testSetGuestCpus() throws Exception {
        Server host = ServerFactoryTest.createTestServer(admin, true);
        GuestBuilder build = new GuestBuilder(admin);
        VirtualInstance guest = build.createGuest().withVirtHost().build();
        guest.setHostSystem(host);

        VirtualInstanceFactory.getInstance().saveVirtualInstance(guest);

        int id = handler.setGuestCpus(admin,
                guest.getGuestSystem().getId().intValue(), 3);

        List<Action> actions = ActionFactory.listActionsForServer(admin, host);

        boolean contains = false;
        for (Action act : actions) {
            if (act.getId() == id) {
                contains = true;
                assertEquals(act.getActionType(),
                        ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS);
                VirtualizationSetVcpusAction action = HibernateFactory.getSession().load(
                        VirtualizationSetVcpusAction.class,  (long) id);
                assertEquals(action.getVcpu(), Integer.valueOf(3));
            }
        }
        assertTrue(contains);
    }

    public void testListActivationKeys() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        ActivationKey key = ActivationKeyTest.createTestActivationKey(admin);

        List<String> keys = handler.listActivationKeys(admin, server.getId().intValue());
        assertEquals(0, keys.size());

        key.getToken().getActivatedServers().add(server);
        TestUtils.saveAndFlush(key);

        keys = handler.listActivationKeys(admin, server.getId().intValue());
        assertEquals(1, keys.size());
    }

    public void testGetConnectionPath() throws Exception {

        Server server = ServerFactoryTest.createTestServer(admin, true);

        // check the initial state of the connection path for the server
        Object[] results = handler.getConnectionPath(admin,
                server.getId().intValue());

        assertEquals(0, results.length);

        // create 2 dummy servers to represent proxys and add them to the
        // server's 'server path'
        Server proxy1 = ServerFactoryTest.createTestServer(admin, true);
        Server proxy2 = ServerFactoryTest.createTestServer(admin, true);

        Long position1 = 0L;
        Long position2 = 1L;

        WriteMode m = ModeFactory.getWriteMode("test_queries",
        "insert_into_rhnServerPath");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("server_id", server.getId());
        params.put("proxy_server_id", proxy1.getId());
        params.put("proxy_hostname", proxy1.getName());
        params.put("position", position1);
        m.executeUpdate(params);

        params.clear();
        params.put("server_id", server.getId());
        params.put("proxy_server_id", proxy2.getId());
        params.put("proxy_hostname", proxy2.getName());
        params.put("position", position2);
        m.executeUpdate(params);

        // execute test...
        results = handler.getConnectionPath(admin,
                server.getId().intValue());

        assertEquals(2, results.length);
        assertEquals(proxy1.getId(), ((ServerPath) results[0]).getId());
        assertEquals(proxy1.getName(), ((ServerPath) results[0]).getHostname());

        assertEquals(proxy2.getId(), ((ServerPath) results[1]).getId());
        assertEquals(proxy2.getName(), ((ServerPath) results[1]).getHostname());
    }


    public void testTest() throws Exception {
        String pattern = "0 \\d+ \\d+ \\? \\* \\*";
        String str = "0 0 23 ? * *";

        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(str);
        assertTrue(m.matches());
    }

    public void testListMigrationTargetNoProducts() throws Exception {

        Server server = ServerFactoryTest.createTestServer(admin, true);
        boolean thrown = false;
        try {
            handler.listMigrationTargets(admin, server.getId().intValue());
        }
        catch(FaultException e) {
            if(e.getMessage().contains("Server has no Products installed")) {
                thrown = true;
            }
        }
        assertTrue("Expected exception not thrown", thrown);
    }

    public void testListMigrationTargetBaseOnly() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(admin, null, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        admin = TestUtils.reload(admin);

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1245));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1157));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1322));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1324));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1337));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1357));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1361));
        // Do not sync HA-GEO 12 SP2
        //SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId());

        InstalledProduct installedPrd = new InstalledProduct();
        installedPrd.setName("SLES");
        installedPrd.setVersion("12");
        installedPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        installedPrd.setBaseproduct(true);
        assertNull(installedPrd.getId());

        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertNotNull(server);
        assertNotNull(server.getId());

        Set<InstalledProduct> products = new HashSet<>();
        products.add(installedPrd);

        server.setInstalledProducts(products);
        TestUtils.saveAndReload(server);

        assertNotNull(server.getInstalledProductSet().orElse(null));

        server.getInstalledProductSet().get().getBaseProduct().getUpgrades();

        List<Map<String, Object>> result = handler.listMigrationTargets(admin, server.getId().intValue());

        assertNotEmpty("no target found", result);

        assertContains(result.get(0).get("friendly").toString(), "SUSE Linux Enterprise Server 12 SP2");
        assertContains(result.get(1).get("friendly").toString(), "SUSE Linux Enterprise High Performance Computing 12 SP2");
        assertContains(result.get(2).get("friendly").toString(), "SUSE Linux Enterprise Server 12 SP1");
    }

    public void testListMigrationTargetExtension() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(admin, null, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        admin = TestUtils.reload(admin);

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1245));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1157));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1322));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1324));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1337));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1357));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1361));
        // Do not sync HA-GEO 12 SP2
        //SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId());

        InstalledProduct installedPrd = new InstalledProduct();
        installedPrd.setName("SLES");
        installedPrd.setVersion("12");
        installedPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        installedPrd.setBaseproduct(true);
        assertNull(installedPrd.getId());

        InstalledProduct installedExt = new InstalledProduct();
        installedExt.setName("sle-ha");
        installedExt.setVersion("12");
        installedExt.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        assertNull(installedExt.getId());

        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertNotNull(server);
        assertNotNull(server.getId());

        Set<InstalledProduct> products = new HashSet<>();
        products.add(installedPrd);
        products.add(installedExt);

        server.setInstalledProducts(products);
        TestUtils.saveAndReload(server);

        assertNotNull(server.getInstalledProductSet().orElse(null));

        server.getInstalledProductSet().get().getBaseProduct().getUpgrades();

        List<Map<String, Object>> result = handler.listMigrationTargets(admin, server.getId().intValue());

        assertNotEmpty("no target found", result);

        assertContains(result.get(0).get("friendly").toString(), "SUSE Linux Enterprise Server 12 SP2");
        assertContains(result.get(0).get("friendly").toString(), "SUSE Linux Enterprise High Availability Extension 12 SP2");
        assertContains(result.get(1).get("friendly").toString(), "SUSE Linux Enterprise Server 12 SP1");
        assertContains(result.get(1).get("friendly").toString(), "SUSE Linux Enterprise High Availability Extension 12 SP1");
    }

    public void testListMigrationTargetExtensionNotSynced() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(admin, null, true);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        admin = TestUtils.reload(admin);

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1117));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1245));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1157));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1322));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1324));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1337));

        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1357));
        SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId(1361));
        // Do not sync HA-GEO 12 SP2
        //SUSEProductTestUtils.addChannelsForProduct(SUSEProductFactory.lookupByProductId());

        InstalledProduct installedPrd = new InstalledProduct();
        installedPrd.setName("SLES");
        installedPrd.setVersion("12");
        installedPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        installedPrd.setBaseproduct(true);
        assertNull(installedPrd.getId());

        InstalledProduct installedExt = new InstalledProduct();
        installedExt.setName("sle-ha");
        installedExt.setVersion("12");
        installedExt.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        assertNull(installedExt.getId());

        InstalledProduct installedExt2 = new InstalledProduct();
        installedExt2.setName("sle-ha-geo");
        installedExt2.setVersion("12");
        installedExt2.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        assertNull(installedExt2.getId());

        Server server = ServerFactoryTest.createTestServer(admin, true);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertNotNull(server);
        assertNotNull(server.getId());

        Set<InstalledProduct> products = new HashSet<>();
        products.add(installedPrd);
        products.add(installedExt);
        products.add(installedExt2);

        server.setInstalledProducts(products);
        TestUtils.saveAndReload(server);

        assertNotNull(server.getInstalledProductSet().orElse(null));

        List<Map<String, Object>> result = handler.listMigrationTargets(admin, server.getId().intValue());

        assertNotEmpty("no target found", result);
        assertEquals(1, result.size());
        assertContains(result.get(0).get("friendly").toString(), "SUSE Linux Enterprise Server 12 SP1");
        assertContains(result.get(0).get("friendly").toString(), "SUSE Linux Enterprise High Availability Extension 12 SP1");
    }

    public void testGetInstalledProducts() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProductEnvironment(admin, null, true);

        Server server = ServerFactoryTest.createTestServer(admin, true);
        assertNotNull(server);
        assertNotNull(server.getId());

        List<SUSEInstalledProduct> results = handler.getInstalledProducts(admin,
                server.getId().intValue());
        assertEquals(0, results.size());

        PackageArch arch = PackageFactory.lookupPackageArchByLabel("x86_64");

        Set<InstalledProduct> products = new HashSet<>();
        products.add(new InstalledProduct("SLES", "12", arch, null, true));
        products.add(new InstalledProduct("sle-ha", "12", arch, null, false));
        products.add(new InstalledProduct("sle-ha-geo", "12", arch, null, false));
        //This one should be ignored (only SUSE products):
        products.add(new InstalledProduct("unknown-product", "1", arch, null, false));

        server.setInstalledProducts(products);
        TestUtils.saveAndReload(server);

        results = handler.getInstalledProducts(admin, server.getId().intValue());

        assertEquals("invalid number of results", 3, results.size());

    }

    public void testGetKernelLivePatch() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(admin);
        assertNotNull(server);
        assertNotNull(server.getId());

        String testVersion = "kgraft_patch_2_2_1";

        String result = handler.getKernelLivePatch(admin, server.getId().intValue());
        assertEquals("", result);

        server.setKernelLiveVersion(testVersion);
        TestUtils.saveAndReload(server);

        result = handler.getKernelLivePatch(admin, server.getId().intValue());
        assertEquals(testVersion, result);

        Server nonMinionServer = ServerFactoryTest.createTestServer(admin, true);
        assertNotNull(nonMinionServer);
        assertNotNull(nonMinionServer.getId());

        result = handler.getKernelLivePatch(admin, nonMinionServer.getId().intValue());
        assertEquals("", result);
    }

    public void testScheduleApplyHighstate() throws Exception {
        Server testServer = MinionServerFactoryTest.createTestMinionServer(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = getMockedHandler().scheduleApplyHighstate(
                admin, testServer.getId().intValue(), scheduleDate, false);
        assertNotNull(actionId);

        DataResult schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, ((ScheduledAction) schedule.get(0)).getId());

        // Look up the action and verify the details
        ApplyStatesAction action = (ApplyStatesAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, action.getActionType());
        assertEquals(scheduleDate, action.getEarliestAction());

        ApplyStatesActionDetails details = action.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertFalse(details.isTest());
    }

    public void testScheduleApplyHighstateTest() throws Exception {
        Server testServer = MinionServerFactoryTest.createTestMinionServer(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = getMockedHandler().scheduleApplyHighstate(
                admin, testServer.getId().intValue(), scheduleDate, true);
        assertNotNull(actionId);

        DataResult schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, ((ScheduledAction) schedule.get(0)).getId());

        // Look up the action and verify the details
        ApplyStatesAction action = (ApplyStatesAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, action.getActionType());
        assertEquals(scheduleDate, action.getEarliestAction());

        ApplyStatesActionDetails details = action.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertTrue(details.isTest());
    }

    public void testHighstateNoMinion() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin);
        try {
            Long actionId = getMockedHandler().scheduleApplyHighstate(
                    admin, server.getId().intValue(), new Date(), false);
            fail("Should throw UnsupportedOperationException");
        }
        catch (UnsupportedOperationException e) {
            assertEquals("System not managed with Salt: " + server.getId(), e.getMessage());
        }
    }

    /**
     * Tests creating a system profile with missing HW address.
     * @throws Exception if anything goes wrong
     */
    public void testCreateSystemProfileNoHwAddress() throws Exception {
        try {
            getMockedHandler().createSystemProfile(admin, "test system", Collections.emptyMap());
            fail("An exception should have been thrown.");
        } catch (InvalidParameterException e) {
            // no-op
        }
    }

    /**
     * Tests creating a system profile.
     * @throws Exception if anything goes wrong
     */
    public void testCreateSystemProfile() throws Exception {
        String hwAddress = "aa:bb:cc:dd:ee:00";
        int result = getMockedHandler().createSystemProfile(admin, "test system",
                Collections.singletonMap("hwAddress", hwAddress));
        List<NetworkInterface> nics = NetworkInterfaceFactory
                .lookupNetworkInterfacesByHwAddress(hwAddress)
                .collect(Collectors.toList());

        assertEquals(1, nics.size());
        Server server = nics.get(0).getServer();
        assertEquals("test system", server.getName());
        assertEquals(result, server.getId().intValue());
    }

    private SystemHandler getMockedHandler() throws Exception {
        TaskomaticApi taskomaticMock = MOCK_CONTEXT.mock(TaskomaticApi.class);
        SystemHandler systemHandler = new SystemHandler(taskomaticMock);

        MOCK_CONTEXT.checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
            allowing(taskomaticMock)
                    .scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        }});

        return systemHandler;
    }
}
