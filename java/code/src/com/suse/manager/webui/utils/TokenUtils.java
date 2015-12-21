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
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
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
     * @param secret the secret to use for generating the key in hex
     *               string format
     * @return the key
     */
    public static Key getKeyForSecret(String secret) {
        byte[] bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(secret);
        return new HmacKey(bytes);
    }

    /**
     * Create a token for a given org or set of channels. The resulting token will allow
     * access to all channels that orgId has access to, plus access to all extra given
     * channels.
     *
     * @param key the key to create a token from
     * @param orgId id of the organization
     * @param onlyChannels if present, only allow access to these channels
     * @return the token
     * @throws JoseException in case of problems during key generation
     */
    public static String createTokenWithKey(Key key, long orgId,
            Optional<Set<String>> onlyChannels) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        claims.setClaim("org", orgId);
        claims.setGeneratedJwtId();
        onlyChannels.ifPresent(
                channels -> claims.setStringListClaim("onlyChannels",
                        new ArrayList<String>(channels)));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(key);

        return jws.getCompactSerialization();
    }

    /**
     * Create a token for a given org or set of channels from the server secret. The
     * resulting token will allow access to all channels that orgId has access to, plus
     * access to all extra given channels.
     *
     * @param orgId id of the organization
     * @param onlyChannels if present, only allow access to these channels
     * @return the token
     * @throws JoseException in case of problems during key generation
     */
    public static String createTokenWithServerKey(long orgId,
            Optional<Set<String>> onlyChannels) throws JoseException {
        return createTokenWithKey(TokenUtils.getServerKey(), orgId, onlyChannels);
    }
}
