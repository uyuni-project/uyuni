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

package com.redhat.rhn.manager.kickstart.tree;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerCommand;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Tests the distro kernel options merge of {@link BaseTreeEditOperation}
 * with mocked entities, without a database.
 */
public class BaseTreeEditOperationKernelOptionsTest extends MockObjectTestCase {

    private int mockId;

    private User user;
    private Channel parentChannel;
    private TestTreeOperation operation;

    /**
     * Minimal concrete operation: only the kernel options merge is exercised,
     * the Cobbler command is never invoked.
     */
    private static class TestTreeOperation extends BaseTreeEditOperation {
        TestTreeOperation(User userIn) {
            super(userIn);
        }

        @Override
        protected CobblerCommand getCobblerCommand() {
            return null;
        }
    }

    /**
     * Sets up the operation with class mocking enabled.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        user = mock(User.class, "user");
        parentChannel = mock(Channel.class, "parentChannel");
        operation = new TestTreeOperation(user);
        operation.setServerName("localhost");
    }

    private KickstartableTree tree(KickstartInstallType installType, String kernelOptions) {
        KickstartableTree tree = mock(KickstartableTree.class, "tree" + ++mockId);
        checking(new Expectations() {{
            allowing(tree).getKernelOptions(); will(returnValue(kernelOptions));
            allowing(tree).getKernelOptionsPost(); will(returnValue(""));
            allowing(tree).getInstallType(); will(returnValue(installType));
            allowing(tree).getLabel(); will(returnValue("tree1"));
            allowing(tree).getChannel(); will(returnValue(parentChannel));
            allowing(parentChannel).getAccessibleChildrenFor(user);
            will(returnValue(List.of()));
        }});
        operation.tree = tree;
        return tree;
    }

    private KickstartInstallType installType(String label) {
        KickstartInstallType type = new KickstartInstallType();
        type.setLabel(label);
        return type;
    }

    private String fqdn() {
        try {
            return InetAddress.getByName("localhost").getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * User-typed options win over the computed ones and only missing
     * computed options are appended.
     */
    @Test
    public void testSles15MergeUserWins() {
        KickstartableTree tree = tree(installType("sles15generic"),
                "install=http://mymirror.example.com/sles15 useonlinerepo=1");
        checking(new Expectations() {{
            one(tree).setKernelOptions("install=http://mymirror.example.com/sles15 " +
                    "useonlinerepo=1 self_update=0");
        }});
        operation.applyDistroKernelOptions();
    }

    /**
     * An SLE16 tree gets the Agama live root and inst.self_update, and no
     * install= option.
     */
    @Test
    public void testSles16AgamaOptions() {
        KickstartableTree tree = tree(installType("opensuse16.1"), "");
        checking(new Expectations() {{
            one(tree).setKernelOptions("root=live:http://" + fqdn() +
                    "/ks/dist/tree1/LiveOS/squashfs.img inst.self_update=0");
            never(tree).setKernelOptionsPost(with(any(String.class)));
        }});
        operation.applyDistroKernelOptions();
    }

    /**
     * A RHEL tree gets inst.repo= pointing at the dist tree.
     */
    @Test
    public void testRhelInstRepo() {
        KickstartableTree tree = tree(installType("rhel_7"), "");
        checking(new Expectations() {{
            one(tree).setKernelOptions("inst.repo=http://" + fqdn() + "/ks/dist/tree1");
        }});
        operation.applyDistroKernelOptions();
    }
}
