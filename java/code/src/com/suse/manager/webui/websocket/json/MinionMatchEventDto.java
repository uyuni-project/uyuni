package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 9/28/16.
 */
public class MinionMatchEventDto extends RemoteSaltCommandEventDto {

    public MinionMatchEventDto(String minion) {
        super("match", minion);
    }

}
