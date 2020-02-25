/**
 * Copyright (c) 2015 SUSE LLC
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.util.RecurringEventPicker;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.utils.gson.RecurringStateScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.Request;
import spark.Response;

/**
 * Controller class providing the backend for API calls to work with recurring actions.
 */
public class RecurringActionController {

    /** Logger */
    private static final Logger LOG = Logger.getLogger(RecurringActionController.class);
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();
    private static final Gson GSON = new GsonBuilder().create();

    private RecurringActionController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public static void initRoutes() {
        get("/manager/api/states/schedules", withUser(RecurringActionController::schedules));
        get("/manager/api/states/schedules/:scheduleId", withUser(RecurringActionController::singleSchedule));
        post("/manager/api/states/schedules/save", withUser(RecurringActionController::createSchedule));
        post("/manager/api/states/schedules/:scheduleId/update", withUser(RecurringActionController::updateSchedule));
        delete("/manager/api/states/schedules/:scheduleId/delete", withUser(RecurringActionController::deleteSchedule));
    }

    /**
     * Processes a GET request to get a list of all Recurring Schedules
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String schedules(Request request, Response response, User user) {
        try {
            List<Map<String, String>> schedules = getSchedules(user);
            return json(response,
                    ResultJson.success(schedules));
        }
        catch (TaskomaticApiException e) {
            return json(response, ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Processes a GET request to get a single Recurring Schedule
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result schedule object
     */
    public static String singleSchedule(Request request, Response response, User user) {
        String scheduleId = request.params("scheduleId");
        try {
            Optional<Map<String, String>> schedule = getSingleSchedule(scheduleId, user);
            if (schedule.isEmpty()) {
                return json(response, ResultJson.error("Schedule not found"));
            }
            return json(response, ResultJson.success(schedule.get()));
        }
        catch (TaskomaticApiException e) {
            return json(response,
                    ResultJson.error(e.getMessage()));
        }
    }

    /**
     * Creates a list of objects from a list of {@link TaskoSchedule} instances
     *
     * @param user the authorized user
     * @return the list of Recurring State Apply Schedules
     */
    private static List<Map<String, String>> getSchedules(User user) throws TaskomaticApiException {
        /* TODO: Check user accessability */
        List<Map> taskoSchedules = TASKOMATIC_API.findActiveSchedulesByBunch(user,
                "recurring-state-apply-bunch");
        return taskoSchedules.stream().map(taskoSchedule -> {
            Map<String, String> schedule = getScheduleDetails(taskoSchedule, user);
            return schedule;
        }).collect(Collectors.toList());
    }

    /**
     * Returns a single schedule with the given scheduleId
     *
     * @param scheduleId the id of the schedule
     * @param user the authorized user
     * @return the result schedule object
     */
    private static Optional<Map<String, String>> getSingleSchedule(String scheduleId, User user)
            throws TaskomaticApiException {
        /* TODO: Create own JSON class for schedule type */
        Map<String, String> schedule = null;
        Map taskoSchedule = TASKOMATIC_API.lookupScheduleById(user, Long.parseLong(scheduleId));
        if (taskoSchedule.get("id") != null) {
            schedule = getScheduleDetails(taskoSchedule, user);
        }
        return Optional.ofNullable(schedule);
    }

    /**
     * Returns a single schedule Object
     *
     * @param taskoSchedule the schedule Object of taskomatic
     * @return the result schedule object
     */
    private static Map<String, String> getScheduleDetails(Map taskoSchedule, User user) {
        Map<String, String> schedule = (Map<String, String>) taskoSchedule.get("data_map");
        String date = new Timestamp(((Date) taskoSchedule.get("active_from")).getTime()).toString();
        String cronExpr = taskoSchedule.get("cron_expr").toString();
        getMinionNamesAndIds(schedule.get("targetType"),
                Long.parseLong(schedule.get("targetId")),
                user).ifPresent(schedule::putAll);
        schedule.put("cron", cronExpr);
        schedule.put("scheduleId", taskoSchedule.get("id").toString());
        schedule.put("scheduleName", taskoSchedule.get("job_label").toString());
        schedule.put("createdAt", date.substring(0, date.indexOf(".")));
        RecurringEventPicker picker = RecurringEventPicker.prepopulatePicker("date", null, null, cronExpr);
        schedule.put("minute", picker.getMinute());
        schedule.put("hour", picker.getHour());
        schedule.put("dayOfMonth", picker.getDayOfMonth());
        schedule.put("dayOfWeek", picker.getDayOfWeek());
        schedule.put("type", picker.getStatus());
        return schedule;
    }

    /**
     * Returns Map containing minion Ids and Names
     *
     * @param targetType type of the target
     * @param targetId Id of the target
     * @param user the authorized user
     * @return the resulting map
     */
    public static Optional<Map<String, String>> getMinionNamesAndIds(String targetType, Long targetId, User user) {
        /* TODO: Simplify acquiring minion Ids and names */
        Map<String, String> minions = new HashMap<>();
        if (targetType.matches("Minion")) {
            List<String> minionName = Collections.singletonList(ServerFactory.lookupById(targetId).getName());
            minions.put("minionIds", Collections.singletonList(targetId).toString());
            minions.put("minionNames", minionName.toString());
        }
        else if (targetType.matches("Group")) {
            ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(targetId, user.getOrg());
            minions.put("groupName", group.getName());
            List<SimpleMinionJson> servers = MinionServerUtils.filterSaltMinions(group.getServers()).map(
                    SimpleMinionJson::fromMinionServer).collect(Collectors.toList()
            );
            minions.put("minionIds", servers.stream().map(
                    SimpleMinionJson::getId).collect(Collectors.toList()).toString()
            );
            minions.put("minionNames", servers.stream().map(
                    SimpleMinionJson::getName).collect(Collectors.toList()).toString()
            );
        }
        else if (targetType.matches("Organization")) {
            Set<Long> systems = Arrays.stream(new SystemHandler().listSystems(user)).map(
                    system -> ((SystemOverview) system).getId()).collect(Collectors.toSet()
            );
            List<SimpleMinionJson> servers = MinionServerUtils.filterSaltMinions(ServerFactory.lookupByIdsAndOrg(
                    systems, user.getOrg())).map(SimpleMinionJson::fromMinionServer).collect(Collectors.toList()
            );
            minions.put("minionIds", servers.stream().map(
                    SimpleMinionJson::getId).collect(Collectors.toList()).toString()
            );
            minions.put("minionNames", servers.stream().map(
                    SimpleMinionJson::getName).collect(Collectors.toList()).toString()
            );
        }
        else {
            minions = null;
        }
        return Optional.ofNullable(minions);
    }

    /**
     * Creates a new Recurring State Schedule
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String createSchedule(Request request, Response response, User user) {
        response.type("application/json");

        List<String> errors = new LinkedList<>();

        RecurringStateScheduleJson json = GSON.fromJson(request.body(),
                RecurringStateScheduleJson.class);
        try {
            String scheduleName = json.getScheduleName();
            if (scheduleName != null && TASKOMATIC_API.satScheduleActive(scheduleName, user)) {
                errors.add("Schedule Label already in use.");
            }
            else {
                errors.addAll(saveSchedule(json, user));
            }
        }
        catch (TaskomaticApiException e) {
            errors.add(e.getMessage());
        }

        if (errors.isEmpty()) {
            return json(response, ResultJson.success());
        }
        return json(response, ResultJson.error(errors));
    }

    /**
     * Updates an existing Recurring State Schedule
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String updateSchedule(Request request, Response response, User user) {
        response.type("application/json");

        List<String> errors = new LinkedList<>();

        String scheduleId = request.params("scheduleId");
        try {
            if (getSingleSchedule(scheduleId, user).isEmpty()) {
                errors.add("Schedule not found.");
            }
            else {
                RecurringStateScheduleJson json = GSON.fromJson(request.body(),
                        RecurringStateScheduleJson.class);
                errors.addAll(saveSchedule(json, user));
            }
        }
        catch (TaskomaticApiException e) {
            errors.add(e.getMessage());
        }

        if (errors.isEmpty()) {
            return json(response, ResultJson.success());
        }
        return json(response, ResultJson.error(errors));
    }

    /**
     * Saves a Recurring State Schedule
     *
     * @param json the RecurringStateScheduleJson Object
     * @return String List containing errors
     */
    private static List<String> saveSchedule(RecurringStateScheduleJson json, User user) {
        List<String> errors = new LinkedList<>();

        String scheduleName = json.getScheduleName();
        String type = json.getType();
        Map<String, String> cronTimes = json.getCronTimes();
        String cron = json.getCron();

        Map<String, String> params = new HashMap<>();
        params.put("user_id", user.getId().toString());
        params.put("active", json.isActive() ? "true" : "false");
        params.put("targetType", json.getTargetType());
        params.put("targetId", json.getTargetId().toString());
        params.put("test", json.isTest() ? "true" : "false");

        if (StringUtils.isEmpty(scheduleName)) {
            errors.add("Schedule Name must be specified.");
        }

        if (errors.isEmpty()) {
            try {
                if (cron.isEmpty()) {
                    RecurringEventPicker picker = RecurringEventPicker.prepopulatePicker("date", type, cronTimes, null);
                    cron = picker.getCronEntry();
                }
                TASKOMATIC_API.scheduleSatBunch(user, scheduleName, "recurring-state-apply-bunch", cron, params);
            }
            catch (TaskomaticApiException e) {
                if (e.getMessage().contains("InvalidParamException")) {
                    if (e.getMessage().contains("Cron trigger")) {
                        errors.add("Invalid Cron expression.");
                    }
                    else {
                        errors.add("Schedule label already in use.");
                    }
                }
                else {
                    errors.add("Taskomatic is down.");
                }
            }
        }
        return errors;
    }

    /**
     * Processes a DELETE request to delete a Recurring State Schedule
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String deleteSchedule(Request request, Response response, User user) {
        String scheduleId = request.params("scheduleId");
        try {
            Optional<Map<String, String>> schedule = getSingleSchedule(scheduleId, user);
            if (schedule.isEmpty()) {
                return json(response, ResultJson.error("Schedule not found."));
            }
            String scheduleName = schedule.get().get("scheduleName");
            TASKOMATIC_API.unscheduleSatTask(scheduleName, user);
        }
        catch (TaskomaticApiException e) {
            return json(response, ResultJson.error(e.getMessage()));
        }
        return json(response, ResultJson.success());
    }
}
