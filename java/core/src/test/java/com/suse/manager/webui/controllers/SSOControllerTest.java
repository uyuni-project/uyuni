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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.frontend.security.AuthenticationServiceFactory;
import com.redhat.rhn.frontend.security.PxtAuthenticationService;
import com.redhat.rhn.frontend.servlets.PxtSessionDelegate;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import spark.Request;
import spark.Response;

public class SSOControllerTest extends BaseControllerTestCase {

    private SSOController ssoController;

    private boolean ssoEnabled;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        ssoEnabled = Config.get().getBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED);
        ssoController = new SSOController(Optional.of(SSOTestUtils.getSaml2Settings()));
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        // Restore the original SSO Enabled value
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, Boolean.toString(ssoEnabled));

        super.tearDown();
    }

    @Test
    public void testACSWithoutSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        assertNull(ssoController.getACS(getRequestWithCsrf("/manager/sso/acs"), response));
    }

    @Test
    public void testMetadataWithSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        String metadata = (String) ssoController.getMetadata(requestWithCsrf, response);
        assertTrue(response.type().contains("text/xml"));
        Assertions.assertNotNull(metadata);
        assertTrue(metadata.contains("md:EntityDescriptor"));
    }

    @Test
    public void testMetadataWithoutSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");

        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        String metadata = (String) ssoController.getMetadata(requestWithCsrf, response);
        Assertions.assertNull(metadata);
    }

    @Test
    public void testACSWithSSOInvalidSamlResponseRedirectsToRoot() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String invalidSamlResponse = Base64.getEncoder()
                .encodeToString("<Response/>".getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("/manager/sso/acs", Map.of(
                "SAMLResponse", invalidSamlResponse,
                "RelayState", "/rhn/systems/Overview.do"
        ));

        Object result = ssoController.getACS(request, response);
        Assertions.assertSame(response, result);
        Assertions.assertEquals("/", ((RhnMockHttpServletResponse) response.raw()).getRedirect());
        Assertions.assertNull(request.raw().getSession().getAttribute("attributes"));
        Assertions.assertNull(request.raw().getSession().getAttribute("nameId"));
    }

    @Test
    public void testACSWithSSOMalformedBase64RedirectsToRoot() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        Request request = getRequestWithCsrfAndParams("/manager/sso/acs", Map.of(
                "SAMLResponse", "not-base64",
                "RelayState", "/rhn/systems/Overview.do"
        ));

        Object result = ssoController.getACS(request, response);
        Assertions.assertNull(result);
        Assertions.assertNull(((RhnMockHttpServletResponse) response.raw()).getRedirect());
    }

    @Test
    public void testACSWithSSOMissingSamlResponseRedirectsToRoot() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        Request request = getRequestWithCsrfAndParams("/manager/sso/acs", Map.of(
                "RelayState", "/rhn/systems/Overview.do"
        ));

        Object result = ssoController.getACS(request, response);
        Assertions.assertNull(result);
        Assertions.assertNull(((RhnMockHttpServletResponse) response.raw()).getRedirect());
        Assertions.assertNull(request.raw().getSession().getAttribute("attributes"));
        Assertions.assertNull(request.raw().getSession().getAttribute("nameId"));
    }

    @Test
    public void testACSWithSSOInvalidSamlResponseWithoutRelayStateRedirectsToRoot() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String invalidSamlResponse = Base64.getEncoder()
                .encodeToString("<Response/>".getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("/manager/sso/acs", Map.of(
                "SAMLResponse", invalidSamlResponse
        ));

        Object result = ssoController.getACS(request, response);
        Assertions.assertSame(response, result);
        Assertions.assertEquals("/", ((RhnMockHttpServletResponse) response.raw()).getRedirect());
    }

    @Test
    public void testACSWithSSOInvalidSamlResponseAndExternalRelayStateRedirectsToRoot() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String invalidSamlResponse = Base64.getEncoder()
                .encodeToString("<Response/>".getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("/manager/sso/acs", Map.of(
                "SAMLResponse", invalidSamlResponse,
                "RelayState", "https://attacker.invalid/phish"
        ));

        Object result = ssoController.getACS(request, response);
        Assertions.assertSame(response, result);
        Assertions.assertEquals("/", ((RhnMockHttpServletResponse) response.raw()).getRedirect());
    }

    @Test
    public void testACSWithMissingUidRedirectsToRoot() throws Exception {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String requestId = "testRequest-" + TestUtils.randomString(4);
        String xml = FileUtils.readStringFromFile(TestUtils.findTestData("sso/response.xml"))
                // Change uuid to email to invalidate the request
                .replace("Name=\"uid\"", "Name=\"email\"")
                .replace("{user}", user.getLogin())
                .replace("{requestId}", requestId);

        String signedXml = SSOTestUtils.signSamlDocument(xml, requestId);
        String encodedSamlResponse = Base64.getEncoder().encodeToString(signedXml.getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("https://localhost", "/acs.jsp", Map.of(
                "SAMLResponse", encodedSamlResponse,
                "RelayState", "/rhn/systems/Overview.do"
        ));

        // Execute the controller
        Object result = ssoController.getACS(request, response);

        // Verify it falls through the missing UID logic and redirects to the root "/"
        Response rr = assertInstanceOf(Response.class, result);
        RhnMockHttpServletResponse mockResponse = assertInstanceOf(RhnMockHttpServletResponse.class, rr.raw());
        assertEquals("/", mockResponse.getRedirect());
    }

    @Test
    public void testACSWithValidResponse() throws Exception {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String requestId = "testRequest-" + TestUtils.randomString(4);
        String xml = FileUtils.readStringFromFile(TestUtils.findTestData("sso/response.xml"))
                .replace("{user}", user.getLogin())
                .replace("{requestId}", requestId);

        String signedXml = SSOTestUtils.signSamlDocument(xml, requestId);
        String encodedSamlResponse = Base64.getEncoder().encodeToString(signedXml.getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("https://localhost", "/acs.jsp", Map.of(
                "SAMLResponse", encodedSamlResponse,
                "RelayState", "/rhn/systems/Overview.do"
        ));

        // 3. Execute the controller
        Object result = ssoController.getACS(request, response);

        // 4. Assertions
        // Verify the toolkit successfully parsed the XML and pulled the UID into the session
        assertEquals(user.getLogin(), request.raw().getSession().getAttribute("nameId"));

        @SuppressWarnings("unchecked")
        var sessionAttributes = (Map<String, List<String>>) request.raw().getSession().getAttribute("attributes");
        assertNotNull(sessionAttributes);
        assertEquals(user.getLogin(), sessionAttributes.get("uid").get(0));

        Response rr = assertInstanceOf(Response.class, result);
        RhnMockHttpServletResponse mockResponse = assertInstanceOf(RhnMockHttpServletResponse.class, rr.raw());
        assertEquals("/rhn/systems/Overview.do", mockResponse.getRedirect());
    }

    @Test
    public void testACSWithNonExistingUser() throws Exception {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        String requestId = "testRequest-" + TestUtils.randomString(4);
        String xml = FileUtils.readStringFromFile(TestUtils.findTestData("sso/response.xml"))
                .replace("{user}", "wrong_username")
                .replace("{requestId}", requestId);

        String signedXml = SSOTestUtils.signSamlDocument(xml, requestId);
        String encodedSamlResponse = Base64.getEncoder().encodeToString(signedXml.getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("https://localhost", "/acs.jsp", Map.of(
                "SAMLResponse", encodedSamlResponse,
                "RelayState", "/rhn/systems/Overview.do"
        ));

        // Execute the controller
        Object result = ssoController.getACS(request, response);

        String errorMessage = assertInstanceOf(String.class, result);
        assertEquals(
            "Internal error during SSO authentication phase. " +
                 "Have you created the corresponding user in SUSE Manager? See product documentation", errorMessage
        );
    }

    @Test
    public void testLogoutWithSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        // Simulate hitting the logout endpoint
        Request request = getRequestWithCsrf("https://localhost", "/manager/sso/logout");

        // Mocking an active session to ensure it gets cleared (assuming your test harness does this)
        request.raw().getSession().setAttribute("nameId", user.getLogin());

        // Execute the controller
        Object result = ssoController.logout(request, response);

        // Assertions
        Response rr = assertInstanceOf(Response.class, result);
        RhnMockHttpServletResponse mockResponse = assertInstanceOf(RhnMockHttpServletResponse.class, rr.raw());

        // Verify the toolkit built a LogoutRequest and is redirecting the user to the IdP
        String redirectUrl = mockResponse.getRedirect();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.contains("SAMLRequest="));

        // Ensure the HTTP session authentication is removed
        assertNull(request.raw().getSession().getAttribute("csrf_token"));
        assertNull(request.raw().getSession().getAttribute("webUserID"));

        // Check the PTX delegate is also clean
        PxtAuthenticationService pxtAuthenticationService = assertInstanceOf(PxtAuthenticationService.class,
                AuthenticationServiceFactory.getInstance().getAuthenticationService());

        PxtSessionDelegate pxtDelegate = pxtAuthenticationService.getPxtDelegate();
        assertNull(pxtDelegate.getWebUserId(request.raw()));
    }

    @Test
    public void testSlsWithSSO() throws Exception {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");

        // Prepare the incoming LogoutRequest from the IdP
        String requestId = "testRequest-" + TestUtils.randomString(4);
        String xml = FileUtils.readStringFromFile(TestUtils.findTestData("sso/logout_request.xml"))
                .replace("{user}", user.getLogin())
                .replace("{requestId}", requestId);

        String signedXml = SSOTestUtils.signSamlDocument(xml, requestId);
        String encodedSamlRequest = Base64.getEncoder().encodeToString(signedXml.getBytes(StandardCharsets.UTF_8));

        Request request = getRequestWithCsrfAndParams("https://localhost", "/manager/sso/sls", Map.of(
                "SAMLRequest", encodedSamlRequest
        ));

        // Inject a fake active session so the toolkit can "destroy" it
        request.raw().getSession().setAttribute("nameId", user.getLogin());

        // Execute the controller
        Object result = ssoController.sls(request, response);

        // Verify the controller successfully processed the SLO and returned the expected string
        assertEquals("You have been logged out", result);

        // Verify the toolkit is issuing a redirect back to the IdP with a SAMLResponse (LogoutResponse)
        RhnMockHttpServletResponse mockResponse = assertInstanceOf(RhnMockHttpServletResponse.class, response.raw());
        String redirectUrl = mockResponse.getRedirect();
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.startsWith("https://idp/slo"));
        assertTrue(redirectUrl.contains("SAMLResponse="));

        // The session should have been invalidated
        var ex = assertThrows(IllegalStateException.class, () -> request.raw().getSession().getAttribute("attr"));
        assertEquals("Session has been invalidated", ex.getMessage());

        // Check the PTX delegate is also clean
        PxtAuthenticationService pxtAuthenticationService = assertInstanceOf(PxtAuthenticationService.class,
                AuthenticationServiceFactory.getInstance().getAuthenticationService());

        PxtSessionDelegate pxtDelegate = pxtAuthenticationService.getPxtDelegate();
        assertNull(pxtDelegate.getWebUserId(request.raw()));
    }
}
