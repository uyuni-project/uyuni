/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.kickstart;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.frontend.struts.BaseSetListAction;
import com.redhat.rhn.frontend.struts.RequestContext;

import org.apache.struts.action.ActionForm;

import java.util.Iterator;

/**
 * BaseKickstartListSetupAction - base class for Kickstart Details list pages that show
 * a list of items to associate with the kickstart.
 *
 */
public abstract class BaseKickstartListSetupAction extends BaseSetListAction {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void processForm(RequestContext rctx, ActionForm form) {
        super.processForm(rctx, form);
        KickstartData ksdata = KickstartFactory
            .lookupKickstartDataByIdAndOrg(rctx.getCurrentUser().getOrg(),
                    rctx.getRequiredParam(RequestContext.KICKSTART_ID));
        rctx.getRequest().setAttribute(RequestContext.KICKSTART, ksdata);

        if (!rctx.isSubmitted()) {
            populateNewSet(rctx, getCurrentItemsIterator(ksdata));
        }
    }

    /**
     * Get the Iterator for a Collection of Objects
     * that implement the Identifiable interface.
     * @param ksdata KickstartData to fetch info from
     * @return Iterator containing Identifiable objects.
     */
    protected abstract Iterator getCurrentItemsIterator(KickstartData ksdata);


}
