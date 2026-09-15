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
package com.redhat.rhn.frontend.action.configuration.sdc;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.dto.ConfigFileNameDto;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;


/**
 * ImportFileAction
 */
public class ImportFileAction extends BaseSetListAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataResult<ConfigFileNameDto> getDataResult(RequestContext rctx, PageControl pc) {
        User user = rctx.getCurrentUser();
        Server server = rctx.lookupServer();
        return ConfigurationManager.getInstance().listFileNamesForSystem(user, server, pc);
    }

    @Override
    protected void processRequestAttributes(RequestContext rctx) {
        if (!rctx.isSubmitted()) {
            getSetDecl().clear(rctx.getCurrentUser());
        }
        super.processRequestAttributes(rctx);
        Server server = rctx.lookupAndBindServer();
        User user = rctx.getCurrentUser();
        SdcHelper.ssmCheck(rctx.getRequest(), server.getId(), user);
    }

    @Override
    protected void processPageControl(PageControl pc) {
        pc.setFilter(true);
        pc.setFilterColumn("path");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RhnSetDecl getSetDecl() {
        return RhnSetDecl.CONFIG_FILE_NAMES;
    }

}
