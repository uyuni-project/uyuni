package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

public class ScheduleMetadata {

    @SerializedName("suma-action-id")
    private long sumaActionId = 0L;

    public ScheduleMetadata(long sumaActionId) {
        this.sumaActionId = sumaActionId;
    }

    public long getSumaActionId() {
        return sumaActionId;
    }
}
