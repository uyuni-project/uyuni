/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.configuration.files;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.configuration.ConfigActionHelper;
import com.redhat.rhn.frontend.dto.ConfigGlobalDeployDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

/**
 * GlobalRevisionDeploySetup
 */
public class GlobalRevisionDeploySetup extends BaseSetListAction {

    @Override
    protected DataResult<ConfigGlobalDeployDto> getDataResult(RequestContext ctx, PageControl pc) {
        User usr = ctx.getCurrentUser();
        ConfigFile cf = ConfigActionHelper.getFile(ctx.getRequest());
        ConfigChannel cc = cf.getConfigChannel();
        return ConfigurationManager.getInstance().listGlobalFileDeployInfo(usr, cc, cf, pc);
    }

    @Override
    protected void processRequestAttributes(RequestContext rctxIn) {
        ConfigActionHelper.processRequestAttributes(rctxIn);
        super.processRequestAttributes(rctxIn);
    }

    @Override
    protected void processPageControl(PageControl pc) {
        pc.setFilterColumn("name");
        pc.setFilter(true);
    }

    /**
     * We affect the selected-files set
     * @return FILE_LISTS identifier
     */
    @Override
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.CONFIG_FILE_DEPLOY_SYSTEMS;
    }


}
