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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Ensures the correct behavior of the "Convert to Proxy" button.
 * If the server is convertible to a proxy and:
 * a) Does not have the proxy entitlement, it is added.
 * b) Does not have a {@link ProxyInfo}, it is created.
 */
public class ProxyConfigGetFormDataProxyInitializer implements ProxyConfigGetFormDataContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigGetFormDataProxyInitializer.class);
    private static final SystemEntitlementManager SYSTEM_ENTITLEMENT_MANAGER =
            GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER;

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        Server server = context.getServer();
        if (!server.isConvertibleToProxy()) {
            return;
        }

        ensureServerHasProxyInfo(server);
        if (!ensureServerHasProxyEntitlement(context)) {
            context.setInitFailMessage("Failed to add proxy entitlement to server.");
        }
    }

    /**
     * Ensures that the server has a {@link ProxyInfo}.
     *
     * @param server the server
     */
    public void ensureServerHasProxyInfo(Server server) {
        if (server.getProxyInfo() != null) {
            return;
        }

        ProxyInfo proxyInfo = new ProxyInfo();
        proxyInfo.setServer(server);
        server.setProxyInfo(proxyInfo);
        SystemManager.updateSystemOverview(server.getId());
    }

    private boolean ensureServerHasProxyEntitlement(ProxyConfigGetFormDataContext context) {
        Server server = context.getServer();
        if (server.hasProxyEntitlement()) {
            return true;
        }

        if (!SYSTEM_ENTITLEMENT_MANAGER.canEntitleServer(server, EntitlementManager.PROXY)) {
            LOG.error("Server is not entitleable for proxy entitlement. ID: {}", server.getId());
            return false;
        }

        ValidatorResult result = SYSTEM_ENTITLEMENT_MANAGER.addEntitlementToServer(server, EntitlementManager.PROXY);
        if (!result.getErrors().isEmpty()) {
            LOG.error("Failed to add proxy entitlement to server. ID: {}, Errors: {}",
                    server.getId(), result.getErrors());
            return false;
        }

        return true;
    }

}
