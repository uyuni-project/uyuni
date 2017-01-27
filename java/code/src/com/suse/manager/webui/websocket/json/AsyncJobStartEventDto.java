package com.suse.manager.webui.websocket.json;

import java.util.List;

/**
 * Created by matei on 9/28/16.
 */
public class AsyncJobStartEventDto extends AbstractSaltEventDto {

    private List<String> minions;

    public AsyncJobStartEventDto() {
        super("asyncJobStart");
    }

    public AsyncJobStartEventDto(String actionType, List<String> minions) {
        super("asyncJobStart", null, actionType);
        this.minions = minions;
    }

    public List<String> getMinions() {
        return minions;
    }

    public void setMinions(List<String> minions) {
        this.minions = minions;
    }
}
