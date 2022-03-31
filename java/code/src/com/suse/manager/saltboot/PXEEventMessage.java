/*
 * Copyright (c) 2022 SUSE LLC
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

package com.suse.manager.saltboot;

import com.redhat.rhn.common.messaging.EventMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class PXEEventMessage implements EventMessage {

    private PXEEvent PXEEvent;

    /**
     * Standard constructor.
     *
     * @param pxeEventIn - 'Saltboot PXE' event
     */
    public PXEEventMessage(PXEEvent pxeEventIn) {
        this.PXEEvent = pxeEventIn;
    }

    /**
     * Gets the event.
     *
     * @return PXEEvent
     */
    public PXEEvent getPXEEventMessage() {
        return PXEEvent;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("PXEEvent", PXEEvent)
                .toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }
}
