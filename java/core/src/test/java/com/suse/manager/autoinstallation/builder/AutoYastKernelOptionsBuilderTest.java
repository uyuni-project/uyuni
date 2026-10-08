/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.autoinstallation.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests the AutoYaST kernel options builder with mocked entities.
 */
public class AutoYastKernelOptionsBuilderTest extends MockObjectTestCase {

    private static final String FQDN = "server.example.com";

    private int mockId;

    private AutoYastKernelOptionsBuilder builder;
    private User user;
    private Channel parentChannel;

    /**
     * Sets up the builder with class mocking enabled and a user-scoped
     * installer-updates lookup.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        user = mock(User.class, "user");
        parentChannel = mock(Channel.class, "parentChannel");
        builder = new AutoYastKernelOptionsBuilder(FQDN, user);
    }

    private KickstartableTree tree() {
        KickstartableTree tree = mock(KickstartableTree.class, "tree" + ++mockId);
        checking(new Expectations() {{
            allowing(tree).getLabel(); will(returnValue("tree1"));
            allowing(tree).getChannel(); will(returnValue(parentChannel));
        }});
        return tree;
    }

    private Channel child(String label, boolean installerUpdates) {
        Channel child = mock(Channel.class, "child" + ++mockId);
        checking(new Expectations() {{
            allowing(child).getLabel(); will(returnValue(label));
            allowing(child).isInstallerUpdates(); will(returnValue(installerUpdates));
        }});
        return child;
    }

    private SystemRecord system() {
        SystemRecord system = mock(SystemRecord.class, "system" + ++mockId);
        checking(new Expectations() {{
            allowing(system).getName(); will(returnValue("system1"));
        }});
        return system;
    }

    /**
     * The installer-updates child URL is self_update=.
     */
    @Test
    public void testSelfUpdateOptionWithAccessibleChild() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of(child("child1", true))));
        }});
        assertEquals("self_update=http://" + FQDN + "/ks/dist/child/child1/tree1",
                builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Children without the installer-updates flag are skipped.
     */
    @Test
    public void testSelfUpdateOptionIgnoresOtherChildren() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of(child("regular", false), child("child1", true))));
        }});
        assertEquals("self_update=http://" + FQDN + "/ks/dist/child/child1/tree1",
                builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Without an installer-updates child the self update is disabled with 0.
     */
    @Test
    public void testSelfUpdateOptionWithoutChild() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of()));
        }});
        assertEquals("self_update=0", builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Distro options are install= plus the self update option.
     */
    @Test
    public void testDistroOptions() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of()));
        }});
        assertEquals("install=http://" + FQDN + "/ks/dist/tree1 self_update=0",
                builder.distroOptions(tree()).toString());
    }

    /**
     * Profile options stay empty until the Cobbler exit.
     */
    @Test
    public void testProfileOptionsEmpty() {
        assertEquals("", builder.profileOptions(mock(Profile.class, "profile" + ++mockId)).toString());
    }

    /**
     * System options are autoyast= pointing at the Cobbler system
     * autoinstall URL.
     */
    @Test
    public void testSystemOptions() {
        assertEquals("autoyast=http://" + FQDN + "/cblr/svc/op/autoinstall/system/system1",
                builder.systemOptions(system()).toString());
    }

    /**
     * User-typed options win over computed ones and re-merging the same
     * computed options changes nothing.
     */
    @Test
    public void testMergeIsUserWinsAndIdempotent() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of()));
        }});
        KernelOptionsList options = new KernelOptionsList("ksdevice=eth0 install=http://custom");
        options.addMissingOptions(builder.distroOptions(tree()));
        String merged = options.toString();
        assertFalse(options.hasOption("install", "http://" + FQDN + "/ks/dist/tree1"));
        assertTrue(merged.contains("install=http://custom"));
        options.addMissingOptions(builder.distroOptions(tree()));
        assertEquals(merged, options.toString());
    }

    /**
     * Null inputs are rejected.
     */
    @Test
    public void testNullInputsThrow() {
        assertThrows(KernelOptionsBuilderException.class, () -> builder.distroOptions(null));
        assertThrows(KernelOptionsBuilderException.class, () -> builder.selfUpdateOption(null));
        assertThrows(KernelOptionsBuilderException.class, () -> builder.systemOptions(null));
    }
}
