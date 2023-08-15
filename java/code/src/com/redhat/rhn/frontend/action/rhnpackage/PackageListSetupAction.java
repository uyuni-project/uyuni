/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.action.rhnpackage;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.rhnpackage.PackageManager;


/**
 * PackageListSetupAction
 */
public class PackageListSetupAction extends BaseSystemPackagesAction {
    /**
     * Returns the packages  in the given system
     * @param server The system.
     * @return List of installed packages
     */
    @Override
    protected DataResult<PackageListItem> getDataResult(Server server) {
        DataResult<PackageListItem> result = PackageManager.shallowSystemPackageList(server.getId(), null);
        result.elaborate();

        // Force the selection to be restricted to only non ptf packages.
        // Master ptf will be selectable only if unistallation is supported by the package manager
        boolean ptfUninstallationSupported = ServerFactory.isPtfUninstallationSupported(server);
        result.stream()
              .filter(p -> p.isPartOfPtf() || (!ptfUninstallationSupported && p.isMasterPtfPackage()))
              .forEach(p -> p.setSelectable(false));

        return result;
    }
}
