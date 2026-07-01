/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.proxy.get.formdata;

import static com.suse.proxy.ProxyConfigUtils.getSubscribableMgrpxyChannels;
import static com.suse.proxy.ProxyConfigUtils.isMgrpxyInstalled;
import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import java.util.Set;

/**
 * Pre-condition checks before handling Proxy Config Get Form Data
 * The main goal is to detect blockers that would prevent a server from being converted to a proxy.
 * These may be:
 * a) Server does not have the proxy entitlement and is not entitleable for it.
 * b) Server is not subscribed to the appropriate proxy extension channel (ie, at least one containing the mgrpxy
 * package), it is subscribed to it.
 */
public class ProxyConfigGetFormDataPreConditions implements ProxyConfigGetFormDataContextHandler {

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        Server server = context.getServer();

        if (isAbsent(server)) {
            context.getErrorReport().register("Server not found");
            return;
        }

        if (server.isMgrServer()) {
            context.getErrorReport().register("The system is a Management Server and cannot be converted to a Proxy");
            return;
        }

        if (!server.hasProxyEntitlement() &&
                !context.getSystemEntitlementManager().canEntitleServer(server, EntitlementManager.PROXY)) {
            context.getErrorReport().register("Cannot entitle server ID: {0}", server.getId());
        }

        if (ConfigDefaults.get().isUyuni() || isMgrpxyInstalled(server)) {
            return;
        }

        Set<Channel> subscribableMgrpxyChannels = getSubscribableMgrpxyChannels(server, context.getUser());
        context.setSubscribableChannels(subscribableMgrpxyChannels);
        if (subscribableMgrpxyChannels == null) {
            context.getErrorReport().register(
                    "No channel with mgrpxy package found for server ID: {0}", server.getId());
        }
    }

}
