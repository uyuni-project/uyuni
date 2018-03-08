package com.suse.manager.model.products;

import com.redhat.rhn.frontend.dto.SetupWizardProductDto;

public class JsonChannel {

    private final String name;
    private final String label;
    private final String summary;
    private final boolean optional;
    private final SetupWizardProductDto.SyncStatus.SyncStage status;

    public JsonChannel(String nameIn, String labelIn, String summeryIn, boolean optionalIn, SetupWizardProductDto.SyncStatus.SyncStage statusIn) {
        this.name = nameIn;
        this.label = labelIn;
        this.summary = summeryIn;
        this.optional = optionalIn;
        this.status = statusIn;
    }

    public SetupWizardProductDto.SyncStatus.SyncStage getStatus() {
        return status;
    }

    public String getLabel() {
        return label;
    }

    public String getName() {
        return name;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isOptional() {
        return optional;
    }
}
