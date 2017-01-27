package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 1/27/17.
 */
public class ActionErrorEventDto extends AbstractSaltEventDto {

    private String message;

    public ActionErrorEventDto(String minionId, String message) {
        super("error", minionId);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
