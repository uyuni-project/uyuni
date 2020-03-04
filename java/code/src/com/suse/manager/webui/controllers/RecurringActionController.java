/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.webui.controllers;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.util.RecurringEventPicker;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction.Type;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.webui.utils.gson.RecurringStateScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for API calls to work with recurring actions.
 */
public class RecurringActionController {

    private static final Logger LOG = Logger.getLogger(RecurringActionController.class);
    private static final Gson GSON = new GsonBuilder().create();

    private RecurringActionController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/recurring-actions",
                withUserPreferences(withCsrfToken(withUser(RecurringActionController::recurringActions))),
                jade);

        get("/manager/api/recurringactions", withUser(RecurringActionController::listAll));
        get("/manager/api/recurringactions/:type/:id", withUser(RecurringActionController::listByEntity));
        post("/manager/api/recurringactions/save", withUser(RecurringActionController::save));
        delete("/manager/api/recurringactions/:id/delete", withUser(RecurringActionController::deleteSchedule));
    }

    /**
     * Handler for the Recurring Actions schedule page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView recurringActions(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "templates/schedule/recurring-actions.jade");
    }

    /**
     * Processes a GET request to get a list of all Recurring Schedules visible to given user
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listAll(Request request, Response response, User user) {
        List<RecurringStateScheduleJson> schedules =
                actionsToJson(RecurringActionManager.listAllRecurringActions(user));

        return json(response, ResultJson.success(schedules));
    }

    /**
     * Processes a GET request to get a list of all Recurring Schedules corresponding to given entity
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listByEntity(Request request, Response response, User user) {
        Type type = Type.valueOf(request.params("type"));
        long id = Long.parseLong(request.params("id"));

        List<? extends RecurringAction> schedules;
        switch (type) {
            case MINION:
                schedules = RecurringActionManager.listMinionRecurringActions(id, user);
                break;
            case GROUP:
                schedules = RecurringActionManager.listGroupRecurringActions(id, user);
                break;
            case ORG:
                schedules = RecurringActionManager.listOrgRecurringActions(id, user);
                break;
            default:
                throw new IllegalStateException("Unsupported type " + type);
        }

        return json(response, ResultJson.success(actionsToJson(schedules)));
    }

    private static List<RecurringStateScheduleJson> actionsToJson(List<? extends RecurringAction> actions) {
        return actions
                .stream()
                .map(a -> actionToJson(a, a.getType()))
                .collect(Collectors.toList());
    }

    private static RecurringStateScheduleJson actionToJson(RecurringAction a, Type targetType) {
        RecurringStateScheduleJson json = new RecurringStateScheduleJson();
        json.setRecurringActionId(a.getId());
        json.setScheduleName(a.getName());

        String cronExpr = a.getCronExpr();
        json.setCron(cronExpr);
        RecurringEventPicker picker = RecurringEventPicker.prepopulatePicker("date", null, null, cronExpr);
        Map<String, String> cronTimes = new HashMap<>();
        cronTimes.put("minute", picker.getMinute());
        cronTimes.put("hour", picker.getHour());
        cronTimes.put("dayOfMonth", picker.getDayOfMonth());
        cronTimes.put("dayOfWeek", picker.getDayOfWeek());

        json.setType(picker.getStatus());
        json.setCronTimes(cronTimes);
        json.setActive(a.isActive());
        json.setTest(a.isTestMode());
        json.setTargetType(targetType.toString());
        json.setTargetId(a.getEntityId());
        json.setCreated(a.getCreated());
        return json;
    }

    /**
     * Creates a new Recurring State Schedule
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String save(Request request, Response response, User user) {
        response.type("application/json");

        List<String> errors = new LinkedList<>();

        RecurringStateScheduleJson json = GSON.fromJson(request.body(), RecurringStateScheduleJson.class);
        RecurringAction action = createOrGetAction(user, json);
        mapJsonToAction(json, action);

        try {
            RecurringActionManager.saveAndSchedule(action, user);
        }
        catch (EntityExistsException e) {
            errors.add("Action with given name already exists.");
        }
        catch (TaskomaticApiException e) {
            errors.add("Error when scheduling the action.");
        }

        if (errors.isEmpty()) {
            return json(response, ResultJson.success());
        }

        return json(response, ResultJson.error(errors));
    }

    private static RecurringAction createOrGetAction(User user, RecurringStateScheduleJson json) {
        if (json.getRecurringActionId() == null) {
            return RecurringActionManager.createRecurringAction(
                    Type.valueOf(json.getTargetType().toUpperCase()),
                    json.getTargetId(),
                    user);
        }
        else {
            return RecurringActionFactory.lookupById(json.getRecurringActionId()).orElseThrow();
        }
    }

    /**
     * Deletes a recurring action
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String deleteSchedule(Request request, Response response, User user) {
        long id = Long.parseLong(request.params("id"));
        Optional<RecurringAction> action = RecurringActionFactory.lookupById(id);
        if (action.isEmpty()) {
            return json(response, ResultJson.error("Schedule with id: " + id + " does not exists"));
        }
        try {
            RecurringActionManager.deleteAndUnschedule(action.get(), user);
        }
        catch (TaskomaticApiException e) {
            return json(response, ResultJson.error("Error when deleting the action"));
        }
        return json(response, ResultJson.success());
    }

    private static void mapJsonToAction(RecurringStateScheduleJson json, RecurringAction action) {
        action.setName(json.getScheduleName());
        action.setActive(json.isActive());
        action.setTestMode(json.isTest());

        String cron = json.getCron();
        if (StringUtils.isBlank(cron)) {
            cron = RecurringEventPicker
                    .prepopulatePicker("date", json.getType(), json.getCronTimes(), null)
                    .getCronEntry();
        }
        action.setCronExpr(cron);
    }
}
