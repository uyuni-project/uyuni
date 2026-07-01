/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;

import com.suse.spec.channel.software.dto.SyncRequest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.Transaction;

/**
 * SyncFromVendorErrataEvent - publish event to clone vendor errata into a channel.
 */
public class SyncFromVendorErrataEvent implements EventDatabaseMessage {

    private final Transaction transaction;
    private final Long userId;
    private final String targetChannelLabel;
    private final SyncRequest syncRequest;

    /**
     * Constructor
     *
     * @param userIn the user
     * @param targetChannelLabelIn channel to clone errata into
     * @param syncRequestIn request parameters with sync details
     */
    public SyncFromVendorErrataEvent(User userIn, String targetChannelLabelIn, SyncRequest syncRequestIn) {
        userId = userIn.getId();
        targetChannelLabel = targetChannelLabelIn;
        syncRequest = new SyncRequest(
                syncRequestIn.criteria(),
                syncRequestIn.operation(),
                false,
                syncRequestIn.alignModules(),
                syncRequestIn.forceRefresh()
        );
        transaction = HibernateFactory.getSession().getTransaction();
    }

    @Override
    public String toText() {
        return "";
    }

    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    public User getUser() {
        return UserFactory.lookupById(userId);
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    public String getTargetChannelLabel() {
        return targetChannelLabel;
    }

    public SyncRequest getSyncRequest() {
        return syncRequest;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        SyncFromVendorErrataEvent that = (SyncFromVendorErrataEvent) oIn;
        return new EqualsBuilder()
                .append(transaction, that.transaction)
                .append(userId, that.userId)
                .append(targetChannelLabel, that.targetChannelLabel)
                .append(syncRequest, that.syncRequest)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(transaction)
                .append(userId)
                .append(targetChannelLabel)
                .append(syncRequest)
                .toHashCode();
    }
}
