/*
 * Copyright (c) 2019 SUSE LLC
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
package com.redhat.rhn.manager.formula;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;


/**
 * Util class for checking permissions on for formulas.
 */
public class FormulaUtil {

    private static final ServerGroupManager SERVER_GROUP_MANAGER = GlobalInstanceHolder.SERVER_GROUP_MANAGER;

    private FormulaUtil() { }

    /**
     * Verify user has access to system group
     * @param user Logged in user
     * @param group Target group to check for permissions
     */
    public static void ensureUserHasPermissionsOnServerGroup(User user, ServerGroup group) {
        try {
            SERVER_GROUP_MANAGER.validateAccessCredentials(user, group, group.getName());
            SERVER_GROUP_MANAGER.validateAdminCredentials(user);
        }
        catch (NullPointerException | LookupException e) {
            throw new LookupException("Unable to find user or group");
        }
        catch (PermissionException e) {
            throw new PermissionException(LocalizationService.getInstance().getMessage("formula.accessdenied"));
        }
    }


    /**
     * Verify user has access to server
     * @param user Logged in user
     * @param server Targer server to check for permissions
     */
    public static void ensureUserHasPermissionsOnServer(User user, Server server) {
        if (!SystemManager.isAvailableToUser(user, server.getId())) {
            throw new PermissionException(LocalizationService.getInstance().getMessage("formula.accessdenied"));
        }
    }
}
