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
package com.suse.manager.webui.services.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import com.suse.manager.webui.services.OidcAuthException;
import com.suse.manager.webui.services.OidcAuthHandler;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import org.jose4j.base64url.Base64Url;
import org.jose4j.json.JsonUtil;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.keys.resolvers.JwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.InvalidAlgorithmException;
import org.jose4j.lang.JoseException;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.ECGenParameterSpec;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OidcAuthHandlerTest extends JMockBaseTestCaseWithUser {

    private static final String ISSUER = "https://auth.localhost";
    private static final String TEST_KID = "test-key-id";
    private static final String MCP_AUDIENCE = "mcp-server-uyuni";
    private static final String MLM_AUDIENCE = ConfigDefaults.get().getOidcAudience();
    private static final String USERNAME_CLAIM = ConfigDefaults.get().getOidcUsernameClaim();
    private static final String JWKS_URI = "/.well-known/jwks.json";
    private KeyPair rsaKeyPair;


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        enableOidcLogin();
        rsaKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
    }

    @Test
    public void testOidcDisabled() throws JoseException {
        String token = issueValidToken();

        Config.get().setBoolean(ConfigDefaults.OIDC_ENABLED, "false");
        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        assertEquals("OIDC authorization is not enabled.", e.getMessage());
    }

    @Test
    public void testHandleSuccessfulLogin() throws JoseException, OidcAuthException {
        String token = issueValidToken();

        OidcAuthHandler handler = getHandler();
        String username = handler.handleOidcLogin(token);

        Assertions.assertEquals(user.getLogin(), username);
    }

    @Test
    public void testIssuerMismatch() throws JoseException {
        String token = issueToken(rsaKeyPair.getPrivate(), AlgorithmIdentifiers.RSA_USING_SHA256,
                "https://not-me.localhost", List.of(MCP_AUDIENCE, MLM_AUDIENCE), Map.of(USERNAME_CLAIM, user.getId()));

        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        Throwable cause = e.getCause();

        assertEquals(InvalidJwtException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Issuer (iss) claim value (https://not-me.localhost) " +
                "doesn't match expected value of " + ISSUER));
    }

    @Test
    public void testAudienceMismatch() throws JoseException {
        String token = issueToken(rsaKeyPair.getPrivate(), AlgorithmIdentifiers.RSA_USING_SHA256, ISSUER,
                List.of(MCP_AUDIENCE), Map.of(USERNAME_CLAIM, user.getId()));

        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        assertTrue(e.getMessage().contains("Missing '" + MLM_AUDIENCE + "' in the audience claim."));
    }

    @Test
    public void testUsernameClaimMismatch() throws JoseException {
        String token = issueToken(rsaKeyPair.getPrivate(), AlgorithmIdentifiers.RSA_USING_SHA256, ISSUER,
                List.of(MCP_AUDIENCE, MLM_AUDIENCE), Map.of("not_uyuni_username", user.getLogin()));

        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        assertTrue(e.getMessage().contains("Missing '" + USERNAME_CLAIM + "' claim."));
    }

    @Test
    public void testAlternativeAlgoritm() throws JoseException, NoSuchAlgorithmException, OidcAuthException,
            InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec);
        KeyPair ecdsaKeyPair = keyPairGenerator.generateKeyPair();

        String token = issueValidToken(ecdsaKeyPair.getPrivate(),
                AlgorithmIdentifiers.ECDSA_USING_P256_CURVE_AND_SHA256, user.getLogin());

        OidcAuthHandler handler = getHandler(ecdsaKeyPair.getPublic());
        String username = handler.handleOidcLogin(token);

        Assertions.assertEquals(user.getLogin(), username);
    }

    @Test
    public void testInsecureHash() throws NoSuchAlgorithmException, SignatureException, InvalidKeyException,
            JoseException {
        // Token with insecure signing hash
        String token = signTokenWithRs1(generateValidClaims(), (RSAPrivateKey) rsaKeyPair.getPrivate());

        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        Throwable cause = e.getCause();

        assertEquals(InvalidJwtException.class, cause.getClass());
        cause = cause.getCause();
        assertEquals(UnresolvableKeyException.class, cause.getClass());
        cause = cause.getCause();
        assertEquals(InvalidAlgorithmException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("RS1 is an unknown, unsupported or unavailable alg algorithm"));
    }

    @Test
    public void testUnsecuredToken() throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(generateValidClaims().toJson());
        jws.setAlgorithmConstraints(AlgorithmConstraints.NO_CONSTRAINTS);
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.NONE);
        String token = jws.getCompactSerialization();
        OidcAuthHandler handler = getHandler();

        Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(token));
        Throwable cause = e.getCause();

        assertEquals(InvalidJwtException.class, cause.getClass());
        cause = cause.getCause();
        assertEquals(UnresolvableKeyException.class, cause.getClass());
        assertTrue(cause.getMessage().contains("Unable to find a suitable verification key"));
    }

    @Test
    public void testFetchJwksUri() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        try {
            String issuer = wireMockServer.baseUrl();
            String jwksUri = issuer + "/.not-so-well-known/jwks.json";
            String oidcConfig = String.format("{\"jwks_uri\":\"%s\"}", jwksUri);

            wireMockServer.stubFor(WireMock.get(OidcAuthHandler.OIDC_DISCOVERY_PATH)
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(oidcConfig)));

            Config.get().setString(ConfigDefaults.OIDC_IDP_ISSUER, issuer);
            Config.get().setString(ConfigDefaults.OIDC_IDP_JWKS_PATH, "");

            // keyResolver is not used when only fetching the jwks uri
            OidcAuthHandler handler = new OidcAuthHandler(null, new HttpClientAdapter());
            assertEquals(jwksUri, handler.getJwksUri());
        }
        finally {
            wireMockServer.stop();
        }
    }

    @Test
    public void testValidJwksUri() throws JoseException, OidcAuthException {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        try {
            String issuer = wireMockServer.baseUrl();

            // Generate the JWKS to serve
            PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(rsaKeyPair.getPublic());
            jwk.setKeyId(TEST_KID);
            String jwks = new JsonWebKeySet(jwk).toJson();

            wireMockServer.stubFor(WireMock.get(JWKS_URI)
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(jwks)));

            Config.get().setString(ConfigDefaults.OIDC_IDP_ISSUER, issuer);
            OidcAuthHandler handler = new OidcAuthHandler();

            String token = issueToken(rsaKeyPair.getPrivate(), AlgorithmIdentifiers.RSA_USING_SHA256, issuer,
                    List.of(MCP_AUDIENCE, MLM_AUDIENCE), Map.of(USERNAME_CLAIM, user.getLogin()));
            String username = handler.handleOidcLogin(token);

            assertEquals(user.getLogin(), username);
        }
        finally {
            wireMockServer.stop();
        }
    }

    @Test
    public void testInvalidJwksUri() {
        WireMockServer wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        try {
            String issuer = wireMockServer.baseUrl();

            wireMockServer.stubFor(WireMock.get(JWKS_URI)
                    .willReturn(WireMock.aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")));

            Config.get().setString(ConfigDefaults.OIDC_IDP_ISSUER, issuer);
            Config.get().setString(ConfigDefaults.OIDC_IDP_JWKS_PATH, "/.not-so-well-known/jwks.json");
            OidcAuthHandler handler = new OidcAuthHandler();

            Exception e = assertThrows(OidcAuthException.class, () -> handler.handleOidcLogin(issueValidToken()));
            Throwable cause = e.getCause();
            assertEquals(InvalidJwtException.class, cause.getClass());
            cause = cause.getCause();
            assertEquals(UnresolvableKeyException.class, cause.getClass());
            cause = cause.getCause();
            assertEquals(IOException.class, cause.getClass());
            assertTrue(cause.getMessage().contains("404 Not Found"));
        }
        finally {
            wireMockServer.stop();
        }
    }

    private OidcAuthHandler getHandler() throws JoseException {
        return getHandler(rsaKeyPair.getPublic());
    }

    private OidcAuthHandler getHandler(PublicKey publicKey) throws JoseException {
        return getHandler(publicKey, new HttpClientAdapter());
    }

    private OidcAuthHandler getHandler(PublicKey publicKey, HttpClientAdapter httpClient) throws JoseException {
        PublicJsonWebKey jwk = PublicJsonWebKey.Factory.newPublicJwk(publicKey);
        jwk.setKeyId(TEST_KID);
        VerificationKeyResolver keyResolver = new JwksVerificationKeyResolver(new JsonWebKeySet(jwk).getJsonWebKeys());
        return new OidcAuthHandler(keyResolver, httpClient);
    }

    private void enableOidcLogin() {
        Config.get().setBoolean(ConfigDefaults.OIDC_ENABLED, "true");
        Config.get().setString(ConfigDefaults.OIDC_IDP_ISSUER, ISSUER);
        Config.get().setString(ConfigDefaults.OIDC_IDP_JWKS_PATH, JWKS_URI);
    }

    private String signToken(JwtClaims claims, Key signingKey, String algorithm) throws JoseException {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(signingKey);
        jws.setKeyIdHeaderValue(TEST_KID);
        jws.setAlgorithmHeaderValue(algorithm);

        return jws.getCompactSerialization();
    }

    private String issueToken(PrivateKey privateKey, String algorithm, String issuer, List<String> audienceList,
            Map<String, Object> extraClaims) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(issuer);
        claims.setSubject(UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setAudience(audienceList);
        extraClaims.forEach(claims::setClaim);

        return signToken(claims, privateKey, algorithm);
    }

    private String issueValidToken() throws JoseException {
        return issueValidToken(rsaKeyPair.getPrivate(), AlgorithmIdentifiers.RSA_USING_SHA256, user.getLogin());
    }

    private String issueValidToken(PrivateKey privateKey, String algorithm, String username) throws JoseException {
        return issueToken(privateKey, algorithm, ISSUER, List.of(MCP_AUDIENCE, MLM_AUDIENCE),
                Map.of(USERNAME_CLAIM, username));
    }

    private JwtClaims generateValidClaims() {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(ISSUER);
        claims.setSubject(UUID.randomUUID().toString());
        claims.setExpirationTimeMinutesInTheFuture(10);
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setAudience(List.of("uyuni-server"));
        claims.setClaim(USERNAME_CLAIM, user.getId());
        return claims;
    }

    /**
     * Sign a JWT insecurely with RSA SHA-1 to test rejection.
     * @param claims the jwt claims object
     * @param privateKey the RSA private key
     * @return the JWT signed with RSA + SHA-1
     */
    private static String signTokenWithRs1(JwtClaims claims, RSAPrivateKey privateKey) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        // jose4j doesn't allow signing keys insecurely so this method does it manually.
        String headerJson = "{\"alg\":\"RS1\",\"typ\":\"JWT\"}";

        String payloadJson = JsonUtil.toJson(claims.getClaimsMap());

        String headerB64 = Base64Url.encode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadB64 = Base64Url.encode(payloadJson.getBytes(StandardCharsets.UTF_8));

        String signingInput = headerB64 + "." + payloadB64;

        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initSign(privateKey);
        signature.update(signingInput.getBytes(StandardCharsets.UTF_8));
        byte[] sigBytes = signature.sign();

        String sigB64 = Base64Url.encode(sigBytes);

        // Compact serialization
        return signingInput + "." + sigB64;
    }
}
