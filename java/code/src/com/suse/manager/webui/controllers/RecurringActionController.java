/*
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.notFound;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.RecurringEventPicker;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction.TargetType;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.manager.recurringactions.StateConfigFactory;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.maintenance.NotInMaintenanceModeException;
import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.RecurringActionDetailsDto;
import com.suse.manager.webui.utils.gson.RecurringActionScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import com.suse.manager.webui.utils.gson.StateConfigJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.template.jade.JadeTemplateEngine;

/**
 * Controller class providing the backend for API calls to work with recurring actions.
 */
public class RecurringActionController {

    private static final Logger LOG = LogManager.getLogger(RecurringActionController.class);
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

        get("/manager/api/recurringactions", asJson(withUser(RecurringActionController::listAll)));
        get("/manager/api/recurringactions/targets/:type/:id", asJson(withUser(RecurringActionController::getTargets)));
        get("/manager/api/recurringactions/:id/details", asJson(withUser(RecurringActionController::getDetails)));
        get("/manager/api/recurringactions/:type/:id", asJson(withUser(RecurringActionController::listByEntity)));
        get("/manager/api/recurringactions/states", asJson(withUser(RecurringActionController::getStatesConfig)));
        post("/manager/api/recurringactions/save", asJson(withUser(RecurringActionController::save)));
        post("/manager/api/recurringactions/custom/execute",
                asJson(withUser(RecurringActionController::executeCustom)));
        delete("/manager/api/recurringactions/:id/delete", asJson(withUser(RecurringActionController::deleteSchedule)));

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
        PageControlHelper pageHelper = new PageControlHelper(request, "scheduleName");
        PageControl pc = pageHelper.getPageControl();
        DataResult<RecurringActionScheduleJson> schedules =
                RecurringActionManager.listAllRecurringActions(user, pc, PagedSqlQueryBuilder::parseFilterAsText);
        return json(response, new PagedDataResultJson<>(schedules, schedules.getTotalSize(), Collections.emptySet()),
                new TypeToken<>() { });
    }

    /**
     * Processes a GET request to get a list of all the members of an entity
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String getTargets(Request request, Response response, User user) {
        TargetType type = TargetType.valueOf(request.params("type"));
        Long id = Long.parseLong(request.params("id"));

        PageControlHelper pageHelper = new PageControlHelper(request, "name");
        PageControl pc = pageHelper.getPageControl();
        DataResult<SimpleMinionJson> members;

        if ("id".equals(pageHelper.getFunction())) {
            pc.setStart(1);
            pc.setPageSize(0);

            members = RecurringActionManager.listEntityMembers(
                    type, id, user, pc, PagedSqlQueryBuilder::parseFilterAsText);
            return json(response, members.stream()
                    .map(SimpleMinionJson::getId)
                    .collect(Collectors.toList()));
        }

        members = RecurringActionManager.listEntityMembers(
                type, id, user, pc, PagedSqlQueryBuilder::parseFilterAsText);
        return json(response, new PagedDataResultJson<>(members, members.getTotalSize(), Collections.emptySet()));
    }

    /**
     * Processes a GET request to get the details of a recurring action based on its id.
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return JSON representing the action details object
     */
    public static String getDetails(Request request, Response response, User user) {
        long id = Long.parseLong(request.params("id"));
        Optional<RecurringAction> action = RecurringActionManager.find(id);
        if (action.isEmpty()) {
            return notFound(response, "Action " + id + " not found");
        }
        return json(response, actionToDetailsDto(action.get()), new TypeToken<>() { });
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
        TargetType type = TargetType.valueOf(request.params("type"));
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

        return json(response, actionsToJson(schedules), new TypeToken<>() { });
    }

    /**
     * Get a list of all available internal states and config channels as well as the current assignments for
     * a given recurring states action
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return JSON result of the API call
     */
    public static String getStatesConfig(Request request, Response response, User user) {
        String target = request.queryParams("target");
        String idParam = request.queryParams("id");
        String targetLowerCase = target != null ? target.toLowerCase() : "";

        Set<StateConfigJson> result = new HashSet<>(); // use a set to avoid duplicates
        if (idParam != null) {
            Long id = Long.parseLong(idParam);
            Optional<RecurringAction> action = RecurringActionManager.find(id);
            if (action.isEmpty()) {
                return notFound(response, "Action " + id + " not found");
            }
            if (!(action.get().getRecurringActionType() instanceof RecurringState)) {
                Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(
                        LocalizationService.getInstance().getMessage("recurring_action_invalid_action_type"))));
            }
            result.addAll(StateConfigJson.listOrderedStates(
                    ((RecurringState)action.get().getRecurringActionType()).getStateConfig())
                    .stream().filter(config -> config.getName().toLowerCase().contains(targetLowerCase))
                    .collect(Collectors.toList())
            );
        }

        // Add available config channels
        ConfigurationManager.getInstance().listGlobalChannels(user).stream()
                .filter(s -> s.getName().toLowerCase().contains(targetLowerCase))
                .map(StateConfigJson::new)
                .forEach(result::add);

        // Add internal states
        RecurringActionFactory.listInternalStates().stream()
                .filter(s -> s.getName().toLowerCase().contains(targetLowerCase))
                .map(StateConfigJson::new)
                .forEach(result::add);

        return json(response, result, new TypeToken<>() { });
    }

    private static List<RecurringActionScheduleJson> actionsToJson(List<? extends RecurringAction> actions) {
        return actions
                .stream()
                .map(RecurringActionScheduleJson::new)
                .collect(Collectors.toList());
    }

    private static RecurringActionDetailsDto actionToDetailsDto(RecurringAction action) {
        RecurringEventPicker picker = RecurringEventPicker.prepopulatePicker("date", null, null, action.getCronExpr());
        Map<String, String> cronTimes = new HashMap<>();
        cronTimes.put("minute", picker.getMinute());
        cronTimes.put("hour", picker.getHour());
        cronTimes.put("dayOfMonth", picker.getDayOfMonth());
        cronTimes.put("dayOfWeek", picker.getDayOfWeek());
        RecurringActionDetailsDto dto = new RecurringActionDetailsDto();
        dto.setCreated(action.getCreated());
        dto.setCreatorLogin(action.getCreator().getLogin());
        dto.setType(picker.getStatus());
        dto.setCronTimes(cronTimes);
        if (RecurringActionType.ActionType.HIGHSTATE.equals(action.getActionType())) {
            dto.setTest(((RecurringHighstate) action.getRecurringActionType()).isTestMode());
        }
        else if (RecurringActionType.ActionType.CUSTOMSTATE.equals(action.getActionType())) {
            dto.setTest(((RecurringState) action.getRecurringActionType()).isTestMode());
            dto.setStates(StateConfigJson.listOrderedStates(
                    ((RecurringState) action.getRecurringActionType()).getStateConfig()));
        }
        return dto;
    }


    /**
     * Creates a new Recurring Action Schedule
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String save(Request request, Response response, User user) {
        List<String> errors = new LinkedList<>();

        RecurringActionScheduleJson json = GSON.fromJson(request.body(), RecurringActionScheduleJson.class);

        try {
            RecurringAction action = createOrGetAction(user, json);
            HibernateFactory.getSession().evict(action); // entity -> detached, prevent hibernate flushes
            mapJsonToAction(json, action);
            RecurringActionManager.saveAndSchedule(action, user);
        }
        catch (ValidatorException e) {
            errors.add(e.getMessage()); // we assume the messages are already localized
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
        catch (TaskomaticApiException e) {
            LOG.error("Rolling back transaction because of Taskomatic exception", e);
            HibernateFactory.rollbackTransaction();
            String errMsg = LocalizationService.getInstance().getMessage("recurring_action.taskomatic_error");
            Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE, GSON.toJson(ResultJson.error(errMsg)));
        }

        return json(response, ResultJson.success(), new TypeToken<>() { });
    }

    /**
     * Schedules a one-shot custom state execution
     *
     * @param request the request
     * @param response the response
     * @param user the user
     * @return string containing the json response
     */
    public static String executeCustom(Request request, Response response, User user) {
        RecurringActionScheduleJson json = GSON.fromJson(request.body(), RecurringActionScheduleJson.class);
        Set<RecurringStateConfig> configs = getStateConfigFromJson(json.getDetails().getStates(), user);
        List<Long> minionIds = json.getMemberIds();

        List<String> mods = configs.stream()
                .sorted(Comparator.comparingLong(RecurringStateConfig::getPosition))
                .map(RecurringStateConfig::getStateName)
                .collect(Collectors.toList());

        try {
            Action a = ActionManager.scheduleApplyStates(user,
                    minionIds, mods,
                    Optional.empty(),
                    new Date(),
                    Optional.of(json.getDetails().isTest()),
                    true);
            ActionFactory.save(a);
            new TaskomaticApi().scheduleActionExecution(a);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Rolling back transaction because of Taskomatic exception", e);
            HibernateFactory.rollbackTransaction();
            String errMsg = LocalizationService.getInstance().getMessage("recurring_action.taskomatic_error");
            Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE, GSON.toJson(ResultJson.error(errMsg)));
        }
        catch (NotInMaintenanceModeException e) {
            String errMsg = LocalizationService.getInstance().getMessage("recurring_action.not_in_maint_mode");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(errMsg)));
        }
        return json(response, ResultJson.success());
    }

    private static RecurringAction createOrGetAction(User user, RecurringActionScheduleJson json) {
        if (json.getRecurringActionId() == null) {
            RecurringAction.TargetType type = RecurringAction.TargetType.valueOf(json.getTargetType().toUpperCase());
            return RecurringActionManager.createRecurringAction(type, json.getActionType(), json.getTargetId(), user);
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
            Spark.halt(HttpStatus.SC_BAD_REQUEST, "Schedule with id: " + id + " does not exists");
        }
        try {
            RecurringActionManager.deleteAndUnschedule(action.get(), user);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Rolling back transaction because of Taskomatic exception", e);
            HibernateFactory.rollbackTransaction();
            // Report just code. It seems that body in the DELETE response is not sent correctly
            Spark.halt(HttpStatus.SC_SERVICE_UNAVAILABLE);
        }
        return result(response, ResultJson.success(), new TypeToken<>() { });
    }

    private static Set<RecurringStateConfig> getStateConfigFromJson(Set<StateConfigJson> json, User user) {
        if (json == null) {
            throw new ValidatorException(LocalizationService.getInstance().getMessage(
                    "recurring_action.empty_states_config"));
        }
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        StateConfigFactory stateConfigFactory = new StateConfigFactory();
        Set<RecurringStateConfig> stateConfig = new HashSet<>();
        json.forEach(config -> {
            String type = config.getType();
            if (type.equals("internal_state")) {
                RecurringActionFactory.lookupInternalStateByName(config.getName()).ifPresent(state ->
                        stateConfig.add(stateConfigFactory.getRecurringState(state, config.getPosition().longValue())));
            }
            else {
                ConfigChannel channel = configManager.lookupConfigChannel(user, config.getId());
                if (channel != null) {
                    stateConfig.add(stateConfigFactory.getRecurringState(channel, config.getPosition().longValue()));
                }
            }
        });
        return stateConfig;
    }

    private static void mapJsonToAction(RecurringActionScheduleJson json, RecurringAction action) {
        action.setName(json.getScheduleName());
        action.setActive(json.isActive());

        RecurringActionDetailsDto details = json.getDetails();
        if (details == null) {
            return;
        }

        if (action.getRecurringActionType() instanceof RecurringHighstate) {
            ((RecurringHighstate) action.getRecurringActionType()).setTestMode(details.isTest());
        }
        else if (action.getRecurringActionType() instanceof RecurringState) {
            RecurringState stateType = (RecurringState) action.getRecurringActionType();
            stateType.setTestMode(details.isTest());
            if (json.getRecurringActionId() == null ||
                    (json.getRecurringActionId() != null && details.getStates() != null)) {
                Set<RecurringStateConfig> newConfig = getStateConfigFromJson(details.getStates(), action.getCreator());
                ((RecurringState) action.getRecurringActionType()).saveStateConfig(newConfig);
            }
        }

        String cron = json.getCron();
        if (StringUtils.isBlank(cron)) {
            cron = RecurringEventPicker
                    .prepopulatePicker("date", details.getType(), details.getCronTimes(), null)
                    .getCronEntry();
        }
        action.setCronExpr(cron);
    }
}
