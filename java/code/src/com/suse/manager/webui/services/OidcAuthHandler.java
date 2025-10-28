/*
 * Copyright (c) 2025 SUSE LLC
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
package com.suse.manager.webui.services;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.conf.ConfigException;
import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Handles OpenID Connect (OIDC) authentication.
 */
public class OidcAuthHandler {

    public static final String OIDC_DISCOVERY_PATH = "/.well-known/openid-configuration";

    private static final Logger LOG = LogManager.getLogger(OidcAuthHandler.class);

    private boolean oidcEnabled;
    private String issuer;
    private String jwksUri;
    private String audience;
    private String usernameClaim;

    private JwtConsumer jwtConsumer;
    private HttpClientAdapter httpClient;

    /**
     * Constructs an OidcAuthHandler and loads configuration.
     */
    public OidcAuthHandler() {
        this(null, new HttpClientAdapter());
    }

    /**
     * Constructs an OidcAuthHandler with a custom {@link VerificationKeyResolver}.
     *
     * @param keyResolver the verification key resolver, if {@code null} a default one is created
     * @param httpClientIn the HTTP client to use when fetching from the discovery endpoint
     */
    public OidcAuthHandler(VerificationKeyResolver keyResolver, HttpClientAdapter httpClientIn) {
        httpClient = httpClientIn;

        try {
            loadConfiguration();
            if (!isOidcEnabled()) {
                return;
            }
        }
        catch (URISyntaxException eIn) {
            throw new ConfigException("Malformed URI in the OIDC configuration.");
        }
        VerificationKeyResolver resolver = keyResolver;
        if (resolver == null) {
            HttpsJwks httpsJwks = new HttpsJwks(jwksUri);

            // No proxy support in jose4j
            // HttpsJwks can be subclassed for proxy support
            resolver = new HttpsJwksVerificationKeyResolver(httpsJwks);
        }

        AlgorithmConstraints jwsAlgConstraints = new AlgorithmConstraints(
                AlgorithmConstraints.ConstraintType.PERMIT,
                AlgorithmIdentifiers.RSA_USING_SHA256,
                AlgorithmIdentifiers.RSA_USING_SHA384,
                AlgorithmIdentifiers.RSA_USING_SHA512,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA256,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA384,
                AlgorithmIdentifiers.RSA_PSS_USING_SHA512,
                AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256,
                AlgorithmIdentifiers.ECDSA_USING_P384_CURVE_AND_SHA384
        );

        jwtConsumer = new JwtConsumerBuilder()
                .setVerificationKeyResolver(resolver)
                .setJweAlgorithmConstraints(jwsAlgConstraints)
                .setRequireExpirationTime()
                .setAllowedClockSkewInSeconds(5)
                .setRequireSubject()
                .setExpectedIssuer(true, issuer)
                .setSkipDefaultAudienceValidation()
                .build();

        LOG.info("OIDC authentication is enabled. Issuer: {}, Audience: {}, Username attribute: {}",
                issuer, audience, usernameClaim);
    }

    /**
     * Loads OIDC configuration from {@link ConfigDefaults}.
     * @throws URISyntaxException if a malformed URI is encountered.
     */
    private void loadConfiguration() throws URISyntaxException {
        LOG.debug("Loading OIDC configuration.");
        oidcEnabled = ConfigDefaults.get().isOidcEnabled();

        if (!oidcEnabled) {
            LOG.debug("OIDC authentication is disabled.");
            return;
        }

        issuer = ConfigDefaults.get().getOidcIssuer();
        if (StringUtils.isEmpty(issuer)) {
            throw new ConfigException("OIDC issuer URI cannot be empty.");
        }
        URI issuerUri = new URI(issuer);

        audience = ConfigDefaults.get().getOidcAudience();
        if (StringUtils.isEmpty(audience)) {
            throw new ConfigException("OIDC audience cannot be empty.");
        }

        usernameClaim = ConfigDefaults.get().getOidcUsernameClaim();
        if (StringUtils.isEmpty(usernameClaim)) {
            throw new ConfigException("OIDC username claim name cannot be empty.");
        }

        String jwksPath = ConfigDefaults.get().getOidcJwksPath();
        if (StringUtils.isEmpty(jwksPath)) {
            LOG.info("JWKS path not provided. Trying to fetch from the OIDC discovery endpoint.");
            jwksUri = fetchJwksUri(issuerUri);
        }
        else {
            jwksUri = appendUriPath(issuerUri, jwksPath).toString();
        }
    }

    /**
     * Fetches the JWKS URI from the OIDC discovery endpoint of the issuing identity provider.
     * @param issuerIn The OIDC issuer URI.
     * @return The JWKS URI.
     * @throws URISyntaxException if a malformed URI is encountered.
     */
    private String fetchJwksUri(URI issuerIn) throws URISyntaxException {
        URI discoveryEndpoint = appendUriPath(issuerIn, OIDC_DISCOVERY_PATH);
        LOG.warn("Fetching JWKS path from OIDC discovery endpoint: {}", discoveryEndpoint);

        HttpGet request = new HttpGet(discoveryEndpoint);
        HttpResponse response;
        String uri;

        try {
            response = httpClient.executeRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new ConfigException("Failed to fetch OIDC discovery document from " + discoveryEndpoint +
                        ". HTTP status code: " + statusCode);
            }

            String jsonResponse = EntityUtils.toString(response.getEntity());
            JsonObject jsonObject = new Gson().fromJson(jsonResponse, JsonObject.class);
            uri = jsonObject.get("jwks_uri").getAsString();

            if (StringUtils.isEmpty(uri)) {
                throw new ConfigException("JWKS URI not found in the OIDC discovery document from " +
                        discoveryEndpoint);
            }
        }
        catch (IOException e) {
            throw new ConfigException("Error while fetching OIDC discovery document from " + discoveryEndpoint, e);
        }
        finally {
            request.releaseConnection();
        }

        LOG.debug("Successfully fetched JWKS URI: {}", uri);
        return uri;
    }

    /**
     * Appends a path to a base URI.
     * <p>
     * If the base URI includes a path itself, it is concatenated with the path to be appended.
     * @param base The base URI.
     * @param path The path to append.
     * @return The new URI with the appended path.
     * @throws URISyntaxException if a malformed URI is encountered.
     */
    private static URI appendUriPath(URI base, String path) throws URISyntaxException {
        if (StringUtils.isEmpty(path)) {
            return base;
        }

        String basePath = base.getPath();
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        // Remove trailing slashes in path
        String normalizedPath = path.replaceAll("^/+", "");

        return new URIBuilder(base)
                .setPath(basePath + normalizedPath)
                .build();
    }

    /**
     * Handles OIDC login by verifying the provided token using a {@link JwtConsumer}.
     * <p>
     * Following validations are applied to the token:
     * <ul>
     *     <li>Signature verification using keys from the JWKS URI.</li>
     *     <li>Allowed JWS algorithms: RSA/PSS SHA-256, SHA-384, SHA-512 and ESDCA SHA-256, SHA-384.</li>
     *     <li>Requires an expiration time claim.</li>
     *     <li>Allows for a 5-second clock skew.</li>
     *     <li>Requires a subject claim.</li>
     *     <li>Validates the issuer claim against the configured OIDC issuer.</li>
     *     <li>Custom audience validation against the configured OIDC audience.</li>
     *     <li>Matches the configured Uyuni username claim to an existing user.</li>
     * </ul>
     * @param token The OIDC token.
     * @return The username claim.
     * @throws OidcAuthException if token verification fails or OIDC is not enabled.
     */
    public String handleOidcLogin(String token) throws OidcAuthException {
        if (!isOidcEnabled()) {
            throw new OidcAuthException("OIDC authentication is not enabled.");
        }

        try {
            JwtClaims claims = jwtConsumer.processToClaims(token);
            if (!claims.getAudience().contains(audience)) {
                throw new OidcAuthException("Token verification failed. Missing '" + audience +
                        "' in the audience claim.");
            }
            if (!claims.hasClaim(usernameClaim)) {
                throw new OidcAuthException("Token verification failed. Missing '" + usernameClaim + "' claim.");
            }
            return claims.getClaimValueAsString(usernameClaim);
        }
        catch (InvalidJwtException | MalformedClaimException e) {
            throw new OidcAuthException("Token verification failed.", e);
        }
    }

    /**
     * Checks if OIDC authentication is enabled by configuration.
     * @return {@code true} if OIDC is enabled, {@code false} otherwise.
     */
    public boolean isOidcEnabled() {
        return oidcEnabled;
    }

    /**
     * Gets the configured JWKS URI
     * @return the JWKS URI
     */
    public String getJwksUri() {
        return jwksUri;
    }
}
