/*
 * Copyright (c) 2019--2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.controllers.login.LoginController;
import com.suse.manager.webui.services.OidcAuthHandler;
import com.suse.manager.webui.utils.LoginHelper;
import com.suse.utils.Json;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.util.Util;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import spark.ModelAndView;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.routematch.RouteMatch;

public class LoginControllerTest extends BaseControllerTestCase {

    private LoginController loginController;

    private boolean ssoEnabled;
    private Saml2Settings saml2Settings;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ssoEnabled = Config.get().getBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED);
        saml2Settings = SSOTestUtils.getSaml2Settings();
        loginController = new LoginController(new OidcAuthHandler(), Optional.of(saml2Settings));
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original SSO Enabled value
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, Boolean.toString(ssoEnabled));

        super.tearDown();
    }

    @Test
    public void testUrlBounce() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        final String requestUrl = "http://localhost:8080/rhn/manager/login";
        final RouteMatch match = new RouteMatch(new Object(), requestUrl, requestUrl, "");
        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();

        mockRequest.setRequestURL(requestUrl);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        mockRequest.addParameter("url_bounce", "/rhn/users/UserDetails.do?uid=1");

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        // logging in
        LoginHelper.successfulLogin(mockRequest, response.raw(), user);
        ModelAndView result = loginController.loginView(RequestResponseFactory.create(match, mockRequest), response);

        @SuppressWarnings("unchecked")
        Map<String, String> model = (Map<String, String>) result.getModel();
        assertNotNull(mockRequest.getSession().getAttribute("webUserID"));
        assertEquals("/rhn/users/UserDetails.do?uid=1", model.get("url_bounce"));
    }

    @Test
    public void testLoginWithSSO() throws URISyntaxException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        final String requestUrl = "http://localhost:8080/rhn/manager/login";
        final RouteMatch match = new RouteMatch(new Object(), requestUrl, requestUrl, "");
        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        mockRequest.addParameter("url_bounce", "/rhn/users/UserDetails.do?uid=1");

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        ModelAndView result = loginController.loginView(RequestResponseFactory.create(match, mockRequest), response);
        assertNotNull(result);

        // we still need to check that the model has been correctly populated
        @SuppressWarnings("unchecked")
        Map<String, Object> model = (Map<String, Object>) result.getModel();
        assertNotNull(model.get("webTheme"));

        // check if we redirect to the SSO login page
        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();

        assertNotNull(mockResponse.getRedirect(), "The controller must issue a redirect to the IdP");
        String redirectUrl = URLDecoder.decode(mockResponse.getRedirect(), StandardCharsets.UTF_8);

        assertTrue(redirectUrl.startsWith("https://idp/sso"));

        Map<String, String> parametersMap = new HashMap<>();
        Stream.of(new URI(redirectUrl).getRawQuery().split("&"))
                .forEach(keyValuePair -> {
                    String[] parts = keyValuePair.split("=", 2);
                    parametersMap.put(parts[0], parts[1]);
                });

        // The url_bounce is ignored when doing IdP login
        assertEquals("/rhn/YourRhn.do", parametersMap.get("RelayState"));
        String samlRequest = parametersMap.get("SAMLRequest");
        assertNotNull(samlRequest);

        String rawSamlXml = Util.base64decodedInflated(samlRequest);
        // Verify the request is the correct type
        assertTrue(rawSamlXml.contains("<samlp:AuthnRequest"));
        // Check the issuer (SP Entity ID)
        assertTrue(rawSamlXml.contains("<saml:Issuer>https://localhost/metadata.jsp</saml:Issuer>"));
        // Check the ACL url
        assertTrue(rawSamlXml.contains("AssertionConsumerServiceURL=\"https://localhost/acs.jsp\""));
        // Check the destination (IdP SSO URL)
        assertTrue(rawSamlXml.contains("Destination=\"https://idp/sso\""));
    }

    @Test
    public void testLoginWithSSOSignatureValidation() throws Exception {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        // Force the AuthNRequest signature logic to trigger
        saml2Settings.setAuthnRequestsSigned(true);

        final String requestUrl = "http://localhost:8080/rhn/manager/login";
        final RouteMatch match = new RouteMatch(new Object(), requestUrl, requestUrl, "");
        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setRequestURL(requestUrl);
        mockRequest.addParameter("url_bounce", "/rhn/systems/Overview.do");

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        // Execute Controller
        loginController.loginView(RequestResponseFactory.create(match, mockRequest), response);

        // 5. Extract the Redirect URL and Parameters
        RhnMockHttpServletResponse mockResponse = (RhnMockHttpServletResponse) response.raw();
        assertNotNull(mockResponse.getRedirect(), "The controller must issue a redirect to the IdP");
        String redirectUrl = URLDecoder.decode(mockResponse.getRedirect(), StandardCharsets.UTF_8);

        // Deconstruct the URL using Apache URIBuilder
        Map<String, String> parametersMap = new HashMap<>();
        Stream.of(StringUtils.substringAfter(redirectUrl, "?").split("&"))
                .forEach(keyValuePair -> {
                    String[] parts = keyValuePair.split("=", 2);
                    parametersMap.put(parts[0], parts[1]);
                });

        String samlRequest = parametersMap.get("SAMLRequest");
        String relayState = parametersMap.get("RelayState");
        String sigAlg = parametersMap.get("SigAlg");
        String signatureBase64 = parametersMap.get("Signature");

        // Ensure the processor successfully attached the signature parameters
        assertAll(
            () -> assertNotNull(samlRequest, "SAMLRequest parameter is missing"),
            () -> assertNotNull(relayState, "RelayState parameter is missing"),
            () -> assertNotNull(sigAlg, "SigAlg parameter is missing"),
            () -> assertNotNull(signatureBase64, "Signature parameter is missing")
        );

        StringBuilder signedMessage = new StringBuilder()
                .append("SAMLRequest=").append(Util.urlEncoder(samlRequest))
                .append("&RelayState=").append(Util.urlEncoder(relayState))
                .append("&SigAlg=").append(Util.urlEncoder(sigAlg));

        // Verify the signature
        X509Certificate cert = SSOTestUtils.getTestCertificate();
        Signature sig = java.security.Signature.getInstance("SHA256withRSA");
        sig.initVerify(cert.getPublicKey());
        sig.update(signedMessage.toString().getBytes(StandardCharsets.UTF_8));

        boolean isSignatureValid = sig.verify(Base64.getDecoder().decode(signatureBase64));
        assertTrue(isSignatureValid, "The cryptographic signature must be valid");
    }

    @Test
    public void testUrlBounceNotAuthenticated() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        final String requestUrl = "http://localhost:8080/rhn/manager/login";
        final RouteMatch match = new RouteMatch(new Object(), requestUrl, requestUrl, "");
        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();

        mockRequest.setRequestURL(requestUrl);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        mockRequest.addParameter("url_bounce", "/rhn/users/UserDetails.do?uid=1");

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        ModelAndView result = loginController.loginView(RequestResponseFactory.create(match, mockRequest), response);

        @SuppressWarnings("unchecked")
        Map<String, String> model = (Map<String, String>) result.getModel();
        assertNull(mockRequest.getSession().getAttribute("webUserID"));
        assertEquals("/rhn/users/UserDetails.do?uid=1", model.get("url_bounce"));
    }

    @Test
    public void testLoginOK() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials(user.getLogin(), "password")),
                params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertTrue(result.isSuccess());
    }

    @Test
    public void testLoginKO() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials("admin", "wrong")),
                params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertFalse(result.isSuccess());
        assertEquals(
                LocalizationService.getInstance().getMessage("error.invalid_login"),
                String.join("", result.getMessage())
        );
    }

    @Test
    public void testLoginWithEmptyPassword() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials("admin", "")),
                params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertFalse(result.isSuccess());
        assertEquals(
                LocalizationService.getInstance().getMessage("error.invalid_login"),
                String.join("", result.getMessage())
        );
    }

    @Test
    public void testLoginWithEmptyUsername() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials("admin", "")),
                params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertFalse(result.isSuccess());
        assertEquals(
                LocalizationService.getInstance().getMessage("error.invalid_login"),
                String.join("", result.getMessage())
        );
    }

    @Test
    public void testLoginWithInvalidUsername() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials("017324193274913741974",
                        "017324193274913741974")), params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertFalse(result.isSuccess());
        assertEquals(
                LocalizationService.getInstance().getMessage("error.invalid_login"),
                String.join("", result.getMessage()));
    }

    @Test
    public void testLoginWithDisabledUsername() throws UnsupportedEncodingException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        User u = UserTestUtils.createUser(this);
        UserManager.disableUser(u, u);
        Map<String, String> params = new HashMap<>();
        Request request = SparkTestUtils.createMockRequestWithBody(
                "http://localhost:8080/rhn/manager/api/login",
                new HashMap<>(),
                Json.GSON.toJson(new LoginController.LoginCredentials(u.getLogin(),
                        "password")), params);
        Response response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        String modelView = loginController.login(request, response);
        LoginController.LoginResult result = Json.GSON.fromJson(modelView, LoginController.LoginResult.class);
        assertFalse(result.isSuccess());
        assertEquals(
                LocalizationService.getInstance().getMessage("account.disabled"),
                String.join("", result.getMessage())
        );
    }
}
