/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.utils;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the Salt ports a client connects to on its Salt master, the server or a proxy, as pillar entries.
 * Ports equal to the Salt defaults are left out, so the default setup generates the same configuration as before.
 */
public final class SaltPortsResolver {

    public static final String PILLAR_PUBLISH_PORT = "mgr_salt_publish_port";
    public static final String PILLAR_REQUEST_PORT = "mgr_salt_request_port";

    private SaltPortsResolver() {
    }

    /**
     * @param proxy the proxy acting as Salt master, or empty for the server
     * @return the Salt ports pillar of the proxy or the server
     */
    public static Map<String, Object> forMaster(Optional<Server> proxy) {
        Integer publishPort;
        Integer requestPort;
        if (proxy.isPresent()) {
            ProxyInfo info = proxy.get().getProxyInfo();
            publishPort = info == null ? null : info.getSaltPublishPort();
            requestPort = info == null ? null : info.getSaltRequestPort();
        }
        else {
            publishPort = ConfigDefaults.get().getSaltPublishPort().orElse(null);
            requestPort = ConfigDefaults.get().getSaltRequestPort().orElse(null);
        }
        Map<String, Object> pillar = new HashMap<>();
        if (publishPort != null && publishPort != 4505) {
            pillar.put(PILLAR_PUBLISH_PORT, publishPort);
        }
        if (requestPort != null && requestPort != 4506) {
            pillar.put(PILLAR_REQUEST_PORT, requestPort);
        }
        return pillar;
    }
}
