package com.suse.manager.webui.controllers.contentmanagement.request;

import java.util.Optional;

public class EnvironmentRequest {

    private String projectLabel;
    private String predecessorLabel;
    private String label;
    private String name;
    private String description;

    public String getProjectLabel() {
        return projectLabel;
    }

    public String getPredecessorLabel() {
        return predecessorLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
