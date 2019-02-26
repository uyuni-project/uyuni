package com.suse.manager.webui.controllers.contentmanagement.handlers;

import com.suse.manager.webui.controllers.contentmanagement.request.ProjectPropertiesRequest;
import com.suse.manager.webui.controllers.contentmanagement.request.ProjectRequest;
import com.suse.utils.Json;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;

import spark.Request;
import spark.Spark;

public class ProjectHandler {
    private static final Gson GSON = Json.GSON;

    public static ProjectPropertiesRequest getProjectPropertiesRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectPropertiesRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    public static ProjectRequest getProjectRequest(Request req) {
        try {
            return GSON.fromJson(req.body(), ProjectRequest.class);
        }
        catch (JsonParseException e) {
            throw Spark.halt(HttpStatus.SC_BAD_REQUEST);
        }
    }

    public static HashMap<String,String> validateProjectPropertiesRequest(ProjectPropertiesRequest projPropsRequest) {
        HashMap<String,String> requestErrors = new HashMap<>();

        if(StringUtils.isEmpty(projPropsRequest.getLabel())) {
            requestErrors.put("label", "Label is Required");
        }

        if(StringUtils.isEmpty(projPropsRequest.getName())) {
            requestErrors.put("name", "Name is Required");
        }

        return requestErrors;
    }

    public static HashMap<String,String> validateProjectRequest(ProjectRequest projectRequest) {
        HashMap<String,String> requestErrors = new HashMap<>();

        requestErrors.putAll(validateProjectPropertiesRequest(projectRequest.getProperties()));

        return requestErrors;
    }


}
