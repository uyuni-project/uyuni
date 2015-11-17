package com.suse.manager.webui.controllers;

import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;

import java.util.HashMap;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;
import spark.Response;

/**
 * todo all javadocs
 * todo conventions, conventions, conventions!!!!
 *  - jade template filenames
 *  - spark parameters
 *  - spark endpoints!
 *  todo polish the csrf machinery
 */
public class VirtualHostManagerController {
    public static ModelAndView getAll(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("virtualHostManagers", getFactory()
                .listVirtualHostManagers(user.getOrg()));
        return new ModelAndView(data, "virtualhostmanager/all.jade");
    }

    private static VirtualHostManagerFactory getFactory() {
        return VirtualHostManagerFactory.getInstance();
    }
}
