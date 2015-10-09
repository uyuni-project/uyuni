package com.suse.manager.webui.events;

import com.google.gson.annotations.SerializedName;

public class ManagedFileChangedEvent implements Event {

    @SerializedName("$type")
    private final String type = this.getClass().getCanonicalName();
    private final String minionId;
    private final String path;
    private final String diff;

    /**
     * Constructor for creating events.
     *
     * @param minionId the minion id
     * @param path the path of the managed file
     * @param diff the diff of the change
     */
    public ManagedFileChangedEvent(String minionId, String path, String diff) {
        super();
        this.minionId = minionId;
        this.path = path;
        this.diff = diff;
    }

    /**
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }
    
    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the diff
     */
    public String getDiff() {
        return diff;
    }
}
