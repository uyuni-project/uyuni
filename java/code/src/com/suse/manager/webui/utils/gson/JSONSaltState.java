package com.suse.manager.webui.utils.gson;

/**
 * Created by matei on 2/16/16.
 */
public class JSONSaltState {

    private String name;

    private boolean assigned;

    public JSONSaltState(String name, boolean assigned) {
        this.name = name;
        this.assigned = assigned;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }
}
