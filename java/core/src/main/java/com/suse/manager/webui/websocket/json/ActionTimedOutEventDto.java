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
 * Action timed out DTO.
 */
public class ActionTimedOutEventDto extends AbstractSaltEventDto {

    private boolean timedOutSSH;

    /**
     * @param minionId the minion id
     * @param actionType the action type
     */
    public ActionTimedOutEventDto(String minionId, String actionType) {
        super("timedOut", minionId, actionType);
    }

    /**
     * @param timedOutSSHIn waiting for salt-ssh timed out
     * @param actionType the action type
     */
    public ActionTimedOutEventDto(boolean timedOutSSHIn, String actionType) {
        super("timedOut", null, actionType);
        this.timedOutSSH = timedOutSSHIn;
    }

    /**
     * @return waiting for salt-ssh timed out
     */
    public boolean isTimedOutSSH() {
        return timedOutSSH;
    }
}
