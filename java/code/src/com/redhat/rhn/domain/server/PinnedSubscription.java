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
    private Server server;
    private Long subscriptionId;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Getter for server
     * @return the server associated with the subscription id
     */
    public Server getServer() {
        return server;
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
     * associate a server with a subscription id
     * @param serverIn the Server to associate
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
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
        return new EqualsBuilder().append(getServer(), castOther.getServer())
                .append(getSubscriptionId(), castOther.getSubscriptionId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(getServer())
                .append(getSubscriptionId())
                .toHashCode();
    }
}
