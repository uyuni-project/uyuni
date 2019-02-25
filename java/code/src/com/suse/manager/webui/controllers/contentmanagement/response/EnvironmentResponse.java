package com.suse.manager.webui.controllers.contentmanagement.response;

import java.util.Optional;

public class EnvironmentResponse {

    private String projectLabel;
    private String label;
    private String name;
    private String description;

    public void setProjectLabel(String projectLabel) {
        this.projectLabel = projectLabel;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
