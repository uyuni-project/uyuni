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
package com.redhat.rhn.frontend.xmlrpc.org.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.MultiOrgUserOverview;
import com.redhat.rhn.frontend.dto.OrgDto;
import com.redhat.rhn.frontend.xmlrpc.MigrationToSameOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchOrgException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.OrgNotInTrustException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.frontend.xmlrpc.org.OrgHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.org.MigrationManager;
import com.redhat.rhn.manager.org.OrgManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

public class OrgHandlerTest extends BaseHandlerTestCase {

    private OrgHandler handler = new OrgHandler(new MigrationManager(new ServerGroupManager(new TestSaltApi())));

    private static final String LOGIN = "fakeadmin";
    private static final String PASSWORD = "fakeadmin";
    private static final String FIRST = "Bill";
    private static final String LAST = "FakeAdmin";
    private static final String EMAIL = "fakeadmin@example.com";
    private static final String PREFIX = "Mr.";
    private String[] orgName = {"Test Org 1", "Test Org 2"};
    private ChannelFamily channelFamily = null;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        admin.addPermanentRole(RoleFactory.SAT_ADMIN);
        for (int i = 0; i < orgName.length; i++) {
            orgName[i] = "Test Org " + TestUtils.randomString();
        }
        TestUtils.saveAndFlush(admin);

        channelFamily = ChannelFamilyFactoryTest.createTestChannelFamily(admin, true);
    }

    @Test
    public void testCreateFirst() {
        /*
         * "dockerrun_pg" already creates a new Org during Init right now.
         * Therefore check first if Org with ID '1' exist without performing further tests then.
         * If there is no Org yet, perform actual test of createFirst method.

        */
        Org initialOrg = OrgFactory.lookupById((long) 1);
        if (initialOrg != null) {
            assertNotNull(initialOrg);
        }
        else {
            handler.createFirst(orgName[1], "fakeadmin", "password", "First",
                    "Admin", "firstadmin@example.com");
            Org testOrg = OrgFactory.lookupByName(orgName[1]);
            assertNotNull(testOrg);
        }
    }

    @Test
    public void testCreateFirstTwice() {
        try {
            handler.createFirst(orgName[1], "fakeadmin", "password", "First",
                    "Admin", "firstadmin@example.com");
            handler.createFirst(orgName[1], "fakeadmin", "password", "First",
                    "Admin", "firstadmin@example.com");
            fail();
        }
        catch (ValidationException e) {
            // expected, initial org/user can only be created once
        }
    }

    @Test
    public void testCreate() {
        handler.create(admin, orgName[0], "fakeadmin", "password", "Mr.", "Bill",
                "FakeAdmin", "fakeadmin@example.com", Boolean.FALSE);
        Org testOrg = OrgFactory.lookupByName(orgName[0]);
        assertNotNull(testOrg);
    }

    @Test
    public void testCreateShortOrgName() {
        String shortName = "aa"; // Must be at least 3 characters in UI
        try {
            handler.create(admin, shortName, "fakeadmin", "password", "Mr.", "Bill",
                    "FakeAdmin", "fakeadmin@example.com", Boolean.FALSE);
            fail();
        }
        catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void testCreateDuplicateOrgName() {
        String dupOrgName = "Test Org " + TestUtils.randomString();
        handler.create(admin, dupOrgName, "fakeadmin1", "password", "Mr.", "Bill",
                "FakeAdmin", "fakeadmin1@example.com", Boolean.FALSE);
        try {
            handler.create(admin, dupOrgName, "fakeadmin2", "password", "Mr.", "Bill",
                    "FakeAdmin", "fakeadmin2@example.com", Boolean.FALSE);
            fail();
        }
        catch (ValidationException e) {
            // expected
        }
    }

    @Test
    public void testListOrgs() {
        Org testOrg = createOrg();
        OrgDto dto = OrgManager.toDetailsDto(testOrg);
        List<OrgDto> orgs = handler.listOrgs(admin);
        assertTrue(orgs.contains(dto));
    }

    @Test
    public void testDeleteNoSuchOrg() {
        try {
            handler.delete(admin, -1);
            fail();
        }
        catch (NoSuchOrgException e) {
            // expected
        }
    }

    @Test
    public void testContentStagingSettings() {
        Org testOrg = createOrg();
        int testId = testOrg.getId().intValue();
        assertFalse(handler.isContentStagingEnabled(admin, testId));
        handler.setContentStaging(admin, testId, true);
        assertTrue(handler.isContentStagingEnabled(admin, testId));
        handler.setContentStaging(admin, testId, false);
        assertFalse(handler.isContentStagingEnabled(admin, testId));
    }

    @Test
    public void testDelete() {
        Org testOrg = createOrg();
        handler.delete(admin, testOrg.getId().intValue());
        testOrg = OrgFactory.lookupByName(orgName[0]);
        assertNull(testOrg);
    }

    @Test
    public void testListActiveUsers() {
        Org testOrg = createOrg();
        List<MultiOrgUserOverview> users = handler.listUsers(admin,
                                                testOrg.getId().intValue());
        assertEquals(1, users.size());
        User user = UserFactory.lookupByLogin(
                testOrg.getActiveOrgAdmins().get(0).getLogin());
        assertEquals(users.get(0).getId(), user.getId());
    }

    @Test
    public void testGetDetails() {
        Org testOrg = createOrg();
        OrgDto actual = handler.getDetails(admin, testOrg.getId().intValue());
        OrgDto expected = OrgManager.toDetailsDto(testOrg);
        assertNotNull(actual);
        compareDtos(expected, actual);

        actual = handler.getDetails(admin, testOrg.getName());
        assertNotNull(actual);
        compareDtos(expected, actual);
    }

    @Test
    public void testUpdateName() {
        Org testOrg = createOrg();
        String newName = "Foo" + TestUtils.randomString();
        OrgDto dto = handler.updateName(admin, testOrg.getId().intValue(), newName);
        assertEquals(newName, dto.getName());
        assertNotNull(OrgFactory.lookupByName(newName));
    }

    private void compareDtos(OrgDto expected, OrgDto actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getActivationKeys(), actual.getActivationKeys());
        assertEquals(expected.getSystems(), actual.getSystems());
        assertEquals(expected.getKickstartProfiles(), actual.getKickstartProfiles());
        assertEquals(expected.getUsers(), actual.getUsers());
        assertEquals(expected.getServerGroups(), actual.getServerGroups());
        assertEquals(expected.getConfigChannels(), actual.getConfigChannels());
    }

    private Org createOrg() {
        return createOrg(0);
    }

    private Org createOrg(int index) {
        return createOrg(orgName[index], LOGIN + TestUtils.randomString(),
                PASSWORD, PREFIX, FIRST, LAST, EMAIL, false);
    }

    private Org createOrg(String name, String login,
                        String password, String prefix, String first,
                        String last, String email, boolean usePam) {
        handler.create(admin, name, login, password, prefix, first,
                                                    last, email, usePam);
        Org org =  OrgFactory.lookupByName(name);
        assertNotNull(org);
        return org;
    }

    @Test
    public void testMigrateSystem() throws Exception {
        User newOrgAdmin = UserTestUtils.findNewUser("newAdmin", "newOrg", true);
        newOrgAdmin.getOrg().getTrustedOrgs().add(admin.getOrg());
        OrgFactory.save(newOrgAdmin.getOrg());

        Server server = ServerTestUtils.createTestSystem(admin);
        assertNotNull(server.getOrg());
        List<Integer> servers = new LinkedList<>();
        servers.add(server.getId().intValue());
        // Actual migration is tested internally, just make sure the API call doesn't
        // error out:
        handler.migrateSystems(admin, newOrgAdmin.getOrg().getId().intValue(), servers);
    }

    @Test
    public void testMigrateInvalid() throws Exception {

        User orgAdmin1 = UserTestUtils.findNewUser("orgAdmin1", "org1", true);
        orgAdmin1.getOrg().getTrustedOrgs().add(admin.getOrg());

        User orgAdmin2 = UserTestUtils.findNewUser("orgAdmin2", "org2", true);

        Server server = ServerTestUtils.createTestSystem(admin);
        List<Integer> servers = new LinkedList<>();
        servers.add(server.getId().intValue());

        // attempt migration where user is not a satellite admin and orginating
        // org is not the same as the user's.
        try {
            handler.migrateSystems(orgAdmin2, orgAdmin1.getOrg().getId().intValue(),
                    servers);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            // expected
        }

        // attempt to migrate systems to an org that does not exist
        try {
            handler.migrateSystems(admin, -1, servers);
            fail();
        }
        catch (NoSuchOrgException e) {
            // expected
        }

        // attempt to migrate systems from/to the same org
        try {
            handler.migrateSystems(admin, admin.getOrg().getId().intValue(), servers);
            fail();
        }
        catch (MigrationToSameOrgException e) {
            // expected
        }

        // attempt to migrate systems to an org that isn't defined in trust
        try {
            handler.migrateSystems(admin, orgAdmin2.getOrg().getId().intValue(),
                    servers);
            fail();
        }
        catch (OrgNotInTrustException e) {
            // expected
        }

        // attempt to migrate systems that do not exist
        List<Integer> invalidServers = new LinkedList<>();
        invalidServers.add(-1);
        try {
            handler.migrateSystems(admin, orgAdmin1.getOrg().getId().intValue(),
                    invalidServers);
            fail();
        }
        catch (NoSuchSystemException e) {
            // expected
        }
    }
}
