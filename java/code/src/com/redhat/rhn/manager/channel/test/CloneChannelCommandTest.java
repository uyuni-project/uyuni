/*
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.manager.channel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.Set;

public class CloneChannelCommandTest extends BaseTestCaseWithUser {

    /**
     * Tests that cloning a channel does NOT respect the parent of the original
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testNoParentOnClone() throws Exception {
        Channel originalBase = ChannelTestUtils.createBaseChannel(user);
        Channel originalChild = ChannelTestUtils.createChildChannel(user, originalBase);
        originalChild.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        originalChild.setSummary("summary");
        CloneChannelCommand ccc = new CloneChannelCommand(
                CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, originalChild);
        ccc.setUser(user);

        Channel clone = ccc.create();
        assertNull(clone.getParentChannel());
    }

    /**
     * Test cloning a channel without modular data
     *
     * @throws Exception
     */
    @Test
    public void testCloneNoModular() throws Exception {
        Channel original = createBaseChannel();
        assertFalse(original.isModular());

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, original);
        ccc.setUser(user);
        Channel clone = ccc.create();
        assertFalse(clone.isModular());
    }

    /**
     * Test cloning a channel with modular data
     *
     * @throws Exception
     */
    @Test
    public void testCloneModularSource() throws Exception {
        Channel original = createBaseChannel();
        Modules modules = new Modules("blablafilename1", new Date());
        original.addModules(modules);

        modules = new Modules("blablafilename2", new Date());
        original.addModules(modules);

        assertTrue(original.isModular());

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, original);
        ccc.setUser(user);
        Channel clone = ccc.create();
        Set<Modules> originalModules = original.getModules();
        Set<Modules> cloneModules = clone.getModules();

        assertEquals(2, cloneModules.size());
        cloneModules.forEach(m -> {
            Modules orig = originalModules.stream()
                    .filter(o -> m.getRelativeFilename().equals(o.getRelativeFilename())).findFirst().get();
            assertEquals(orig.getLastModified(), m.getLastModified());
            assertEquals(clone, m.getChannel());
            assertNotEquals(orig.getId(), m.getId());
        });
    }

    /**
     * Test cloning a modular channel as a regular channel, stripping modular metadata
     */
    @Test
    public void testStripModularMetadata() throws Exception {
        Channel original = createBaseChannel();
        Modules modules = new Modules("blablafilename", new Date());
        original.addModules(modules);
        assertTrue(original.isModular());

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, original);
        ccc.setUser(user);
        ccc.setStripModularMetadata(true);
        Channel c = ccc.create();
        c = ChannelFactory.reload(c);

        assertFalse(c.isModular());
    }


    private Channel createBaseChannel() throws Exception {
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        channel.setSummary("summary");
        return channel;
    }
}
