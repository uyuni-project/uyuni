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

import com.suse.impl.channel.software.SyncFromVendorServiceImpl;
import com.suse.spec.channel.software.SyncFromVendorService;

/**
 * SyncFromVendorErrataAction - executes async vendor errata cloning
 */
public class SyncFromVendorErrataAction implements MessageAction {

    @Override
    public void execute(EventMessage msgIn) {
        SyncFromVendorErrataEvent msg = (SyncFromVendorErrataEvent) msgIn;

        SyncFromVendorService service = new SyncFromVendorServiceImpl();
        service.sync(msg.getUser(), msg.getTargetChannelLabel(), msg.getSyncRequest());
    }
}
