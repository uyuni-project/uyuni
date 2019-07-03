/**
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

public class CloneChannelCommandTest extends BaseTestCaseWithUser {

    /**
     * Tests that cloning a channel does NOT respect the parent of the original
     *
     * @throws Exception if anything goes wrong
     */
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
}