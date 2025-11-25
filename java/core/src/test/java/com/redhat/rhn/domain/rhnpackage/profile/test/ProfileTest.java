/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.rhnpackage.profile.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * ProfileTest
 */
public class ProfileTest extends RhnBaseTestCase {

    private static Logger log = LogManager.getLogger(ProfileTest.class);

    /**
     * Test the Equals method of Profile
     */
    @Test
    public void testProfileEquals() {
        User user = UserTestUtils.createUser(this);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Profile p1 = createTestProfile(user, channel);
        Profile p2 = new Profile();
        assertNotEquals(p1, p2);

        /*
         * Get coverage on the "!(other instanceof Profile)" block
         * of Profile.equals()
         */
        assertNotEquals(p1, channel);

        p2 = ProfileFactory.lookupByIdAndOrg(p1.getId(), user.getOrg());
        assertEquals(p1, p2);
    }

    /**
     * Helper method to create a Profile for testing purposes
     * @param user the user
     * @param channel the channel
     * @return Returns a fresh Profile
     */
    public static Profile createTestProfile(User user, Channel channel) {

        Profile p = new Profile();
        p.setInfo("Test information for a test Profile.");
        p.setName("RHN-JAVA" + TestUtils.randomString());
        p.setDescription("This is only a test.");
        p.setBaseChannel(channel);
        p.setOrg(user.getOrg());
        p.setProfileType(ProfileFactory.TYPE_NORMAL);

        assertNull(p.getId());
        TestUtils.saveAndFlush(p);
        assertNotNull(p.getId());

        return p;
    }

    @Test
    public void testCompatibleServer() {
        // create a profile
        // create a channel
        // create a server
        // user and org
        User user = UserTestUtils.createUser(this);
        Server server = ServerFactoryTest.createTestServer(user);
        log.debug("CreateTest channel");
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        log.debug("Created test channel");
        createTestProfile(user, channel);
        Session session = HibernateFactory.getSession();

        // gotta make sure the Channel gets saved.
        session.flush();

        List<Profile> list = ProfileFactory.compatibleWithServer(server, user.getOrg());
        assertNotNull(list, "List is null");
        assertFalse(list.isEmpty(), "List is empty");
        for (Object o : list) {
            assertEquals(Profile.class, o.getClass(), "Contains non Profile objects");
        }
    }
}
