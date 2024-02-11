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

package com.redhat.rhn.manager.recurringactions;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.GroupRecurringAction;
import com.redhat.rhn.domain.recurringactions.MinionRecurringAction;
import com.redhat.rhn.domain.recurringactions.OrgRecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskoQuartzHelper;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.utils.SaltFileUtils;
import com.suse.manager.webui.utils.gson.RecurringActionScheduleJson;
import com.suse.manager.webui.utils.gson.SimpleMinionJson;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * RecurringActionManager
 */
public class RecurringActionManager extends BaseManager {

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();
    private static final ServerGroupManager SERVER_GROUP_MANAGER = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }

    /**
     * Private constructor.
     */
    private RecurringActionManager() {
    }

    /**
     * Create a minimal {@link RecurringAction} of given type.
     *
     * @param targetType the Recurring Action entity type
     * @param actionType the Recurring Action type
     * @param entityId the ID of the target entity
     * @param user the creator
     * @return the newly created {@link RecurringAction}
     */
    public static RecurringAction createRecurringAction(RecurringAction.TargetType targetType,
                                                        RecurringActionType.ActionType actionType,
                                                        long entityId, User user) {
        switch (targetType) {
            case MINION:
                return createMinionRecurringAction(actionType, entityId, user);
            case GROUP:
                return createGroupRecurringAction(actionType, entityId, user);
            case ORG:
                return createOrgRecurringAction(actionType, entityId, user);
            default:
                throw new UnsupportedOperationException("type not supported");
        }
    }

    /**
     * Create a minimal {@link RecurringActionType} of given type.
     *
     * @param actionType the type of the action
     * @return the newly crated {@link RecurringActionType}
     */
    private static RecurringActionType createRecurringActionType(RecurringActionType.ActionType actionType) {
        if (actionType == null) {
            throw new ValidatorException(getLocalization().getMessage("recurring_action.empty_action_type"));
        }
        switch (actionType) {
            case HIGHSTATE:
                return new RecurringHighstate(false);
            case CUSTOMSTATE:
                return new RecurringState(false);
            default:
                throw new UnsupportedOperationException("type not supported");
        }
    }

    /**
     * Create a minimal minion recurring action
     *
     * @param minionId id of the minion
     * @param user the user
     * @return
     */
    private static MinionRecurringAction createMinionRecurringAction(RecurringActionType.ActionType actionType,
                                                                     long minionId, User user) {
        MinionServer minion = MinionServerFactory.lookupById(minionId)
                .orElseThrow(() -> new EntityNotExistsException(MinionServer.class, minionId));
        return new MinionRecurringAction(createRecurringActionType(actionType), true, minion, user);
    }

    /**
     * Create a minimal group recurring action
     *
     * @param groupId id of the group
     * @param user the user
     * @return
     */
    private static GroupRecurringAction createGroupRecurringAction(RecurringActionType.ActionType actionType,
                                                                   long groupId, User user) {
        ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(groupId, user.getOrg());
        if (group == null) {
            throw new EntityNotExistsException(ServerGroup.class, groupId);
        }
        return new GroupRecurringAction(createRecurringActionType(actionType), true, group, user);
    }

    /**
     * Create a minimal org recurring action
     *
     * @param orgId id of the organization
     * @param user the user
     * @return
     */
    private static OrgRecurringAction createOrgRecurringAction(RecurringActionType.ActionType actionType,
                                                               long orgId, User user) {
        Org org = OrgFactory.lookupById(orgId);
        if (org == null) {
            throw new EntityNotExistsException(Org.class, orgId);
        }
        return new OrgRecurringAction(
                createRecurringActionType(actionType),
                true, org, user);
    }

    /**
     * List minion recurring action with minion id
     *
     * @param minionId id of the minion
     * @param user the user
     * @return list of minion recurring actions
     */
    public static List<RecurringAction> listMinionRecurringActions(long minionId, User user) {
        try {
            return RecurringActionFactory.listMinionRecurringActions(SystemManager.lookupByIdAndUser(minionId, user));
        }
        catch (LookupException e) {
            throw new PermissionException(String.format("Minion id %d not accessible to user ", minionId), e);
        }
    }

    /**
     * List group recurring action with group id
     *
     * @param groupId id of the group
     * @param user the user
     * @return list of group recurring actions
     */
    public static List<RecurringAction> listGroupRecurringActions(long groupId, User user) {
        if (!user.hasRole(RoleFactory.SYSTEM_GROUP_ADMIN)) {
            throw new PermissionException(String.format("User does not have access to group id %d", groupId));
        }
        try {
            /* Check if user has permission to access the group */
            return RecurringActionFactory.listGroupRecurringActions(SERVER_GROUP_MANAGER.lookup(groupId, user));
        }
        catch (LookupException e) {
            throw new PermissionException(String.format("User does not have access to group id %d", groupId), e);
        }
    }

    /**
     * List group recurring action with org id
     *
     * @param orgId id of the organization
     * @param user the user
     * @return list of org recurring actions
     */
    public static List<RecurringAction> listOrgRecurringActions(long orgId, User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException("Org not accessible to user");
        }
        return RecurringActionFactory.listOrgRecurringActions(orgId);
    }

    /**
     * List all members of a given entity
     *
     * @param type the type of the entity
     * @param id the entity id
     * @param user the user
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the actions visible to the user
     */
    public static DataResult<SimpleMinionJson> listEntityMembers(
            RecurringAction.TargetType type, Long id, User user, PageControl pc, Function<Optional<PageControl>,
            PagedSqlQueryBuilder.FilterWithValue> parser) {
        DataResult<SimpleMinionJson> members;
        switch (type) {
            case GROUP:
                if (!user.hasRole(RoleFactory.SYSTEM_GROUP_ADMIN)) {
                    throw new PermissionException(String.format("User does not have access to group id %d", id));
                }
                members = RecurringActionFactory.listGroupMembers(id, pc, parser);
                break;
            case ORG:
                if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
                    throw new PermissionException("Org not accessible to user");
                }
                members = RecurringActionFactory.listOrgMembers(id, pc, parser);
                break;
            default:
                throw new IllegalStateException("Unsupported type " + type);
        }
        return members;
    }

    /**
     * List all {@link RecurringAction}s visible to the given user
     *
     * @param user the user
     * @param pc the page control
     * @param parser the parser for filters when building query
     * @return the actions visible to the user
     */
    public static DataResult<RecurringActionScheduleJson> listAllRecurringActions(
            User user, PageControl pc, Function<Optional<PageControl>, PagedSqlQueryBuilder.FilterWithValue> parser) {

        DataResult<RecurringActionScheduleJson> allActions =
                RecurringActionFactory.listAllRecurringActions(user, pc, parser);

        if (user.hasRole(RoleFactory.ORG_ADMIN)) {
            return allActions;
        }

        // if the user is not org admin, check if she can access the target.
        allActions.forEach(a -> {
            Optional<RecurringAction> action = find(a.getRecurringActionId());
            a.setTargetAccessible(
                action.isPresent() && action.get().canAccess(user)
            );
        });

        return allActions;
    }

    /**
     * Find a recurring action with given id.
     *
     * @param id - id of the recurring action
     * @return optional of matching recurring action
     */
    public static Optional<RecurringAction> find(Long id) {
        return RecurringActionFactory.lookupById(id);
    }

    /**
     * Checks permissions on given {@link RecurringAction}, validates it,
     * saves it and schedules corresponding taskomatic job.
     *
     * All save/update operations on {@link RecurringAction} must use this method
     * (to make sure the taskomatic schedule is updated as well).
     *
     * The method can accept an entity in detached state.
     * Such entity is merged and returned (in managed state). The original entity stays in detached state.
     *
     * When taskomatic throws an exception (e.g. it is down), make sure that the transaction is rolled back
     * (normally it is enough to re-throw the exception), so that changes made to the entity are not persisted!
     * We should make sure that either both the entity and the taskomatic schedule is updated, or none of them is.
     *
     * @param action the action
     * @param user the user performing the operation
     * @throws ValidatorException if an entity validation fails
     * @throws TaskomaticApiException when there is a problem with taskomatic during scheduling
     * @return updated action entity in managed state
     */
    public static RecurringAction saveAndSchedule(RecurringAction action, User user) throws TaskomaticApiException {
        validateAction(action, user);
        RecurringAction saved = (RecurringAction) HibernateFactory.getSession().merge(action);
        taskomaticApi.scheduleRecurringAction(saved, user);
        saveStateConfig(saved);
        return saved;
    }

    /**
     * Save the recurring state configuration .sls file for Recurring State actions
     *
     * @param action the recurring action
     */
    public static void saveStateConfig(RecurringAction action) {
        if (action.getActionType().equals(RecurringActionType.ActionType.CUSTOMSTATE)) {
            SaltStateGeneratorService.INSTANCE.generateRecurringState(action);
        }
    }

    /**
     * Validate given {@link RecurringAction} parameters and user access
     *
     * @param action the {@link RecurringAction} to validate
     * @param user the user for access check
     * @throws ValidatorException if an entity validation fails
     */
    public static void validateAction(RecurringAction action, User user) {
        if (!action.canAccess(user)) {
            throw new ValidatorException(
                    getLocalization().getMessage("recurring_action.no_permissions"),
                    new PermissionException(String.format("%s not accessible to user %s", action, user)));
        }

        String cronExpr = action.getCronExpr();
        if (StringUtils.isBlank(cronExpr) || !TaskoQuartzHelper.isValidCronExpression(cronExpr)) {
            throw new ValidatorException(getLocalization().getMessage("recurring_action.invalid_cron"));
        }

        if (StringUtils.isBlank(action.getName())) {
            throw new ValidatorException(getLocalization().getMessage("recurring_action.empty_name"));
        }

        RecurringActionFactory.lookupEqualEntityId(action)
                .ifPresent(existingId -> validateExistingEntityName(action, existingId));
    }

    private static void validateExistingEntityName(RecurringAction action, Long existingId) {
        // Equal entity already exists. Throw an exception if:
        // - either given action is null (create scenario),
        // - or the ID of given action is different from the existing entity (update scenario).
        Long actionId = action.getId();
        if (actionId == null || !actionId.equals(existingId)) {
            throw new ValidatorException(
                    getLocalization().getMessage("recurring_action.action_name_exists"),
                    new EntityExistsException(String.format("Equal entity already exists: ID %d", existingId)));
        }
    }

    /**
     * Checks permission on given {@link RecurringAction}, deletes it and unschedules corresponding taskomatic job.
     *
     * @param action the action
     * @param user the user performing the action
     * @throws PermissionException if the user does not have permission to delete the action
     * @throws TaskomaticApiException when there is a problem with taskomatic during unscheduling
     */
    public static void deleteAndUnschedule(RecurringAction action, User user) throws TaskomaticApiException {
        if (!action.canAccess(user)) {
            throw new PermissionException(String.format("%s not accessible to user %s", action, user));
        }
        RecurringActionFactory.delete(action);
        removeStateFile(action);
        taskomaticApi.unscheduleRecurringAction(action, user);
    }

    /**
     * Remove state files associated with given recurring action
     *
     * @param action the recurring action
     */
    public static void removeStateFile(RecurringAction action) {
        if (action.getActionType().equals(RecurringActionType.ActionType.CUSTOMSTATE)) {
            File stateFile = Paths
                    .get(SaltConstants.SUMA_STATE_FILES_ROOT_PATH)
                    .resolve(SaltConstants.SALT_RECURRING_STATES_DIR)
                    .resolve(SaltFileUtils.defaultExtension(
                            SaltConstants.SALT_RECURRING_STATE_FILE_PREFIX + action.getId()))
                    .toFile();
            FileUtils.deleteQuietly(stateFile);
        }
    }

    private static LocalizationService getLocalization() {
        return LocalizationService.getInstance();
    }

}
