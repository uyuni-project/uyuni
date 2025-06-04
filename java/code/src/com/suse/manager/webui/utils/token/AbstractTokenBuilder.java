/*
 * Copyright (c) 2015--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.utils.token;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Base class for builders of JWT tokens
 *
 * @param <B> the type of the builder class, used for allowing method chaining in the derived implementations
 */
abstract class AbstractTokenBuilder<B extends AbstractTokenBuilder<B>> extends SecretHolder {

    private static final int NOT_BEFORE_MINUTES = 2;

    // Optional expiration date for the token (minutes in the future). Default is one year.
    private long expirationTimeMinutesInTheFuture = Config.get().getLong(ConfigDefaults.TOKEN_LIFETIME, 525_600L);

    // Number of minutes before the token becomes valid
    private long notBeforeMinutesInThePast = NOT_BEFORE_MINUTES;

    private Instant issuedAt = Instant.now();

    /**
     * Use the server configured secret key string as the secret.
     * @return the builder
     */
    public B usingServerSecret() {
        setSecret(Objects.requireNonNull(Config.get().getString("server.secret_key"), "Server has no secret key"));
        return self();
    }

    /**
     * Sets the secret to derive the key to sign the token.
     *
     * @param secretIn the secret to use (Has to be a hex (even-length) string)
     * @return the builder
     */
    public B withCustomSecret(String secretIn) {
        setSecret(Objects.requireNonNull(secretIn, "Invalid secret"));
        return self();
    }

    /**
     * Set the number of minutes before which the token is not valid
     * @param minutes the number of minutes
     * @return the builder
     */
    public B validBeforeMinutes(long minutes) {
        this.notBeforeMinutesInThePast = minutes;
        return self();
    }

    /**
     * Set expiration time of the token.
     * @param minutes minutes in the future when the token expires
     * @return the builder
     */
    public B expiringAfterMinutes(long minutes) {
        expirationTimeMinutesInTheFuture = minutes;
        return self();
    }

    /**
     * set the issued at date.
     * @param issuedAtIn issued at data.
     * @return the builder
     */
    public B issuedAt(Instant issuedAtIn) {
        issuedAt = issuedAtIn;
        return self();
    }

    /**
     * @return the current token JWT claims
     */
    protected JwtClaims getClaims() {
        JwtClaims claims = new JwtClaims();

        // Compute directly the expiration and not before, instead of relying on the methods provided by jose4j
        // Those methods use now() instead of  the issuing date, causing flakiness in the tests
        Instant expiration = issuedAt.plus(expirationTimeMinutesInTheFuture, ChronoUnit.MINUTES);
        Instant notBefore = issuedAt.minus(notBeforeMinutesInThePast, ChronoUnit.MINUTES);

        claims.setGeneratedJwtId();
        claims.setExpirationTime(NumericDate.fromSeconds(expiration.getEpochSecond()));
        claims.setIssuedAt(NumericDate.fromSeconds(issuedAt.getEpochSecond()));
        claims.setNotBefore(NumericDate.fromSeconds(notBefore.getEpochSecond()));

        return claims;
    }

    /**
     * @return a download token with the current builder parameters.
     * @throws TokenBuildingException if there is an error generating the token
     */
    public Token build() throws TokenBuildingException {
        JwtClaims claims = getClaims();

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(getKeyForSecret());

        try {
            return new Token(jws.getCompactSerialization(), claims);
        }
        catch (JoseException ex) {
            throw new TokenBuildingException("Unable to create token", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private B self() {
        return (B) this;
    }
}
