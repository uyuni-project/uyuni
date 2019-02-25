package com.suse.manager.webui.controllers.contentmanagement.response;

public class ProjectHistoryEntryResponse {
    private String message;
    private Long version;

    public void setMessage(String message) {
        this.message = message;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
