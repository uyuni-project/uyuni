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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.gatherer.GathererCache;
import com.suse.manager.model.gatherer.GathererModule;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * todo javadoc!
 */
public class VirtualHostManagerHandler extends BaseHandler {

    public List<Object> listVirtualHostManagers(User loggedInUser) {
        return null;
    }

    public int create(User loggedInUser, String label, String moduleName,
            Map<String, String> parameters) {
        return 1;
    }

    public int delete(User loggedInUser, String label) {
        return 1;
    }

    public Map<String, String> getDetail(User loggedInUser, String label) {
        return Collections.emptyMap();
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
        Map<String, String> ret = gm.getParameter();
        ret.put("module", gm.getName());
        return ret;
    }
}
