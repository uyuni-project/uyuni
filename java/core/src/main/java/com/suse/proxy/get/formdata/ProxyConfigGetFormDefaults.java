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
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.notification.types.ManagerVersion;

import java.util.Map;

/**
 * Ensures new proxy configurations are set with default values
 */
public class ProxyConfigGetFormDefaults implements ProxyConfigGetFormDataContextHandler {

    public static final String DEFAULT_UYUNI_REGISTRY_URL = "registry.opensuse.org/uyuni/";
    public static final String DEFAULT_UYUNI_REGISTRY_TAG = "latest";
    public static final String DEFAULT_MLM_REGISTRY_URL_FORMAT = "registry.suse.com/suse/multi-linux-manager/%d.%d/%s";
    public static final String UYUNI_REGISTRY_URL_EXAMPLE = "registry.opensuse.org/.../uyuni";
    public static final String MLM_REGISTRY_URL_EXAMPLE = "registry.suse.com/suse/multi-linux-manager/...";

    private final ManagerVersion version = new ManagerVersion();

    @Override
    public void handle(ProxyConfigGetFormDataContext context) {
        context.setRegistryUrlExample(version.isUyuni() ? UYUNI_REGISTRY_URL_EXAMPLE : MLM_REGISTRY_URL_EXAMPLE);
        context.setRegistryTagExample(version.isUyuni() ? DEFAULT_UYUNI_REGISTRY_TAG : version.toString());

        Map<String, Object> proxyConfigAsMap = context.getProxyConfigAsMap();
        if (!proxyConfigAsMap.isEmpty()) {
            return;
        }

        proxyConfigAsMap.put(SOURCE_MODE_FIELD, SOURCE_MODE_RPM);
        proxyConfigAsMap.put(REGISTRY_MODE, REGISTRY_MODE_SIMPLE);

        if (version.isUyuni()) {
            proxyConfigAsMap.put(REGISTRY_BASE_URL, DEFAULT_UYUNI_REGISTRY_URL);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG, DEFAULT_UYUNI_REGISTRY_TAG);
        }
        else {
            String registryBaseUrl = String.format(DEFAULT_MLM_REGISTRY_URL_FORMAT,
                    version.getMajor(), version.getMinor(), context.getServer().getServerArch().getName());

            proxyConfigAsMap.put(REGISTRY_BASE_URL, registryBaseUrl);
            proxyConfigAsMap.put(REGISTRY_BASE_TAG,
                    ConfigDefaults.get().getProductVersion().toLowerCase().replace(" ", "-")
            );
        }
    }
}
