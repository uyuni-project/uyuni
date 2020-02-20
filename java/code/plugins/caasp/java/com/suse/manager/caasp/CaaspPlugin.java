/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.caasp;

import com.redhat.rhn.domain.server.Server;
import com.suse.manager.extensions.PackageProfileUpdateListener;
import org.apache.log4j.Logger;
import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.pf4j.PluginWrapper;

public class CaaspPlugin extends Plugin {

    private static final Logger LOG = Logger.getLogger(CaaspPlugin.class);

    public CaaspPlugin(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Extension
    public static class CaaspPackageProfileUpdate implements PackageProfileUpdateListener {
        @Override
        public void onProfileUpdate(Server server) {
            LOG.info("CaaspPackageProfileUpdate!!!!!!");
        }
    }

}
