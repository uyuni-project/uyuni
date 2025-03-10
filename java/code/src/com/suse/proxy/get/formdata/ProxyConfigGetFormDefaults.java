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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.notification.types.Version;

import java.util.Map;

/**
 * Ensures new proxy configurations are set with default values
 */
public class ProxyConfigGetFormDefaults implements ProxyConfigGetFormDataContextHandler {

    public static final String DEFAULT_UYUNI_REGISTRY_URL = "registry.opensuse.org/uyuni/";
    public static final String DEFAULT_UYUNI_REGISTRY_TAG = "latest";
    private final boolean isUyuni = ConfigDefaults.get().isUyuni();

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        Map<String, Object> proxyConfigAsMap = context.getProxyConfigAsMap();
        if (!proxyConfigAsMap.isEmpty()) {
            return;
        }

        proxyConfigAsMap.put(SOURCE_MODE_FIELD, SOURCE_MODE_REGISTRY);
        proxyConfigAsMap.put(REGISTRY_MODE, REGISTRY_MODE_SIMPLE);

        if (isUyuni) {
            proxyConfigAsMap.put(REGISTRY_BASE_URL, DEFAULT_UYUNI_REGISTRY_URL);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG, DEFAULT_UYUNI_REGISTRY_TAG);
        }
        else {
            String productVersion = ConfigDefaults.get().getProductVersion();
            Version version = new Version(productVersion, isUyuni);
            String registryBaseUrl = String.format("registry.suse.com/suse/manager/%d.%d/%s",
                    version.getMajor(), version.getMinor(), context.getServer().getServerArch().getName());

            proxyConfigAsMap.put(REGISTRY_BASE_URL, registryBaseUrl);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG, productVersion.toLowerCase().replace(" ", "-"));
        }
    }
}
