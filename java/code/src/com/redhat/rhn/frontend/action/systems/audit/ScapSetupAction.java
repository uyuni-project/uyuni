/*
 * Copyright (c) 2012--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.systems.audit;

import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.audit.ScapManager;

/**
 * ScapSetupAction
 */

public abstract class ScapSetupAction extends RhnAction {
    private static final String SCAP_ENABLED = "scapEnabled";
    private static final String REQUIRED_PKG = "requiredPackage";
    private static final String OPENSCAP_SUSE_PKG = "openscap-utils";
    private static final String OPENSCAP_REDHAT_PKG = "openscap-scanner";
    private static final String OPENSCAP_DEBIAN_PKG = "libopenscap8";

    protected void setupScapEnablementInfo(RequestContext context) {
        Server server = context.lookupAndBindServer();
        User user = context.getCurrentUser();
        boolean enabled = false;
        String requiredPkg = "";
        if (server.asMinionServer().isPresent()) {
            MinionServer minion = server.asMinionServer().get();
            switch (minion.getOsFamily()) {
                case "Suse":
                    requiredPkg = OPENSCAP_SUSE_PKG;
                    break;

                case "Debian":
                    requiredPkg = OPENSCAP_DEBIAN_PKG;
                    break;

                default:
                    requiredPkg = OPENSCAP_REDHAT_PKG;
            }
            InstalledPackage installedPkg =
                    PackageFactory.lookupByNameAndServer(requiredPkg, server);
            if (installedPkg != null) {
                enabled = true;
            }
        }
        else {
            enabled = ScapManager.isScapEnabled(server, user);
        }
        context.getRequest().setAttribute(SCAP_ENABLED, enabled);
        context.getRequest().setAttribute(REQUIRED_PKG, requiredPkg);

        SdcHelper.ssmCheck(context.getRequest(), server.getId(), user);
    }
}
