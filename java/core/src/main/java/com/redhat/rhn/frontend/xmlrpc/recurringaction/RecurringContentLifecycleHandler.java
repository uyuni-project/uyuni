/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.recurringaction;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringContentBuildType;
import com.redhat.rhn.domain.recurringactions.type.RecurringContentPromoteType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;

import com.suse.manager.api.ReadOnly;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for recurring Content Lifecycle Management actions
 *
 * @apidoc.namespace recurring.clm
 * @apidoc.doc Provides methods to handle recurring CLM build and promote actions.
 */
public class RecurringContentLifecycleHandler extends BaseHandler {
    private final RecurringActionHandler actionHandler = new RecurringActionHandler();

    /**
     * Create a new recurring CLM build action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring CLM build action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("string", "project_label", "the label of the target Content Project")
     *      #prop_desc("string", "name", "the name of the recurring action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action as a cron expression")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the newly created recurring action")
     */
    public int createBuild(User loggedInUser, Map<String, Object> actionProps) {
        ensureOrgAdmin(loggedInUser);
        if (!actionProps.containsKey("project_label")) {
            throw new InvalidArgsException("'project_label' is required.");
        }
        String projectLabel = (String) actionProps.get("project_label");
        ContentProject project = ContentManager.lookupProject(projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException("Project not found: " + projectLabel));

        Map<String, Object> props = new HashMap<>(actionProps);
        props.put("entity_type", "NO_ENTITY");
        props.put("entity_id", Math.toIntExact(loggedInUser.getOrg().getId()));

        RecurringAction action =
                actionHandler.createAction(RecurringActionType.ActionType.CLM_BUILD, props, loggedInUser);
        RecurringContentBuildType buildType = (RecurringContentBuildType) action.getRecurringActionType();
        buildType.setProject(project);

        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Update a recurring CLM build action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Update a recurring CLM build action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_desc("string", "project_label", "the label of the target Content Project (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the updated recurring action")
     */
    public int updateBuild(User loggedInUser, Map<String, Object> actionProps) {
        ensureOrgAdmin(loggedInUser);
        RecurringAction action = actionHandler.updateAction(actionProps, loggedInUser);
        if (!(action.getRecurringActionType() instanceof RecurringContentBuildType buildType)) {
            throw new InvalidArgsException("Action is not a CLM build action");
        }
        if (actionProps.containsKey("project_label")) {
            String projectLabel = (String) actionProps.get("project_label");
            ContentProject project = ContentManager.lookupProject(projectLabel, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException("Project not found: " + projectLabel));
            buildType.setProject(project);
        }
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Create a new recurring CLM promote action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring CLM promote action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("string", "project_label", "the label of the target Content Project")
     *      #prop_desc("string", "environment_label", "the label of the environment to promote")
     *      #prop_desc("string", "name", "the name of the recurring action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action as a cron expression")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the newly created recurring action")
     */
    public int createPromote(User loggedInUser, Map<String, Object> actionProps) {
        ensureOrgAdmin(loggedInUser);
        if (!actionProps.containsKey("project_label")) {
            throw new InvalidArgsException("'project_label' is required.");
        }
        if (!actionProps.containsKey("environment_label")) {
            throw new InvalidArgsException("'environment_label' is required.");
        }
        String projectLabel = (String) actionProps.get("project_label");
        String envLabel = (String) actionProps.get("environment_label");

        ContentProject project = ContentManager.lookupProject(projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException("Project not found: " + projectLabel));
        ContentEnvironment env = ContentManager.lookupEnvironment(envLabel, projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException("Environment not found: " + envLabel));

        Map<String, Object> props = new HashMap<>(actionProps);
        props.put("entity_type", "NO_ENTITY");
        props.put("entity_id", Math.toIntExact(loggedInUser.getOrg().getId()));

        RecurringAction action =
                actionHandler.createAction(RecurringActionType.ActionType.CLM_PROMOTE, props, loggedInUser);
        RecurringContentPromoteType promoteType = (RecurringContentPromoteType) action.getRecurringActionType();
        promoteType.setProject(project);
        promoteType.setEnvironment(env);

        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Update a recurring CLM promote action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Update a recurring CLM promote action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_desc("string", "project_label", "the label of the target Content Project (optional)")
     *      #prop_desc("string", "environment_label", "the label of the environment to promote (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the updated recurring action")
     */
    public int updatePromote(User loggedInUser, Map<String, Object> actionProps) {
        ensureOrgAdmin(loggedInUser);
        RecurringAction action = actionHandler.updateAction(actionProps, loggedInUser);
        if (!(action.getRecurringActionType() instanceof RecurringContentPromoteType promoteType)) {
            throw new InvalidArgsException("Action is not a CLM promote action");
        }
        if (actionProps.containsKey("project_label")) {
            String projectLabel = (String) actionProps.get("project_label");
            ContentProject project = ContentManager.lookupProject(projectLabel, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException("Project not found: " + projectLabel));
            promoteType.setProject(project);
        }
        if (actionProps.containsKey("environment_label")) {
            String envLabel = (String) actionProps.get("environment_label");
            String projectLabel = promoteType.getProject().getLabel();
            ContentEnvironment env = ContentManager.lookupEnvironment(envLabel, projectLabel, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException("Environment not found: " + envLabel));
            promoteType.setEnvironment(env);
        }
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * List all recurring actions associated with a Content Project.
     *
     * @param loggedInUser The current user
     * @param projectLabel the project label
     * @return list of recurring actions
     *
     * @apidoc.doc List all recurring actions associated with a Content Project.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "projectLabel", "the project label")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<RecurringAction> listByProject(User loggedInUser, String projectLabel) {
        ensureOrgAdmin(loggedInUser);
        ContentProject project = ContentManager.lookupProject(projectLabel, loggedInUser)
                .orElseThrow(() -> new EntityNotExistsFaultException("Project not found: " + projectLabel));
        return RecurringActionFactory.listActionsForProject(project);
    }
}
