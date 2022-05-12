/*
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

package com.redhat.rhn.frontend.xmlrpc.virtualhostmanager;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchGathererModuleException;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.gatherer.GathererRunner;
import com.suse.manager.model.gatherer.GathererModule;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @apidoc.namespace virtualhostmanager
 * @apidoc.doc Provides the namespace for the Virtual Host Manager methods.
 */
public class VirtualHostManagerHandler extends BaseHandler {

    /**
     * Lists Virtual Host Managers visible to a user
     * @param loggedInUser the currently logged in user
     * @return List of Virtual Host Managers
     *
     * @apidoc.doc Lists Virtual Host Managers visible to a user
     * @apidoc.param #session_key()
     * @apidoc.returntype
     *     #return_array_begin()
     *         $VirtualHostManagerSerializer
     *     #array_end()
     */
    @ReadOnly
    public List<VirtualHostManager> listVirtualHostManagers(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return VirtualHostManagerFactory.getInstance()
                .listVirtualHostManagers(loggedInUser.getOrg());
    }

    /**
     * Creates a Virtual Host Manager from given arguments
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @param moduleName the name of the Gatherer module
     * @param parameters additional parameters (credentials, parameters for
     * virtual-host-gatherer)
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Creates a Virtual Host Manager from given arguments
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label" "Virtual Host Manager label")
     * @apidoc.param #param_desc("string", "moduleName" "the name of the Gatherer module")
     * @apidoc.param #param_desc("parameters", "parameters"
     *         "additional parameters (credentials, parameters for virtual-host-gatherer)")
     * @apidoc.returntype #return_int_success()
     */
    public int create(User loggedInUser, String label, String moduleName,
            Map<String, String> parameters) {
        ensureOrgAdmin(loggedInUser);
        if (StringUtils.isEmpty(label)) {
            throw new IllegalArgumentException("Label cannot not be empty.");
        }
        if (StringUtils.isEmpty(moduleName)) {
            throw new IllegalArgumentException("Module name cannot not be empty.");
        }
        if (parameters == null) {
            throw new InvalidParameterException("Parameters are missing name is missing.");
        }
        if (VirtualHostManagerFactory.getInstance().lookupByLabel(label) != null) {
            throw new InvalidParameterException("Another Virtual Host Manager with the " +
                    "same label already exists.");
        }

        VirtualHostManagerFactory factory = VirtualHostManagerFactory.getInstance();
        if (!factory.isConfigurationValid(moduleName, parameters)) {
            throw new InvalidParameterException("Parameter validation failed.");
        }
        else {
            VirtualHostManager vhm = factory.createVirtualHostManager(label,
                    loggedInUser.getOrg(), moduleName, parameters);
            factory.save(vhm);
            return BaseHandler.VALID;
        }
    }

    /**
     * Deletes a Virtual Host Manager with a given label
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @return 1 if successful, exception otherwise
     *
     * @apidoc.doc Deletes a Virtual Host Manager with a given label
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "Virtual Host Manager label")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        VirtualHostManager manager =
                VirtualHostManagerFactory.getInstance().lookupByLabelAndOrg(label,
                    loggedInUser.getOrg());
        if (manager == null) {
            throw new InvalidParameterException("Virtual Host Manager with label '" +
                    label + "' doesn't exist.");
        }
        VirtualHostManagerFactory.getInstance().delete(manager);
        return BaseHandler.VALID;
    }

    /**
     * Gets details of a Virtual Host Manager with a given label
     *
     * @param loggedInUser the currently logged in user
     * @param label Virtual Host Manager label
     * @return Virtual Host Manager details
     *
     * @apidoc.doc Gets details of a Virtual Host Manager with a given label
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "label", "Virtual Host Manager label")
     * @apidoc.returntype $VirtualHostManagerSerializer
     */
    @ReadOnly
    public VirtualHostManager getDetail(User loggedInUser, String label) {
        ensureOrgAdmin(loggedInUser);
        return VirtualHostManagerFactory.getInstance().lookupByLabel(label);
    }

    /**
     * List all available modules from virtual-host-gatherer
     *
     * @param loggedInUser the currently logged in user
     * @return List of available module names
     *
     * @apidoc.doc List all available modules from virtual-host-gatherer
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "moduleName")
     */
    @ReadOnly
    public Collection<String> listAvailableVirtualHostGathererModules(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return new GathererRunner().listModules().keySet();
    }

    /**
     * Get a list of parameters for a virtual-host-gatherer module. It returns a
     * map of parameters with their typical default values.
     *
     * @param loggedInUser the currently logged in user
     * @param moduleName the module name
     * @return Map of module parameters
     *
     * @apidoc.doc Get a list of parameters for a virtual-host-gatherer module.
     * It returns a map of parameters with their typical default values.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "moduleName", "The name of the module")
     * @apidoc.returntype #param_desc("map", "module_params", "module parameters")
     */
    @ReadOnly
    public Map<String, String> getModuleParameters(User loggedInUser, String moduleName) {
        ensureOrgAdmin(loggedInUser);

        GathererModule gm = new GathererRunner().listModules().get(moduleName);
        if (gm == null) {
            throw new NoSuchGathererModuleException(moduleName);
        }
        Map<String, String> ret = gm.getParameters();
        ret.put("module", gm.getName());
        return ret;
    }
}
