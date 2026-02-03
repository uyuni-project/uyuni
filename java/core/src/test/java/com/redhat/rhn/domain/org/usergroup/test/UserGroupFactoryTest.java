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

package com.redhat.rhn.domain.org.usergroup.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.usergroup.UserGroup;
import com.redhat.rhn.domain.org.usergroup.UserGroupFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;

/** JUnit test case for the UserGroup
 *  class.
 */

public class UserGroupFactoryTest extends RhnBaseTestCase {

    /**
    * Test to see if the Org can translate a Role to the
    * appropriate UserGroupId.  This is the only public
    * usage of anything related to a UserGroup
     */
    @Test
    public void testGetUserGroup() {
        Org org1 = UserTestUtils.createOrg(this);
        UserGroup ugid = org1.getUserGroup(RoleFactory.ORG_ADMIN);
        assertNotNull(ugid);
    }

    @Test
    public void generatedCoverageTestLookupExtGroupById() {
        // this test has been generated programmatically to test UserGroupFactory.lookupExtGroupById
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        UserGroupFactory.lookupExtGroupById(0L);
    }

    @Test
    public void generatedCoverageTestLookupOrgExtGroupByIdAndOrg() {
        // this test has been generated programmatically to test UserGroupFactory.lookupOrgExtGroupByIdAndOrg
        // containing a hibernate query that is not covered by any test so far
        // feel free to modify and/or complete it
        Org arg1 = new Org();
        UserGroupFactory.lookupOrgExtGroupByIdAndOrg(0L, arg1);
    }
}
