package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 9/27/16.
 */
public class RemoteMinionCommandDto {

    private String target;

    private String command;

    private boolean preview;

    private boolean cancel;

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
