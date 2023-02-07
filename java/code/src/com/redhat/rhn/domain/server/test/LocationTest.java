/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Location;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.Date;

/**
 * LocationTest
 */
public class LocationTest extends RhnBaseTestCase {

    public static final String MACHINE = "Skyrunner";
    public static final String RACK = "Test Rack";
    public static final String ROOM = "Lobby";
    public static final String BUILDING = "Centennial";
    public static final String ADDRESS1 = "1801 Varsity Drive";
    public static final String ADDRESS2 = "Lobby";
    public static final String CITY = "Raleigh";
    public static final String STATE = "NC";
    public static final String COUNTRY = "USA";

    @Test
    public void testLocation() throws Exception {
        Location loc1 = createTestLocation();
        Location loc2 = new Location();

        assertNotEquals(loc1, loc2);
        assertNotEquals(loc1, new Date());

        Session session = HibernateFactory.getSession();
        loc2 = (Location) session.getNamedQuery("Location.findById")
                                      .setLong("id", loc1.getId())
                                      .uniqueResult();
        assertEquals(loc1, loc2);
    }

    public static Location createTestLocation() throws Exception {
        Location loc = new Location();
        loc.setMachine(MACHINE);
        loc.setRack(RACK);
        loc.setRoom(ROOM);
        loc.setBuilding("Centennial");
        loc.setAddress1(ADDRESS1);
        loc.setAddress2(ADDRESS2);
        loc.setCity(CITY);
        loc.setCity(STATE);
        loc.setCity(COUNTRY);

        User user = UserTestUtils.createUser("testuser",
                UserTestUtils.createOrg("testorg"));
        Server s = ServerFactoryTest.createTestServer(user);
        loc.setServer(s);

        assertNull(loc.getId());
        TestUtils.saveAndFlush(loc);
        assertNotNull(loc.getId());

        return loc;
    }

}
