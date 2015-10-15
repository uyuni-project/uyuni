/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.events;

import com.google.gson.annotations.SerializedName;

/**
 * Event to signalize that a managed file has been changed on a minion.
 */
public class ManagedFileChangedEvent implements Event {

    @SerializedName("$type")
    private final String type = this.getClass().getCanonicalName();
    private final String minionId;
    private final String path;
    private final String diff;

    /**
     * Constructor for creating events.
     *
     * @param minionIdIn the minion id
     * @param pathIn the path of the managed file
     * @param diffIn the diff of the change
     */
    public ManagedFileChangedEvent(String minionIdIn, String pathIn, String diffIn) {
        super();
        this.minionId = minionIdIn;
        this.path = pathIn;
        this.diff = diffIn;
    }

    /**
     * Get the minion id.
     *
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * Get the path of the managed file that has been changed.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Get the diff of the change.
     *
     * @return the diff
     */
    public String getDiff() {
        return diff;
    }
}
