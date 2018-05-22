/**
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.sshpush;

/**
 * Simple DTO class used to encapsulate rows queried from the database.
 */
public class SSHPushAction {

    private String actionType;
    private int actionStatus;
    private long actionId;
    private long systemId;
    private String systemName;
    private String minionId;

    /**
     * No arg constructor needed for instantiation.
     */
    public SSHPushAction() {
    }

    /**
     * @return actionType to get
     */
    public String getActionType() {
        return actionType;
    }

    /**
     * @param actionTypeIn to set
     */
    public void setActionType(String actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * @return actionStatus to get
     */
    public int getActionStatus() {
        return actionStatus;
    }

    /**
     * @param actionStatusIn to set
     */
    public void setActionStatus(int actionStatusIn) {
        this.actionStatus = actionStatusIn;
    }

    /**
     * @return actionId to get
     */
    public long getActionId() {
        return actionId;
    }

    /**
     * @param actionIdIn to set
     */
    public void setActionId(long actionIdIn) {
        this.actionId = actionIdIn;
    }

    /**
     * @return systemId to get
     */
    public long getSystemId() {
        return systemId;
    }

    /**
     * @param systemIdIn to set
     */
    public void setSystemId(long systemIdIn) {
        this.systemId = systemIdIn;
    }

    /**
     * @return systemName to get
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * @param systemNameIn to set
     */
    public void setSystemName(String systemNameIn) {
        this.systemName = systemNameIn;
    }

    /**
     * @return minionId to get
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * @param minionIdIn to set
     */
    public void setMinionId(String minionIdIn) {
        this.minionId = minionIdIn;
    }
}
