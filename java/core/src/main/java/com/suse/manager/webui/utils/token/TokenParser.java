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

import com.redhat.rhn.common.conf.Config;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

import java.util.Objects;

public class TokenParser extends SecretHolder {

    private boolean verifySignature = true;

    private boolean verifyNotBefore = false;

    private boolean verifyExpiration = false;

    /**
     * Skip the verification of the signature
     * @return the parser
     */
    public TokenParser skippingSignatureVerification() {
        this.verifySignature = false;
        clearSecret();
        return this;
    }

    /**
     * Use the server configured secret key string as the secret.
     * @return the parser
     */
    public TokenParser usingServerSecret() {
        setSecret(Objects.requireNonNull(Config.get().getString("server.secret_key"), "Server has no secret key"));
        return this;
    }

    /**
     * Sets the secret to derive the key to sign the token.
     *
     * @param secretIn the secret to use (Has to be a hex (even-length) string)
     * @return the parser
     */
    public TokenParser withCustomSecret(String secretIn) {
        setSecret(Objects.requireNonNull(secretIn, "Invalid secret"));
        return this;
    }

    /**
     * Skip the verification of the expiration date
     * @return the parser
     */
    public TokenParser skippingExpirationVerification() {
        this.verifyExpiration = false;
        return this;
    }

    /**
     * Enforce the verification of the expiration date
     * @return the parser
     */
    public TokenParser verifyingExpiration() {
        this.verifyExpiration = true;
        return this;
    }

    /**
     * Skip the verification of the not-before date
     * @return the parser
     */
    public TokenParser skippingNotBeforeVerification() {
        this.verifyNotBefore = false;
        return this;
    }

    /**
     * Enforce the verification of the not-before date
     * @return the parser
     */
    public TokenParser verifyingNotBefore() {
        this.verifyNotBefore = true;
        return this;
    }

    /**
     * Verify that a token is valid.
     *
     * @param token token to verify
     *
     * @return true if the token is valid, false otherwise
     */
    public boolean verify(String token) {
        JwtConsumerBuilder builder = createJwtConsumerBuilder();

        try {
            builder.build().processToClaims(token);
        }
        catch (InvalidJwtException e) {
            return false;
        }

        return true;
    }

    /**
     * Extract the claims from the token
     * @param serializedForm the token
     * @return the parsed claims
     */
    public Token parse(String serializedForm) throws TokenParsingException {

        JwtConsumerBuilder jwtConsumerBuilder = createJwtConsumerBuilder();

        try {
            return new Token(serializedForm, jwtConsumerBuilder.build().processToClaims(serializedForm));
        }
        catch (InvalidJwtException ex) {
            throw new TokenParsingException("Unable to parse token claims: " + ex.getMessage(), ex);
        }
    }

    private JwtConsumerBuilder createJwtConsumerBuilder() {
        JwtConsumerBuilder jwtConsumerBuilder = new JwtConsumerBuilder()
            .setJwsAlgorithmConstraints(AlgorithmConstraints.ConstraintType.PERMIT, AlgorithmIdentifiers.HMAC_SHA256);

        if (verifySignature) {
            jwtConsumerBuilder.setVerificationKey(getKeyForSecret());
        }
        else {
            jwtConsumerBuilder.setSkipSignatureVerification();
        }

        if (verifyExpiration) {
            jwtConsumerBuilder.setRequireExpirationTime();
        }

        if (verifyNotBefore) {
            jwtConsumerBuilder.setRequireNotBefore();
        }

        return jwtConsumerBuilder;
    }

}
