/**
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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ansible.AnsiblePath;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Ansible XMLRPC handler
 * @xmlrpc.namespace ansible
 * @xmlrpc.doc Provides methods to manage Ansible systems
 */
public class AnsibleHandler extends BaseHandler {

    /**
     * Schedule a playbook execution
     *
     * @param loggedInUser the current user
     * @param playbookPath the path to the playbook file
     * @param inventoryPath the path to the inventory file
     * @param controlNodeId the system ID of the control node
     * @param earliestOccurrence earliest occurrence of the execution command
     * @return the execute playbook action id
     *
     * @xmlrpc.doc Schedule a playbook execution
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "playbookPath", "path to the playbook file in the control node")
     * @xmlrpc.param #param_desc("string", "inventoryPath", "path to Ansible inventory or empty")
     * @xmlrpc.param #param_desc("int", "controlNodeId", "system ID of the control node")
     * @xmlrpc.param #param_desc("dateTime.iso8601", "earliestOccurrence",
     * "earliest the execution command can be sent to the control node.")
     * @xmlrpc.returntype #param_desc("int", "id", "ID of the playbook execution action created")
     */
    public Long schedulePlaybook(User loggedInUser, String playbookPath, String inventoryPath,
            Integer controlNodeId, Date earliestOccurrence) {
        if (StringUtils.isEmpty(playbookPath)) {
            throw new InvalidParameterException("Playbook path cannot be empty.");
        }
        Server controlNode = validateAnsibleControlNode(controlNodeId, loggedInUser.getOrg());

        try {
            return ActionChainManager.scheduleExecutePlaybook(loggedInUser, controlNode.getId(), playbookPath,
                    inventoryPath, null, earliestOccurrence).getId();
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
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
     * @xmlrpc.doc List ansible paths for server (control node)
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "controlNodeId", "id of ansible control node server")
     * @xmlrpc.returntype
     * #array_begin()
     * $AnsiblePathSerializer
     * #array_end()
     */
    public List<AnsiblePath> listAnsiblePaths(User loggedInUser, Integer controlNodeId) {
        try {
            return SystemManager.listAnsiblePaths(controlNodeId, loggedInUser);
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
     * @xmlrpc.doc Lookup ansible path by path id
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "pathId", "path id")
     * @xmlrpc.returntype
     * $AnsiblePathSerializer
     */
    public AnsiblePath lookupAnsiblePathById(User loggedInUser, Integer pathId) {
        try {
            return SystemManager.lookupAnsiblePathById(pathId, loggedInUser)
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
     * @xmlrpc.doc Create ansible path
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "pathId", "path id")
     * @xmlrpc.param
     *  #struct_begin("props")
     *      #prop_desc("string", "type", "The ansible path type: 'inventory' or 'playbook'")
     *      #prop_desc("int", "server_id", "ID of control node server")
     *      #prop_desc("string", "path", "The local path to inventory/playbook")
     *  #struct_end()
     * @xmlrpc.returntype
     * $AnsiblePathSerializer
     */
    public AnsiblePath createAnsiblePath(User loggedInUser, Map<String, Object> props) {
        String typeLabel = getFieldValue(props, "type");
        Integer controlNodeId = getFieldValue(props, "server_id");
        String path = getFieldValue(props, "path");

        try {
            return SystemManager.createAnsiblePath(typeLabel, controlNodeId, path, loggedInUser);
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
     * @xmlrpc.doc Create ansible path
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "pathId", "path id")
     * @xmlrpc.param
     *  #struct_begin("props")
     *      #prop_desc("string", "path", "The local path to inventory/playbook")
     *  #struct_end()
     * @xmlrpc.returntype
     * $AnsiblePathSerializer
     */
    public AnsiblePath updateAnsiblePath(User loggedInUser, Integer pathId, Map<String, Object> props) {
        try {
            String newPath = getFieldValue(props, "path");
            return SystemManager.updateAnsiblePath(pathId, newPath, loggedInUser);
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
     * @xmlrpc.doc Create ansible path
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "pathId", "path id")
     * @xmlrpc.returntype #return_int_success()
     *
     */
    public int removeAnsiblePath(User loggedInUser, Integer pathId) {
        try {
            SystemManager.removeAnsiblePath(pathId, loggedInUser);
            return 1;
        }
        catch (LookupException e) {
            throw new EntityNotExistsFaultException(pathId);
        }
    }

    private Server validateAnsibleControlNode(long systemId, Org org) {
        Server controlNode = ServerFactory.lookupByIdAndOrg(systemId, org);
        if (controlNode == null) {
            throw new NoSuchSystemException();
        }
        if (!controlNode.hasAnsibleControlNodeEntitlement()) {
            throw new NoSuchSystemException(controlNode.getHostname() + " is not an Ansible control node");
        }
        return controlNode;
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
