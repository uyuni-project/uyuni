/**
 * Copyright (c) 2013 SUSE
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

package com.redhat.rhn.domain.action.rhnpackage;

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

/**
 *
 * @author bo
 */
public class PackageLockAction extends PackageAction {
    @Override
    public void onCancelAction() {
        for (ServerAction action : this.getServerActions()) {
            PackageManager.syncLockedPackages(action.getServer().getId(), this.getId());
        }

        ActionManager.deleteActionsByIdAndType(this.getId(), this.getActionType().getId());
    }
}
