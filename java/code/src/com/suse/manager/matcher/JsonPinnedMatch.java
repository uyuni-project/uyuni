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

package com.suse.manager.matcher;

/**
 * JSON representation of a Pinned Match
 */
public class JsonPinnedMatch {

    private Long systemId;
    private Long subscriptionId;

    /**
     * Constructor
     * @param systemIdIn the system Id
     * @param subscriptionIdIn the subscription Id (order Item Id)
     */
    public JsonPinnedMatch(Long systemIdIn, Long subscriptionIdIn) {
        systemId = systemIdIn;
        subscriptionId = subscriptionIdIn;
    }

    /**
     * @return the systemId
     */
    public Long getSystemId() {
        return systemId;
    }

    /**
     * @return the subscriptionId
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param systemIdIn the systemId to set
     */
    public void setSystemId(Long systemIdIn) {
        this.systemId = systemIdIn;
    }

    /**
     * @param subscriptionIdIn the subscriptionId to set
     */
    public void setSubscriptionId(Long subscriptionIdIn) {
        this.subscriptionId = subscriptionIdIn;
    }
}
