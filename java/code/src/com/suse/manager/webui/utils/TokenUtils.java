/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.utils;

import com.redhat.rhn.common.conf.Config;
import org.jose4j.jwe.kdf.ConcatKeyDerivationFunction;
import org.jose4j.keys.AesKey;

import java.security.Key;

/**
 * Utility functions to generate access tokens
 */
public class TokenUtils {

    /**
     * Creates a cryptographic key from the server secret
     * @return the key
     */
    public static Key getServerKey() {
        String serverSecret = Config.get().getString("server.secret_key");
        if (serverSecret == null) {
            throw new RuntimeException("Server has no secret key");
        }
        return getKeyForSecret(serverSecret);
    }

    /**
     * Creates a cryptographic key from the given secret
     * @return the key
     */
    public static Key getKeyForSecret(String secret) {
        ConcatKeyDerivationFunction func = new ConcatKeyDerivationFunction("SHA-256");
        // AES 128 key
        return new AesKey(func.kdf(secret.getBytes(), 128, new byte[]{}));
    }
}
