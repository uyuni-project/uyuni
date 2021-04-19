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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchSystemException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.manager.action.ActionChainManager;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

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
}
