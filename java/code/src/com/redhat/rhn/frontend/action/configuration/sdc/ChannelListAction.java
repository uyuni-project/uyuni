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
package com.redhat.rhn.frontend.action.configuration.sdc;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * ChannelListAction,  for configuration in sdc
 * @version $Rev$
 */
public class ChannelListAction extends BaseSetListAction {

    /**
     * {@inheritDoc}
     */
    protected DataResult getDataResult(RequestContext rctxIn, PageControl pc) {
        User user = rctxIn.getCurrentUser();
        Server server = rctxIn.lookupAndBindServer();
        SdcHelper.ssmCheck(rctxIn.getRequest(), server.getId(), user);
        return ConfigurationManager.getInstance().listChannelsForSystem(user, server, pc);
    }

    protected void processPageControl(PageControl pc) {
        pc.setFilter(true);
        pc.setFilterColumn("name");
    }


    /**
     *
     * {@inheritDoc}
     */
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.CONFIG_CHANNELS_TO_UNSUBSCRIBE;
    }
}
