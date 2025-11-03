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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.proxy.get.formdata;

import static com.suse.proxy.get.ProxyConfigGetFacadeImpl.MGRPXY;
import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * Check if mgrpxy is installed on the server
     * @param server the server to check
     * @return true if installed, false otherwise
     */
    private boolean isMgrpxyInstalled(Server server) {
        return PackageManager.shallowSystemPackageList(server.getId())
                .stream().anyMatch(p -> p.getName().equals(MGRPXY));
    }

    /**
     * Returns the set of channels providing the mgrpxy package that should be subscribed to.
     * Logic:
     * - If mgrpxy is already available from a subscribed channel, returns an empty set.
     * - If mgrpxy is available only from non-subscribed channels, returns those channels.
     * - If no channels provide mgrpxy at all, returns {@code null}.
     *
     * @param server the server to inspect
     * @param user   the user requesting the channels
     * @return set of subscribable channels providing mgrpxy, an empty set if already subscribed,
     * or {@code null} if unavailable
     */
    private Set<Channel> getSubscribableMgrpxyChannels(Server server, User user) {
        Channel baseChannel = server.getBaseChannel();
        if (baseChannel == null) {
            return null;
        }

        Map<Boolean, Set<Channel>> channelsBySubscription =
                collectAccessibleChannels(baseChannel, user).stream()
                        .filter(c -> !ChannelManager.listLatestPackagesEqual(c.getId(), MGRPXY).isEmpty())
                        .collect(Collectors.partitioningBy(
                                server::isSubscribed,
                                Collectors.toSet()
                        ));

        Set<Channel> subscribed = channelsBySubscription.getOrDefault(true, Set.of());
        Set<Channel> unsubscribed = channelsBySubscription.getOrDefault(false, Set.of());

        if (!subscribed.isEmpty()) {
            return Set.of();
        }

        return unsubscribed.isEmpty() ? null : unsubscribed;
    }

    /**
     * Recursively collects accessible channels for a given user.
     * @param channel the channel to check
     * @param user the user requesting access
     * @return a set of accessible channels
     */
    private Set<Channel> collectAccessibleChannels(Channel channel, User user) {
        Set<Channel> collected = new HashSet<>();
        collected.add(channel);
        for (Channel child : channel.getAccessibleChildrenFor(user)) {
            collected.addAll(collectAccessibleChannels(child, user));
        }
        return collected;
    }

}
