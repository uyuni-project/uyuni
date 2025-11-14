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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.Modules;
import com.redhat.rhn.domain.channel.ProductName;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.channel.ForbiddenCloneChannelPAYGException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;

import org.junit.jupiter.api.Test;

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

    /**
     * Tests if it's possible to clone base channels in a SUMA PAYG instace.
     *
     * @throws Exception if something goes wrong
     */
    @Test
    public void canCloneParentChannelInPAYGInstance() throws Exception {
        CloudPaygManager fakeCloudPaygManager = getFakeCloudPaygManager();
        Channel parentChannel = createBaseChannel();

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE,
                                                        parentChannel, fakeCloudPaygManager);
        ccc.setUser(user);
        Channel clonedParentChannel = ccc.create();

        // Asserts that the channel actually exists after cloning
        Channel gotChannel = ChannelFactory.lookupByIdAndUser(clonedParentChannel.getId(), user);
        assertNotNull(gotChannel);
    }

    /**
     * Tests that cloning a channel under the same base channel works in a SUMA PAYG instace.
     *
     * @throws Exception
     */
    @Test
    public void canCloneChildChannelUnderSameParentChannel() throws Exception {
        CloudPaygManager fakeCloudPaygManager = getFakeCloudPaygManager();

        Channel parentChannelWithProductChannels = createBaseChannel();
        Channel childrenChannel = createChildrenWIthProductChannel(parentChannelWithProductChannels);

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE,
                childrenChannel, fakeCloudPaygManager);
        ccc.setUser(user);
        ccc.setParentId(parentChannelWithProductChannels.getId());
        Channel clonedChildChannel1 = ccc.create();

        // Asserts that the channel actually exists after cloning
        Channel gotChannelTest1 = ChannelFactory.lookupByIdAndUser(clonedChildChannel1.getId(), user);
        assertNotNull(gotChannelTest1);
    }


    /**
     * Tests that cloning a channel under a different base channel that has not the channel product we are
     * cloning from fails in a SUMA PAYG instace.
     *
     * @throws Exception
     */
    @Test
    public void cannotCloneChildChannelUnderDifferentParentChannelWithoutProductChannel() throws Exception {
        CloudPaygManager fakeCloudPaygManager = getFakeCloudPaygManager();

        Channel parentChannelWithProductChannels = createBaseChannel();
        Channel parentChannelWithoutProductChannels = createBaseChannel();
        Channel childrenChannel = createChildrenWIthProductChannel(parentChannelWithProductChannels);

        CloneChannelCommand ccc = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE,
                childrenChannel, fakeCloudPaygManager);
        ccc.setUser(user);
        ccc.setParentId(parentChannelWithoutProductChannels.getId());

        // Assert that cloning operation throws ForbiddenCloneChannelPAYGException exception
        assertThrows(ForbiddenCloneChannelPAYGException.class, () -> ccc.create());
    }

    /**
     * Tests some recursiveness. It clones a base channel that has associated some product channel, and then
     * under that cloned base channel it clones a child channel with the same product channel as the first one.
     * This test is for SUMA PAYG instaces.
     *
     * @throws Exception
     */
    @Test
    public void canClonedBasedChannelGetChildChannelWithProductChannel() throws Exception {
        CloudPaygManager fakeCloudPaygManager = getFakeCloudPaygManager();

        Channel parentChannelWithProductChannels = createBaseChannel();
        Channel childrenChannel = createChildrenWIthProductChannel(parentChannelWithProductChannels);

        CloneChannelCommand cccBase = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE,
                parentChannelWithProductChannels, fakeCloudPaygManager);
        cccBase.setUser(user);
        Channel clonedBaseChannel = cccBase.create();

        CloneChannelCommand cccChildren = new CloneChannelCommand(CloneChannelCommand.CloneBehavior.ORIGINAL_STATE,
                childrenChannel, fakeCloudPaygManager);
        cccChildren.setUser(user);
        cccChildren.setParentId(clonedBaseChannel.getId());
        Channel clonedChildChannel = cccChildren.create();

        // Assert that the channel actually exists
        Channel gotChannelTest3 = ChannelFactory.lookupByIdAndUser(clonedChildChannel.getId(), user);
        assertNotNull(gotChannelTest3);
    }

    private Channel createBaseChannel() throws Exception {
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        channel.setSummary("summary");
        return channel;
    }

    private CloudPaygManager getFakeCloudPaygManager() {
        return new TestCloudPaygManagerBuilder()
            .withPaygInstance()
            .build();
    }

    private Channel createChildrenWIthProductChannel(Channel parentChannel) throws Exception {
        Channel childrenChannel = ChannelTestUtils.createChildChannel(user, parentChannel);
        childrenChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        childrenChannel.setSummary("summary");

        ProductName pn = ChannelFactoryTest.lookupOrCreateProductName("sap-ha");
        childrenChannel.setProductName(pn);
        TestUtils.saveAndFlush(pn);
        ChannelFactory.save(childrenChannel);

        return childrenChannel;
    }
}
