/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;

import com.suse.manager.webui.websocket.VirtNotifications;

/**
 * Virt engine virtual network lifecycle event action handler
 */
public class LibvirtEngineNetworkMessageAction implements MessageAction {
    @Override
    public void execute(EventMessage msg) {
        // Notify that there was a pool change
        VirtNotifications.spreadRefresh("network");
    }
}
