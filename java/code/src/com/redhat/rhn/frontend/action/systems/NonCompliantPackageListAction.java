/**
 * Copyright (c) 2012 Novell
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
package com.redhat.rhn.frontend.action.systems;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.action.rhnpackage.BaseSystemPackagesAction;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * Action for listing foreign packages installed on a system.
 */
public class NonCompliantPackageListAction extends BaseSystemPackagesAction {
    @Override
    protected DataResult getDataResult(Server server) {
        return SystemManager.listProfileForeignPackages(server.getId());
    }
}
