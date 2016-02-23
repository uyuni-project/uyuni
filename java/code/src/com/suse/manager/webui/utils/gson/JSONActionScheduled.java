package com.suse.manager.webui.utils.gson;

/**
 * Created by matei on 2/23/16.
 */
public class JSONActionScheduled {

    private int actionId;

    // TODO other attributes?

    public JSONActionScheduled(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }
}
