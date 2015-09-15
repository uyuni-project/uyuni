package com.redhat.rhn.frontend.xmlrpc.system.virtualhostmanager;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

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
    public List<String> listAvailableGathererModules(User loggedInUser) {
        return Collections.emptyList();
    }

    public Map<String, String> getGathererModuleDetail(User loggedInUser, String moduleName) {
        return Collections.emptyMap();
    }

}
