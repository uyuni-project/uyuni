/*
 * Copyright (c) 2014 Red Hat, Inc.
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.common.util.DatePicker;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.MaintenanceWindowsAware;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.events.SsmSystemRebootEvent;
import com.redhat.rhn.frontend.struts.ActionChainHelper;
import com.redhat.rhn.frontend.struts.MaintenanceWindowHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListRhnSetHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.Listable;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.ssm.SsmManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Confirm reboot of given systems
 */
public class RebootSystemConfirmAction extends RhnAction
    implements Listable<SystemOverview>, MaintenanceWindowsAware {

    /** Logger instance */
    private static Logger log = Logger.getLogger(RebootSystemConfirmAction.class);

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
        helper.setDataSetName(RequestContext.PAGE_LIST);
        helper.setListName("systemList");
        helper.execute();
        if (helper.isDispatched()) {
            return handleDispatch(mapping, (DynaActionForm) formIn, request);
        }

        getStrutsDelegate().prepopulateDatePicker(request,
                (DynaActionForm) formIn, "date", DatePicker.YEAR_RANGE_POSITIVE);
        ActionChainHelper.prepopulateActionChains(request);

        Set<Long> systemIds = new HashSet<>(SsmManager.listServerIds(new RequestContext(request).getCurrentUser()));
        populateMaintenanceWindows(request, systemIds);

        return mapping.findForward(RhnHelper.DEFAULT_FORWARD);
    }

    /** {@inheritDoc} */
    protected RhnSetDecl getSetDecl() {
        return RhnSetDecl.SSM_SYSTEMS_REBOOT;
    }

    private ActionForward handleDispatch(
            ActionMapping mapping,
            DynaActionForm formIn,
            HttpServletRequest request) {

        RequestContext context = new RequestContext(request);
        User user = context.getCurrentUser();
        RhnSet set = getSetDecl().get(user);

        Date earliest = getStrutsDelegate().readScheduleDate(formIn,
                "date", DatePicker.YEAR_RANGE_POSITIVE);
        ActionChain actionChain = ActionChainHelper.readActionChain(formIn, user);

        // Is taskomatic running?
        if (!TASKOMATIC_API.isRunning()) {
            log.error("Cannot schedule action: Taskomatic is not running");
            ActionErrors errors = new ActionErrors();
            getStrutsDelegate().addError("taskscheduler.down", errors);
            getStrutsDelegate().saveMessages(request, errors);
            return mapping.findForward(RhnHelper.CONFIRM_FORWARD);
        }

        MessageQueue.publish(new SsmSystemRebootEvent(user.getId(), earliest, actionChain,
            set.getElementValues()));

        int n = set.size();

        String messageKeySuffix = n == 1 ? "singular" : "plural";
        LocalizationService ls = LocalizationService.getInstance();

        if (actionChain == null) {
            createMessage(request, "ssm.misc.reboot.message.success." + messageKeySuffix,
                    new String[] {ls.formatNumber(n, request.getLocale()),
                            ls.formatDate(earliest, request.getLocale())});
        }
        else {
            createMessage(request, "ssm.misc.reboot.message.queued." + messageKeySuffix,
                    new String[] {ls.formatNumber(n, request.getLocale()),
                            actionChain.getId().toString(), actionChain.getLabel()});
        }

        set.clear();
        RhnSetManager.store(set);

        return mapping.findForward(RhnHelper.CONFIRM_FORWARD);
    }

    /** {@inheritDoc} */
    public List<SystemOverview> getResult(RequestContext context) {
        return SystemManager.inSet(context.getCurrentUser(),
              getSetDecl().getLabel());
    }

    @Override
    public void populateMaintenanceWindows(HttpServletRequest request, Set<Long> systemIds) {
        if (ActionFactory.TYPE_REBOOT.isMaintenancemodeOnly()) {
            MaintenanceWindowHelper.prepopulateMaintenanceWindows(request, systemIds);
        }
    }
}
