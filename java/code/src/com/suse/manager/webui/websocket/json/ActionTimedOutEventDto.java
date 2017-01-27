package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 9/29/16.
 */
public class ActionTimedOutEventDto extends AbstractSaltEventDto {

    public ActionTimedOutEventDto(String minionId, String actionType) {
        super("timedOut", minionId, actionType);
    }
}
