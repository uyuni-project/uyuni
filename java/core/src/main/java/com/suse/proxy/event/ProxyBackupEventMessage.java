/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.event;

import com.redhat.rhn.common.messaging.EventMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class ProxyBackupEventMessage implements EventMessage {
    private final ProxyBackupEvent proxyEvent;

    /**
     * Standard constructor.
     *
     * @param proxyEventIn - 'Proxy Backup' event
     */
    public ProxyBackupEventMessage(ProxyBackupEvent proxyEventIn) {
        this.proxyEvent = proxyEventIn;
    }

    /**
     * Gets the event.
     *
     * @return PXEEvent
     */
    public ProxyBackupEvent getProxyBackupEvent() {
        return this.proxyEvent;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ProxyBackupEvent", proxyEvent)
                .toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }
}
