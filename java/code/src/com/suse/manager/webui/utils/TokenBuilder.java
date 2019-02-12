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
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

import java.security.Key;
import java.time.Instant;
import java.util.Optional;

/**
 * Utility functions to generate JWT tokens.
 */
public class TokenBuilder {

    private static final int NOT_BEFORE_MINUTES = 2;

    /**
     * The secret used to generate the token signature
     */
    private Optional<String> secret = Optional.empty();

    /**
     * Optional expiration date for the token
     * (minutes in the future)
     *
     * @note: Default is a year
     */
    private long expirationTimeMinutesInTheFuture = Config.get().getInt(
        ConfigDefaults.TOKEN_LIFETIME,
        525600
    );

    private Instant issuedAt = Instant.now();

    /**
     * Use the server configured secret key string as the secret.
     */
    public void useServerSecret() {
        this.secret =
                Optional.ofNullable(Config.get().getString("server.secret_key"));
        if (!this.secret.isPresent()) {
            throw new IllegalArgumentException("Server has no secret key");
        }
    }

    /**
     * Sets the secret to derive the key to sign the token.
     *
     * @param secretIn the secret to use
     *               (Has to be a hex (even-length) string)
     */
    public void setSecret(String secretIn) {
        this.secret = Optional.ofNullable(secretIn);
        if (!this.secret.isPresent()) {
            throw new IllegalArgumentException("Invalid secret");
        }
    }

    /**
     * @return the server secret if set or {@link Optional#empty()}
     */
    public static Optional<String> getServerSecret() {
        return Optional.ofNullable(Config.get().getString("server.secret_key"));
    }

    /**
     * Create a cryptographic key from the given secret.
     *
     * @param secret the secret to use for generating the key in hex
     *               string format
     * @return the key
     */
    public static Key getKeyForSecret(String secret) {
        try {
            byte[] bytes = Hex.decodeHex(secret.toCharArray());
            return new HmacKey(bytes);
        }
        catch (DecoderException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Set expiration time of the token.
     * @param minutes minutes in the future when the token expires
     */
    public void setExpirationTimeMinutesInTheFuture(long minutes) {
        this.expirationTimeMinutesInTheFuture = minutes;
    }

    /**
     * get the currently set expiration time in minutes in the future.
     * @return expiration time in minutes in the future.
     */
    public long getExpirationTimeMinutesInTheFuture() {
        return expirationTimeMinutesInTheFuture;
    }

    /**
     * set the issued at date.
     * @param issuedAtIn issued at data.
     */
    public void setIssuedAt(Instant issuedAtIn) {
        this.issuedAt = issuedAtIn;
    }

    /**
     * get the issued at data.
     * @return issued at data.
     */
    public Instant getIssuedAt() {
        return issuedAt;
    }

    /**
     * @return the current token JWT claims
     */
    public JwtClaims getClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(expirationTimeMinutesInTheFuture);
        claims.setIssuedAt(NumericDate.fromSeconds(issuedAt.getEpochSecond()));
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);
        claims.setGeneratedJwtId();
        return claims;
    }

    /**
     * @return a download token with the current builder parameters.
     * @throws JoseException if there is an error generating the token
     */
    public String getToken() throws JoseException {
        JwtClaims claims = getClaims();

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(getKeyForSecret(
                this.secret.orElseThrow(
                    () -> new IllegalArgumentException("No secret has been set"))));

        return jws.getCompactSerialization();
    }
}
