/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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

package com.redhat.rhn.domain.user.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.Address;
import com.redhat.rhn.domain.user.RhnTimeZone;
import com.redhat.rhn.domain.user.State;
import com.redhat.rhn.domain.user.StateChange;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.domain.user.UserServerPreference;
import com.redhat.rhn.domain.user.UserServerPreferenceId;
import com.redhat.rhn.domain.user.legacy.UserImpl;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

/**
 * JUnit test case for the User class.
 */
public class UserFactoryTest extends RhnBaseTestCase {
    private UserFactory factory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        factory = UserFactory.getInstance();
    }

    @Test
    public void testStateChanges() throws InterruptedException {

        User orgAdmin = UserTestUtils.createUser("UFTOrgAdmin", "UFTTestOrg");
        User normalUser = UserTestUtils.createUser("UFTNormalUser", orgAdmin.getOrg().getId());

        //disable the normal user
        factory.disable(normalUser, orgAdmin);

        /*
         * We have to sleep here for a second since enabling/disabling a user within
         * the same second causes db problems.
         */
        Thread.sleep(1000);

        assertEquals(1, normalUser.getStateChanges().size());
        assertTrue(normalUser.isDisabled());

        //make sure our state change was set correctly
        StateChange change = new ArrayList<>(normalUser.getStateChanges()).get(0);
        assertEquals(change.getUser(), normalUser);
        assertEquals(change.getChangedBy(), orgAdmin);
        assertEquals(change.getState(), UserFactory.DISABLED);

        //enable the normal user
        factory.enable(normalUser, orgAdmin);

        assertEquals(2, normalUser.getStateChanges().size());
        assertFalse(normalUser.isDisabled());

        Long id = normalUser.getId();

        //Evict the user and look back up. This make sure our changes got saved
        //to the db.
        flushAndEvict(normalUser);

        User usr = UserFactory.lookupById(id);
        assertFalse(usr.isDisabled());
        assertEquals(2, usr.getStateChanges().size());
    }

    @Test
    public void testStates() {
        State e = UserFactory.ENABLED;
        State d = UserFactory.DISABLED;

        assertNotNull(e);
        assertNotNull(d);
        assertEquals(e.getLabel(), "enabled");
        assertEquals(d.getLabel(), "disabled");
    }

    @Test
    public void testCreateAddress() {
        Address addr = UserFactory.createAddress();
        assertNotNull(addr);
    }

    @Test
    public void testLookupById() {
        Long id = UserTestUtils.createUser().getId();
        User usr = UserFactory.lookupById(id);
        assertNotNull(usr);
        assertNotNull(usr.getFirstNames());
    }

    @Test
    public void testLookupByIds() {
        List<Long> idList = new ArrayList<>();
        Long firstId = UserTestUtils.createUser("testUserOne", "testOrgOne").getId();
        Long secondId = UserTestUtils.createUser("testUserSecond", "testOrgSecond").getId();
        idList.add(firstId);
        idList.add(secondId);
        List<User> userList = UserFactory.lookupByIds(idList);
        assertNotNull(userList);
        assertNotNull(userList.get(1).getFirstNames());
        assertContains(userList.get(1).getLogin(), "testUserSecond");
    }

    @Test
    public void testLookupByLogin() {
        Long id = UserTestUtils.createUser().getId();
        User usr = UserFactory.lookupById(id);
        String createdLogin = usr.getLogin();
        assertNotNull(usr);
        User usrByLogin = UserFactory.lookupByLogin(usr.getLogin());
        assertNotNull(usrByLogin);
        assertNotNull(usrByLogin.getLogin());
        assertEquals(usrByLogin.getLogin(), createdLogin);
        assertNotNull(usrByLogin.getOrg());
    }

    @Test
    public void testLookupNotExists() {
        User usr = UserFactory.lookupById(-99999L);
        assertNull(usr);
    }

    @Test
    public void testEmailA() {
        Long id = UserTestUtils.createUser().getId();
        User usr = UserFactory.lookupById(id);
        UserFactory.save(usr);
    }

    @Test
    public void testGetTimeZoneOlson() {
        RhnTimeZone tz = UserFactory.getTimeZone("America/Los_Angeles");
        assertNotNull(tz);
        assertEquals("America/Los_Angeles", tz.getOlsonName());

        RhnTimeZone tz2 = UserFactory.getTimeZone("foo");
        assertNull(tz2);
    }

    @Test
    public void testGetTimeZoneId() {
        RhnTimeZone tz = UserFactory.getTimeZone(UserFactory
                .getTimeZone("America/Los_Angeles").getTimeZoneId());
        assertEquals(UserFactory.getTimeZone("America/Los_Angeles"), tz);
        assertEquals("America/Los_Angeles", tz.getOlsonName());

        RhnTimeZone tz2 = UserFactory.getTimeZone(-23);
        assertNull(tz2);
    }

    @Test
    public void testGetTimeZoneDefault() {
        RhnTimeZone tz = UserFactory.getDefaultTimeZone();
        assertNotNull(tz);
        assertEquals(tz.getTimeZone().getRawOffset(), TimeZone.getDefault().getRawOffset());
    }

    @Test
    public void testTimeZoneLookupAll() {
        List<RhnTimeZone> tzList = UserFactory.lookupAllTimeZones();
        // Total seems to fluctuate, check for 30+:
        assertTrue(tzList.size() > 30);
        assertNotNull(tzList.get(2));
        // Order-test:
        // 1) Start at GMT
        // 2) Then E-to-W from GMT (ie, all negative offsets followed by pos offsets)
        // Note: There are several GMT-equivalent TZs at the beginning of all this -
        //       skip past them
        assertEquals("GMT", tzList.get(0).getOlsonName());
        assertTrue(tzList.get(4).getTimeZone().getRawOffset() < 0);
        assertTrue(tzList.get(tzList.size() - 1).getTimeZone().getRawOffset() > 0);
    }

    @Test
    public void testCommitUser() {

        Long id = UserTestUtils.createUser().getId();
        User usr = UserFactory.lookupById(id);
        usr.setFirstNames("UserFactoryTest.testCommitUser.change " +
                    TestUtils.randomString());
        UserFactory.save(usr);
        flushAndEvict(usr);

        // Now lets manually test to see if the user got updated
        HibernateFactory.getSession().doWork(connection -> {
            ResultSet rs = null;
            PreparedStatement ps = null;
            String rawValue = null;
            try {
                ps = connection.prepareStatement("SELECT first_names " +
                    "FROM web_user_personal_info " +
                    "WHERE web_user_id = " + id
                );
                rs = ps.executeQuery();
                rs.next();
                rawValue = rs.getString("first_names");
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                rs.close();
                ps.close();
            }

            User actualUser = UserFactory.lookupById(id);
            assertEquals(actualUser.getFirstNames(), rawValue);
        });
    }


    @Test
    public void testLookupMultiple() {
        int len = 3;
        String[] logins = new String[len];
        for (int i = 0; i < len; i++) {
            Long id = UserTestUtils.createUser().getId();
            User usr = UserFactory.lookupById(id);
            logins[i] = usr.getLogin();
        }

        for (int i = 0; i < len; i++) {
            User usr = UserFactory.lookupByLogin(logins[i]);
            assertEquals(usr.getLogin(), logins[i]);
        }
    }

    @Test
    public void testCreateNewUser() {
        /* This specifically DOESN'T use UserTestUtils.createUser(), because
         * I am testing how commitNewUser works.
         */

        String orgName = "userFactoryTestOrg ";
        String userName = "userFactoryTestUser " + TestUtils.randomString();

        Org org = UserTestUtils.createOrg(orgName);

        User usr = UserFactory.createUser();
        usr.setLogin(userName);
        usr.setPassword("password");
        usr.setFirstNames("userName");
        usr.setLastName("userName");
        String prefix = (String) LocalizationService.getInstance().
                            availablePrefixes().toArray()[0];
        usr.setPrefix(prefix);

        usr.setEmail("javaTest@example.com");

        Address addr1 = UserFactory.createAddress();
        addr1.setAddress1("444 Castro");
        addr1.setAddress2("#1");
        addr1.setCity("Mountain View");
        addr1.setZip("94043");
        addr1.setCountry("US");
        addr1.setPhone("650-555-1212");
        addr1.setFax("650-555-1212");

        usr = UserFactory.saveNewUser(usr, addr1, org.getId());

        assertTrue(usr.getId() > 0);

        assertNotNull(usr.getOrg());

        assertNotNull(usr.getEnterpriseUser().getAddress());
        Address dbAddr = usr.getEnterpriseUser().getAddress();
        assertTrue(dbAddr.getId().intValue() > 0);
        assertEquals("444 Castro", dbAddr.getAddress1());
    }

    @Test
    public void testUserServerPreferenceLookup() {
        User user = UserTestUtils.createUser();

        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        UserServerPreferenceId id = new UserServerPreferenceId(user,
                                                               s,
                                                               UserServerPreferenceId
                                                               .RECEIVE_NOTIFICATIONS);

        UserServerPreference usp = new UserServerPreference();
        usp.setId(id);
        usp.setValue("0");
        TestUtils.saveAndFlush(usp);

        usp = factory.lookupServerPreferenceByUserServerAndName(user, s,
                                      UserServerPreferenceId.RECEIVE_NOTIFICATIONS);

        assertNotNull(usp);
        assertEquals(usp.getValue(), "0");

    }

    @Test
    public void testSetUserServerPreferenceTrue() {
        User user = UserTestUtils.createUser();

        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());

        UserFactory.getInstance().setUserServerPreferenceValue(user,
                                                 s,
                                                 UserServerPreferenceId
                                                 .RECEIVE_NOTIFICATIONS,
                                                 false);

        assertFalse(UserManager.lookupUserServerPreferenceValue(user,
                                                                s,
                                                                UserServerPreferenceId
                                                                .RECEIVE_NOTIFICATIONS));

        factory.setUserServerPreferenceValue(user,
                                                 s,
                                                 UserServerPreferenceId
                                                 .RECEIVE_NOTIFICATIONS,
                                                 true);

        assertTrue(UserManager.lookupUserServerPreferenceValue(user,
                                                               s,
                                                               UserServerPreferenceId
                                                               .RECEIVE_NOTIFICATIONS));

    }

    @Test
    public void testSatelliteHasUsers() {
        new UserTestUtils.UserBuilder().orgName("testUserOrg").orgAdmin(true).build();
        assertTrue(UserFactory.satelliteHasUsers());
    }

    @Test
    public void testFindAllOrgAdmins() {
        User user = new UserTestUtils.UserBuilder().orgName("findAdminsOrg").orgAdmin(true).build();

        Org o = user.getOrg();

        List<UserImpl> orgAdmins = UserFactory.getInstance().findAllOrgAdmins(o);
        assertEquals(1, orgAdmins.size());
        assertTrue(orgAdmins.contains(user));
    }
}
