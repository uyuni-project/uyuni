/**
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
package com.suse.manager.utils;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.salt.netapi.calls.wheel.Key;

import java.util.stream.Stream;

/**
 * SaltKeyUtils
 */
public class SaltKeyUtils {

    private static final SystemQuery SALT_SERVICE = SaltService.INSTANCE;

    private SaltKeyUtils() { }

    /**
     * Delete a salt key
     * @param user the user
     * @param minionId the key identifier (minion id)
     * @return true on success otherwise false
     * @throws PermissionCheckFailureException requires org admin privileges
     */
    public static boolean deleteSaltKey(User user, String minionId)
            throws PermissionCheckFailureException {

        //Note: since salt only allows globs we have to do our own strict matching
        Key.Names keys = SALT_SERVICE.getKeys();
        boolean exists = Stream.concat(
                Stream.concat(
                        keys.getDeniedMinions().stream(),
                        keys.getMinions().stream()),
                Stream.concat(
                        keys.getUnacceptedMinions().stream(),
                        keys.getRejectedMinions().stream())
        ).anyMatch(minionId::equals);

        if (exists) {
            return MinionServerFactory.findByMinionId(minionId).map(minionServer -> {
                if (minionServer.getOrg().equals(user.getOrg())) {
                    SALT_SERVICE.deleteKey(minionId);
                    return true;
                }
                else {
                    return false;
                }
            }).orElseGet(() -> {
                SALT_SERVICE.deleteKey(minionId);
                return true;
            });
        }
        else {
            return false;
        }
    }
}
