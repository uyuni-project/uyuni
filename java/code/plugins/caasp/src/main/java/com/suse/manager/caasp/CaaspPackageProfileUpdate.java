/**
 * Copyright (c) 2016 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.caasp;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.extensions.PackageProfileUpdateExtensionPoint;
import org.apache.log4j.Logger;
import org.pf4j.Extension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Extension
public class CaaspPackageProfileUpdate implements PackageProfileUpdateExtensionPoint {

    private static final Logger LOG = Logger.getLogger(CaaspPackageProfileUpdate.class);

    public static final String CAASP_PRODUCT_IDENTIFIER = "caasp";

    @Override
    public void onProfileUpdate(Server server) {
        // enable minion blackout (= locking) via pillar
        if (server.asMinionServer().isPresent() && server.getInstalledProducts().stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(CAASP_PRODUCT_IDENTIFIER))) {
            // Minion blackout is only enabled for nodes that have installed the `caasp-*` package
            SystemManager.lockServer(server, "CaaSP plugin");
        }
    }

}
