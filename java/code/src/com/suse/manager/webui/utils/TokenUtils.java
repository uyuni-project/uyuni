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

import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwe.kdf.ConcatKeyDerivationFunction;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;

import java.security.Key;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

/**
 * Utility functions to generate access tokens
 */
public class TokenUtils {

    private static final int YEAR_IN_MINUTES = 525600;
    private static final int NOT_BEFORE_MINUTES = 2;

    /**
     * Private constructor.
     */
    private TokenUtils() {
    }

    /**
     * Create a cryptographic key from the server secret.
     *
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
     * Create a cryptographic key from the given secret.
     *
     * @param secret the secret to use for generating the key.
     * @return the key
     */
    public static Key getKeyForSecret(String secret) {
        ConcatKeyDerivationFunction func = new ConcatKeyDerivationFunction("SHA-256");
        // AES 128 key
        return new AesKey(func.kdf(secret.getBytes(), 128, new byte[]{}));
    }

    /**
     * Create a token for a given org or set of channels. The resulting token will allow
     * access to all channels that orgId has access to, plus access to all extra given
     * channels.
     *
     * @param key the key to create a token from
     * @param orgId id of the organization
     * @param channels a set of channel labels
     * @return the token
     * @throws JoseException in case of problems during key generation
     */
    public static String createTokenWithKey(Key key, Optional<Long> orgId,
            Set<String> channels) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        orgId.ifPresent(id -> claims.setClaim("org", id));
        if (!channels.isEmpty()) {
            claims.setStringListClaim("channels", new ArrayList<String>(channels));
        }

        JsonWebEncryption jwt = new JsonWebEncryption();
        jwt.setPayload(claims.toJson());
        jwt.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
        jwt.setEncryptionMethodHeaderParameter(
                ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwt.setKey(key);

        return jwt.getCompactSerialization();
    }

    /**
     * Create a token for a given org or set of channels from the server secret. The
     * resulting token will allow access to all channels that orgId has access to, plus
     * access to all extra given channels.
     *
     * @param orgId id of the organization
     * @param channels a set of channel labels
     * @return the token
     * @throws JoseException in case of problems during key generation
     */
    public static String createTokenWithServerKey(Optional<Long> orgId,
            Set<String> channels) throws JoseException {
        return createTokenWithKey(TokenUtils.getServerKey(), orgId, channels);
    }
}
