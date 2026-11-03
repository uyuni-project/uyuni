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
package com.redhat.rhn.manager.kickstart;

import com.redhat.rhn.common.conf.ConfigDefaults;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Helper for resolving the server FQDN used in kickstart URLs.
 */
public class KickstartHostHelper {

    private KickstartHostHelper() {
    }

    /**
     * Resolves the FQDN of the server to use in kickstart URLs. A provided
     * server FQDN is canonicalized; when none is provided (null or empty),
     * the configured Java hostname is returned unchanged - canonicalizing it
     * could resolve to a container-internal name instead of the external
     * FQDN that has to be used.
     *
     * @param serverFqdn the server FQDN, may be null or empty
     * @return the server FQDN
     */
    public static String getServerFqdn(String serverFqdn) {
        if (serverFqdn == null || serverFqdn.isEmpty()) {
            return ConfigDefaults.get().getJavaHostname();
        }
        try {
            return InetAddress.getByName(serverFqdn).getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            return serverFqdn;
        }
    }
}
