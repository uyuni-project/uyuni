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

import static com.suse.utils.Predicates.allProvided;
import static com.suse.utils.Predicates.isProvided;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerPath;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.model.ProxyConfig;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Set;

/**
 * This step acquires and maps the proxy configuration data and also the electable parents FQDNs
 */
public class ProxyConfigGetFormDataAcquisitor implements ProxyConfigGetFormDataContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigGetFormDataAcquisitor.class);

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        ProxyConfig proxyConfig = context.getProxyConfig();
        context.setHasCertificates(isProvided(proxyConfig) && allProvided(
                proxyConfig.getRootCA(),
                proxyConfig.getProxyCert(),
                proxyConfig.getProxyKey())
        );
        context.getProxyConfigAsMap().putAll(ProxyConfigUtils.dataMapFromProxyConfig(proxyConfig));
        retrieveElectableParentsFqdn(context);
    }

    /**
     * Retrieves the electable parents FQDNs
     * Electable parents are all the proxy in the organization (except the current server) and also the local manager
     *
     * @param context the context
     */
    private void retrieveElectableParentsFqdn(ProxyConfigGetFormDataContext context) {
        Set<String> parentFqdns = new java.util.HashSet<>();

        java.util.Optional<ServerPath> parentPath = context.getServer().getFirstServerPath();
        if (parentPath.isPresent()) {
            Server parentProxy = parentPath.get().getId().getProxyServer();
            if (parentProxy != null) {
                parentFqdns.addAll(parentProxy.getFqdns().stream()
                        .map(ServerFQDN::getName)
                        .toList());
            }
            else {
                parentFqdns.add(parentPath.get().getHostname());
            }
        }
        else {
            String localManagerFqdn = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME);
            if (isProvided(localManagerFqdn)) {
                parentFqdns.add(localManagerFqdn);
            }
            else {
                LOG.error("Could not determine the Server FQDN. Skipping it as a parent.");
            }
        }

        context.getElectableParentsFqdn().addAll(parentFqdns.stream().sorted().toList());
    }
}
