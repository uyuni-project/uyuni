/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.channel.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.RetiredChannelTreeAction;
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.listview.ListControl;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.testing.ActionHelper;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.util.Date;

/**
 * RetiredChannelTreeSetupActionTest
 * @version $Rev$
 */
public class RetiredChannelTreeActionTest extends RhnBaseTestCase {

    public void testPerformExecute() throws Exception {
        RetiredChannelTreeAction action = new RetiredChannelTreeAction() {
            protected DataResult<ChannelTreeNode> getDataResult(User user, ListControl lc) {
                return ChannelManager.retiredChannelTree(user, lc);
            }
        };
        ActionHelper sah = new ActionHelper();
        sah.setUpAction(action, RhnHelper.DEFAULT_FORWARD);
        sah.getRequest().setupAddParameter(RequestContext.FILTER_STRING, (String) null);

        User user = sah.getUser();
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setEndOfLife(new Date(System.currentTimeMillis() - 100000));
        user.getOrg().addOwnedChannel(channel);

        OrgFactory.save(user.getOrg());

        sah.executeAction();
        DataResult dr = (DataResult) sah.getRequest().getAttribute(
                RequestContext.PAGE_LIST);
        assertNotEmpty(dr);
   }
}
