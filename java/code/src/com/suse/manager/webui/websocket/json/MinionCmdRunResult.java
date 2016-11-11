package com.suse.manager.webui.websocket.json;

/**
 * Created by matei on 10/3/16.
 */
public class MinionCmdRunResult extends RemoteSaltCommandEventDto  {

    private String out;

    public MinionCmdRunResult(String minionId, String out) {
        super("runResult", minionId);
        this.out = out;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }
}
