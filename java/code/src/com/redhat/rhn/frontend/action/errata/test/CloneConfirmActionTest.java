/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.errata.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetFactory;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.testing.RhnPostMockStrutsTestCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * CloneConfirmActionTest
 */
public class CloneConfirmActionTest extends RhnPostMockStrutsTestCase {

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/errata/manage/CloneConfirmSubmit");
        user.getOrg().addRole(RoleFactory.CHANNEL_ADMIN);
    }

    @Test
    public void testExecute() throws Exception {

        List list = new ArrayList<>();

        RhnSet errataToClone = RhnSetFactory.createRhnSet(user.getId(),
                                                          "clone_errata_list",
                                                          SetCleanup.NOOP);

        Channel original = ChannelFactoryTest.createTestChannel(user);

        for (int j = 0; j < 5; ++j) {
            Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
            original.addErrata(e);
            errataToClone.addElement(e.getId());
            list.add(e);
        }

        RhnSetManager.store(errataToClone);

        RhnSet set = RhnSetDecl.ERRATA_CLONE.get(user);
        assertEquals(5, set.size());

        Channel destination = ChannelFactoryTest.createTestChannel(user);
        RhnSet destinationChannels = RhnSetFactory.createRhnSet(user.getId(),
                RhnSetDecl.CHANNELS_FOR_ERRATA.getLabel(),
                SetCleanup.NOOP);
        destinationChannels.addElement(destination.getId());
        RhnSetManager.store(destinationChannels);

        RhnSet channelSet = RhnSetDecl.CHANNELS_FOR_ERRATA.get(user);
        assertEquals(1, channelSet.size());

        request.addParameter("dispatch", "Confirm");

        actionPerform();
        verifyForward("success");
        set = RhnSetDecl.ERRATA_CLONE.get(user);
        assertEquals(0, set.size());

        for (Object oIn : list) {
            Errata e = (Errata) oIn;
            List clones = ErrataManager.lookupByOriginal(user, e);

            assertEquals(1, clones.size());
            var clone = (ClonedErrata) clones.get(0);
            assertEquals(clone.getOriginal(), e);
        }
    }
}
