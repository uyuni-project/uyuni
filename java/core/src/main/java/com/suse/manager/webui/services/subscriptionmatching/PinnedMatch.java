/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services.subscriptionmatching;

/**
 * JSON representation of a pinned match for the matcher UI.
 */
public class PinnedMatch {

    /** The id. */
    private Long id;

    /** The subscription id. */
    private Long subscriptionId;

    /** The system id. */
    private Long systemId;

    /** The pin status. */
    private String status;

    /**
     * Instantiates a new pinned match.
     *
     * @param idIn the id
     * @param subscriptionIdIn the subscription id
     * @param systemIdIn the system id
     * @param statusIn the status
     */
    public PinnedMatch(Long idIn, Long subscriptionIdIn, Long systemIdIn,
            String statusIn) {
        this.id = idIn;
        this.subscriptionId = subscriptionIdIn;
        this.systemId = systemIdIn;
        this.status = statusIn;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the subscription id.
     *
     * @return the subscription id
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * Sets the subscription id.
     *
     * @param subscriptionIdIn the new subscription id
     */
    public void setSubscriptionId(Long subscriptionIdIn) {
        subscriptionId = subscriptionIdIn;
    }

    /**
     * Gets the system id.
     *
     * @return the system id
     */
    public Long getSystemId() {
        return systemId;
    }

    /**
     * Sets the system id.
     *
     * @param systemIdIn the new system id
     */
    public void setSystemId(Long systemIdIn) {
        systemId = systemIdIn;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status.
     *
     * @param statusIn the new status
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
    }
}
