/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.token;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jose4j.keys.HmacKey;

import java.security.Key;
import java.util.Optional;

/**
 * A simple class to hold a secret and create a key from it
 */
public class SecretHolder {

    // The secret used to generate the token signature
    private Optional<String> secret;

    /**
     * Builds a holder with an empty secret
     */
    public SecretHolder() {
        this.secret = Optional.empty();
    }

    /**
     * Builds a holder with the given secret
     * @param secretIn the secret
     */
    public SecretHolder(String secretIn) {
        this.secret = Optional.of(secretIn);
    }

    public Optional<String> getSecret() {
        return secret;
    }

    /**
     * Removes the current stored secret
     */
    public void clearSecret() {
        this.secret = Optional.empty();
    }

    public void setSecret(String secretIn) {
        this.secret = Optional.of(secretIn);
    }

    /**
     * Create a cryptographic key from the current secret.
     * @return the key
     */
    public Key getKeyForSecret() {
        char[] secretData = secret.map(String::toCharArray)
            .orElseThrow(() -> new IllegalArgumentException("No secret has been set"));

        try {
            byte[] bytes = Hex.decodeHex(secretData);
            return new HmacKey(bytes);
        }
        catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
