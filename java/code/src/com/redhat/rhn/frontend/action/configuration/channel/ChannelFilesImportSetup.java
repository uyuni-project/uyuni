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
package com.redhat.rhn.frontend.action.configuration.channel;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * ChannelFilesImportSetup
 * @version $Rev$
 */
public class ChannelFilesImportSetup extends BaseSetListAction {

    protected DataResult getDataResult(RequestContext ctx, PageControl pc) {
        User user = ctx.getCurrentUser();
        ConfigChannel cc = ConfigActionHelper.getChannel(ctx.getRequest());
        DataResult dr = ConfigurationManager.getInstance().
            listFilesNotInChannel(user, cc, pc);
        return dr;
    }

    protected void processRequestAttributes(RequestContext rctx) {
        super.processRequestAttributes(rctx);
        ConfigChannel cc = ConfigActionHelper.getChannel(rctx.getRequest());
        ConfigActionHelper.setupRequestAttributes(rctx, cc);
    }

    /**
     * {@inheritDoc}
     */
    protected void processPageControl(PageControl pc) {
        pc.setFilterColumn("path");
        pc.setFilter(true);
    }

    /**
     * We affect the selected-files set
     * @return FILE_LISTS identifier
     */
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.CONFIG_FILES;
    }


}
