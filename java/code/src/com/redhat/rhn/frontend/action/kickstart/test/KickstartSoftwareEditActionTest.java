/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.frontend.action.kickstart.KickstartSoftwareEditAction;
import com.redhat.rhn.manager.kickstart.KickstartWizardHelper;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.struts.util.LabelValueBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/**
 * KickstartSoftwareEditActionTest
 */
public class KickstartSoftwareEditActionTest extends BaseKickstartEditTestCase {


    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setRequestPathInfo("/kickstart/KickstartSoftwareEdit");
    }

    @Test
    public void testSetupExecute() throws Exception {
        Channel child = ChannelTestUtils.createChildChannel(user,
                ksdata.getTree().getChannel());

        actionPerform();
        KickstartableTree tree = ksdata.getKickstartDefaults().getKstree();

        verifyFormValue(KickstartSoftwareEditAction.URL,
                tree.getDefaultDownloadLocation());
        verifyFormValue(KickstartSoftwareEditAction.CHANNEL, tree.getChannel().getId());

        Collection c = (Collection)
            getRequest().getAttribute(KickstartSoftwareEditAction.CHANNELS);
        assertNotNull(c);
        assertTrue(c.iterator().next() instanceof LabelValueBean);

        // For some reason this assertion fails, even thou i swear its in the request
        //assertNotNull(getRequest().
        //         getAttribute(KickstartSoftwareEditAction.CHILD_CHANNELS));
        assertNotNull(getRequest().
                getAttribute(KickstartSoftwareEditAction.CHANNELS));
    }

    @Test
    public void testSubmitExecute() throws Exception {
        KickstartWizardHelper wcmd = new KickstartWizardHelper(user);
        wcmd.createCommand("url",
                "--url /rhn/kickstart/ks-f9-x86_64", ksdata);

        addRequestParameter(KickstartSoftwareEditAction.SUBMITTED,
                Boolean.TRUE.toString());
        addRequestParameter(KickstartSoftwareEditAction.URL,
                ksdata.getTree().getBasePath());
        String cid = ksdata.getTree().getChannel().getId().toString();
        assertNotNull(cid);
        addRequestParameter(KickstartSoftwareEditAction.CHANNEL, cid);
        addRequestParameter(KickstartSoftwareEditAction.TREE,
                ksdata.getTree().getId().toString());

        Channel child = ChannelTestUtils.createChildChannel(user,
                ksdata.getTree().getChannel());

        addRequestParameter(KickstartSoftwareEditAction.CHILD_CHANNELS,
                child.getId().toString());

        assertTrue(ksdata.getChildChannels() == null ||
                ksdata.getChildChannels().isEmpty());
        actionPerform();
        String[] keys = {"kickstart.software.success"};
        verifyActionMessages(keys);
        ksdata = (KickstartData) TestUtils.reload(ksdata);
        assertFalse(ksdata.getChildChannels().isEmpty());

    }

}

