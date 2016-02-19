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

package com.redhat.rhn.domain.server;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Representing Subscriptions pinned to systems
 */
public class PinnedSubscription {

    private Long id;
    private Server systemId;
    private Long subscriptionId;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Getter for systemId
     * @return the systemId associated with the subscription id
     */
    public Server getSystemId() {
        return systemId;
    }

    /**
     * @return the subscriptionId
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * associate a systemId with a subscription id
     * @param systemIdIn the System to associate
     */
    public void setSystemId(Server systemIdIn) {
        this.systemId = systemIdIn;
    }

    /**
     * @param subscriptionId the subscriptionId to set
     */
    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof PinnedSubscription)) {
            return false;
        }
        PinnedSubscription castOther = (PinnedSubscription) other;
        return new EqualsBuilder().append(getSystemId(), castOther.getSystemId())
                .append(getSubscriptionId(), castOther.getSubscriptionId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(getSystemId())
                .append(getSubscriptionId())
                .toHashCode();
    }
}
