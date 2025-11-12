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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.taskomatic.task.KickstartFileSyncTask;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.Profile;
import org.junit.jupiter.api.Test;

import java.io.File;

public class KickstartFileSyncTaskTest extends BaseTestCaseWithUser {



    @Test
    public void testTask() throws Exception {

        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        KickstartData ks = KickstartDataTest.createTestKickstartData(user.getOrg());
        ks.setKickstartDefaults(KickstartDataTest.createDefaults(ks, user));
        KickstartDataTest.createCobblerObjects(ks);
        KickstartFactory.saveKickstartData(ks);


        ks = TestUtils.saveAndReload(ks);

        Profile p = Profile.lookupById(CobblerXMLRPCHelper.getConnection(user),
                ks.getCobblerId());

        File f = new File(p.getKickstart());
        assertTrue(f.exists());
        assertTrue(f.delete());
        assertFalse(f.exists());
        KickstartFileSyncTask task = new KickstartFileSyncTask();
        task.execute(null);
        f = new File(p.getKickstart());
        assertTrue(f.exists());

    }

}
