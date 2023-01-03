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
package com.redhat.rhn.frontend.xmlrpc.channel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ChannelHandlerTest
 */
public class ChannelHandlerTest extends BaseHandlerTestCase {

    private final ChannelHandler handler = new ChannelHandler();

    @Test
    public void testListSoftwareChannels() throws Exception {

        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        List<Map<String, Object>> result = handler.listSoftwareChannels(admin);
        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (Map<String, Object> item : result) {
            Set<String> keys = item.keySet();
            for (Object key : keys) {
                // make sure we don't send out null
                assertNotNull(item.get(key));
            }
        }
    }

    @Test
    public void testListAllChannels() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listAllChannels(admin);

        // verify
        assertNotNull(result);
        assertTrue(result.length > 0);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);
    }

    @Test
    public void testListManageableChannels() throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        Object[] result = handler.listManageableChannels(regular);

        assertNotNull(result);
        assertEquals(0, result.length);

        regular.addPermanentRole(RoleFactory.CHANNEL_ADMIN);

        result = handler.listManageableChannels(regular);
        assertNotNull(result);
        assertTrue(result.length > 0);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);

        regular.removePermanentRole(RoleFactory.CHANNEL_ADMIN);
    }

    @Test
    public void testListPopularChannels() throws Exception {
        // setup
        Server server = ServerFactoryTest.createTestServer(admin, true);

        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        channel.setParentChannel(null);  // base channel
        SystemManager.subscribeServerToChannel(admin, server, channel);

        // execute
        Object[] result = handler.listPopularChannels(admin, 1);

        // verify
        assertNotNull(result);
        assertTrue(result.length > 0);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);

        // execute
        result = handler.listPopularChannels(admin, 50000);

        // verify
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    public void testListMyChannels() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listMyChannels(admin);

        // verify
        assertNotNull(result);
        assertTrue(result.length > 0);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);
    }

    @Test
    public void testListSharedChannels() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        admin.getOrg().addOwnedChannel(channel);

        Org org2 = createOrg();
        Org org3 = createOrg();
        org2.addTrust(admin.getOrg());
        org3.addTrust(admin.getOrg());
        channel.getTrustedOrgs().add(org2);
        channel.getTrustedOrgs().add(org3);
        channel.setAccess(Channel.PUBLIC);

        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listSharedChannels(admin);

        // verify
        assertNotNull(result);
        assertTrue(result.length > 0);
        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);
    }

    @Test
    public void testListRetiredChannels() throws Exception {
        // setup
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        Date date = new Date();
        date.setTime(0); // Initialize date to Jan 1, 1970 00:00:00
        channel.setEndOfLife(date);
        admin.getOrg().addOwnedChannel(channel);
        OrgFactory.save(admin.getOrg());
        ChannelFactory.save(channel);
        flushAndEvict(channel);

        // execute
        Object[] result = handler.listRetiredChannels(admin);

        // verify
        assertNotNull(result);
        assertTrue(result.length > 0);

        boolean foundChannel = false;
        for (Object oIn : result) {
            ChannelTreeNode item = (ChannelTreeNode) oIn;
            if (item.getName() != null) {
                if (item.getName().equals(channel.getName())) {
                    foundChannel = true;
                    break;
                }
            }
        }
        assertTrue(foundChannel);
    }

    private Org createOrg() {
        TestUtils.randomString();
        Org org = OrgFactory.createOrg();
        org.setName("org created by OrgFactory test: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        assertTrue(org.getId() > 0);
        return org;
    }
}
