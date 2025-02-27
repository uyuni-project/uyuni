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

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_TAG;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_URL;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.utils.Predicates.isAbsent;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.notification.types.Version;
import com.redhat.rhn.frontend.dto.OrgProxyServer;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.proxy.ProxyConfigUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This step acquired the data required for the proxy configuration form
 * But also sets a few defaults
 */
public class ProxyConfigGetFormDataAcquisitor implements ProxyConfigGetFormDataContextHandler {
    private static final Logger LOG = LogManager.getLogger(ProxyConfigGetFormDataAcquisitor.class);

    private static final String DEFAULT_REGISTRY_UYUNI_URL = "registry.opensuse.org/uyuni/";
    private static final String DEFAULT_UYUNI_REGISTRY_TAG = "latest";

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        context.setProxyConfigAsMap(ProxyConfigUtils.dataMapFromProxyConfig(context.getProxyConfig()));
        ensureRegistryBaseForNewProxyConfig(context);
        retrieveElectableParentsFqdn(context);
    }

    /**
     * Ensures that the registry base is set for a new proxy configuration
     *
     * @param context the context
     */
    private void ensureRegistryBaseForNewProxyConfig(ProxyConfigGetFormDataContext context) {
        Map<String, Object> proxyConfigAsMap = context.getProxyConfigAsMap();
        if (proxyConfigAsMap.isEmpty()) {
            return;
        }

        proxyConfigAsMap.put(SOURCE_MODE_FIELD, SOURCE_MODE_REGISTRY);
        proxyConfigAsMap.put(REGISTRY_MODE, REGISTRY_MODE_SIMPLE);

        if (context.isUyuni()) {
            proxyConfigAsMap.put(REGISTRY_BASE_URL, DEFAULT_REGISTRY_UYUNI_URL);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG, DEFAULT_UYUNI_REGISTRY_TAG);
        }
        else {
            String productVersion = ConfigDefaults.get().getProductVersion();
            Version version = new Version(productVersion, ConfigDefaults.get().isUyuni());
            String registryBaseUrl = "registry.suse.com/suse/manager/" +
                    version.getMajor() + "." + version.getMinor() + "/x86_64";

            proxyConfigAsMap.put(REGISTRY_BASE_URL, registryBaseUrl);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG, productVersion.toLowerCase().replace(" ", "-"));
        }
    }

    /**
     * Retrieves the electable parents FQDNs
     * Electable parents are all the proxy in the organization (except the current server) and also the local manager
     *
     * @param context the context
     */
    private void retrieveElectableParentsFqdn(ProxyConfigGetFormDataContext context) {
        String localManagerFqdn = Config.get().getString(ConfigDefaults.SERVER_HOSTNAME);

        DataResult<OrgProxyServer> orgProxyServers = SystemManager.listProxies(context.getUser().getOrg());
        Set<String> electableParents = orgProxyServers.stream()
                .filter(s -> !Objects.equals(s.getId(), context.getServer().getId()))
                .map(OrgProxyServer::getName)
                .collect(Collectors.toSet());

        if (isAbsent(localManagerFqdn)) {
            LOG.error("Could not determine the Server FQDN. Skipping it as a parent.");
        }
        else {
            electableParents.add(localManagerFqdn);
        }

        context.setElectableParentsFqdn(electableParents.stream().sorted().toList());
    }
}
