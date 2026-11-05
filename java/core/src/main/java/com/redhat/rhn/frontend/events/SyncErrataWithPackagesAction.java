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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.manager.errata.syncwithpackages.SyncErrataWithPackagesServiceImpl;

/**
 * SyncErrataWithPackagesAction
 */
public class SyncErrataWithPackagesAction implements MessageAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msgIn) {
        SyncErrataWithPackagesEvent msg = (SyncErrataWithPackagesEvent) msgIn;
        try {
            Channel target = msg.getTarget();
            if (target != null) {
                new SyncErrataWithPackagesServiceImpl().syncValidated(msg.getUser(), target, msg.getErrata(),
                        msg.isForceRefresh());
            }
        }
        finally {
            msg.deregister();
        }
    }
}
