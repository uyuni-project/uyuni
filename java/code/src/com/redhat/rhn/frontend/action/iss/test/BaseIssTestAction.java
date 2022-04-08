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
package com.redhat.rhn.frontend.action.iss.test;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssMaster;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class BaseIssTestAction extends RhnMockStrutsTestCase {

    protected IssMaster masterDto;
    protected IssSlave slaveDto;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        String masterName = "testMaster" + TestUtils.randomString();
        masterDto = new IssMaster();
        masterDto.setLabel(masterName);
        IssFactory.save(masterDto);
        masterDto = (IssMaster) IssFactory.reload(masterDto);

        String slaveName = "testSlave" + TestUtils.randomString();
        slaveDto = new IssSlave();
        slaveDto.setSlave(slaveName);
        slaveDto.setEnabled("Y");
        slaveDto.setAllowAllOrgs("Y");
        IssFactory.save(slaveDto);
        slaveDto = (IssSlave) IssFactory.reload(slaveDto);
    }

    @Test
    public void testPermission() throws Exception {
        permissionCheck();
    }

    @Test
    public void testList() throws Exception {
        if (getListName() == null) {
            return;
        }

        doPerform(true);
        verifyList(getListName(), getListClass());
        }

    protected void permissionCheck() {
        doPerform(false);
        assertPermissionException();

        doPerform(true);
        assertFalse(getActualForward().indexOf("errors/Permission.do") > 0);
    }

    protected void doPerform(boolean asSatAdmin) {
        setRequestPathInfo(getUrl());
        if (asSatAdmin) {
            user.addPermanentRole(RoleFactory.SAT_ADMIN);
        }
        actionPerform();
    }

    protected abstract String getUrl();

    protected abstract String getListName();
    protected abstract Class getListClass();
}
