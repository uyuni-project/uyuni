/*
 * Copyright (c) 2011--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.tasko;

import com.redhat.rhn.common.util.RecurringEventPicker;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.frontend.struts.RhnHelper;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * ScheduleDetailAction
 */
public class ScheduleDetailAction extends RhnAction {

    static final String SCHEDULE_NAME_REGEX = "^[a-z\\d][a-z\\d\\-\\.\\_]*$";

    /** {@inheritDoc} */
    @Override
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm formIn,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        DynaActionForm form = (DynaActionForm)formIn;
        Map<String, Object> params = makeParamMap(request);
        RequestContext ctx = new RequestContext(request);
        User loggedInUser = ctx.getCurrentUser();
        Long scheduleId = ctx.getParamAsLong(("schid"));

        if (ctx.hasParam("schid")) {
            params.put("schid", scheduleId);
            Map<String, Object> schedule = new HashMap<>();
            try {
                schedule = new TaskomaticApi().lookupScheduleById(loggedInUser, scheduleId);
            }
            catch (TaskomaticApiException e) {
                createErrorMessage(request,
                        "repos.jsp.message.taskomaticdown", null);
            }
            String scheduleName = (String) schedule.get("job_label");
            String bunchName = (String) schedule.get("bunch");
            request.setAttribute("schedulename", scheduleName);
            form.set("schedulename", scheduleName);
            request.setAttribute("bunch", bunchName);
            boolean active = isActive(schedule);
            request.setAttribute("active", active);
            if (!active) {
                request.setAttribute("activetill", schedule.get("active_till"));
            }
        }

        prepDropdowns(ctx);
        if (!isSubmitted(form)) {
            setupForm(request, form);
            return getStrutsDelegate().forwardParams(
                    mapping.findForward(RhnHelper.DEFAULT_FORWARD),
                    request.getParameterMap());
        }

        RecurringEventPicker picker = RecurringEventPicker.prepopulatePicker(
                request, "date", null);

        if ((picker.isDisabled() || StringUtils.isEmpty(picker.getCronEntry())) &&
                !ctx.hasParam("activate_button")) {
            if (scheduleId == null) {
                prepDropdowns(ctx);
                createErrorMessage(request, "message.scheduledisabled", null);

                return getStrutsDelegate().forwardParams(
                        mapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
            }
            return getStrutsDelegate().forwardParams(
                    mapping.findForward("disable"), params);
        }

        String bunchName = form.getString("bunch");
        String scheduleName = form.getString("schedulename");
        if (!Pattern.compile(SCHEDULE_NAME_REGEX).matcher(scheduleName).find()) {
            createErrorMessage(request, "schedule.jsp.schedulenameregex", null);

            return getStrutsDelegate().forwardParams(
                    mapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
        }

        try {
            TaskomaticApi tapi = new TaskomaticApi();
            // check, whether there's not already a schedule of this name
            if (scheduleId == null && tapi.satScheduleActive(scheduleName, loggedInUser)) {
                createErrorMessage(request,
                        "schedule.jsp.schedulenameinuse", scheduleName);
                return getStrutsDelegate().forwardParams(
                        mapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
            }
            if (ctx.hasParam("create_button")) {
                // create new schedule
                tapi.scheduleSatBunch(loggedInUser, scheduleName, bunchName, picker.getCronEntry());
            }
            else if (ctx.hasParam("activate_button")) {
                // activate existing schedule
                tapi.activateSatBunch(loggedInUser, scheduleName, bunchName);
            }
            else {
                // update the schedule
                tapi.updateSatBunch(loggedInUser, scheduleName, bunchName, picker.getCronEntry());
            }
            // check, whether it was created
            Map<String, Object> schedule = tapi.lookupScheduleByBunchAndLabel(loggedInUser, bunchName,
                    scheduleName);
            if (schedule != null) {
                if (ctx.hasParam("create_button")) {
                    createSuccessMessage(request, "message.schedulecreated", scheduleName);
                }
                else if (ctx.hasParam("activate_button")) {
                    createSuccessMessage(request, "message.scheduleactivated", scheduleName);
                }
                else {
                    createSuccessMessage(request, "message.scheduleupdated", scheduleName);
                }
                params.put("schid", schedule.get("id"));
                return getStrutsDelegate().forwardParams(
                        mapping.findForward("success"), params);
            }
            // something went wrong
            tapi.unscheduleSatTask(scheduleName, loggedInUser);
            createErrorMessage(request,
                    "schedule.jsp.schedulefailed", scheduleName);
        }
        catch (TaskomaticApiException e) {
            if (e.getMessage().contains("InvalidParamException")) {
                if (e.getMessage().contains("Cron trigger")) {
                    createErrorMessage(request,
                            "repos.jsp.message.invalidcron", picker.getCronEntry());
                }
                else {
                    createErrorMessage(request,
                            "schedule.jsp.schedulefailed", scheduleName);
                }
            }
            else {
                createErrorMessage(request,
                        "repos.jsp.message.taskomaticdown", null);
            }
        }
        return getStrutsDelegate().forwardParams(
                mapping.findForward(RhnHelper.DEFAULT_FORWARD), params);
    }

    private Boolean isActive(Map<String, Object> schedule) {
        Date till = (Date) schedule.get("active_till");
        if (till == null) {
            return Boolean.TRUE;
        }
        Date now = new Date();
        return now.before(till);
    }

    private void setupForm(HttpServletRequest request, DynaActionForm form) {
        RequestContext ctx = new RequestContext(request);
        User loggedInUser = ctx.getCurrentUser();
        Long schid = ctx.getParamAsLong("schid");

        if (schid != null) {
            try {
                TaskomaticApi tapi = new TaskomaticApi();
                Map<String, Object> schedule = tapi.lookupScheduleById(loggedInUser, schid);
                String scheduleName = (String) schedule.get("job_label");
                if (scheduleName.equals("payg-dimension-computation-default")) {
                    // not modifiable
                    return;
                }
                String bunchName = (String) schedule.get("bunch");
                request.setAttribute("schedulename", scheduleName);
                form.set("schedulename", scheduleName);
                request.setAttribute("bunch", bunchName);
                Map<String, Object> bunch = tapi.lookupBunchByName(loggedInUser, bunchName);
                request.setAttribute("bunchdescription", bunch.get("description"));
                RecurringEventPicker.prepopulatePicker(request, "date",
                        (String) schedule.get(("cron_expr")));
            }
            catch (TaskomaticApiException e) {
                createErrorMessage(request,
                        "repos.jsp.message.taskomaticdown", null);
            }
        }
        else {
            RecurringEventPicker.prepopulatePicker(request, "date", null);
        }
    }

    private void prepDropdowns(RequestContext ctx) {
        User loggedInUser = ctx.getCurrentUser();
        // populate parent base channels
        List<Map<String, String>> dropDown = new ArrayList<>();
        try {
            List<Map<String, Object>> bunches = new TaskomaticApi().listSatBunchSchedules(loggedInUser);
            // Since recurring states have their own place in the webUI we don't
            // want them to show up in the Task Schedules UI
            bunches.removeIf(bunch -> bunch.get("name").equals("recurring-action-executor-bunch"));

            for (Map<String, Object> b : bunches) {
                addOption(dropDown, (String)b.get("name"), (String)b.get("name"));
            }
        }
        catch (TaskomaticApiException e) {
            // do not create error message, it was created before
        }
        ctx.getRequest().setAttribute("bunches", dropDown);
    }

    private void addOption(List<Map<String, String>> options, String key, String value) {
        Map<String, String> selection = new HashMap<>();
        selection.put("label", key);
        selection.put("value", value);
        options.add(selection);
    }
}
