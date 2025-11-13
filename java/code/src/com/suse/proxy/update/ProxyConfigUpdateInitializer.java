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

package com.suse.proxy.update;

import static java.util.Collections.singleton;

import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Ensures the server is properly initialized for proxy configuration.
 * This means:
 * a) If server does not have a {@link ProxyInfo}, it is created.
 * b) If server does not have a proxy entitlement, it is added.
 * c) If server is not subscribed to the appropriate proxy extension channel,it is subscribed to it.
 */
public class ProxyConfigUpdateInitializer implements ProxyConfigUpdateContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigUpdateInitializer.class);

    private MinionServer minion;
    private User user;

    @Override
    public void handle(ProxyConfigUpdateContext context) {
        this.minion = context.getProxyMinion();
        this.user = context.getUser();

        ensureServerHasProxyInfo();

        if (!ensureServerHasProxyEntitlement(context.getSystemEntitlementManager())) {
            context.getErrorReport().register("Failed to add proxy entitlement to server ID {0}", minion.getId());
        }

        if (!ensureMgrpxyIsAvailable(context)) {
            context.getErrorReport().register(
                    "Failed to subscribe to appropriate proxy extension channel for server ID {0}", minion.getId());
        }
    }

    /**
     * Ensures that the server has a {@link ProxyInfo}.
     */
    private void ensureServerHasProxyInfo() {
        if (minion.getProxyInfo() != null) {
            return;
        }

        LOG.debug("ProxyInfo not found for server. Creating new ProxyInfo for server ID: {}", minion.getId());
        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setServer(minion);
        minion.setProxyInfo(proxyInfo);
        SystemManager.updateSystemOverview(minion.getId());
    }

    /**
     * Ensures server has a proxy entitlement
     * @param systemEntitlementManager the System Entitlement Manager
     * @return true if the server has proxy entitlement, false otherwise
     */
    private boolean ensureServerHasProxyEntitlement(SystemEntitlementManager systemEntitlementManager) {
        if (minion.hasProxyEntitlement()) {
            return true;
        }

        ValidatorResult result = systemEntitlementManager.addEntitlementToServer(minion, EntitlementManager.PROXY);
        if (result.hasErrors()) {
            LOG.error("Failed to add proxy entitlement to server. ID: {}, Errors: {}",
                    minion.getId(), result.getErrors());
        }

        return !result.hasErrors();
    }

     /**
      * Ensure mgrpxy package is available on the server by subscribing to appropriate channels.
      * @param context the ProxyConfigGetFormDataContext
      * @return true if mgrpxy is available, false otherwise
      */
    private boolean ensureMgrpxyIsAvailable(ProxyConfigUpdateContext context) {
        Set<Channel> subscribableChannels = context.getSubscribableChannels();
        if (context.getSubscribableChannels().isEmpty()) {
            return true;
        }

        Set<Channel> newChildChannels = new HashSet<>(minion.getChildChannels());
        newChildChannels.addAll(subscribableChannels);

        try {
            ActionChainManager.scheduleSubscribeChannelsAction(user,
                    singleton(minion.getId()),
                    Optional.ofNullable(minion.getBaseChannel()),
                    newChildChannels,
                    new Date(System.currentTimeMillis() - 1000), null);
        }
        catch (TaskomaticApiException e) {
            LOG.error("Failed to subscribe server to proxy extensions channels {}. ID: {}, Error: {}",
                    subscribableChannels, minion.getId(), e.getMessage());
            return false;
        }
        return true;
    }
}
