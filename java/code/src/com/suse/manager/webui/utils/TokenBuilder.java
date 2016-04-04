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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility functions to generate download access tokens.
 */
public class TokenBuilder {

    private static final float YEAR_IN_MINUTES = 525600;
    private static final int NOT_BEFORE_MINUTES = 2;

    /**
     * The organization the token will give access to
     */
    private final long orgId;

    /**
     * By default, a token gives access to all channels in the organization.
     * If this is set, only the specified channels will be allowed
     * (whitelist of channel label list)
     */
    private Optional<Set<String>> onlyChannels = Optional.empty();

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
    private Optional<Float> expirationTimeMinutesInTheFuture =
            Optional.of(YEAR_IN_MINUTES);

    /**
     * Constructs a token builder.
     * @param orgIdIn Organization id the generated tokens will give access to
     */
    public TokenBuilder(long orgIdIn) {
        this.orgId = orgIdIn;
    }

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
        byte[] bytes = javax.xml.bind.DatatypeConverter.parseHexBinary(secret);
        return new HmacKey(bytes);
    }

    /**
     * Set expiration time of the token.
     * @param minutes minutes in the future when the token expires
     */
    public void setExpirationTimeMinutesInTheFuture(float minutes) {
        this.expirationTimeMinutesInTheFuture = Optional.of(minutes);
    }

    /**
     * The token would only allow access to the given list of channels
     * in the organization.
     * @param channels list of channels to allow access to
     */
    public void onlyChannels(Set<String> channels) {
        this.onlyChannels = Optional.of(channels);
    }

    /**
     * @return a download token with the current builder parameters.
     * @throws JoseException if there is an error generating the token
     */
    public String getToken() throws JoseException {
        JwtClaims claims = new JwtClaims();
        this.expirationTimeMinutesInTheFuture.ifPresent(exp -> {
            claims.setExpirationTimeMinutesInTheFuture(exp);
        });
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);
        claims.setClaim("org", this.orgId);
        claims.setGeneratedJwtId();
        onlyChannels.ifPresent(channels ->
                claims.setStringListClaim("onlyChannels",
                        channels.stream().collect(Collectors.toList())));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
        jws.setKey(getKeyForSecret(
                this.secret.orElseThrow(
                    () -> new IllegalArgumentException("No secret has been set"))));

        return jws.getCompactSerialization();
    }
}
