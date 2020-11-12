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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.rhnpackage.profile.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.profile.Profile;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileFactory;
import com.redhat.rhn.domain.rhnpackage.profile.ProfileType;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * ProfileFactoryTest
 */
public class ProfileFactoryTest  extends RhnBaseTestCase {

    @Test
    public void testCreateProfile() {
        Profile p = ProfileFactory.createProfile(ProfileFactory.TYPE_NORMAL);
        assertNotNull(p);
        assertEquals(ProfileFactory.TYPE_NORMAL, p.getProfileType());
    }

    @Test
    public void testLookupByLabel() {
        ProfileType pt = ProfileFactory.lookupByLabel("normal");
        assertNotNull(pt, "ProfileType is null");
        assertEquals(ProfileFactory.TYPE_NORMAL, pt, "Not equal to normal");

        pt = ProfileFactory.lookupByLabel("foo");
        assertNull(pt, "Found a ProfileType labeled foo");
    }

    @Test
    public void testCompatibleWithServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        ProfileTest.createTestProfile(user, channel);
        Session session = HibernateFactory.getSession();

        // gotta make sure the Channel gets saved.
        session.flush();

        List<Profile> list = ProfileFactory.compatibleWithServer(server, user.getOrg());
        assertNotNull(list, "List is null");
        assertFalse(list.isEmpty(), "List is empty");
        for (Object o : list) {
            assertEquals(Profile.class, o.getClass(), "List contains something other than Profiles");
        }
    }

    @Test
    public void testLookupById() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Profile p = ProfileTest.createTestProfile(user, channel);
        assertNotNull(p);
        assertEquals(ProfileFactory.TYPE_NORMAL, p.getProfileType());
        TestUtils.saveAndFlush(p);
        Profile p1 = ProfileFactory.lookupByIdAndOrg(p.getId(), user.getOrg());
        assertEquals(p, p1);
    }

    @Test
    public void testFindByNameAndOrgId() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Profile p = ProfileTest.createTestProfile(user, channel);
        String name = p.getName();
        Long orgid = p.getOrg().getId();
        assertNotNull(p);
        assertEquals(ProfileFactory.TYPE_NORMAL, p.getProfileType());
        TestUtils.saveAndFlush(p);
        Profile p1 = ProfileFactory.findByNameAndOrgId(name, orgid);
        assertEquals(p, p1);
    }
}
