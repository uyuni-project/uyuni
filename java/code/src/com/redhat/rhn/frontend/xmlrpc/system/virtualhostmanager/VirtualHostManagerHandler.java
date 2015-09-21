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

package com.redhat.rhn.frontend.xmlrpc.system.virtualhostmanager;

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.server.virtualhostmanager.InvalidGathererModuleException;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.InvalidGathererModuleFaultException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchGathererModuleException;
import com.suse.manager.gatherer.GathererCache;
import com.suse.manager.model.gatherer.GathererModule;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @xmlrpc.namespace virtualhostmanager
 * @xmlrpc.doc Provides the namespace for the Virtual Host Manager methods.
 */
public class VirtualHostManagerHandler extends BaseHandler {

    /**
     * Lists Virtual Host Managers for a user
     * @param loggedInUser the currently logged in user
     * @return List of Virtual Host Managers corresponding with the given user
     *
     * @xmlrpc.doc Lists Virtual Host Managers for a user
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype
     *
     */
    public List<VirtualHostManager> listVirtualHostManagers(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return VirtualHostManagerFactory.getInstance()
                .listVirtualHostManagers(loggedInUser.getOrg());
    }

    /**
     * Creates a Virtual Host Manager from given arguments
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @param moduleName the name of the Gatherer module
     * @param parameters additional parameters (credentials, paremeters for the gatherer)
     * @throws InvalidGathererModuleFaultException when the gatherer module
     * doesn't exist or if some required gatherer parameters are missing
     * @return The ID of the created Virtual Host Manager
     *
     * @xmlrpc.doc Creates a Virtual Host Manager from given arguments
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "label" "Virtual Host Manager label")
     * @xmlrpc.param #param_desc("string", "moduleName" "the name of the Gatherer module")
     * @xmlrpc.param #param_desc("parameters", "parameters"
     *         "additional parameters (credentials, paremeters for the gatherer)")
     * @xmlrpc.returntype int  - The ID of the created Virtual Host Manager
     */
    public int create(User loggedInUser, String label, String moduleName,
            Map<String, String> parameters) {
        ensureSatAdmin(loggedInUser);
        if (StringUtil.nullOrValue(label) == null) {
            throw new InvalidParameterException("Virtual Host Manager label is missing.");
        }
        if (StringUtil.nullOrValue(moduleName) == null) {
            throw new InvalidParameterException("Gatherer module name is missing.");
        }
        if (VirtualHostManagerFactory.getInstance().lookupByLabel(label) != null) {
            throw new InvalidParameterException("Another Virtual Host Manager with the same"
                    + " label already exists.");
        }
        try {
            return VirtualHostManagerFactory.getInstance().createVirtualHostManager(label,
                    loggedInUser.getOrg(),
                    moduleName,
                    parameters).getId().intValue();
        } catch (InvalidGathererModuleException e) {
            throw new InvalidGathererModuleFaultException(e.getMessage());
        }
    }

    /**
     * Deletes a Virtual Host Manager with given label
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @return State of the action result. Negative is false. Positive: number
     * of successfully deleted entries.
     *
     * @xmlrpc.doc Deletes a Virtual Host Manager with given label
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "label", "Virtual Host Manager label")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String label) {
        ensureSatAdmin(loggedInUser);
        VirtualHostManager manager =
                VirtualHostManagerFactory.getInstance().lookupByLabel(label);
        if (manager == null) {
            throw new InvalidParameterException("Virtual Host Manager with label '" + label
                    + "' doesn't exist.");
        }
        VirtualHostManagerFactory.getInstance().delete(manager);
        return BaseHandler.VALID;
    }

    /**
     * Gets detail of a Virtual Host Manager with given label
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @return Virtual Host Manager with given label
     *
     * @xmlrpc.doc Gets detail of a Virtual Host Manager with given label
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "label", "Virtual Host Manager label")
     * @xmlrpc.returntype $VirtualHostManagerSerializer
     */
    public VirtualHostManager getDetail(User loggedInUser, String label) {
        ensureSatAdmin(loggedInUser);
        return VirtualHostManagerFactory.getInstance().lookupByLabel(label);
    }

    // todo think about (not) including 'gatherer' word in the api
    /**
     * List all available modules of the gatherer
     *
     * @param loggedInUser the currently logged in user
     * @return List of available module names
     *
     * @xmlrpc.doc List all available modules of the gatherer
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.returntype #array()
     *                       string
     *                    #array_end()
     */
    public Collection<String> listAvailableGathererModules(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return GathererCache.INSTANCE.listAvailableModules();
    }

    /**
     * Get details about a gatherer module. It returns as key the available
     * parameter and the value is a typical default value.
     *
     * @param loggedInUser the currently logged in user
     * @param moduleName the module name
     * @return Map of module details
     *
     * @xmlrpc.doc Get details about a gatherer module. It returns as key the available
     * parameter and the value is a typical default value.
     * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
     * @xmlrpc.param #param_desc("string", "moduleName", "The name of the module")
     * @xmlrpc.returntype map
     */
    public Map<String, String> getGathererModuleDetail(User loggedInUser, String moduleName) {
        ensureSatAdmin(loggedInUser);

        GathererModule gm = GathererCache.INSTANCE.getDetails(moduleName);
        if (gm == null) {
            throw new NoSuchGathererModuleException(moduleName);
        }
        Map<String, String> ret = gm.getParameters();
        ret.put("module", gm.getName());
        return ret;
    }
}
