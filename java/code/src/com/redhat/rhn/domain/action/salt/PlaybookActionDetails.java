/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.domain.action.ActionChild;

/**
 * PlaybookActionDetails - Class representation of the table rhnActionPlaybook.
 */
public class PlaybookActionDetails extends ActionChild {

    private long id;
    private long actionId;
    private String playbookPath;
    private String inventoryPath;
    private boolean testMode;
    private boolean flushCache;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return the action id
     */
    public long getActionId() {
        return actionId;
    }

    /**
     * @param actionIdIn the action id to set
     */
    public void setActionId(long actionIdIn) {
        this.actionId = actionIdIn;
    }

    public String getPlaybookPath() {
        return playbookPath;
    }

    public void setPlaybookPath(String playbookPathIn) {
        this.playbookPath = playbookPathIn;
    }

    public String getInventoryPath() {
        return inventoryPath;
    }

    public void setInventoryPath(String inventoryPathIn) {
        this.inventoryPath = inventoryPathIn;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testModeIn) {
        this.testMode = testModeIn;
    }

    public boolean isFlushCache() {
        return flushCache;
    }

    public void setFlushCache(boolean flushCacheIn) {
        this.flushCache = flushCacheIn;
    }
}
