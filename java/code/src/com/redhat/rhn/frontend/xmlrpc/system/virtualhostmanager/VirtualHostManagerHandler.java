package com.redhat.rhn.frontend.xmlrpc.system.virtualhostmanager;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.gatherer.GathererCache;
import com.suse.manager.model.gatherer.GathererModule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public Set<String> listAvailableGathererModules(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        return GathererCache.INSTANCE.listAvailableModules();
    }

    public Map<String, String> getGathererModuleDetail(User loggedInUser, String moduleName) {
        ensureSatAdmin(loggedInUser);

        GathererModule gm = GathererCache.INSTANCE.getDetails(moduleName);
        Map<String, String> ret = gm.getParameter();
        ret.put("module", gm.getName());
        return ret;
    }
}
