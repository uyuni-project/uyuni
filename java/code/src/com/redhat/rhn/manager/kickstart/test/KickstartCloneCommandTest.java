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

import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartCommandName;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.manager.kickstart.KickstartCloneCommand;
import com.redhat.rhn.testing.TestUtils;

import java.util.Date;
import java.util.LinkedHashSet;

/**
 * KickstartCloneCommandTest
 */
public class KickstartCloneCommandTest extends BaseKickstartCommandTestCase {

    public void testClone() throws Exception {
        KickstartCloneCommand cmd = new KickstartCloneCommand(ksdata.getId(), user,
                "someNewLabel [" + TestUtils.randomString() + "]");

        LinkedHashSet<KickstartCommand> customSet = new LinkedHashSet<>();
        KickstartCommand custom = new KickstartCommand();
        KickstartCommandName cn = KickstartFactory
                .lookupKickstartCommandName("custom");
        custom.setCommandName(cn);
        custom.setArguments("this is a test");
        custom.setKickstartData(cmd.getKickstartData());
        custom.setCustomPosition(0);
        custom.setCreated(new Date());
        custom.setModified(new Date());
        customSet.add(custom);
        cmd.getKickstartData().setCustomOptions(customSet);

        cmd.store();

        assertNotNull(cmd.getClonedKickstart());
        assertNotNull(cmd.getClonedKickstart().getId());
        assertNotNull(cmd.getClonedKickstart().getCommand("custom").getCustomPosition());
        assertFalse(cmd.getClonedKickstart().getId().equals(ksdata.getId()));
    }

}
