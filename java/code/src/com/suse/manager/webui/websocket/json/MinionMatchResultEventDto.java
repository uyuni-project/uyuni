package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 9/28/16.
 */
public class MinionMatchResultEventDto extends AbstractSaltEventDto {

    public MinionMatchResultEventDto(String minion) {
        super("match", minion);
    }

}
