package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.security.CSRFTokenValidator;
import com.redhat.rhn.domain.user.User;
import com.suse.manager.webui.services.impl.SaltAPIService;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by matei on 2/1/16.
 */
public class StateCatalogController {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
            .serializeNulls()
            .create();

    public static ModelAndView show(Request request, Response response, User user) {
        Map<String, Object> data = new HashMap<>();
        data.put("csrf_token", CSRFTokenValidator.getToken(request.session().raw()));
        return new ModelAndView(data, "state_catalog/show.jade");
    }

    public static String data(Request request, Response response, User user) {

        List<String> data = SaltAPIService.INSTANCE.getOrgStates(user.getOrg().getId());

        response.type("application/json");
        return GSON.toJson(data);
    }

}
