/*
 * Copyright (c) 2017 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.websocket.json;

/**
 * Execute an action (run command/preview/cancel) on the target minions DTO.
 */
public class ExecuteMinionActionDto {

    private String target;

    private String command;

    private boolean preview;

    private boolean cancel;

    /**
     * @return minion target expression
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param targetIn minion target expression
     */
    public void setTarget(String targetIn) {
        this.target = targetIn;
    }

    /**
     * @return the command to run
     */
    public String getCommand() {
        return command;
    }

    /**
     * @param commandIn the command to run
     */
    public void setCommand(String commandIn) {
        this.command = commandIn;
    }

    /**
     * @return only preview the target minions
     */
    public boolean isPreview() {
        return preview;
    }

    /**
     * @param previewIn only preview the target minions
     */
    public void setPreview(boolean previewIn) {
        this.preview = previewIn;
    }

    /**
     * @return cancel waiting for results from minions
     */
    public boolean isCancel() {
        return cancel;
    }

    /**
     * @param cancelIn cancel waiting for results from minions
     */
    public void setCancel(boolean cancelIn) {
        this.cancel = cancelIn;
    }
}
