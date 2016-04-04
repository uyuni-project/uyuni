package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

public class StateApplyResult<R> {

    private String comment;
    private String name;
    @SerializedName("start_time")
    private String startTime;
    private boolean result;
    private double duration;
    @SerializedName("__run_num__")
    private int runNum;
    private R changes;

    public String getComment() {
        return comment;
    }

    public String getName() {
        return name;
    }

    public String getStartTime() {
        return startTime;
    }

    public boolean isResult() {
        return result;
    }

    public double getDuration() {
        return duration;
    }

    public int getRunNum() {
        return runNum;
    }

    public R getChanges() {
        return changes;
    }
}
