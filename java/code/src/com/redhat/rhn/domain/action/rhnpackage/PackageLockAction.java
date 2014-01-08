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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bo
 */
public class PackageLockAction extends PackageAction {
    public static final String PARAM_PENDING = "pending";

    @Override
    public void onCancelAction(Map params) {
        if (params == null) {
            params = new HashMap();
        }

        for (ServerAction action : this.getServerActions()) {
            try {
                PackageManager.syncLockedPackages(action.getServer().getId(),
                    this.getId(), (String) params.get(PackageLockAction.PARAM_PENDING));
            } catch (Exception e) {
                Logger.getLogger(PackageLockAction.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        ActionManager.deleteActionsByIdAndType(this.getId(), this.getActionType().getId());
    }
}
