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
package com.suse.manager.webui.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.redhat.rhn.common.conf.Config;
import com.suse.manager.webui.utils.TokenUtils;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwe.kdf.ConcatKeyDerivationFunction;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.JoseException;
import spark.Request;
import spark.Response;

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class TokensAPI {

    private static final Gson GSON = new GsonBuilder().create();
    private final static int YEAR_IN_MINUTES = 525600;
    private final static int NOT_BEFORE_MINUTES = 2;

    /**
     * Creates a token for a given org or channel set
     *
     * The resulting token will allow access to all channels that orgId
     * has access to, plus access to all extra given channels.
     *
     * @param orgId id of the organization
     * @param channels a list of channel labels
     * @return the token
     */
    private static String createToken(Optional<Long> orgId, Optional<String[]> channels) throws JoseException {
        Key key = TokenUtils.getServerKey();
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        if (orgId.isPresent()) {
            claims.setClaim("org", orgId.get());
        }

        if (channels.isPresent()) {
            String[] labels = channels.get();
            claims.setStringListClaim("channels", labels);
        }

        JsonWebEncryption jwt = new JsonWebEncryption();
        jwt.setPayload(claims.toJson());
        jwt.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
        jwt.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwt.setKey(key);

        return jwt.getCompactSerialization();
    }

    /**
     * API endpoint to create a token for a given org or channel set
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String create(Request request, Response response) {

        Key key = TokenUtils.getServerKey();
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        Optional<Long> orgId = Optional.empty();
        Optional<String[]> channels = Optional.empty();

        String orgStr = request.queryParams("orgid");
        if (!StringUtils.isBlank(orgStr)) {
            orgId = Optional.of(Long.parseLong(orgStr));
        }

        String channelsStr = request.queryParams("channels");
        if (!StringUtils.isBlank(channelsStr)) {
            channels = Optional.of(StringUtils.split(","));
        }

        try {
            response.type("application/json");
            return GSON.toJson(createToken(orgId, channels));
        } catch (JoseException e) {
            response.status(500);
            return e.getMessage();
        }
    }
}
