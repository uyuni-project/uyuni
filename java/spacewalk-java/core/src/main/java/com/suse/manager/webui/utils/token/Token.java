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

import com.suse.utils.Exceptions;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * A JWT token
 */
public class Token {

    private final JwtClaims claims;

    private final String serializedForm;

    /**
     * Builds an instance with the given token and claims
     * @param serializedFormIn the serialized form of the token
     * @param jwtClaims the claims contained in the token
     */
    public Token(String serializedFormIn, JwtClaims jwtClaims) {
        this.claims = jwtClaims;
        this.serializedForm = serializedFormIn;
    }

    /**
     * Retrieves the JWT ID
     * @return the JWT id
     * @throws TokenParsingException if parsing the claims fails
     */
    public String getJwtId() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> claims.getJwtId(),
            ex -> new TokenParsingException("Unable to parse jwt id", ex)
        );
    }

    /**
     * Retrieves the subject
     * @return the subject
     * @throws TokenParsingException if parsing the claims fails
     */
    public String getSubject() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> claims.getSubject(),
            ex -> new TokenParsingException("Unable to parse subject", ex)
        );
    }

    /**
     * Retrieves the audience
     * @return the audience
     * @throws TokenParsingException if parsing the claims fails
     */
    public List<String> getAudience() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> claims.getAudience(),
            ex -> new TokenParsingException("Unable to parse audience", ex)
        );
    }

    /**
     * Retrieves the instant when the token was issued
     * @return the instant when the token was issued
     * @throws TokenParsingException if parsing the claims fails
     */
    public Instant getIssuingTime() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> numericDateToInstant(claims.getIssuedAt()),
            ex -> new TokenParsingException("Unable to parse the issued time", ex)
        );
    }

    /**
     * Retrieves the instant when the token expires
     * @return the instant when the token expires
     * @throws TokenParsingException if parsing the claims fails
     */
    public Instant getExpirationTime() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> numericDateToInstant(claims.getExpirationTime()),
            ex -> new TokenParsingException("Unable to parse the expiration time", ex)
        );
    }

    /**
     * Retrieves the instant before which the token is not valid
     * @return the instant before which the token is not valid
     * @throws TokenParsingException if parsing the claims fails
     */
    public Instant getNotBeforeTime() throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> numericDateToInstant(claims.getNotBefore()),
            ex -> new TokenParsingException("Unable to parse the not before time", ex)
        );
    }

    /**
     * Retrieves the specified claim from the token
     * @param claim the name of the claim
     * @param claimType the type of the claim
     * @return the claim extracted from the token
     * @param <T> the type of the claim
     * @throws TokenParsingException if parsing the claims fails
     */
    public <T> T getClaim(String claim, Class<T> claimType) throws TokenParsingException {
        return Exceptions.handleByWrapping(
            () -> claims.getClaimValue(claim, claimType),
            ex -> new TokenParsingException("Unable to parse claim %s".formatted(claim), ex)
        );
    }

    /**
     * Retrieves the specified claim from the token. The claim value must be a list.
     * @param claim the name of the claim
     * @param listItemType the type of items of the list claim
     * @return the claim extracted from the token
     * @param <T> the type of item of the list claim
     * @throws TokenParsingException if parsing the claims fails
     */
    public <T> List<T> getListClaim(String claim, Class<T> listItemType) throws TokenParsingException {
        try {
            List<?> uncheckedList = claims.getClaimValue(claim, List.class);
            if (uncheckedList == null) {
                return List.of();
            }

            return uncheckedList.stream()
                .map(listItemType::cast)
                .toList();
        }
        catch (MalformedClaimException ex) {
            throw new TokenParsingException("Unable to parse claim %s".formatted(claim), ex);
        }
        catch (ClassCastException ex) {
            throw new TokenParsingException(
                "Some items of the list %s are not of type %s".formatted(claim, listItemType.getName())
            );
        }
    }

    /**
     * Retrieves the serialized form of the token
     * @return the serialized token
     */
    public String getSerializedForm() {
        return serializedForm;
    }

    /**
     * Converts a jose4j {@link NumericDate} to a standard java {@link Instant}.
     *
     * @param claimValue the numeric date to convert
     * @return the instant representing the same numeric date
     */
    private static Instant numericDateToInstant(NumericDate claimValue) {
        return Optional.ofNullable(claimValue)
            .map(NumericDate::getValue)
            .map(Instant::ofEpochSecond)
            .orElse(null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        String sep = ", ";

        builder.append("Token {");
        builder.append("value = [%s]".formatted(serializedForm)).append(sep);
        builder.append("valueLen = [%d]".formatted(serializedForm.length())).append(sep);

        try {
            builder.append("jwtId = [%s]".formatted(getJwtId())).append(sep);
            builder.append("jwtIdLen = [%d]".formatted(getJwtId().length())).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("jwtId = [%s]".formatted(ex.getMessage())).append(sep);
        }

        try {
            builder.append("issuingTime = [%s]".formatted(getIssuingTime())).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("issuingTime = [%s]".formatted(ex.getMessage())).append(sep);
        }

        try {
            builder.append("expirationTime = [%s]".formatted(getExpirationTime())).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("expirationTime = [%s]".formatted(ex.getMessage())).append(sep);
        }

        try {
            builder.append("notBeforeTime = [%s]".formatted(getNotBeforeTime())).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("notBeforeTime = [%s]".formatted(ex.getMessage())).append(sep);
        }

        try {
            builder.append("subject = [%s]".formatted(getSubject())).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("subject = [%s]".formatted(ex.getMessage())).append(sep);
        }

        try {
            builder.append("name = [%s]".formatted(getClaim("name", String.class))).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("name = [%s]".formatted(ex.getMessage())).append(sep);
        }
        try {
            builder.append("orgId = [%d]".formatted(getClaim("org", Long.class))).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("orgId = [none]");
        }

        try {
            List<String> claimList = getListClaim("onlyChannels", String.class);
            builder.append("onlyChannelsSize = [%d]".formatted(claimList.size())).append(sep);
            builder.append("onlyChannels = [%s]".formatted(claimList)).append(sep);
        }
        catch (TokenParsingException ex) {
            builder.append("onlyChannels = [%s]".formatted(ex.getMessage())).append(sep);
        }

        return builder.toString();
    }
}
