/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.admin.gpg;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import com.suse.manager.api.ReadOnly;
import com.suse.utils.CertificateUtils;
import com.suse.utils.CertificateUtils.GpgKeyListing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AdminGpgHandler
 * @apidoc.namespace admin.gpg
 * @apidoc.doc Provides methods to manage GPG data.
 */
public class AdminGpgHandler extends BaseHandler {

    /**
     * Upload and add a GPG key to the keyring
     * @param loggedInUser the current user
     * @param gpgKey the GPG key (armored text)
     * @return 1 on success
     *
     * @apidoc.doc Upload and add a GPG key to the keyring
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "gpgKey")
     * @apidoc.returntype #return_int_success()
     */
    public int uploadGpgKey(User loggedInUser, String gpgKey) {
        ensureSatAdmin(loggedInUser);

        if (gpgKey == null || gpgKey.isBlank()) {
            return 0;
        }

        try {
            CertificateUtils.importGpgKey(gpgKey);
            return 1;
        }
        catch (IOException | RuntimeException e) {
            return 0;
        }
    }

    /**
     * List all GPG keys from the keyring
     * @param loggedInUser the current user
     * @return all GPG keys from the keyring
     *
     * @apidoc.doc Get all GPG keys from the keyring
     * @apidoc.param #session_key()
     * @apidoc.returntype
    *     #return_array_begin()
    *         #struct_begin("GPG key")
    *             #prop_desc("int", "keyType", "OpenPGP public key algorithm type")
    *             #prop_desc("int", "keySize", "Key size in bits")
    *             #prop_desc("string", "fingerprint", "Fingerprint of the GPG key")
    *             #prop_desc("array", "names", "Users associated with the GPG key")
    *         #struct_end()
    *     #array_end()
     *
     */
    @ReadOnly
    public List<Map<String, Object>> listGpgKeys(User loggedInUser) {
        ensureSatAdmin(loggedInUser);

        List<GpgKeyListing> keys = CertificateUtils.getGpgKeys();

        List<Map<String, Object>> result = new ArrayList<>();

        for (GpgKeyListing key : keys) {
            Map<String, Object> keyResult = new HashMap<>();

            keyResult.put("keyType", key.getKeyType());
            keyResult.put("keySize", key.getKeySize());
            keyResult.put("fingerprint", key.getFingerprint());
            keyResult.put("names", key.getNames());

            result.add(keyResult);
        }

        return result;
    }

    /**
     * Remove a GPG key from the keyring
     * @param loggedInUser the current user
     * @param fingerprint the fingerprint of the GPG key
     * @return 1 on success
     *
     * @apidoc.doc Remove a GPG key from the keyring
     * @apidoc.param #session_key()
     * @apidoc.param #param("string", "fingerprint")
     * @apidoc.returntype #return_int_success()
     */
    public int removeGpgKey(User loggedInUser, String fingerprint) {
        ensureSatAdmin(loggedInUser);

        if (!CertificateUtils.removeGpgKey(fingerprint)) {
            return 0;
        }

        return 1;
    }

}
