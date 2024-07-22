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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.systems.sdc.SdcHelper;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.frontend.struts.RhnAction;
import com.redhat.rhn.manager.audit.ScapManager;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;
import java.util.Objects;

/**
 * ScapSetupAction
 */

public abstract class ScapSetupAction extends RhnAction {
    public static final String SCAP_ENABLED = "scapEnabled";
    public static final String REQUIRED_PKG = "requiredPackage";

    private static final List<String> SPACEWALK_OSCAP = List.of("spacewalk-oscap");
    private static final List<String> OPENSCAP_SUSE_PKG = List.of("openscap-utils");
    private static final List<String> OPENSCAP_REDHAT_PKG = List.of("openscap-scanner");
    private static final List<String> OPENSCAP_DEBIAN_PKG = List.of("libopenscap25", "openscap-common");
    private static final List<String> OPENSCAP_DEBIAN_LEGACY_PKG = List.of("libopenscap8");

    protected void setupScapEnablementInfo(RequestContext context) {
        Server server = context.lookupAndBindServer();
        User user = context.getCurrentUser();

        boolean enabled;
        List<String> requiredPackages;

        if (server.asMinionServer().isPresent()) {
            MinionServer minion = server.asMinionServer().get();
            switch (minion.getOsFamily()) {
                case "Suse":
                    requiredPackages = OPENSCAP_SUSE_PKG;
                    break;

                case "Debian":
                    requiredPackages = getPackagesForVersion(minion.getRelease());
                    break;

                default:
                    requiredPackages = OPENSCAP_REDHAT_PKG;
            }

            // Verify all the packages are installed
            enabled = requiredPackages.stream()
                .map(pkg -> PackageFactory.lookupByNameAndServer(pkg, server))
                .noneMatch(Objects::isNull);
        }
        else {
            enabled = ScapManager.isScapEnabled(server, user);
            requiredPackages = SPACEWALK_OSCAP;
        }

        context.getRequest().setAttribute(SCAP_ENABLED, enabled);
        context.getRequest().setAttribute(REQUIRED_PKG, String.join(" ", requiredPackages));

        SdcHelper.ssmCheck(context.getRequest(), server.getId(), user);
    }

    private static List<String> getPackagesForVersion(String debianRelease) {
        if (NumberUtils.isParsable(debianRelease) && Integer.parseInt(debianRelease) <= 11) {
            return OPENSCAP_DEBIAN_LEGACY_PKG;
        }

        return OPENSCAP_DEBIAN_PKG;
    }
}
