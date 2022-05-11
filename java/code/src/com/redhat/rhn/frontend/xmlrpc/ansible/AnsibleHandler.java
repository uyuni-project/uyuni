/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.frontend.xmlrpc.ansible;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.SaltFaultException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.system.AnsibleManager;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.utils.salt.custom.AnsiblePlaybookSlsResult;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Ansible XMLRPC handler
 * @apidoc.namespace ansible
 * @apidoc.doc Provides methods to manage Ansible systems
 */
public class AnsibleHandler extends BaseHandler {

    // Keys to pass to schedulePlaybook endpoint as additional args for Ansible
    public static final String ANSIBLE_FLUSH_CACHE = "flushCache";

    private final AnsibleManager ansibleManager;

    /**
     * Constructor
     *
     * @param managerIn the ansible manager
     */
    public AnsibleHandler(AnsibleManager managerIn) {
        ansibleManager = managerIn;
    }

    /**
     * Schedule a playbook execution
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence earliest occurrence of the execution command
     * @param actionChainLabel label af action chain to use
     * @return the execute playbook action id
     *
     * @apidoc.doc Schedule a playbook execution
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "playbookPath", "path to the playbook file in the control node")
     * @apidoc.param #param_desc("string", "inventoryPath", "path to Ansible inventory or empty")
     * @apidoc.param #param_desc("int", "controlNodeId", "system ID of the control node")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "earliest the execution command can be sent to the control node. ignored when actionChainLabel is used")
     * @apidoc.param #param_desc("string", "actionChainLabel", "label of an action chain to use, or None")
     * @apidoc.returntype #param_desc("int", "id", "ID of the playbook execution action created")
     */
    public Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
            Date earliestOccurrence, String actionChainLabel) {
        return schedulePlaybook(loggedInUser, playbookPath, inventoryPath, controlNodeId, earliestOccurrence,
                actionChainLabel, false);
    }

    /**
     * Schedule a playbook execution
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence earliest occurrence of the execution command
     * @param actionChainLabel label af action chain to use
     * @param testMode true if the playbook shall be executed in test mode
     * @return the execute playbook action id
     *
     * @apidoc.doc Schedule a playbook execution
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "playbookPath", "path to the playbook file in the control node")
     * @apidoc.param #param_desc("string", "inventoryPath", "path to Ansible inventory or empty")
     * @apidoc.param #param_desc("int", "controlNodeId", "system ID of the control node")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "earliest the execution command can be sent to the control node. ignored when actionChainLabel is used")
     * @apidoc.param #param_desc("string", "actionChainLabel", "label of an action chain to use, or None")
     * @apidoc.param #param_desc("boolean", "testMode", "'true' if the playbook shall be executed in test mode")
     * @apidoc.returntype #param_desc("int", "id", "ID of the playbook execution action created")
     */
    public Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
            Date earliestOccurrence, String actionChainLabel, boolean testMode) {
        return schedulePlaybook(loggedInUser, playbookPath, inventoryPath, controlNodeId, earliestOccurrence,
                actionChainLabel, testMode, Collections.emptyMap());
    }

    /**
     * Schedule a playbook execution with test mode option
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence earliest occurrence of the execution command
     * @param actionChainLabel label af action chain to use
     * @param ansibleArgs the dictionary of additional arguments to pass to ansiblegate
     * @return the execute playbook action id
     *
     * @apidoc.doc Schedule a playbook execution
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "playbookPath", "path to the playbook file in the control node")
     * @apidoc.param #param_desc("string", "inventoryPath", "path to Ansible inventory or empty")
     * @apidoc.param #param_desc("int", "controlNodeId", "system ID of the control node")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "earliest the execution command can be sent to the control node. ignored when actionChainLabel is used")
     * @apidoc.param #param_desc("string", "actionChainLabel", "label of an action chain to use, or None")
     * @apidoc.param
     *     #struct_begin("ansibleArgs")
     *         #prop("boolean", "flushCache", "clear the fact cache for every host in inventory")
     *     #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "ID of the playbook execution action created")
     */
    public Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath, Integer controlNodeId,
            Date earliestOccurrence, String actionChainLabel, Map<String, Object> ansibleArgs) {
        return schedulePlaybook(loggedInUser, playbookPath, inventoryPath, controlNodeId, earliestOccurrence,
                actionChainLabel, false, ansibleArgs);
    }

    /**
     * Schedule a playbook execution with test mode option
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence earliest occurrence of the execution command
     * @param actionChainLabel label af action chain to use
     * @param testMode true if the playbook shall be executed in test mode
     * @param ansibleArgs the dictionary of additional arguments to pass to ansiblegate
     * @return the execute playbook action id
     *
     * @apidoc.doc Schedule a playbook execution
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "playbookPath", "path to the playbook file in the control node")
     * @apidoc.param #param_desc("string", "inventoryPath", "path to Ansible inventory or empty")
     * @apidoc.param #param_desc("int", "controlNodeId", "system ID of the control node")
     * @apidoc.param #param_desc("$date", "earliestOccurrence",
     * "earliest the execution command can be sent to the control node. ignored when actionChainLabel is used")
     * @apidoc.param #param_desc("string", "actionChainLabel", "label of an action chain to use, or None")
     * @apidoc.param #param_desc("boolean", "testMode", "'true' if the playbook shall be executed in test mode")
     * @apidoc.param
     *     #struct_begin("ansibleArgs")
     *         #prop("boolean", "flushCache", "clear the fact cache for every host in inventory")
     *     #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "ID of the playbook execution action created")
     */
    public Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath,
            Integer controlNodeId, Date earliestOccurrence, String actionChainLabel, boolean testMode,
            Map<String, Object> ansibleArgs) {

        // Validate the args map and set defaults
        Map<String, Object> argMap = new HashMap<>(ansibleArgs);
        validateMap(Set.of(ANSIBLE_FLUSH_CACHE), argMap);
        argMap.putIfAbsent(ANSIBLE_FLUSH_CACHE, false);

        try {
            return AnsibleManager.schedulePlaybook(playbookPath, inventoryPath, controlNodeId, testMode,
                    (Boolean) argMap.get(ANSIBLE_FLUSH_CACHE), earliestOccurrence,
                    Optional.ofNullable(actionChainLabel), loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (IllegalArgumentException | ClassCastException e) {
            throw new InvalidParameterException("Invalid parameter", e);
        }
    }

    /**
     * List ansible paths for server (control node)
     *
     * @param loggedInUser the logged in user
     * @param controlNodeId the id of the server
     * @return List ansible paths
     *
     *
     * @apidoc.doc List ansible paths for server (control node)
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "controlNodeId", "id of ansible control node server")
     * @apidoc.returntype
     * #return_array_begin()
     * $AnsiblePathSerializer
     * #array_end()
     */
    @ReadOnly
    public List<AnsiblePath> listAnsiblePaths(User loggedInUser, Integer controlNodeId) {
        try {
            return AnsibleManager.listAnsiblePaths(controlNodeId, loggedInUser);
        }
        catch (LookupException e) {
            throw new NoSuchSystemException(e);
        }
    }

    /**
     * Lookup ansible path by path id
     *
     * @param loggedInUser the logged in user
     * @param pathId the path id
     * @return matching path
     *
     * @apidoc.doc Lookup ansible path by path id
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "path id")
     * @apidoc.returntype
     * $AnsiblePathSerializer
     */
    @ReadOnly
    public AnsiblePath lookupAnsiblePathById(User loggedInUser, Integer pathId) {
        try {
            return AnsibleManager.lookupAnsiblePathById(pathId, loggedInUser)
                    .orElseThrow(() -> new EntityNotExistsFaultException(pathId));
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
    }

    /**
     * Create ansible path
     *
     * @param loggedInUser the logged in user
     * @param props the props with the path properties
     * @return created path
     *
     * @apidoc.doc Create ansible path
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("props")
     *      #prop_desc("string", "type", "The ansible path type: 'inventory' or 'playbook'")
     *      #prop_desc("int", "server_id", "ID of control node server")
     *      #prop_desc("string", "path", "The local path to inventory/playbook")
     *  #struct_end()
     * @apidoc.returntype
     * $AnsiblePathSerializer
     */
    public AnsiblePath createAnsiblePath(User loggedInUser, Map<String, Object> props) {
        String typeLabel = getFieldValue(props, "type");
        Integer controlNodeId = getFieldValue(props, "server_id");
        String path = getFieldValue(props, "path");

        try {
            return AnsibleManager.createAnsiblePath(typeLabel, controlNodeId, path, loggedInUser);
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(controlNodeId);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Update ansible path
     *
     * @param loggedInUser the logged in user
     * @param pathId the path id
     * @param props the props with the path properties
     * @return updated path
     *
     * @apidoc.doc Create ansible path
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "path id")
     * @apidoc.param
     *  #struct_begin("props")
     *      #prop_desc("string", "path", "The local path to inventory/playbook")
     *  #struct_end()
     * @apidoc.returntype
     * $AnsiblePathSerializer
     */
    public AnsiblePath updateAnsiblePath(User loggedInUser, Integer pathId, Map<String, Object> props) {
        try {
            String newPath = getFieldValue(props, "path");
            return AnsibleManager.updateAnsiblePath(pathId, newPath, loggedInUser);
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
        catch (ValidatorException e) {
            throw new ValidationException(e);
        }
    }

    /**
     * Update ansible path
     *
     * @param loggedInUser the logged in user
     * @param pathId the path id
     * @return 1 on success
     * @throws EntityNotExistsFaultException when path not found or not accessible
     *
     * @apidoc.doc Create ansible path
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "path id")
     * @apidoc.returntype #return_int_success()
     *
     */
    public int removeAnsiblePath(User loggedInUser, Integer pathId) {
        try {
            AnsibleManager.removeAnsiblePath(pathId, loggedInUser);
            return 1;
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
    }

    /**
     * Fetch the playbook content from the control node using a synchronous salt call.
     *
     * @param loggedInUser the logged in user
     * @param pathId the PlaybookPath id
     * @param playbookRelPath the relative path to playbook (inside PlaybookPath)
     * @return the playbook contents or empty optional if minion did not respond
     * @throws LookupException when path not found or not accessible
     *
     * @apidoc.doc Fetch the playbook content from the control node using a synchronous salt call.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "playbook path id")
     * @apidoc.param #param_desc("string", "playbookRelPath", "relative path of playbook (inside path specified by
     * pathId)")
     * @apidoc.returntype #param_desc("string", "contents", "Text contents of the playbook")
     */
    public String fetchPlaybookContents(User loggedInUser, Integer pathId, String playbookRelPath) {
        try {
            return ansibleManager.fetchPlaybookContents(pathId, playbookRelPath, loggedInUser)
                    .orElseThrow(() -> new SaltFaultException("Minion not responding"));
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException(e.getMessage());
        }
        catch (IllegalStateException e) {
            throw new SaltFaultException(e.getMessage());
        }
    }

    /**
     * Discover playbooks under given playbook path with given pathId
     *
     * @param loggedInUser the logged in user
     * @param pathId the path id
     * @return the playbooks under given path
     *
     * @apidoc.doc Discover playbooks under given playbook path with given pathId
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "path id")
     * @apidoc.returntype
     * #struct_begin("playbooks")
     *     #struct_begin("playbook")
     *         $AnsiblePathSerializer
     *     #struct_end()
     * #struct_end()
     */
    public Map<String, Map<String, AnsiblePlaybookSlsResult>> discoverPlaybooks(User loggedInUser, Integer pathId) {
        try {
            return ansibleManager.discoverPlaybooks(pathId, loggedInUser)
                    .orElseThrow(() -> new SaltFaultException("Minion not responding"));
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
        catch (IllegalStateException e) {
            throw new SaltFaultException(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidParameterException("Invalid parameter", e);
        }
    }

    /**
     * Introspect inventory under given inventory path with given pathId
     *
     * @param loggedInUser the logged in user
     * @param pathId the path id
     * @return the inventory contents under given path
     *
     * @apidoc.doc Introspect inventory under given inventory path with given pathId and return it in a structured way
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "pathId", "path id")
     * @apidoc.returntype
     * #struct_begin("Inventory in a nested structure")
     *   #param_desc("object", "Inventory item", "Inventory item (can be nested)")
     * #struct_end()
     */
    public Map<String, Map<String, Object>> introspectInventory(User loggedInUser, Integer pathId) {
        try {
            return ansibleManager.introspectInventory(pathId, loggedInUser)
                    .orElseThrow(() -> new SaltFaultException("Minion not responding"));
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
        catch (IllegalStateException e) {
            throw new SaltFaultException(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidParameterException("Invalid parameter", e);
        }
    }

    private static <T> T getFieldValue(Map<String, Object> props, String field) {
        Object val = props.get(field);
        if (val == null) {
            throw new ValidationException(String.format("Missing field '%s'", field));
        }
        try {
            return (T) val;
        }
        catch (ClassCastException e) {
            throw new ValidationException(String.format("Invalid type of field '%s'", field));
        }
    }
}
