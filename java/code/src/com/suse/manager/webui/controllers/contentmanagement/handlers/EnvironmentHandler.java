package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.suse.manager.webui.controllers.contentmanagement.request.EnvironmentRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

public class EnvironmentHandler {
    private static final Gson GSON = Json.GSON;

    public static EnvironmentRequest getEnvironmentRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), EnvironmentRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    public static HashMap<String,String> validateEnvironmentRequest(EnvironmentRequest envRequest) {
        HashMap<String,String> requestErrors = new HashMap<>();

        if(StringUtils.isEmpty(envRequest.getLabel())) {
            requestErrors.put("label", "Label is required");
        }

        if(StringUtils.isEmpty(envRequest.getName())) {
            requestErrors.put("name", "Name is required");
        }

        return requestErrors;
    }

}
