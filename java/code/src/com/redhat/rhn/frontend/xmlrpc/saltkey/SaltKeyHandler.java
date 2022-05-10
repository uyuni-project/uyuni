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
package com.redhat.rhn.frontend.xmlrpc.saltkey;

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;

import com.suse.manager.utils.SaltKeyUtils;

import java.util.List;

/**
 * SaltKeyHandler
 * @apidoc.namespace saltkey
 * @apidoc.doc Provides methods to manage salt keys
 */
public class SaltKeyHandler extends BaseHandler {

    private final SaltKeyUtils saltKeyUtils;

    /**
     * @param saltKeyUtilsIn
     */
    public SaltKeyHandler(SaltKeyUtils saltKeyUtilsIn) {
        this.saltKeyUtils = saltKeyUtilsIn;
    }

    /**
     * API endpoint to list accepted salt keys
     * @param loggedInUser the user
     * @return 1 on success
     *
     * @apidoc.doc List accepted salt keys
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "Accepted salt key list")
     */
    public List<String> acceptedList(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return saltKeyUtils.acceptedSaltKeyList(loggedInUser);
    }

    /**
     * API endpoint to list pending salt keys
     * @param loggedInUser the user
     * @return 1 on success
     *
     * @apidoc.doc List pending salt keys
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "Pending salt key list")
     */
    public List<String> pendingList(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return saltKeyUtils.unacceptedSaltKeyList(loggedInUser);
    }

    /**
     * API endpoint to list rejected salt keys
     * @param loggedInUser the user
     * @return 1 on success
     *
     * @apidoc.doc List of rejected salt keys
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "Rejected salt key list")
     */
    public List<String> rejectedList(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return saltKeyUtils.rejectedSaltKeyList(loggedInUser);
    }

    /**
     * API endpoint to list denied salt keys
     * @param loggedInUser the user
     * @return 1 on success
     *
     * @apidoc.doc List of denied salt keys
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "Denied salt key list")
     */
    public List<String> deniedList(User loggedInUser) {
        ensureOrgAdmin(loggedInUser);
        return saltKeyUtils.deniedSaltKeyList(loggedInUser);
    }

    /**
     * API endpoint to accept minion keys
     * @param loggedInUser the user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     *
     * @apidoc.doc Accept a minion key
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "minionId")
     * @apidoc.returntype #return_int_success()
     */
    public int accept(User loggedInUser, String minionId) {
        ensureOrgAdmin(loggedInUser);
        try {
            saltKeyUtils.acceptSaltKey(loggedInUser, minionId);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
        return 1;
    }

    /**
     * API endpoint to reject minion keys
     * @param loggedInUser the user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     *
     * @apidoc.doc Reject a minion key
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "minionId")
     * @apidoc.returntype #return_int_success()
     */
    public int reject(User loggedInUser, String minionId) {
        ensureOrgAdmin(loggedInUser);
        try {
            saltKeyUtils.rejectSaltKey(loggedInUser, minionId);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
        catch (IllegalArgumentException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
        return 1;
    }

    /**
     * API endpoint to delete minion keys
     * @param loggedInUser the user
     * @param minionId the key identifier (minionId)
     * @return 1 on success
     *
     * @apidoc.doc Delete a minion key
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "minionId")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, String minionId) {
        ensureOrgAdmin(loggedInUser);
        boolean success = false;
        try {
            success = saltKeyUtils.deleteSaltKey(loggedInUser, minionId);
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e);
        }
        if (!success) {
            throw new UnsupportedOperationException("No key found for minionID [" + minionId + "]");
        }
        return 1;
    }
}
