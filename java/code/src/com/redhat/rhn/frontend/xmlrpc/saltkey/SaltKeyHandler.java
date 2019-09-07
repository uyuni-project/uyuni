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
package com.redhat.rhn.frontend.xmlrpc.saltkey;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.utils.SaltKeyUtils;

/**
 * SaltKeyHandler
 * @xmlrpc.namespace saltkey
 * @xmlrpc.doc Provides methods to manage salt keys
 */
public class SaltKeyHandler extends BaseHandler {

    /**
     * API endpoint to delete minion keys
     * @param loggedInUser the user
     * @param minionId the key identifier (minionId)
     * @return 1 on success otherwise 0
     *
     * @xmlrpc.doc Delete a minion key
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.param #param("string", "minionId")
     * @xmlrpc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String minionId) {
        ensureOrgAdmin(loggedInUser);
        if (SaltKeyUtils.deleteSaltKey(loggedInUser, minionId)) {
            return 1;
        }
        return 0;
    }
}
