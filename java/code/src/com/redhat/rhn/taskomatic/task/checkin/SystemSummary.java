/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.checkin;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.redhat.rhn.common.localization.LocalizationService;

/**
 * Simple DTO class used to encapsulate rows queried from the database.
 */
public class SystemSummary {

    private String contactMethodLabel;
    private Date earliestAction;
    private long id;
    private Date lastCheckin;
    private String name;
    private String minionId;
    private boolean rebooting;

    /**
     * No arg constructor needed for instantiation.
     */
    public SystemSummary() {
    }

    /**
     * Constructor.
     * @param idIn system id
     * @param nameIn system name
     * @param minionIdIn minion id
     * @param rebootingIn whether system is rebooting or not
     */
    public SystemSummary(long idIn, String nameIn, String minionIdIn, boolean rebootingIn) {
        this.id = idIn;
        this.name = nameIn;
        this.minionId = minionIdIn;
        this.rebooting = rebootingIn;
    }

    /**
     * Return the contact method label.
     * @return the contactMethodLabel
     */
    public String getContactMethodLabel() {
        return contactMethodLabel;
    }

    /**
     * Set the contact method label.
     * @param contactMethodLabelIn the contactMethodLabel to set
     */
    public void setContactMethodLabel(String contactMethodLabelIn) {
        this.contactMethodLabel = contactMethodLabelIn;
    }

    /**
     * Return the date of earliest action.
     * @return the earliestAction
     */
    public Date getEarliestAction() {
        return earliestAction;
    }

    /**
     * Set the date of earliest action.
     * @param earliestActionIn the earliestAction date to set
     */
    public void setEarliestAction(Date earliestActionIn) {
        this.earliestAction = earliestActionIn;
    }

    /**
     * Return the system id.
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the system id.
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * Get the last checkin as {@link Date}.
     * @return the last checkin date
     */
    public Date getLastCheckin() {
        return lastCheckin;
    }

    /**
     * This is the same code as in the SystemOverview class.
     * @param lastCheckinIn the lastCheckin to set.
     */
    public void setLastCheckin(String lastCheckinIn) {
        if (lastCheckinIn == null) {
            this.lastCheckin = null;
        }
        else {
            try {
                this.lastCheckin = new SimpleDateFormat(
                        LocalizationService.RHN_DB_DATEFORMAT).parse(lastCheckinIn);
            }
            catch (ParseException e) {
                throw new IllegalArgumentException("lastCheckin must be of the: [" +
                        LocalizationService.RHN_DB_DATEFORMAT + "] it was: " +
                        lastCheckinIn);
            }
        }
    }

    /**
     * Return the name of the system.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the system.
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Return the minionId of the system in case it is a minion.
     * @return the minion id or null
     */
    public String getMinionId() {
        return minionId;
    }

    /**
     * Set the minion id of the system.
     * @param minionIdIn the name to set
     */
    public void setMinionId(String minionIdIn) {
        this.minionId = minionIdIn;
    }

    /**
     * @return resumeActionChain to get
     */
    public boolean isRebooting() {
        return rebooting;
    }

    /**
     * @param rebootingIn to set
     */
    public void setRebooting(boolean rebootingIn) {
        this.rebooting = rebootingIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SystemSummary other = (SystemSummary) obj;
        if (id != other.id) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}
