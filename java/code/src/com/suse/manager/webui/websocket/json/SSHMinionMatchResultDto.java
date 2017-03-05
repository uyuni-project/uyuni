package com.suse.manager.webui.websocket.json;

import java.util.List;

/**
 * Created by matei on 3/5/17.
 */
public class SSHMinionMatchResultDto extends AbstractSaltEventDto  {

    private List<String> minions;

    public SSHMinionMatchResultDto(List<String> minionsIn) {
        super("matchSSH");
        this.minions = minionsIn;
    }

    public List<String> getMinions() {
        return minions;
    }
}
