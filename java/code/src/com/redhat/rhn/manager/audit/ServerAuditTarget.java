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
import com.redhat.rhn.domain.product.CachingSUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.server.Server;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * ServerAuditTarget
 */
public class ServerAuditTarget implements AuditTarget {

    private final Server server;

    private final CachingSUSEProductFactory productFactory;

    /**
     * Constructor
     * @param serverIn the server object
     * @param productFactoryIn the factory object
     */
    public ServerAuditTarget(Server serverIn, CachingSUSEProductFactory productFactoryIn) {
        this.server = serverIn;
        this.productFactory = productFactoryIn;
    }

    @Override
    public Set<Channel> getAssignedChannels() {
        return server.getChannels();
    }

    @Override
    public List<SUSEProduct> getSUSEProducts() {
        return productFactory.map(server.getInstalledProducts()).collect(toList());
    }

    @Override
    public ChannelArch getCompatibleChannelArch() {
        return server.getServerArch().getCompatibleChannelArch();
    }
}
