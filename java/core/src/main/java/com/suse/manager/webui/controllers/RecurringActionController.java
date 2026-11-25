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
 * SPDX-License-Identifier: GPL-2.0-only
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
import static com.suse.manager.webui.utils.SparkApplicationHelper.success;
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
import com.redhat.rhn.domain.audit.ScapFactory;
import com.redhat.rhn.domain.audit.ScapPolicy;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction.TargetType;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringContentLifecycleType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
import com.redhat.rhn.domain.recurringactions.type.RecurringScapPolicy;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.manager.recurringactions.StateConfigFactory;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.maintenance.NotInMaintenanceModeException;
import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.ClmBuildHistoryDto;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.RecurringActionDetailsDto;
import com.suse.manager.webui.utils.gson.RecurringActionScheduleJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.ScapPolicyJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;
import com.suse.manager.webui.utils.gson.StateConfigJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;

import java.text.ParseException;
import java.time.Instant;
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

import jakarta.persistence.PersistenceException;
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
    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private RecurringActionController() { }

    /**
     * Set taskomatic API instance.
     *
     * @param taskomaticApiIn taskomatic API instance
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

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
        get("/manager/api/recurringactions/clm/projects",
                asJson(withUser(RecurringActionController::getClmProjects)));
        get("/manager/api/recurringactions/:type/:id", asJson(withUser(RecurringActionController::listByEntity)));
        get("/manager/api/recurringactions/states", asJson(withUser(RecurringActionController::getStatesConfig)));
        get("/manager/api/recurringactions/policies", asJson(withUser(RecurringActionController::listScapPolicies)));
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
        Map<String, Object> data = new HashMap<>();
        data.put("betaEnabled", user.getBetaFeaturesEnabled());
        return new ModelAndView(data, "templates/schedule/recurring-actions.jade");
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
        if (action.isEmpty() || !action.get().canAccess(user)) {
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
        String typeParam = request.params("type");
        TargetType type;
        if ("CLM".equalsIgnoreCase(typeParam)) {
            type = TargetType.NO_ENTITY;
        }
        else {
            type = TargetType.valueOf(typeParam.replace("-", "_").toUpperCase());
        }
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
            case NO_ENTITY:
                schedules = RecurringActionManager.listNoEntityRecurringActions(user);
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
                    .toList()
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
        dto.setActive(action.isActive());
        populateTaskoRunStatus(dto, action);
        try {
            CronExpression cron = new CronExpression(action.getCronExpr());
            dto.setNextExecution(cron.getNextValidTimeAfter(new Date()));
        }
        catch (ParseException e) {
            LOG.warn("Invalid cron expression for recurring action {}: {}",
                    action.getId(), action.getCronExpr());
        }

        if (RecurringActionType.ActionType.HIGHSTATE.equals(action.getActionType())) {
            dto.setTest(((RecurringHighstate) action.getRecurringActionType()).isTestMode());
        }
        else if (RecurringActionType.ActionType.CUSTOMSTATE.equals(action.getActionType())) {
            dto.setTest(((RecurringState) action.getRecurringActionType()).isTestMode());
            dto.setUseTransactionalUpdate(
                    ((RecurringState) action.getRecurringActionType()).isUseTransactionalUpdate());
            dto.setStates(StateConfigJson.listOrderedStates(
                    ((RecurringState) action.getRecurringActionType()).getStateConfig()));
        }
        else if (RecurringActionType.ActionType.SCAPPOLICY.equals(action.getActionType())) {
            dto.setTest(((RecurringScapPolicy) action.getRecurringActionType()).isTestMode());
            ScapPolicy policy = ((RecurringScapPolicy) action.getRecurringActionType()).getScapPolicy();
            if (policy != null) {
                Set<ScapPolicyJson> policies = new HashSet<>();
                ScapPolicyJson policyJson = new ScapPolicyJson();
                policyJson.setId(policy.getId());
                policyJson.setPolicyName(policy.getPolicyName());
                policyJson.setScapContentId(policy.getScapContent().getId());
                policyJson.setXccdfProfileId(policy.getXccdfProfileId());
                if (policy.getDescription() != null) {
                    policyJson.setDescription(policy.getDescription());
                }
                policies.add(policyJson);
                dto.setPolicies(policies);
            }
        }
        else if (RecurringActionType.ActionType.PLAYBOOK.equals(action.getActionType())) {
            dto.setTest(((RecurringPlaybook) action.getRecurringActionType()).isTestMode());
            dto.setPlaybookPath(((RecurringPlaybook) action.getRecurringActionType()).getPlaybookPath());
            dto.setInventoryPath(((RecurringPlaybook) action.getRecurringActionType()).getInventoryPath());
            dto.setFlushCache(((RecurringPlaybook) action.getRecurringActionType()).isFlushCache());
            dto.setExtraVars(HibernateFactory.getByteArrayContents(
                    ((RecurringPlaybook) action.getRecurringActionType()).getExtraVars()));
        }
        else if (action.getRecurringActionType() instanceof RecurringContentLifecycleType clmType) {
            ContentProject project = clmType.getProject();
            if (project != null) {
                dto.setProjectLabel(project.getLabel());
                dto.setProjectName(project.getName());
            }

            if (RecurringActionType.ActionType.CLM_BUILD.equals(action.getActionType())) {
                if (project != null) {
                    project.getFirstEnvironmentOpt().ifPresent(firstEnv -> {
                        dto.setEnvironmentLabel(firstEnv.getLabel());
                        dto.setEnvironmentName(firstEnv.getName());
                        dto.setTargetEnvironmentVersion(firstEnv.getVersion());
                    });

                    Long currentVer = project.getFirstEnvironmentOpt().map(ContentEnvironment::getVersion).orElse(null);
                    List<ClmBuildHistoryDto> history = project.getHistoryEntries().stream()
                            .sorted(Comparator.comparing(ContentProjectHistoryEntry::getVersion).reversed())
                            .limit(10)
                            .map(entry -> {
                                ClmBuildHistoryDto hDto = new ClmBuildHistoryDto();
                                hDto.setVersion(entry.getVersion());
                                hDto.setCreated(entry.getCreated());
                                hDto.setMessage(entry.getMessage());
                                if (entry.getUser() != null) {
                                    hDto.setUserName(entry.getUser().getLogin());
                                }
                                boolean isAuto = entry.getMessage() != null &&
                                        entry.getMessage().contains("recurring action");
                                hDto.setAutomated(isAuto);
                                hDto.setCurrent(currentVer != null && currentVer.equals(entry.getVersion()));
                                return hDto;
                            })
                            .collect(Collectors.toList());
                    dto.setBuildHistory(history);
                }
            }
            else if (RecurringActionType.ActionType.CLM_PROMOTE.equals(action.getActionType())) {
                if (clmType.getEnvironment() != null) {
                    ContentEnvironment srcEnv = clmType.getEnvironment();
                    dto.setEnvironmentLabel(srcEnv.getLabel());
                    dto.setEnvironmentName(srcEnv.getName());
                    dto.setSourceEnvironmentVersion(srcEnv.getVersion());

                    srcEnv.getNextEnvironmentOpt().ifPresent(nextEnv -> {
                        dto.setNextEnvironmentLabel(nextEnv.getLabel());
                        dto.setNextEnvironmentName(nextEnv.getName());
                        dto.setTargetEnvironmentVersion(nextEnv.getVersion());
                        dto.setTargetEnvironmentModified(nextEnv.getModified());
                    });
                }
            }
        }
        return dto;
    }

    private static void populateTaskoRunStatus(RecurringActionDetailsDto dto, RecurringAction action) {
        try {
            String jobLabel = "recurring-action-" + action.getId();
            Object[] runData = HibernateFactory.getSession()
                    .createNativeQuery(
                            "SELECT r.status, r.start_time " +
                            "FROM rhnTaskoRun r " +
                            "JOIN rhnTaskoSchedule s ON r.schedule_id = s.id " +
                            "WHERE s.job_label = :jobLabel " +
                            "ORDER BY r.start_time DESC " +
                            "LIMIT 1",
                            Object[].class)
                    .setParameter("jobLabel", jobLabel)
                    .uniqueResultOptional()
                    .orElse(null);
            if (runData != null) {
                if (runData[0] != null) {
                    dto.setLastExecutionStatus((String) runData[0]);
                }
                if (runData[1] instanceof Instant instant) {
                    dto.setLastExecutionTime(Date.from(instant));
                }
                else if (runData[1] instanceof Date date) {
                    dto.setLastExecutionTime(date);
                }
            }
        }
        catch (Exception e) {
            LOG.warn("Unable to query TaskoRun status for recurring action {}", action.getId(), e);
        }
    }

    /**
     * Processes a GET request to get a list of all the scap policies
     *
     * @param req  the request object
     * @param res  the response object
     * @param user the authorized user
     * @return the result JSON object
     */
    public static String listScapPolicies(Request req, Response res, User user) {
        String idParam = req.queryParams("id");
        String filterParam = req.queryParams("filter");
        List<ScapPolicy> scapPolicies = ScapFactory.listScapPolicies(user.getOrg());

        // Get assigned policy if editing existing recurring action
        Set<Integer> assignedPolicyIds = new HashSet<>();
        if (idParam != null) {
            Long id = Long.parseLong(idParam);
            Optional<RecurringAction> action = RecurringActionManager.find(id);
            if (action.isPresent() &&
                    (action.get().getRecurringActionType() instanceof RecurringScapPolicy recurringScapPolicy)) {
                ScapPolicy assignedPolicy = recurringScapPolicy.getScapPolicy();
                if (assignedPolicy != null) {
                    assignedPolicyIds.add(assignedPolicy.getId());
                }
            }
        }

        // Filter policies if filter parameter is provided
        if (filterParam != null && !filterParam.trim().isEmpty()) {
            String filter = filterParam.toLowerCase();
            scapPolicies = scapPolicies.stream()
                    .filter(policy ->
                        policy.getPolicyName().toLowerCase().contains(filter) ||
                        (policy.getDescription() != null && policy.getDescription().toLowerCase().contains(filter)) ||
                        policy.getDataStreamName().toLowerCase().contains(filter)
                    )
                    .collect(Collectors.toList());
        }

        List<JsonObject> scapPoliciesJson = scapPolicies.stream()
                .map(policy -> {
                    JsonObject json = new JsonObject();
                    json.addProperty("id", policy.getId());
                    json.addProperty("policyName", policy.getPolicyName());
                    json.addProperty("dataStreamName", policy.getDataStreamName());
                    json.addProperty("xccdfProfileId", policy.getXccdfProfileId());

                    if (policy.getDescription() != null) {
                        json.addProperty("description", policy.getDescription());
                    }

                    if (policy.getTailoringFile() != null) {
                        json.addProperty("tailoringFileName", policy.getTailoringFile().getName());
                    }

                    if (policy.getTailoringProfileId() != null) {
                        json.addProperty("tailoringFileProfileId", policy.getTailoringProfileId());
                    }

                    // Mark as assigned if this policy is used in the recurring action
                    json.addProperty("assigned", assignedPolicyIds.contains(policy.getId()));
                    if (assignedPolicyIds.contains(policy.getId())) {
                        json.addProperty("position", 1);
                    }

                    return json;
                })
                .collect(Collectors.toList());

        return json(res, scapPoliciesJson, new TypeToken<>() { });
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
            // This detach is needed because the code first modifies the object, and only after flushing (to execute an
            // additional query on RecurringAction) validates the values.
            HibernateFactory.getSession().detach(action);
            mapJsonToAction(json, action);
            RecurringActionManager.saveAndSchedule(action, user);
            HibernateFactory.getSession().flush();
        }
        catch (ValidatorException e) {
            errors.add(e.getMessage()); // we assume the messages are already localized
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(e.getMessage())));
        }
        catch (PersistenceException e) {
            LOG.warn("Constraint violation while saving recurring action", e);
            HibernateFactory.rollbackTransaction();
            String errMsg = LocalizationService.getInstance().getMessage("recurring_action.action_name_exists");
            Spark.halt(HttpStatus.SC_BAD_REQUEST, GSON.toJson(ResultJson.error(errMsg)));
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

        if (json.getScheduleName() != null && !json.getScheduleName().isBlank()) {
            Context.getCurrentContext().setActionDescription("Recurring Action: " + json.getScheduleName());
        }
        try {
            Action a = ActionManager.scheduleApplyStates(user,
                    minionIds, mods,
                    Optional.empty(),
                    new Date(),
                    Optional.of(json.getDetails().isTest()),
                    true,
                    false,
                    json.getDetails().isUseTransactionalUpdate());
            ActionFactory.save(a);
            taskomaticApi.scheduleActionExecution(a);
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
        finally {
            Context.getCurrentContext().setActionDescription(null);
        }
        return success(response);
    }

    private static RecurringAction createOrGetAction(User user, RecurringActionScheduleJson json) {
        if (json.getRecurringActionId() == null) {
            String typeStr = json.getTargetType();
            RecurringAction.TargetType type;
            if ("CLM".equalsIgnoreCase(typeStr) || "NO-ENTITY".equalsIgnoreCase(typeStr)) {
                type = RecurringAction.TargetType.NO_ENTITY;
            }
            else {
                type = RecurringAction.TargetType.valueOf(typeStr.replace("-", "_").toUpperCase());
            }
            long targetId = json.getTargetId() != null && json.getTargetId() != 0 ?
                    json.getTargetId() : user.getOrg().getId();
            return RecurringActionManager.createRecurringAction(type, json.getActionType(), targetId, user);
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

    private static void setPlaybookDetails(RecurringPlaybook playbookType, RecurringActionDetailsDto details) {
        if (details.getPlaybookPath() == null || details.getPlaybookPath().isEmpty()) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.empty_playbook_path"));
        }
        playbookType.setPlaybookPath(details.getPlaybookPath());
        playbookType.setTestMode(details.isTest());
        playbookType.setInventoryPath(details.getInventoryPath());
        playbookType.setFlushCache(details.isFlushCache());
        playbookType.setExtraVars(details.getExtraVars().getBytes());
    }

    private static void mapJsonToAction(RecurringActionScheduleJson json, RecurringAction action) {
        action.setName(json.getScheduleName());
        action.setActive(json.isActive());

        RecurringActionDetailsDto details = json.getDetails();
        if (details == null) {
            return;
        }

        if (action.getRecurringActionType() instanceof RecurringHighstate recurringHighstate) {
            recurringHighstate.setTestMode(details.isTest());
        }
        else if (action.getRecurringActionType() instanceof RecurringState stateType) {
            stateType.setTestMode(details.isTest());
            stateType.setUseTransactionalUpdate(details.isUseTransactionalUpdate());
            if (json.getRecurringActionId() == null ||
                    (json.getRecurringActionId() != null && details.getStates() != null)) {
                Set<RecurringStateConfig> newConfig = getStateConfigFromJson(details.getStates(), action.getCreator());
                ((RecurringState) action.getRecurringActionType()).saveStateConfig(newConfig);
            }
        }
        else if (action.getRecurringActionType() instanceof RecurringScapPolicy recurringScapPolicy) {
            recurringScapPolicy.setTestMode(details.isTest());
            if (details.getPolicies() != null && !details.getPolicies().isEmpty()) {
                details.getPolicies()
                        .stream()
                        .findFirst()
                        .flatMap(policyJson -> ScapFactory.lookupScapPolicyByIdAndOrg(policyJson.getId(),
                          action.getCreator().getOrg()))
                        .ifPresent(recurringScapPolicy::setScapPolicy);
            }
        }
        else if (action.getRecurringActionType() instanceof RecurringPlaybook playbookType) {
            setPlaybookDetails(playbookType, details);
        }
        else if (action.getRecurringActionType() instanceof RecurringContentLifecycleType clmType) {
            if (RecurringActionType.ActionType.CLM_BUILD.equals(action.getActionType())) {
                setClmBuildDetails(clmType, details, action.getCreator());
            }
            else if (RecurringActionType.ActionType.CLM_PROMOTE.equals(action.getActionType())) {
                setClmPromoteDetails(clmType, details, action.getCreator());
                if (clmType.getEnvironment() == null) {
                    throw new ValidatorException(LocalizationService.getInstance()
                            .getMessage("recurring_action.empty_clm_environment"));
                }
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

    private static void setClmBuildDetails(RecurringContentLifecycleType buildType,
                                           RecurringActionDetailsDto details, User user) {
        String projectLabel = details.getProjectLabel();
        if (StringUtils.isBlank(projectLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.empty_clm_project"));
        }
        if (buildType.getProject() != null && !buildType.getProject().getLabel().equals(projectLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.cannot_change_project"));
        }
        ContentProject project = ContentManager.lookupProject(projectLabel, user)
                .orElseThrow(() -> new ValidatorException("Project not found: " + projectLabel));
        if (project.getFirstEnvironmentOpt().isEmpty()) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.clm_project_no_environments", projectLabel));
        }
        if (project.lookupSwSourceLeader().isEmpty()) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.clm_project_no_base_channel", projectLabel));
        }
        buildType.setProject(project);
        buildType.setEnvironment(null);
    }

    private static void setClmPromoteDetails(RecurringContentLifecycleType promoteType,
                                             RecurringActionDetailsDto details, User user) {
        String projectLabel = details.getProjectLabel();
        if (StringUtils.isBlank(projectLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.empty_clm_project"));
        }
        if (promoteType.getProject() != null && !promoteType.getProject().getLabel().equals(projectLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.cannot_change_project"));
        }
        String envLabel = details.getEnvironmentLabel();
        if (StringUtils.isBlank(envLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.empty_clm_environment"));
        }
        if (promoteType.getEnvironment() != null && !promoteType.getEnvironment().getLabel().equals(envLabel)) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.cannot_change_environment"));
        }
        ContentProject project = ContentManager.lookupProject(projectLabel, user)
                .orElseThrow(() -> new ValidatorException("Project not found: " + projectLabel));
        ContentEnvironment env = ContentManager.lookupEnvironment(envLabel, projectLabel, user)
                .orElseThrow(() -> new ValidatorException("Environment not found: " + envLabel));
        if (env.getNextEnvironmentOpt().isEmpty()) {
            throw new ValidatorException(LocalizationService.getInstance()
                    .getMessage("recurring_action.clm_environment_no_successor", envLabel));
        }
        promoteType.setProject(project);
        promoteType.setEnvironment(env);
    }

    /**
     * Get a list of all CLM projects and their environments for the user
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return JSON result of the API call
     */
    public static String getClmProjects(Request request, Response response, User user) {
        List<ContentProject> projects = ContentManager.listProjects(user);
        List<Map<String, Object>> result = projects.stream().map(project -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", project.getId());
            map.put("label", project.getLabel());
            map.put("name", project.getName());
            map.put("hasBaseChannel", project.lookupSwSourceLeader().isPresent());
            List<ContentEnvironment> envs = ContentManager.listProjectEnvironments(project.getLabel(), user);
            map.put("hasEnvironments", !envs.isEmpty());
            List<Map<String, Object>> envList = envs.stream().map(env -> {
                Map<String, Object> envMap = new HashMap<>();
                envMap.put("id", env.getId());
                envMap.put("label", env.getLabel());
                envMap.put("name", env.getName());
                envMap.put("hasSuccessor", env.getNextEnvironmentOpt().isPresent());
                env.getNextEnvironmentOpt().ifPresent(next -> envMap.put("successorName", next.getName()));
                return envMap;
            }).collect(Collectors.toList());
            map.put("environments", envList);
            return map;
        }).collect(Collectors.toList());

        return json(response, result, new TypeToken<>() { });
    }
}
