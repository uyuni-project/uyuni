/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.audit;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;

import java.util.Optional;
import java.util.Set;

/**
 * ServerAuditTarget
 */
public class ServerAuditTarget implements AuditTarget {

    private final Server server;

    /**
     * Constructor
     * @param serverIn the server object
     */
    public ServerAuditTarget(Server serverIn) {
        this.server = serverIn;
    }

    @Override
    public Set<Channel> getAssignedChannels() {
        return server.getChannels();
    }

    @Override
    public Optional<SUSEProductSet> getInstalledProductSet() {
        return Optional.ofNullable(server.getInstalledProductSet());
    }

    @Override
    public ChannelArch getCompatibleChannelArch() {
        return server.getServerArch().getCompatibleChannelArch();
    }
}
