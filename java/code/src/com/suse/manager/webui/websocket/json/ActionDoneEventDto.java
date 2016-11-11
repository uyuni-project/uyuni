package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 9/28/16.
 */
public class ActionDoneEventDto extends RemoteSaltCommandEventDto {

    public ActionDoneEventDto(String actionType) {
        super("actionDone", null, actionType);
    }

}