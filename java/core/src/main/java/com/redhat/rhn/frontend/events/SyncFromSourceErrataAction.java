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

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;

import com.suse.impl.channel.software.SyncFromSourceServiceImpl;
import com.suse.spec.channel.software.SyncFromSourceService;

/**
 * CloneErrataAction
 */
public class SyncFromSourceErrataAction implements MessageAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msgIn) {
        SyncFromSourceErrataEvent msg = (SyncFromSourceErrataEvent) msgIn;

        SyncFromSourceService service = new SyncFromSourceServiceImpl();
        service.sync(msg.getUser(), msg.getSourceChannelLabel(), msg.getTargetChannelLabel(), msg.getSyncRequest());
    }

}
