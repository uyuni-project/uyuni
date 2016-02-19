package com.suse.manager.webui.utils.gson;

import java.util.Set;

/**
 * Created by matei on 2/19/16.
 */
public class JSONServerSaltStates {

    /** Server id */
    private long sid;

    private Set<JSONSaltState> saltStates;

    public long getServerId() {
        return sid;
    }

    public Set<JSONSaltState> getSaltStates() {
        return saltStates;
    }
}
