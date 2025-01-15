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
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.audit.ScapManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ScapSetupAction
 */

public abstract class ScapSetupAction extends RhnAction {
    private static final Logger LOGGER = LogManager.getLogger(ScapSetupAction.class);

    public static final String SCAP_ENABLED = "scapEnabled";
    public static final String REQUIRED_PKG = "requiredPackage";

    private static final String OPENSCAP_SUSE_PKG = "openscap-utils";
    private static final String OPENSCAP_REDHAT_PKG = "openscap-scanner";
    private static final String OPENSCAP_DEBIAN_PKG = "openscap-utils";
    private static final String OPENSCAP_DEBIAN_LEGACY_PKG = "libopenscap8";

    protected void setupScapEnablementInfo(RequestContext context) {
        Server server = context.lookupAndBindServer();
        User user = context.getCurrentUser();

        boolean enabled;
        String requiredPackage;

        if (server.asMinionServer().isPresent()) {
            MinionServer minion = server.asMinionServer().get();
            switch (minion.getOsFamily()) {
                case "Suse":
                    requiredPackage = OPENSCAP_SUSE_PKG;
                    break;

                case "Debian":
                    requiredPackage = getPackageForDebianFamily(minion.getOs(), minion.getRelease());
                    break;

                default:
                    requiredPackage = OPENSCAP_REDHAT_PKG;
            }

            // Verify the packages is installed
            enabled = PackageFactory.lookupByNameAndServer(requiredPackage, server) != null;
        }
        else {
            enabled = ScapManager.isScapEnabled(server, user);
            requiredPackage = "";
        }

        context.getRequest().setAttribute(SCAP_ENABLED, enabled);
        context.getRequest().setAttribute(REQUIRED_PKG, requiredPackage);

        SdcHelper.ssmCheck(context.getRequest(), server.getId(), user);
    }

    private static String getPackageForDebianFamily(String os, String release) {
        try {
            switch (os) {
                case ServerConstants.UBUNTU:
                    int ubuntuVersion = Integer.parseInt(StringUtils.substringBefore(release, "."));
                    if (ubuntuVersion <= 22) {
                        return OPENSCAP_DEBIAN_LEGACY_PKG;
                    }

                    return OPENSCAP_DEBIAN_PKG;

                case ServerConstants.DEBIAN:
                    int debianVersion = Integer.parseInt(release);
                    if (debianVersion <= 11) {
                        return OPENSCAP_DEBIAN_LEGACY_PKG;
                    }

                    return OPENSCAP_DEBIAN_PKG;

                default:
                    LOGGER.warn("Unable to identify os {}. Assuming default Debian package.", os);
                    return OPENSCAP_DEBIAN_PKG;
            }
        }
        catch (NumberFormatException ex) {
            LOGGER.warn("The release number {} of {} is not parseable. Assuming default Debian package.", release, os);
            return OPENSCAP_DEBIAN_PKG;
        }
    }
}
