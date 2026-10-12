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

/**
 * Tests the RHEL kernel options builder with mocked entities.
 */
public class RhelKernelOptionsBuilderTest extends MockObjectTestCase {

    private static final String FQDN = "server.example.com";

    private int mockId;

    private RhelKernelOptionsBuilder builder;

    /**
     * Sets up the builder with class mocking enabled.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        builder = new RhelKernelOptionsBuilder(FQDN, mock(User.class, "user"));
    }

    private KickstartableTree tree() {
        KickstartableTree tree = mock(KickstartableTree.class, "tree" + ++mockId);
        checking(new Expectations() {{
            allowing(tree).getLabel(); will(returnValue("tree1"));
        }});
        return tree;
    }

    private SystemRecord system() {
        SystemRecord system = mock(SystemRecord.class, "system" + ++mockId);
        checking(new Expectations() {{
            allowing(system).getName(); will(returnValue("system1"));
        }});
        return system;
    }

    /**
     * Distro options are inst.repo= pointing at the dist tree,
     * independent of the concrete RHEL-family install type.
     */
    @Test
    public void testDistroOptions() {
        assertEquals("inst.repo=http://" + FQDN + "/ks/dist/tree1",
                builder.distroOptions(tree()).toString());
    }

    /**
     * RHEL has no installer-updates option.
     */
    @Test
    public void testSelfUpdateOptionEmpty() {
        assertEquals("", builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Profile options stay empty until the Cobbler exit.
     */
    @Test
    public void testProfileOptionsEmpty() {
        assertEquals("", builder.profileOptions(mock(Profile.class, "profile" + ++mockId)).toString());
    }

    /**
     * Media options are inst.repo= pointing at the session-tracked media URL.
     */
    @Test
    public void testMediaOptions() {
        assertEquals("inst.repo=http://" + FQDN + "/ty/token1",
                builder.mediaOptions("http://" + FQDN + "/ty/token1").toString());
    }

    /**
     * System options are inst.ks.sendmac plus inst.ks= pointing at the Cobbler
     * system autoinstall URL.
     */
    @Test
    public void testSystemOptions() {
        assertEquals("inst.ks.sendmac inst.ks=http://" + FQDN + "/cblr/svc/op/autoinstall/system/system1",
                builder.systemOptions(system()).toString());
    }

    /**
     * User-typed options win over computed ones and re-merging the same
     * computed options changes nothing.
     */
    @Test
    public void testMergeIsUserWinsAndIdempotent() {
        KernelOptionsList options = new KernelOptionsList("inst.repo=http://mymirror.example/repo");
        options.addMissingOptions(builder.distroOptions(tree()));
        String merged = options.toString();
        assertTrue(merged.contains("inst.repo=http://mymirror.example/repo"));
        assertFalse(merged.contains(FQDN + "/ks/dist"));
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
