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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.channel.ForbiddenCloneChannelPAYGException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import com.redhat.rhn.testing.TestUtils;
import com.suse.cloud.CloudPaygManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        Modules modules = new Modules();
        modules.setChannel(original);
        modules.setRelativeFilename("blablafilename");
        original.setModules(modules);
        assertTrue(original.isModular());

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, original);
        ccc.setUser(user);
        Channel clone = ccc.create();
        Modules originalModules = original.getModules();
        Modules cloneModules = clone.getModules();
        assertEquals(originalModules.getRelativeFilename(), cloneModules.getRelativeFilename());
        assertEquals(clone, cloneModules.getChannel());
        assertFalse(originalModules.getId().equals(cloneModules.getId()));
    }

    /**
     * Test cloning a modular channel as a regular channel, stripping modular metadata
     */
    @Test
    public void testStripModularMetadata() throws Exception {
        Channel original = createBaseChannel();
        Modules modules = new Modules();
        modules.setChannel(original);
        modules.setRelativeFilename("blablafilename");
        original.setModules(modules);
        assertTrue(original.isModular());

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, original);
        ccc.setUser(user);
        ccc.setStripModularMetadata(true);
        Channel c = ccc.create();
        c = ChannelFactory.reload(c);

        assertFalse(c.isModular());
    }

    @Test
    public void testCloneParentChannelPAYG () throws Exception {
        GlobalInstanceHolder.PAYG_MANAGER.setPaygInstance(true); // Mock we are in a PAYG SUMA instance
        Channel parentChannel = createBaseChannel();

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, parentChannel);
        ccc.setUser(user);
        Channel clonedParentChannel = ccc.create();

        // Assert that the channel actually exists
        Channel gotChannel = ChannelFactory.lookupByIdAndUser(clonedParentChannel.getId(), user);
        assertNotNull(gotChannel);
    }

    @Test
    public void testCloneChildChannelPAYG () throws Exception {
        GlobalInstanceHolder.PAYG_MANAGER.setPaygInstance(true); // Mock we are in a PAYG SUMA instance

        Channel parentChannelWithProductChannels = createBaseChannel();
        Channel parentChannelWithoutProductChannels = createBaseChannel();

        Channel childrenChannel = ChannelTestUtils.createChildChannel(user, parentChannelWithProductChannels);
        childrenChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        childrenChannel.setSummary("summary");

        ProductName pn = ChannelFactoryTest.lookupOrCreateProductName("sap-ha");
        childrenChannel.setProductName(pn);
        TestUtils.saveAndFlush(pn);
        ChannelFactory.save(childrenChannel);


        // Test 1 - Check cloning channel under a base channel with the same channel works
        CloneChannelCommand ccc1 = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, childrenChannel);
        ccc1.setUser(user);
        ccc1.setParentId(parentChannelWithProductChannels.getId());
        Channel clonedChildChannel1 = ccc1.create();

        // Assert that the channel actually exists
        Channel gotChannelTest1 = ChannelFactory.lookupByIdAndUser(clonedChildChannel1.getId(), user);
        assertNotNull(gotChannelTest1);
        ChannelFactory.remove(gotChannelTest1);

        // Test 2 - Check cloning channel under a base channel without the same channel fails
        CloneChannelCommand ccc2 = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, childrenChannel);
        ccc2.setUser(user);
        ccc2.setParentId(parentChannelWithoutProductChannels.getId());
        Channel clonedChildChannel2 = null;

        try {
            clonedChildChannel2 = ccc2.create();
        } catch (ForbiddenCloneChannelPAYGException f) {} // Catch this specific exception to avoid aborting the test

        // Assert that the channel is null, which means it wasn't created
        assertNull(clonedChildChannel2);


        // Test 3 - Clone a base channel that has children with product channels, and clone under it a channel
        // with those product channels (Recursive check).
        CloneChannelCommand ccc3 = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE, parentChannelWithProductChannels);
        ccc3.setUser(user);
        Channel clonedBaseChannel = ccc3.create();

        ccc2.setParentId(clonedBaseChannel.getId());
        Channel clonedChildChannel3 = ccc2.create();

        // Assert that the channel actually exists
        Channel gotChannelTest3 = ChannelFactory.lookupByIdAndUser(clonedChildChannel3.getId(), user);
        assertNotNull(gotChannelTest3);
    }

    private Channel createBaseChannel() throws Exception {
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        channel.setSummary("summary");
        return channel;
    }
}
