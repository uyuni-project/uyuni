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
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.user.User;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static spark.Spark.halt;

/**
 * Provides a programmatically way to generate channel access tokens and
 * also exposes it as an http endpoint.
 */
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
     * @param channels a set of channel labels
     * @return the token
     */
    public static String createTokenWithKey(Key key, Optional<Long> orgId, Set<String> channels) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        orgId.ifPresent(id -> claims.setClaim("org", id));
        if (channels.isEmpty()) {
            claims.setStringListClaim("channels", new ArrayList<String>(channels));
        }

        JsonWebEncryption jwt = new JsonWebEncryption();
        jwt.setPayload(claims.toJson());
        jwt.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
        jwt.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        jwt.setKey(key);

        return jwt.getCompactSerialization();
    }

    /**
     * Creates a token for a given org or channel set
     *
     * The resulting token will allow access to all channels that orgId
     * has access to, plus access to all extra given channels.
     *
     * @param orgId id of the organization
     * @param channels a set of channel labels
     * @return the token
     */
    public static String createTokenWithServerKey(Optional<Long> orgId, Set<String> channels) throws JoseException {
        String serverSecret = Config.get().getString("server.secret_key");
        if (serverSecret == null) {
            throw new RuntimeException("Server has no secret key");
        }
        return createTokenWithKey(TokenUtils.getServerKey(), orgId, channels);
    }

    /**
     * API endpoint to create a token for a given org or channel set
     * @param request the request object
     * @param response the response object
     * @return json result of the API call
     */
    public static String create(Request request, Response response, User user) {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(YEAR_IN_MINUTES);
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(NOT_BEFORE_MINUTES);

        try {
            Optional<Long> orgId =
                    Optional.ofNullable(request.queryParams("orgid"))
                            .filter(StringUtils::isNotBlank)
                            .map(Long::parseLong);

            Set<String> channels = Optional.ofNullable(request.queryParams("channels"))
                            .map(str -> StringUtils.split(str, ","))
                            .map(arr -> new HashSet<String>(Arrays.asList(arr)))
                            .orElse(new HashSet<String>());

            // check that the user has access to those channels
            Set<String> orgChannels =
                    ChannelFactory.getAccessibleChannelsByOrg(user.getOrg().getId())
                            .stream()
                            .map(Channel::getLabel)
                            .collect(Collectors.toCollection(HashSet::new));

            // remove all channels not in the current user accessible
            // channels list
            boolean anyNotAuthorized = channels.retainAll(orgChannels);
            if (anyNotAuthorized) {
                halt(403, LocalizationService.getInstance()
                        .getMessage("tokens.usernotallchannelsaccess", user.getOrg().getName()));
            }

            response.type("application/json");
            return GSON.toJson(createTokenWithServerKey(orgId, channels));
        } catch (NumberFormatException | JoseException e) {
            response.status(500);
            return e.getMessage();
        }
    }
}
