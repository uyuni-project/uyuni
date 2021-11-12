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

package com.redhat.rhn.domain.reactor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * POJO for a suseSaltEvent row.
 */
public class SaltEvent {
    private long id;
    private String minionId;
    private String data;
    private int queue;

    /**
     * Standard constructor
     * @param idIn the id
     * @param minionIdIn the minionId id
     * @param dataIn the data
     * @param queueIn the number of the queue handling the event
     */
    public SaltEvent(long idIn, String minionIdIn, String dataIn, int queueIn) {
        this.id = idIn;
        this.minionId = minionIdIn;
        this.data = dataIn;
        this.queue = queueIn;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(long idIn) {
        id = idIn;
    }

    /**
     * Gets the minion id.
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * Sets the minion id.
     * @param minionIdIn the new id
     */
    public void setMinionId(String minionIdIn) {
        this.minionId = minionId;
    }

    /**
     * Gets the data.
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * Sets the data.
     * @param dataIn the new data
     */
    public void setData(String dataIn) {
        data = dataIn;
    }

    /**
     * @return Returns the queue.
     */
    public int getQueue() {
        return queue;
    }


    /**
     * @param queueIn The queue to set.
     */
    public void setQueue(int queueIn) {
        queue = queueIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SaltEvent)) {
            return false;
        }
        SaltEvent otherSaltEvent = (SaltEvent) other;
        return new EqualsBuilder()
                .append(getId(), otherSaltEvent.getId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getId())
                .toHashCode();
    }
}
