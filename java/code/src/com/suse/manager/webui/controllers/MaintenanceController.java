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
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.domain.user.User;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.webui.utils.gson.MaintenanceScheduleJson;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for API calls to work with maintenance windows.
 */
public class MaintenanceController {

    private static final Logger LOG = Logger.getLogger(MaintenanceController.class);
    private static final Gson GSON = new GsonBuilder().create();
    private static final MaintenanceManager mm = MaintenanceManager.instance();

    private MaintenanceController() { }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     *
     * @param jade the template engine
     */
    public static void initRoutes(JadeTemplateEngine jade) {
        get("/manager/schedule/maintenance-schedules",
                withUserPreferences(withCsrfToken(withUser(MaintenanceController::maintenanceSchedules))),
                jade);
        get("/manager/api/maintenance", withUser(MaintenanceController::list));
        post("/manager/api/maintenance/save", withUser(MaintenanceController::save));
        Spark.delete("/manager/api/maintenance/:id/delete", withUser(MaintenanceController::delete));
    }

    /**
     * Handler for the Maintenance Schedules page.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return the ModelAndView object to render the page
     */
    public static ModelAndView maintenanceSchedules(Request request, Response response, User user) {
        return new ModelAndView(new HashMap<>(), "templates/schedule/maintenance-schedules.jade");
    }

    public static String list(Request request, Response response, User user) {
        /* TODO: Implement function to retrieve all maintenance schedules the user has access to */
        List<MaintenanceSchedule> schedules = mm.listScheduleNamesByUser(user).stream().map(
                name -> mm.lookupMaintenanceScheduleByUserAndName(user, name).get()).collect(Collectors.toList());
        return json(response, schedulesToJson(schedules));
    }

    public static String save(Request request, Response response, User user) {
        return "";
    }

    public static String delete(Request request, Response response, User user) {
        return  "";
    }

    private static List<MaintenanceScheduleJson> schedulesToJson(List<MaintenanceSchedule> schedules) {
        return schedules.stream().map(MaintenanceController::scheduleToJson).collect(Collectors.toList());
    }

    private static MaintenanceScheduleJson scheduleToJson(MaintenanceSchedule schedule) {
        MaintenanceScheduleJson json = new MaintenanceScheduleJson();

        json.setId(schedule.getId());
        json.setName(schedule.getName());
        json.setScheduleType(schedule.getScheduleType());

        Optional<MaintenanceCalendar> calendar = schedule.getCalendarOpt();
        calendar.ifPresent(json::setCalendar);

        return json;
    }
}