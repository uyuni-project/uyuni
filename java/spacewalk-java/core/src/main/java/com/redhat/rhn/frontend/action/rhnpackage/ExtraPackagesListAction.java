/*
 * Copyright (c) 2012--2022 SUSE LLC
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

package com.redhat.rhn.frontend.action.rhnpackage;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * ExtraPackagesListAction
 */
public class ExtraPackagesListAction extends BaseSystemPackagesAction {

    @Override
    protected DataResult<PackageListItem> getDataResult(Server server) {
        DataResult<PackageListItem> result = SystemManager.listExtraPackages(server.getId());
        result.elaborate();

        // Force the selection to be restricted to only non ptf packages
        result.stream().filter(p -> p.isPartOfPtf() || p.isMasterPtfPackage()).forEach(p -> p.setSelectable(false));

        return result;
    }
}
