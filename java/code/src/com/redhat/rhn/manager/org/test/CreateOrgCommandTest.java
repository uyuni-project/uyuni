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
package com.redhat.rhn.manager.org.test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.manager.org.CreateOrgCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

/**
 * @author mmccune
 *
 */
public class CreateOrgCommandTest extends BaseTestCaseWithUser {

    @Test
    public void testCreateOrg() {
        CreateOrgCommand cmd = new CreateOrgCommand(
                "newOrg" + TestUtils.randomString(),
                "login" + TestUtils.randomString(),
                "password",
                "test@redhat.com",
                false);
        assertNull(cmd.store());
        assertNotNull(cmd.getNewOrg());
        assertNotNull(cmd.getNewOrg().getId());
    }

    @Test
    public void testFailCreate() {
        CreateOrgCommand cmd = new CreateOrgCommand(
                "newOrg" + TestUtils.randomString(),
                user.getLogin(),
                "password",
                "test@redhat.com",
                false);
        assertNotNull(cmd.store());
        assertNull(cmd.getNewOrg());
    }
}
