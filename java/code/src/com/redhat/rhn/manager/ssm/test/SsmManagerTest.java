/**
 * Copyright (c) 2018 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.manager.ssm.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.PublicChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.ssm.ChannelChangeDto;
import com.redhat.rhn.manager.ssm.ScheduleChannelChangesResultDto;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.manager.ssm.SsmAllowedChildChannelsDto;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.utils.gson.SsmBaseChannelChangesDto;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProduct;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.createTestSUSEProductChannel;
import static com.redhat.rhn.domain.product.test.SUSEProductTestUtils.installSUSEProductOnServer;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelFamily;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannelProduct;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorBaseChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestVendorChildChannel;

/**
 * Test for {@link SsmManager}.
 */
public class SsmManagerTest extends JMockBaseTestCaseWithUser {

    private SUSEProduct product;
    private Channel baseChannel;
    private Channel childChannel1;
    private Channel childChannel2;

    private SUSEProduct product2;
    private Channel baseChannel2;
    private Channel childChannel2_1;
    private Channel childChannel2_2;

    private TaskomaticApi taskomaticMock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Create a SUSE product and channel products
        ChannelFamily channelFamily = createTestChannelFamily();

        {
            product = createTestSUSEProduct(channelFamily);
            ChannelProduct channelProduct = createTestChannelProduct();
            // Create channels
            baseChannel = createTestVendorBaseChannel(channelFamily, channelProduct);

            UserManager.addChannelPerm(user, baseChannel.getId(), "subscribe");
            UserManager.addChannelPerm(user, baseChannel.getId(), "manage");
            childChannel1 = createTestVendorChildChannel(baseChannel, channelProduct);
            UserManager.addChannelPerm(user, childChannel1.getId(), "subscribe");
            UserManager.addChannelPerm(user, childChannel1.getId(), "manage");
            childChannel2 = createTestVendorChildChannel(baseChannel, channelProduct);
            UserManager.addChannelPerm(user, childChannel2.getId(), "subscribe");
            UserManager.addChannelPerm(user, childChannel2.getId(), "manage");

            baseChannel.setOrg(user.getOrg());
            childChannel1.setOrg(user.getOrg());
            childChannel2.setOrg(user.getOrg());

            // Assign channels to SUSE product
            createTestSUSEProductChannel(baseChannel, product, true);
            createTestSUSEProductChannel(childChannel1, product, true);
            createTestSUSEProductChannel(childChannel2, product, true);
        }
        {
            product2 = createTestSUSEProduct(channelFamily);
            ChannelProduct channelProduct = createTestChannelProduct();
            // Create channels
            baseChannel2 = createTestVendorBaseChannel(channelFamily, channelProduct);

            UserManager.addChannelPerm(user, baseChannel2.getId(), "subscribe");
            UserManager.addChannelPerm(user, baseChannel2.getId(), "manage");
            childChannel2_1 = createTestVendorChildChannel(baseChannel2, channelProduct);
            UserManager.addChannelPerm(user, childChannel2_1.getId(), "subscribe");
            UserManager.addChannelPerm(user, childChannel2_1.getId(), "manage");
            childChannel2_2 = createTestVendorChildChannel(baseChannel2, channelProduct);
            UserManager.addChannelPerm(user, childChannel2_2.getId(), "subscribe");
            UserManager.addChannelPerm(user, childChannel2_2.getId(), "manage");

            baseChannel2.setOrg(user.getOrg());
            childChannel2_1.setOrg(user.getOrg());
            childChannel2_2.setOrg(user.getOrg());

            // Assign channels to SUSE product
            createTestSUSEProductChannel(baseChannel2, product2, true);
            createTestSUSEProductChannel(childChannel2_1, product2, true);
            createTestSUSEProductChannel(childChannel2_2, product2, true);
        }
        setImposteriser(ClassImposteriser.INSTANCE);
        taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
    }

    /**
     * Test compute change to default system channel for one server with base channel set.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesChangeBaseDefault1() throws Exception {
        Server server1 = ServerFactoryTest.createTestServer(user, true);

        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        server1.addChannel(parent);
        server1.addChannel(child);

        installSUSEProductOnServer(product, server1);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), -1));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(1, result.size());
        assertEquals(parent.getId().longValue(), result.get(0).getOldBaseChannel().get().getId());
        assertEquals(baseChannel.getId().longValue(), result.get(0).getNewBaseChannel().get().getId());
        assertEquals(2, result.get(0).getChildChannels().size());
        assertTrue(result.get(0).getChildChannels().stream().anyMatch(cc -> cc.getId() == childChannel1.getId()));
        assertTrue(result.get(0).getChildChannels().stream().anyMatch(cc -> cc.getId() == childChannel2.getId()));
    }

    /**
     * Test compute change to default system channel for two servers with base channel set.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesChangeBaseDefault2() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), -1));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(1, result.size());
        assertEquals(parent.getId().longValue(), result.get(0).getOldBaseChannel().get().getId());
        assertEquals(baseChannel.getId().longValue(), result.get(0).getNewBaseChannel().get().getId());
        assertEquals(2, result.get(0).getChildChannels().size());
        assertTrue(result.get(0).getChildChannels().stream().anyMatch(cc -> cc.getId() == childChannel1.getId()));
        assertTrue(result.get(0).getChildChannels().stream().anyMatch(cc -> cc.getId() == childChannel2.getId()));
    }

    /**
     * Test no base channel change for two servers.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesNoBaseChange() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        // base -> no change
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), 0));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(1, result.size());
        assertEquals((long)parent.getId(), result.get(0).getOldBaseChannel().get().getId());
        assertEquals((long)parent.getId(), result.get(0).getNewBaseChannel().get().getId());
        assertFalse(result.get(0).isNewBaseDefault());
        assertEquals(1, result.get(0).getChildChannels().size());
        assertTrue(result.get(0).getChildChannels().stream().filter(cc -> cc.getId() == child.getId()).findAny().isPresent());
    }

    /**
     * Test change to default for 1 server with base channel and 2 servers with no base channel.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesServersWithoutBase() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        Server server3 = ServerFactoryTest.createTestServer(user, true);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        installSUSEProductOnServer(product, server3);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        set.addElement(server3.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        // parent -> default
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), -1));
        // no base -> default
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(-1, -1));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(2, result.size());
        SsmAllowedChildChannelsDto allowedWithBase =
            result.stream().filter(a -> a.getOldBaseChannel() != null).findFirst().get();
        assertEquals(baseChannel.getId(), (Long)allowedWithBase.getNewBaseChannel().get().getId());
        assertTrue(allowedWithBase.isNewBaseDefault());
        assertEquals(1, allowedWithBase.getServers().size());
        assertEquals(server1.getId(), (Long)allowedWithBase.getServers().get(0).getId());

        SsmAllowedChildChannelsDto allowedNoBase =
                result.stream().filter(a -> !a.getOldBaseChannel().isPresent()).findFirst().get();
        assertEquals(baseChannel.getId(), (Long)allowedNoBase.getNewBaseChannel().get().getId());
        assertTrue(allowedNoBase.isNewBaseDefault());
        assertEquals(1, allowedWithBase.getServers().size());
        assertTrue(allowedNoBase.getServers().stream().anyMatch(s -> s.getId() == server2.getId()));
        assertTrue(allowedNoBase.getServers().stream().anyMatch(s -> s.getId() == server3.getId()));

        assertEquals(2, allowedNoBase.getChildChannels().size());
        assertTrue(allowedNoBase.getChildChannels().stream()
                .filter(cc -> cc.getId() == childChannel1.getId()).findAny().isPresent());
        assertTrue(allowedNoBase.getChildChannels().stream()
                .filter(cc -> cc.getId() == childChannel2.getId()).findAny().isPresent());
    }

    /**
     * Test change to default for 2 servers with no base channel.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesTwoServersWithoutBase() throws Exception {
        Server server1 = ServerFactoryTest.createTestServer(user, true);
        Server server2 = ServerFactoryTest.createTestServer(user, true);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        // no base -> default
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(-1, -1));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(1, result.size());

        SsmAllowedChildChannelsDto allowedNoBase =
                result.stream().filter(a -> !a.getOldBaseChannel().isPresent()).findFirst().get();
        assertEquals(baseChannel.getId(), (Long)allowedNoBase.getNewBaseChannel().get().getId());
        assertTrue(allowedNoBase.isNewBaseDefault());
        assertTrue(allowedNoBase.getServers().stream().anyMatch(s -> s.getId() == server1.getId()));
        assertTrue(allowedNoBase.getServers().stream().anyMatch(s -> s.getId() == server2.getId()));

        assertEquals(2, allowedNoBase.getChildChannels().size());
        assertTrue(allowedNoBase.getChildChannels().stream()
                .filter(cc -> cc.getId() == childChannel1.getId()).findAny().isPresent());
        assertTrue(allowedNoBase.getChildChannels().stream()
                .filter(cc -> cc.getId() == childChannel2.getId()).findAny().isPresent());
    }

    /**
     * Test when an empty list of changes is supplied.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesNoChangePresent() throws Exception {
        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Test change to default base for two servers when for one server the base channel
     * cannot be guessed.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesBaseChangeNoGuess() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), -1));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(2, result.size());
        assertEquals(2, result.stream().filter(base -> base.getOldBaseChannel().get().getId() == parent.getId()).count());
        assertTrue(result.stream().filter(base -> !base.getNewBaseChannel().isPresent()).findFirst().isPresent());
        assertTrue(result.stream().filter(base -> !base.getNewBaseChannel().isPresent() && !base.isNewBaseDefault()).findFirst().isPresent());
        assertTrue(result.stream().filter(base -> base.getNewBaseChannel().isPresent()).findFirst().isPresent());
        assertTrue(result.stream().filter(base -> base.getNewBaseChannel().isPresent() && base.isNewBaseDefault()).findFirst().isPresent());

        assertEquals(1, result.stream().filter(base -> !base.getNewBaseChannel().isPresent()).findFirst()
                .map(acc -> acc.getIncompatibleServers())
                .get().size()
        );
        assertEquals(server2.getId(),
                result.stream().filter(base -> !base.getNewBaseChannel().isPresent()).findFirst()
                        .flatMap(acc -> acc.getIncompatibleServers()
                                .stream().findFirst().map(s -> s.getId())
                        ).get()
        );
    }

    /**
     * Test change base channel explicitly for two servers.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesChangeBaseExplicit1() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), baseChannel.getId()));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(1, result.size());
        assertEquals((long)parent.getId(), result.get(0).getOldBaseChannel().get().getId());
        assertEquals((long)baseChannel.getId(), result.get(0).getNewBaseChannel().get().getId());
        assertFalse(result.get(0).isNewBaseDefault());
        assertEquals(2, result.get(0).getChildChannels().size());
        assertTrue(result.get(0).getChildChannels().stream().filter(cc -> cc.getId() == childChannel1.getId()).findAny().isPresent());
        assertTrue(result.get(0).getChildChannels().stream().filter(cc -> cc.getId() == childChannel2.getId()).findAny().isPresent());
    }

    /**
     * Test change base channel explicitly for one server without base and change to default for a server
     * with a base channel.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesChangeExplicitAndDefault() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Channel explicit = ChannelFactoryTest.createTestChannel(user);

        Server server1 = ServerFactoryTest.createTestServer(user, true);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        // base -> default
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), -1));
        // no base -> explicit
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(-1, explicit.getId()));

        List<SsmAllowedChildChannelsDto> result = SsmManager.computeAllowedChannelChanges(changes, user);

        assertEquals(2, result.size());

        SsmAllowedChildChannelsDto allowedWithBase =
                result.stream().filter(a -> a.getOldBaseChannel() != null).findFirst().get();
        assertEquals(baseChannel.getId(), (Long)allowedWithBase.getNewBaseChannel().get().getId());
        assertTrue(allowedWithBase.isNewBaseDefault());
        assertEquals(1, allowedWithBase.getServers().size());
        assertEquals(server2.getId(), (Long)allowedWithBase.getServers().get(0).getId());

        SsmAllowedChildChannelsDto allowedNoBase =
                result.stream().filter(a -> !a.getOldBaseChannel().isPresent()).findFirst().get();
        assertEquals(explicit.getId(), (Long)allowedNoBase.getNewBaseChannel().get().getId());
        assertFalse(allowedNoBase.isNewBaseDefault());
        assertEquals(1, allowedNoBase.getServers().size());
        assertEquals(server1.getId(), (Long)allowedNoBase.getServers().get(0).getId());
    }

    /**
     * Test compute explicit change of base channel when the new base is not compatible with the old one.
     * @throws Exception
     */
    public void testComputeAllowedChannelChangesIllegalChangeBaseExplicit() throws Exception {
        Channel parent = ChannelFactoryTest.createTestChannel(user);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);

        Channel parent2 = ChannelFactoryTest.createTestChannel(user);
        parent2.setOrg(null);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        SsmBaseChannelChangesDto changes = new SsmBaseChannelChangesDto();
        changes.getChanges().add(new SsmBaseChannelChangesDto.Change(parent.getId(), parent2.getId()));
        try {
            SsmManager.computeAllowedChannelChanges(changes, user);
            fail("Should throw IllegalArgumentException");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }

    /**
     * Test schedule change base and subscribe to one channel for two servers.
     * @throws Exception
     */
    public void testScheduleChannelChangesChangeDefaultBaseAndOneChildForTwoServers() throws Exception {
        context().checking(new Expectations() { {
            exactly(1).of(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();
        ChannelChangeDto change1 = new ChannelChangeDto();
        change1.setOldBaseId(Optional.of(server1.getBaseChannel().getId()));
        change1.setNewBaseDefault(true);
        change1.setNewBaseId(Optional.of(baseChannel.getId()));
        change1.getChildChannelActions().put(childChannel1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change1.getChildChannelActions().put(childChannel2.getId(), ChannelChangeDto.ChannelAction.NO_CHANGE);
        changes.add(change1);
        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        assertEquals(2, results.size());
        assertEquals(2,
                results.stream().filter(r -> !r.getErrorMessage().isPresent() && r.getActionId().isPresent()).count());
        List<SubscribeChannelsActionDetails> details = results.stream()
                .map(r -> (SubscribeChannelsAction)ActionManager.lookupAction(user, r.getActionId().get()))
                .map(a -> a.getDetails())
                .collect(Collectors.toList());
        assertEquals(2, details.stream().filter(d -> d.getBaseChannel().getId() == baseChannel.getId()).count());
        assertTrue(details.stream()
                .allMatch(d -> d.getChannels().size() == 1));
        assertTrue(details.stream()
                .allMatch(d -> d.getChannels().stream().findFirst().get().getId() == childChannel1.getId()));
    }

    /**
     * Test schedule change base and subscribe to one channel for two servers.
     * @throws Exception
     */
    public void testScheduleChannelChangesChangeDifferentDefaultBasesForTwoServers() throws Exception {
        context().checking(new Expectations() { {
            exactly(2).of(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);
        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent);
        server1.addChannel(child);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent);
        server2.addChannel(child);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product2, server2);
        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change1 = new ChannelChangeDto();
        change1.setOldBaseId(Optional.of(server1.getBaseChannel().getId()));
        change1.setNewBaseDefault(true);
        change1.setNewBaseId(Optional.of(baseChannel.getId()));
        change1.getChildChannelActions().put(childChannel1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change1.getChildChannelActions().put(childChannel2.getId(), ChannelChangeDto.ChannelAction.NO_CHANGE);
        changes.add(change1);

        ChannelChangeDto change2 = new ChannelChangeDto();
        change2.setOldBaseId(Optional.of(server2.getBaseChannel().getId()));
        change2.setNewBaseDefault(true);
        change2.setNewBaseId(Optional.of(baseChannel2.getId()));
        change2.getChildChannelActions().put(childChannel2_1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change2.getChildChannelActions().put(childChannel2_2.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change2);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(2, results.size());
        assertEquals(2,
                results.stream().filter(r -> !r.getErrorMessage().isPresent() && r.getActionId().isPresent()).count());
        List<SubscribeChannelsActionDetails> details = results.stream()
                .map(r -> (SubscribeChannelsAction)ActionManager.lookupAction(user, r.getActionId().get()))
                .map(a -> a.getDetails())
                .collect(Collectors.toList());

        Optional<SubscribeChannelsActionDetails> details1 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(baseChannel.getId()))
                .findFirst();
        assertTrue(details1.isPresent());
        assertTrue(details1
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server1.getId()))
                .isPresent());
        assertEquals((Integer)1, details1.map(d -> d.getChannels().size()).get());
        assertTrue(details1
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(childChannel1.getId())).findFirst())
                .isPresent());

        Optional<SubscribeChannelsActionDetails> details2 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(baseChannel2.getId()))
                .findFirst();
        assertTrue(details2.isPresent());
        assertTrue(details2
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server2.getId()))
                .isPresent());
        assertEquals((Integer)2, details2.map(d -> d.getChannels().size()).get());
        assertTrue(details2
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(childChannel2_1.getId())).findFirst())
                .isPresent());
        assertTrue(details2
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(childChannel2_2.getId())).findFirst())
                .isPresent());
    }

    /**
     * Test schedule change the same base to some explicit channel and to a default channel for two servers.
     * Should fail with invalid_change.
     * @throws Exception
     */
    public void testScheduleChannelChangesChangeExplicitAndDefaultSameBaseForTwoServers() throws Exception {
        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        Channel parent1 = ChannelFactoryTest.createTestChannel(user);
        parent1.setParentChannel(null);
        Channel child1_1 = ChannelFactoryTest.createTestChannel(user);
        child1_1.setParentChannel(parent1);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent1);
        server1.addChannel(child1_1);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent1);
        server2.addChannel(child1_1);

        Channel parent2 = ChannelFactoryTest.createTestChannel(user);
        parent2.setParentChannel(null);
        Channel child2_1 = ChannelFactoryTest.createTestChannel(user);
        child2_1.setParentChannel(parent2);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product2, server2);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change1 = new ChannelChangeDto();
        change1.setOldBaseId(Optional.of(server1.getBaseChannel().getId()));
        change1.setNewBaseDefault(true);
        change1.setNewBaseId(Optional.of(baseChannel.getId()));
        change1.getChildChannelActions().put(childChannel1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change1.getChildChannelActions().put(childChannel2.getId(), ChannelChangeDto.ChannelAction.NO_CHANGE);
        changes.add(change1);

        ChannelChangeDto change2 = new ChannelChangeDto();
        change2.setOldBaseId(Optional.of(server2.getBaseChannel().getId()));
        change2.setNewBaseDefault(false);
        change2.setNewBaseId(Optional.of(parent2.getId()));
        change2.getChildChannelActions().put(child2_1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change2);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getErrorMessage().isPresent() &&
                r.getErrorMessage().get().equals("invalid_change")));
    }


    /**
     * Test schedule change two different bases, one to default and one to an explicit channel and subscribe
     * to child channels for two servers.
     * @throws Exception
     */
    public void testScheduleChannelChangesChangeExplicitAndDefaultDifferentBasesForTwoServers() throws Exception {
        context().checking(new Expectations() { {
            exactly(2).of(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        Channel parent1 = ChannelFactoryTest.createTestChannel(user);
        parent1.setParentChannel(null);
        Channel child1_1 = ChannelFactoryTest.createTestChannel(user);
        child1_1.setParentChannel(parent1);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent1);
        server1.addChannel(child1_1);

        Channel parent2 = ChannelFactoryTest.createTestChannel(user);
        parent2.setParentChannel(null);
        Channel child2_1 = ChannelFactoryTest.createTestChannel(user);
        child2_1.setParentChannel(parent2);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent2);
        server2.addChannel(child2_1);

        Channel customBase = ChannelFactoryTest.createTestChannel(user);
        customBase.setParentChannel(null);
        Channel customBaseChild1 = ChannelFactoryTest.createTestChannel(user);
        customBaseChild1.setParentChannel(customBase);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product2, server2);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change1 = new ChannelChangeDto();
        change1.setOldBaseId(Optional.of(server1.getBaseChannel().getId()));
        change1.setNewBaseDefault(true);
        change1.setNewBaseId(Optional.of(baseChannel.getId()));
        change1.getChildChannelActions().put(childChannel1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change1.getChildChannelActions().put(childChannel2.getId(), ChannelChangeDto.ChannelAction.NO_CHANGE);
        changes.add(change1);

        ChannelChangeDto change2 = new ChannelChangeDto();
        change2.setOldBaseId(Optional.of(server2.getBaseChannel().getId()));
        change2.setNewBaseDefault(false);
        change2.setNewBaseId(Optional.of(customBase.getId()));
        change2.getChildChannelActions().put(customBaseChild1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change2);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> !r.getErrorMessage().isPresent() &&
                r.getActionId().isPresent()));

        List<SubscribeChannelsActionDetails> details = results.stream()
                .map(r -> (SubscribeChannelsAction)ActionManager.lookupAction(user, r.getActionId().get()))
                .map(a -> a.getDetails())
                .collect(Collectors.toList());

        Optional<SubscribeChannelsActionDetails> details1 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(baseChannel.getId()))
                .findFirst();
        assertTrue(details1.isPresent());
        assertTrue(details1
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server1.getId()))
                .isPresent());
        assertEquals((Integer)1, details1.map(d -> d.getChannels().size()).get());
        assertTrue(details1
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(childChannel1.getId())).findFirst())
                .isPresent());

        Optional<SubscribeChannelsActionDetails> details2 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(customBase.getId()))
                .findFirst();
        assertTrue(details2.isPresent());
        assertTrue(details2
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server2.getId()))
                .isPresent());
        assertEquals((Integer)1, details2.map(d -> d.getChannels().size()).get());
        assertTrue(details2
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(customBaseChild1.getId())).findFirst())
                .isPresent());
    }

    /**
     * Test schedule change base for two servers, one without base to default and one with base to an explicit channel
     * and subscribe child channels.
     * @throws Exception
     */
    public void testScheduleChannelChangesChangeExplicitAndDefaultNoBasesForTwoServers() throws Exception {
        context().checking(new Expectations() { {
            exactly(2).of(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        // no base channel for serve 1

        Channel parent2 = ChannelFactoryTest.createTestChannel(user);
        parent2.setParentChannel(null);
        Channel child2_1 = ChannelFactoryTest.createTestChannel(user);
        child2_1.setParentChannel(parent2);

        Server server2 = ServerFactoryTest.createTestServer(user, true);
        server2.addChannel(parent2);
        server2.addChannel(child2_1);

        Channel customBase = ChannelFactoryTest.createTestChannel(user);
        customBase.setParentChannel(null);
        Channel customBaseChild1 = ChannelFactoryTest.createTestChannel(user);
        customBaseChild1.setParentChannel(customBase);

        installSUSEProductOnServer(product, server1);
        installSUSEProductOnServer(product2, server2);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        set.addElement(server2.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change1 = new ChannelChangeDto();
        change1.setOldBaseId(Optional.empty());
        change1.setNewBaseDefault(true);
        change1.setNewBaseId(Optional.of(baseChannel.getId()));
        change1.getChildChannelActions().put(childChannel1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        change1.getChildChannelActions().put(childChannel2.getId(), ChannelChangeDto.ChannelAction.NO_CHANGE);
        changes.add(change1);

        ChannelChangeDto change2 = new ChannelChangeDto();
        change2.setOldBaseId(Optional.of(server2.getBaseChannel().getId()));
        change2.setNewBaseDefault(false);
        change2.setNewBaseId(Optional.of(customBase.getId()));
        change2.getChildChannelActions().put(customBaseChild1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change2);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(2, results.size());
        // no errs
        assertTrue(results.stream().allMatch(r -> !r.getErrorMessage().isPresent() &&
                r.getActionId().isPresent()));

        List<SubscribeChannelsActionDetails> details = results.stream()
                .map(r -> (SubscribeChannelsAction)ActionManager.lookupAction(user, r.getActionId().get()))
                .map(a -> a.getDetails())
                .collect(Collectors.toList());

        Optional<SubscribeChannelsActionDetails> details1 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(baseChannel.getId()))
                .findFirst();
        assertTrue(details1.isPresent());
        assertTrue(details1
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server1.getId()))
                .isPresent());
        assertEquals((Integer)1, details1.map(d -> d.getChannels().size()).get());
        assertTrue(details1
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(childChannel1.getId())).findFirst())
                .isPresent());

        Optional<SubscribeChannelsActionDetails> details2 = details.stream()
                .filter(d -> d.getBaseChannel().getId().equals(customBase.getId()))
                .findFirst();
        assertTrue(details2.isPresent());
        assertTrue(details2
                .map(d -> d.getParentAction().getServerActions().stream().findFirst().get())
                .filter(sa -> sa.getServer().getId().equals(server2.getId()))
                .isPresent());
        assertEquals((Integer)1, details2.map(d -> d.getChannels().size()).get());
        assertTrue(details2
                .flatMap(d -> d.getChannels().stream().filter(c -> c.getId().equals(customBaseChild1.getId())).findFirst())
                .isPresent());
    }

    /**
     * Schedule an incompatible base channel change on a server that has a base channel.
     * @throws Exception
     */
    public void testScheduleChannelChangesWithIncompatibleBase() throws Exception {
        Org org2 = UserTestUtils.createNewOrgFull("anotherOrg");

        Channel parent1 = ChannelFactoryTest.createTestChannel(user);
        parent1.setParentChannel(null);
        Channel child1_1 = ChannelFactoryTest.createTestChannel(user);
        child1_1.setParentChannel(parent1);

        Server server1 = ServerFactoryTest.createTestServer(user, true);
        server1.addChannel(parent1);
        server1.addChannel(child1_1);

        Channel parent2 = ChannelFactoryTest.createTestChannel(org2); //ChannelFactoryTest.createTestChannel(user);
        parent2.setParentChannel(null);
        Channel child2_1 = ChannelFactoryTest.createTestChannel(user);
        child2_1.setParentChannel(parent2);

        installSUSEProductOnServer(product, server1);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change = new ChannelChangeDto();
        change.setOldBaseId(Optional.of(server1.getBaseChannel().getId()));
        change.setNewBaseDefault(false);
        change.setNewBaseId(Optional.of(parent2.getId()));
        change.getChildChannelActions().put(child2_1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(1, results.size());
        assertEquals("incompatible_base", results.stream().findFirst().get().getErrorMessage().get());
        assertEquals((long)server1.getId(), results.stream().findFirst().get().getServer().getId());
    }

    /**
     * Schedule an incompatible base channel change on a server that has a base channel.
     * @throws Exception
     */
    public void testScheduleChannelChangesWithIncompatibleBaseOnNoBaseServer() throws Exception {
        Org org2 = UserTestUtils.createNewOrgFull("anotherOrg");

        Server server1 = ServerFactoryTest.createTestServer(user, true);

        Channel parent2 = ChannelFactoryTest.createTestChannel(org2);
        parent2.setParentChannel(null);
        Channel child2_1 = ChannelFactoryTest.createTestChannel(user);
        child2_1.setParentChannel(parent2);

        installSUSEProductOnServer(product, server1);

        HibernateFactory.getSession().flush();

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server1.getId() + "");
        RhnSetManager.store(set);

        List<ChannelChangeDto> changes = new ArrayList<>();

        ChannelChangeDto change = new ChannelChangeDto();
        change.setOldBaseId(Optional.empty());
        change.setNewBaseDefault(false);
        change.setNewBaseId(Optional.of(parent2.getId()));
        change.getChildChannelActions().put(child2_1.getId(), ChannelChangeDto.ChannelAction.SUBSCRIBE);
        changes.add(change);

        Date earliest = new Date();

        List<ScheduleChannelChangesResultDto> results = SsmManager.scheduleChannelChanges(changes, earliest, null, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(1, results.size());
        assertEquals("incompatible_base", results.stream().findFirst().get().getErrorMessage().get());
        assertEquals((long)server1.getId(), results.stream().findFirst().get().getServer().getId());
    }
}
