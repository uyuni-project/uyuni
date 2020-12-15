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

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.calls.wheel.Key;

import org.apache.log4j.Logger;

import java.util.stream.Stream;

/**
 * SaltKeyUtils
 */
public class SaltKeyUtils {

    private final SaltApi saltApi;
    private static final Logger LOG = Logger.getLogger(SaltKeyUtils.class);

    /**
     * @param saltApiIn
     */
    public SaltKeyUtils(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * Delete a salt key
     * @param user the user
     * @param minionId the key identifier (minion id)
     * @return true on success otherwise false
     * @throws PermissionException requires org admin privileges
     * or management privileges for the server
     */
    public boolean deleteSaltKey(User user, String minionId) throws PermissionException {

        //Note: since salt only allows globs we have to do our own strict matching
        Key.Names keys = saltApi.getKeys();
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
                if (user.getServers().contains(minionServer)) {
                    saltApi.deleteKey(minionId);
                    return true;
                }
                else {
                    throw new PermissionException("You do not have permissions to " +
                            "perform this action for system id[" + minionServer.getId() + "]");
                }
            }).orElseGet(() -> {
                if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
                    throw new PermissionException(RoleFactory.ORG_ADMIN);
                }
                saltApi.deleteKey(minionId);
                return true;
            });
        }
        else {
            LOG.info(String.format("No key found for minionID [%s]", minionId));
            return false;
        }
    }
}
