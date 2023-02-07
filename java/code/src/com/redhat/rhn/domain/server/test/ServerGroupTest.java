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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ServerGroupTest
 */
public class ServerGroupTest extends RhnBaseTestCase {

    @Test
    public void testEquals() {
        User user = UserTestUtils.findNewUser("testUser", "testorg");
        ServerGroup sg1 = ServerGroupTestUtils.createManaged(user);
        ServerGroup sg2 = new ServerGroup();

        assertNotEquals(sg1, sg2);
        assertNotEquals("foo", sg1);

        Session session = HibernateFactory.getSession();
        sg2 = (ServerGroup) session.getNamedQuery("ServerGroup.lookupByIdAndOrg")
                                            .setParameter("id", sg1.getId())
                                            .setParameter("org", user.getOrg())
                                            .uniqueResult();

        assertEquals(sg1, sg2);
    }

    /**
     * @param user the user
     */
    public static void checkSysGroupAdminRole(User user) {
        if (!user.hasRole(RoleFactory.SYSTEM_GROUP_ADMIN)) {
            user.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        }
    }

    public static ServerGroup createTestServerGroup(Org org, ServerGroupType typeIn) {
        if (typeIn != null) {
            EntitlementServerGroup existingGroup =
                        ServerGroupFactory.lookupEntitled(org, typeIn);
            if (existingGroup != null) {
                return existingGroup;
            }
            EntitlementServerGroup group = ServerGroupFactory.lookupEntitled(
                                        typeIn.getAssociatedEntitlement(), org);
            assertNotNull(group);
            assertNotNull(group.getGroupType().getAssociatedEntitlement());
            return group;

        }
        ManagedServerGroup sg = ServerGroupFactory.create("NewGroup" +
                                                        TestUtils.randomString(),
                                                            "RHN Managed Group",
                                                            org);
        assertNotNull(sg.getId());
        return sg;
    }

    @Test
    public void testGetServerGroupTypeFeatures() {
        Org org1 = UserTestUtils.findNewOrg("testOrg" + this.getClass().getSimpleName());
        assertFalse(org1.getEntitledServerGroups().isEmpty());

        // we assume existence of salt entitlement
        EntitlementServerGroup serverGroup = org1.getEntitledServerGroups().stream()
                .filter(sg -> sg.getGroupType().equals(ServerConstants.getServerGroupTypeSaltEntitled()))
                .findFirst()
                .orElseThrow();
        assertNotNull(serverGroup.getGroupType().getFeatures());
        assertFalse(serverGroup.getGroupType().getFeatures().isEmpty());
    }

    @Test
    public void testServerGroupPillar() {
        Org org1 = UserTestUtils.findNewOrg("testOrg" + this.getClass().getSimpleName());
        ServerGroup group = createTestServerGroup(org1, ServerConstants.getServerGroupTypeSaltEntitled());
        Set<Pillar> pillars = new HashSet<>();
        Map<String, Object> pillar1 = new HashMap<>();
        pillar1.put("data1", "foo");
        pillar1.put("data2", 123);
        pillars.add(new Pillar("category1", pillar1, group));
        Map<String, Object> pillar2 = new HashMap<>();
        pillar1.put("bar1", "baz");
        pillar1.put("bar2", 456);
        pillars.add(new Pillar("category2", pillar2, group));

        group.setPillars(pillars);
        TestUtils.saveAndFlush(group);
        group = reload(group);

        Pillar actual = group.getPillars().stream()
                .filter(item -> "category1".equals(item.getCategory()))
                .findFirst()
                .get();
        assertNotNull(actual);
        assertEquals(123, actual.getPillar().get("data2"));
        assertFalse(actual.isMinionPillar());
        assertFalse(actual.isGlobalPillar());
        assertTrue(actual.isGroupPillar());
        assertFalse(actual.isOrgPillar());
    }
}
