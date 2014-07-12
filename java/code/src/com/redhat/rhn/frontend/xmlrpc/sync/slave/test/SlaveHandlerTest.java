/**
 * Copyright (c) 2013--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.sync.slave.test;

import java.util.ArrayList;
import java.util.List;

import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.sync.slave.SlaveHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.TestUtils;

public class SlaveHandlerTest extends BaseHandlerTestCase {

    private SlaveHandler handler = new SlaveHandler();
    private String slaveName;

    public void setUp() throws Exception {
        super.setUp();
        admin.addPermanentRole(RoleFactory.SAT_ADMIN);
        slaveName = "testSlave" + TestUtils.randomString();
    }

    public void testCreate() {
        // Make sure that non-sat-admin users cannot access
        try {
            IssSlave slave = handler.create(regular, slaveName, true, true);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            IssSlave slave = handler.create(admin, slaveName, true, true);
            assertEquals(slaveName, slave.getSlave());
            assertTrue("Y".equals(slave.getEnabled()));
            assertTrue("Y".equals(slave.getAllowAllOrgs()));
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }
   }

    public void testUpdate() {
        IssSlave slave = handler.create(admin, slaveName, true, true);

        // Make sure that non-sat-admin users cannot access
        try {
            IssSlave updSlave = handler.update(regular,
                    slave.getId().intValue(),
                    "new_" + slaveName,
                    false,
                    false);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            IssSlave updSlave = handler.update(admin, slave.getId().intValue(),
                    "new_" + slaveName, false, false);
            assertEquals("new_" + slaveName, updSlave.getSlave());
            assertEquals(slave.getId(), updSlave.getId());
            assertTrue("N".equals(slave.getEnabled()));
            assertTrue("N".equals(slave.getAllowAllOrgs()));
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }
    }

    public void testDelete() {
        IssSlave slave = handler.create(admin, slaveName, true, true);
        Long slaveId = slave.getId();

        // Make sure that non-sat-admin users cannot access
        try {
            int rmvd = handler.delete(regular, slave.getId().intValue());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            int rmvd = handler.delete(admin, slave.getId().intValue());
            assertEquals(1, rmvd);
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }

        // Make sure it's actually gone
        IssSlave mstr = IssFactory.lookupSlaveById(slaveId);
        assertNull(mstr);
    }

    public void testGetSlave() {
        IssSlave slave = handler.create(admin, slaveName, true, true);
        Integer slaveId = slave.getId().intValue();

        // Make sure that non-sat-admin users cannot access
        try {
            IssSlave gotSlave = handler.getSlave(regular, slaveId);
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            IssSlave gotSlave = handler.getSlave(admin, slaveId);
            assertEquals(slaveId.intValue(), gotSlave.getId().intValue());
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }
    }

    public void testGetAllowedOrgs() {
        IssSlave slave = handler.create(admin, slaveName, true, false);

        // Make sure that non-sat-admin users cannot access
        try {
            List<Integer> orgs = handler.getAllowedOrgs(regular,
                    slave.getId().intValue());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            List<Integer> orgs = handler.getAllowedOrgs(admin,
                    slave.getId().intValue());
            assertNotNull(orgs);
            assertEquals(0, orgs.size());
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }
    }

    public void testSetAllowedOrgs() {
        IssSlave slave = handler.create(admin, slaveName, true, false);

        List<Integer> orgs = getBareOrgs();

        // Make sure that non-sat-admin users cannot access
        try {
            IssSlave m = handler.getSlave(admin, slave.getId().intValue());
            assertNotNull(m);
            orgs = handler.getAllowedOrgs(regular, slave.getId().intValue());
            fail();
        }
        catch (PermissionCheckFailureException e) {
            //success
        }

        // Make sure satellite-admin can
        try {
            IssSlave m = handler.getSlave(admin, slave.getId().intValue());
            assertNotNull(m);
            assertEquals(0, m.getAllowedOrgs().size());
            int rc = handler.setAllowedOrgs(admin, m.getId().intValue(), orgs);
            assertEquals(1, rc);
            IssSlave m2 = handler.getSlave(admin, slave.getId().intValue());
            assertEquals(orgs.size(), m2.getAllowedOrgs().size());
        }
        catch (PermissionCheckFailureException e) {
            fail();
        }

        orgs.clear();
        orgs.add(1);

        // Make sure setting to one-new-one, really sets to one
        IssSlave m3 = handler.getSlave(admin, slave.getId().intValue());
        int rc = handler.setAllowedOrgs(admin,
                m3.getId().intValue(),
                orgs);
        assertEquals(1, rc);
        orgs = handler.getAllowedOrgs(admin,
                slave.getId().intValue());
        assertNotNull(orgs);
        assertEquals(1, orgs.size());

        // Make sure resetting to "empty" works
        IssSlave m4 = handler.getSlave(admin, slave.getId().intValue());
        rc = handler.setAllowedOrgs(admin,
                m4.getId().intValue(),
                new ArrayList<Integer>());
        assertEquals(1, rc);
        orgs = handler.getAllowedOrgs(admin, slave.getId().intValue());
        assertNotNull(orgs);
        assertEquals(0, orgs.size());

    }

    // Add half of any existing Orgs to this list
    private List<Integer> getBareOrgs() {
        List<Integer> orgs = new ArrayList();
        int i = 0;
        for (Org o : OrgFactory.lookupAllOrgs()) {
            if (i++ % 2 == 0) {
                orgs.add(o.getId().intValue());
            }
        }
        return orgs;
    }

}
