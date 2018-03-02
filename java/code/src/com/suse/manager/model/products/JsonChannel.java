package com.suse.manager.model.products;

import com.suse.mgrsync.MgrSyncStatus;

public class JsonChannel {

    private final String name;
    private final String label;
    private final String summery;
    private final boolean optional;
    private final MgrSyncStatus status;

    public JsonChannel(String nameIn, String labelIn, String summeryIn, boolean optionalIn, MgrSyncStatus statusIn) {
        this.name = nameIn;
        this.label = labelIn;
        this.summery = summeryIn;
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

    public String getSummery() {
        return summery;
    }

    public boolean isOptional() {
        return optional;
    }
}
