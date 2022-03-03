/*
 * Copyright (c) 2014--2015 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.ssm;

import static com.redhat.rhn.common.util.DatePicker.YEAR_RANGE_POSITIVE;
import static com.redhat.rhn.domain.action.ActionFactory.TYPE_ERRATA;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.MaintenanceWindowsAware;
import com.redhat.rhn.frontend.action.SetLabels;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.events.SsmErrataEvent;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.MaintenanceWindowHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Confirm application of errata to systems in SSM.
 */
public class ErrataListConfirmAction extends RhnAction implements
        Listable<ErrataOverview>, MaintenanceWindowsAware {

    /** Logger instance */
    private static Logger log = Logger.getLogger(ErrataListConfirmAction.class);

    /** Taskomatic API instance */
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {


        ListRhnSetHelper helper = new ListRhnSetHelper(this, request, getSetDecl());
        helper.setWillClearSet(false);
        helper.setListName("errataList");
        helper.execute();

        if (helper.isDispatched()) {
            return handleDispatch(mapping, (DynaActionForm) formIn, request);
        }

        getStrutsDelegate().prepopulateDatePicker(request, (DynaActionForm) formIn, "date", YEAR_RANGE_POSITIVE);

        Set<Long> systemIds = new HashSet<>(getSystemIds(request));
        populateMaintenanceWindows(request, systemIds);

        ActionChainHelper.prepopulateActionChains(request);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    private List<Long> getSystemIds(HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        return SystemManager.inSet(user, SetLabels.SYSTEM_LIST).stream()
                .map(SystemOverview::getId)
                .collect(Collectors.toList());
    }

    private ActionForward handleDispatch(
            ActionMapping mapping,
            DynaActionForm formIn,
            HttpServletRequest request) {
        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();

        Date earliest = getStrutsDelegate().readScheduleDate(formIn, "date", YEAR_RANGE_POSITIVE);
        ActionChain actionChain = ActionChainHelper.readActionChain(formIn, user);

        // Is taskomatic running?
        if (!TASKOMATIC_API.isRunning()) {
            log.error("Cannot schedule action: Taskomatic is not running");
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError("taskscheduler.down", errors);
            getStrutsDelegate().saveMessages(request, errors);
            return mapping.findForward(RhnHelper.CONFIRM_FORWARD);
        }

        List<Long> serverIds = getSystemIds(request);

        RhnSet erratas = getSetDecl().get(context.getCurrentUser());
        List<Long> errataIds = new ArrayList<>(erratas.size());
        errataIds.addAll(erratas.getElementValues());

        MessageQueue.publish(new SsmErrataEvent(
                user.getId(),
                earliest,
                actionChain,
                errataIds,
                serverIds)
        );

        if (actionChain == null) {
            createMessage(
                request,
                "ssm.errata.message.scheduled",
                new String[] {LocalizationService.getInstance().formatDate(earliest,
                    request.getLocale())});
        }
        else {
            createMessage(request, "ssm.errata.message.queued", new String[] {
                actionChain.getId().toString(), actionChain.getLabel()});
        }

        RhnSet set = getSetDecl().get(context.getCurrentUser());
        set.clear();
        RhnSetManager.store(set);

        return mapping.findForward(RhnHelper.CONFIRM_FORWARD);
    }

    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.ERRATA;
    }

    /** {@inheritDoc} */
    public List<ErrataOverview> getResult(RequestContext context) {
        return ErrataManager.lookupSelectedErrataInSystemSet(context.getCurrentUser(),
                getSetDecl().getLabel());
    }

    @Override
    public void populateMaintenanceWindows(HttpServletRequest request, Set<Long> systemIds) {
        if (TYPE_ERRATA.isMaintenancemodeOnly()) {
            MaintenanceWindowHelper.prepopulateMaintenanceWindows(request, systemIds);
        }
    }
}
