/**
 * Copyright (c) 2013 Novell
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.redhat.rhn.common.localization.LocalizationService;

/**
 * Simple DTO class used to encapsulate rows queried from the database.
 */
public class SSHPushSystem {

    private String contactMethodLabel;
    private Date earliestAction;
    private long id;
    private Date lastCheckin;
    private String name;

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
}
