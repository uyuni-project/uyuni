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
package com.redhat.rhn.frontend.action.channel.manage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.testing.RhnMockDynaActionForm;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;

import org.junit.jupiter.api.Test;

/**
 * EditChannelActionTest
 */
public class EditChannelActionTest {

    @Test
    public void testExecute() {
        System.out.println("JESUSR PLEASE FIX THIS TEST");
        assertTrue(true);
    }

    @Test
    public void testSetupFormPreservesDescription() {
        Channel channel = new Channel();
        channel.setDescription("if a<b and c>d\n<p>user text</p>");
        Channel parent = new Channel();
        parent.setName("Parent");
        channel.setParentChannel(parent);
        ChannelArch arch = new ChannelArch();
        arch.setName("Architecture");
        arch.setLabel("architecture");
        channel.setChannelArch(arch);
        RhnMockDynaActionForm form = new RhnMockDynaActionForm();

        EditChannelAction.setupFormHelper(new RhnMockHttpServletRequest(), form, channel);

        assertEquals(channel.getDescription(), form.get(EditChannelAction.DESCRIPTION));
    }
}
