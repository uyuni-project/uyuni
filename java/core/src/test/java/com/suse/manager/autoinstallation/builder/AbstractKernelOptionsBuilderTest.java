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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
import java.util.stream.Stream;

/**
 * Tests the shared kernel options builder base: URL helpers and the
 * installer-updates child channel resolution in both scoping variants
 * (user-scoped legacy lookup vs. org-global fallback, spec §5.5).
 */
public class AbstractKernelOptionsBuilderTest extends MockObjectTestCase {

    private static final String FQDN = "server.example.com";

    private int mockId;

    private User user;
    private Channel parentChannel;

    /**
     * Minimal concrete builder exposing the protected base methods under
     * test. The contract methods themselves are covered by the per-builder
     * suites.
     */
    private static class TestBuilder extends AbstractKernelOptionsBuilder {
        TestBuilder(String serverFqdn, User userIn) {
            super(serverFqdn, userIn);
        }

        @Override
        public KernelOptionsList distroOptions(KickstartableTree tree) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList selfUpdateOption(KickstartableTree tree) {
            return new KernelOptionsList(installerUpdatesValue(tree));
        }

        @Override
        public KernelOptionsList mediaOptions(String mediaUrl) {
            return new KernelOptionsList();
        }

        @Override
        public KernelOptionsList systemOptions(SystemRecord system) {
            return new KernelOptionsList();
        }
    }

    /**
     * Builder variant stubbing the org-global lookup hook, so the static
     * ChannelFactory call can be replaced.
     */
    private static final class OrgGlobalTestBuilder extends TestBuilder {
        private Stream<Channel> childrenToReturn = Stream.empty();
        private Channel lookedUpParent;

        OrgGlobalTestBuilder(String serverFqdn) {
            super(serverFqdn, null);
        }

        @Override
        protected Stream<Channel> listAllChildren(Channel parent) {
            lookedUpParent = parent;
            return childrenToReturn;
        }
    }

    /**
     * Sets up class mocking and the shared parent channel mock.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        user = mock(User.class, "user");
        parentChannel = mock(Channel.class, "parentChannel");
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

    /**
     * The distro tree URL form.
     */
    @Test
    public void testDistTreeUrl() {
        TestBuilder builder = new TestBuilder(FQDN, user);
        KernelOptionsList options = new KernelOptionsList(builder.distTreeUrl(tree()));
        assertEquals("http://" + FQDN + "/ks/dist/tree1", options.toString());
    }

    /**
     * The Cobbler system autoinstall URL form.
     */
    @Test
    public void testSystemAutoinstallUrl() {
        SystemRecord system = mock(SystemRecord.class, "system" + ++mockId);
        checking(new Expectations() {{
            allowing(system).getName(); will(returnValue("system1"));
        }});
        TestBuilder builder = new TestBuilder(FQDN, user);
        KernelOptionsList options =
                new KernelOptionsList(builder.cobblerSystemAutoinstallUrl(system));
        assertEquals("http://" + FQDN + "/cblr/svc/op/autoinstall/system/system1",
                options.toString());
    }

    /**
     * With a user context the installer-updates lookup is user-scoped
     * (legacy distro/system path behaviour).
     */
    @Test
    public void testInstallerUpdatesValueUserScoped() {
        checking(new Expectations() {{
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of(child("child1", true))));
        }});
        TestBuilder builder = new TestBuilder(FQDN, user);
        assertEquals("http://" + FQDN + "/ks/dist/child/child1/tree1",
                builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Without a user context the lookup falls back to the org-global
     * ChannelFactory lookup (spec §5.5).
     */
    @Test
    public void testInstallerUpdatesValueOrgGlobal() {
        OrgGlobalTestBuilder builder = new OrgGlobalTestBuilder(FQDN);
        builder.childrenToReturn = Stream.of(child("child1", true));
        assertEquals("http://" + FQDN + "/ks/dist/child/child1/tree1",
                builder.selfUpdateOption(tree()).toString());
        assertEquals(parentChannel, builder.lookedUpParent);
    }

    /**
     * Children without the installer-updates flag are skipped in the
     * org-global lookup too.
     */
    @Test
    public void testInstallerUpdatesValueSkipsNonUpdatesChildren() {
        OrgGlobalTestBuilder builder = new OrgGlobalTestBuilder(FQDN);
        builder.childrenToReturn =
                Stream.of(child("child1", false), child("child2", true));
        assertEquals("http://" + FQDN + "/ks/dist/child/child2/tree1",
                builder.selfUpdateOption(tree()).toString());
    }

    /**
     * Without any installer-updates child the self update is disabled
     * with 0.
     */
    @Test
    public void testInstallerUpdatesValueWithoutChild() {
        OrgGlobalTestBuilder builder = new OrgGlobalTestBuilder(FQDN);
        builder.childrenToReturn = Stream.empty();
        assertEquals("0", builder.selfUpdateOption(tree()).toString());
    }

    /**
     * The empty profileOptions default (G6 flip-switch).
     */
    @Test
    public void testProfileOptionsEmpty() {
        Profile profile = mock(Profile.class, "profile" + ++mockId);
        assertEquals("", new TestBuilder(FQDN, user).profileOptions(profile).toString());
    }

    /**
     * A null tree is rejected.
     */
    @Test
    public void testNullTreeThrows() {
        TestBuilder builder = new TestBuilder(FQDN, user);
        assertThrows(KernelOptionsBuilderException.class, () -> builder.distTreeUrl(null));
        assertThrows(KernelOptionsBuilderException.class, () -> builder.selfUpdateOption(null));
    }
}
