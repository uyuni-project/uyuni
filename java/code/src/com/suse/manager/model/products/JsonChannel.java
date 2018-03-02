package com.suse.manager.model.products;

import com.suse.mgrsync.MgrSyncStatus;

public class JsonChannel {

    private final String name;
    private final String label;
    private final String summary;
    private final boolean optional;
    private final MgrSyncStatus status;

    public JsonChannel(String nameIn, String labelIn, String summeryIn, boolean optionalIn, MgrSyncStatus statusIn) {
        this.name = nameIn;
        this.label = labelIn;
        this.summary = summeryIn;
        this.optional = optionalIn;
        this.status = statusIn;
    }

    public MgrSyncStatus getStatus() {
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
