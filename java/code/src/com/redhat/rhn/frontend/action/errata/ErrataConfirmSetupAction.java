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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.action.errata;

import static com.redhat.rhn.domain.action.ActionFactory.TYPE_ERRATA;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.MaintenanceWindowsAware;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.MaintenanceWindowHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.struts.RhnListAction;
import com.redhat.rhn.manager.errata.ErrataManager;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ErrataConfirmSetupAction
 */
public class ErrataConfirmSetupAction extends RhnListAction implements MaintenanceWindowsAware {
    /** {@inheritDoc} */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        RequestContext requestContext = new RequestContext(request);

        User user = requestContext.getCurrentUser();
        PageControl pc = new PageControl();

        clampListBounds(pc, request, user);

        Errata errata = requestContext.lookupErratum();
        DataResult dr = ErrataManager.relevantSystemsInSet(user, SetLabels.AFFECTED_SYSTEMS_LIST, errata.getId(), pc);

        //Setup the datepicker widget
        DatePicker picker = getStrutsDelegate().prepopulateDatePicker(request,
                (DynaActionForm)formIn, "date", DatePicker.YEAR_RANGE_POSITIVE);

        populateMaintenanceWindows(request, getSystemIds(dr));

        //Setup the Action Chain widget
        ActionChainHelper.prepopulateActionChains(request);

        request.setAttribute("date", picker);
        request.setAttribute(RequestContext.PAGE_LIST, dr);
        request.setAttribute("errata", errata);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private Set<Long> getSystemIds(DataResult<SystemOverview> dr) {
        return dr.stream()
                .map(dto -> dto.getId())
                .collect(Collectors.toSet());
    }

    @Override
    public void populateMaintenanceWindows(HttpServletRequest request, Set<Long> systemIds) {
        if (TYPE_ERRATA.isMaintenancemodeOnly()) {
            MaintenanceWindowHelper.prepopulateMaintenanceWindows(request, systemIds);
        }
    }
}
