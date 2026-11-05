/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.errata.AsyncErrataCloneCounter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Transaction;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Publish event to synchronize errata of interest, already found and validated, with their packages into
 * a channel.
 */
public class SyncErrataWithPackagesEvent implements EventDatabaseMessage {

    private static final Logger LOG = LogManager.getLogger(SyncErrataWithPackagesEvent.class);

    private final Long targetChannelId;
    private final Map<Long, Long> errata = new HashMap<>();
    private final boolean forceRefresh;
    private final Long userId;
    private final Transaction txn;

    /**
     * constructor
     * @param targetIn the target channel
     * @param errataIn the errata of interest mapped to their source channel (null when found without one)
     * @param forceRefreshIn refresh the newest package cache and schedule repodata regeneration
     * @param userIn the user
     */
    public SyncErrataWithPackagesEvent(
            Channel targetIn,
            Map<Errata, Channel> errataIn,
            boolean forceRefreshIn,
            User userIn
    ) {
        targetChannelId = targetIn.getId();
        errataIn.forEach((e, source) -> errata.put(e.getId(), source == null ? null : source.getId()));
        forceRefresh = forceRefreshIn;
        userId = userIn.getId();
        txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        // really a noop
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction getTransaction() {
        return txn;
    }

    /**
     * @return the target channel
     */
    public Channel getTarget() {
        return ChannelFactory.lookupById(targetChannelId);
    }

    /**
     * @return the errata of interest mapped to their source channel (null when found without one).
     * Skip when erratum or source channel was deleted since the event was published
     */
    public Map<Errata, Channel> getErrata() {
        Map<Errata, Channel> result = new LinkedHashMap<>();
        errata.forEach((errataId, sourceId) -> {
            Errata erratum = ErrataFactory.lookupById(errataId);
            Channel source = sourceId == null ? null : ChannelFactory.lookupById(sourceId);
            if (erratum != null && (sourceId == null || source != null)) {
                result.put(erratum, source);
            }
            else {
                LOG.warn("Skipping erratum {} with source channel {} in the synchronization into channel {}: " +
                        "no longer available", errataId, sourceId, targetChannelId);
            }
        });
        return result;
    }

    /**
     * @return true to refresh the newest package cache and schedule repodata regeneration
     */
    public boolean isForceRefresh() {
        return forceRefresh;
    }

    /**
     * @return the user
     */
    public User getUser() {
        return UserFactory.lookupById(userId);
    }

    /**
     * Register the async job with the errata clone counter
     */
    public void register() {
        AsyncErrataCloneCounter.getInstance().addAsyncErrataCloneJob(targetChannelId);
    }

    /**
     * De-register the async job from the errata clone counter
     */
    public void deregister() {
        AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(targetChannelId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return userId;
    }
}
