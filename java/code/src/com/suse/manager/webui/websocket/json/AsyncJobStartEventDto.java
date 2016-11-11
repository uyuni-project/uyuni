package com.suse.manager.webui.websocket.json;

import java.util.List;

/**
 * Created by matei on 9/28/16.
 */
public class AsyncJobStartEventDto extends RemoteSaltCommandEventDto {

    private String jid;
    private List<String> minions;

    public AsyncJobStartEventDto() {
        super("asyncJobStart");
    }

    public AsyncJobStartEventDto(String actionType, String jid, List<String> minions) {
        super("asyncJobStart", null, actionType);
        this.jid = jid;
        this.minions = minions;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public List<String> getMinions() {
        return minions;
    }

    public void setMinions(List<String> minions) {
        this.minions = minions;
    }
}
