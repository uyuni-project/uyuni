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
package com.redhat.rhn.manager.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.manager.kickstart.KickstartEditCommand;

import org.junit.jupiter.api.Test;

/**
 * KickstartEditCommandTest - test for KickstartDetailsCommand
 */
public class KickstartEditCommandTest extends BaseKickstartCommandTestCase {

    @Test
    public void testKickstartEditCommand() throws Exception {

        KickstartEditCommand command = new KickstartEditCommand(ksdata.getId(), user);
        command.setComments("My Comment");
        command.setActive(Boolean.TRUE);
        command.setLabel("scoobykickstart");
        command.store();

        KickstartData k2 = command.getKickstartData();
        assertNotNull(k2.getComments());
        assertNotNull(k2.getLabel());

        assertEquals(Boolean.TRUE, k2.getActive());
        assertEquals(command.getComments(), k2.getComments());
        assertEquals(command.getLabel(), k2.getLabel());
    }

    @Test
    public void testKickstartLabel() throws Exception {
        KickstartEditCommand command = new KickstartEditCommand(ksdata.getId(), user);
        command.setLabel("shaggy-ks-rhel4");
        command.store();
        assertEquals(ksdata.getLabel(), command.getLabel());
    }

    @Test
    public void testOrgDefault() throws Exception {
        assertFalse(ksdata.isOrgDefault());
        KickstartData k1 = KickstartDataTest.createKickstartWithChannel(user.getOrg());
        Long oldDefaultId = k1.getId();
        k1.setOrgDefault(Boolean.TRUE);
        assertTrue(k1.isOrgDefault());
        KickstartFactory.saveKickstartData(k1);
        flushAndEvict(k1);

        KickstartEditCommand command = new KickstartEditCommand(ksdata.getId(), user);
        command.setIsOrgDefault(Boolean.TRUE);
        assertTrue(ksdata.isOrgDefault());
        k1 = KickstartFactory.lookupKickstartDataByIdAndOrg(user.getOrg(), oldDefaultId);
        assertFalse(k1.isOrgDefault());
    }

}
