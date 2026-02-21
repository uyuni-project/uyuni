/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.server;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Representing Subscriptions pinned to systems
 */
@Entity
@Table(name = "susePinnedSubscription")
public class PinnedSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "suse_pinsub_seq")
    @SequenceGenerator(name = "suse_pinsub_seq", sequenceName = "suse_pinsub_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "system_id")
    @SerializedName("system_id")
    private Long systemId;

    @Column(name = "subscription_id")
    @SerializedName("subscription_id")
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
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * associate a systemId with a subscription id
     * @param systemIdIn the System to associate
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PinnedSubscription castOther)) {
            return false;
        }
        return new EqualsBuilder().append(getSystemId(), castOther.getSystemId())
                .append(getSubscriptionId(), castOther.getSubscriptionId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getSystemId())
                .append(getSubscriptionId())
                .toHashCode();
    }
}
