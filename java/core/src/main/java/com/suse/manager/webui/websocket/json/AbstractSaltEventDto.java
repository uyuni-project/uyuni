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
 * Base class for UI DTOs to be transferred via websockets.
 */
public abstract class AbstractSaltEventDto {

    private String type;

    private String minion;

    private String actionType;

    /**
     * @param typeIn type of the event
     */
    protected AbstractSaltEventDto(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @param typeIn type of the event
     * @param minionIn minion id
     */
    protected AbstractSaltEventDto(String typeIn, String minionIn) {
        this.type = typeIn;
        this.minion = minionIn;
    }

    /**
     * @param typeIn type of the event
     * @param minionIn minion id
     * @param actionTypeIn action type
     */
    protected AbstractSaltEventDto(String typeIn, String minionIn, String actionTypeIn) {
        this.type = typeIn;
        this.minion = minionIn;
        this.actionType = actionTypeIn;
    }

    /**
     * @return type of the event
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn type of the event
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @return the minion id
     */
    public String getMinion() {
        return minion;
    }

    /**
     * @param minionIn the minion id
     */
    public void setMinion(String minionIn) {
        this.minion = minionIn;
    }

    /**
     * @return the action type
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * @param actionTypeIn the action type
     */
    public void setActionType(String actionTypeIn) {
        this.actionType = actionTypeIn;
    }
}
