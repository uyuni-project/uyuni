/**
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

import com.google.gson.annotations.SerializedName;

/**
 * JSON representation of a pinned match for the matcher UI.
 */
public class PinnedMatch {

    /** The id. */
    private Long id;

    /** The subscription name. */
    @SerializedName("subscription_name")
    private String subscriptionName;

    /** The system name. */
    @SerializedName("system_name")
    private String systemName;

    /** The match. */
    private int match;

    /**
     * Instantiates a new pinned match.
     *
     * @param idIn the id in
     * @param subscriptionNameIn the subscription name in
     * @param systemNameIn the system name in
     * @param matchIn the match in
     */
    public PinnedMatch(Long idIn, String subscriptionNameIn, String systemNameIn,
            int matchIn) {
        this.id = idIn;
        this.subscriptionName = subscriptionNameIn;
        this.systemName = systemNameIn;
        this.match = matchIn;
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
     * Gets the subscription name.
     *
     * @return the subscription name
     */
    public String getSubscriptionName() {
        return subscriptionName;
    }

    /**
     * Sets the subscription name.
     *
     * @param subscriptionNameIn the new subscription name
     */
    public void setSubscriptionName(String subscriptionNameIn) {
        this.subscriptionName = subscriptionNameIn;
    }

    /**
     * Gets the system name.
     *
     * @return the system name
     */
    public String getSystemName() {
        return systemName;
    }

    /**
     * Sets the system name.
     *
     * @param systemNameIn the new system name
     */
    public void setSystemName(String systemNameIn) {
        this.systemName = systemNameIn;
    }

    /**
     * Gets the match.
     *
     * @return the match
     */
    public int getMatch() {
        return match;
    }

    /**
     * Sets the match.
     *
     * @param matchIn the new match
     */
    public void setMatch(int matchIn) {
        this.match = matchIn;
    }
}
