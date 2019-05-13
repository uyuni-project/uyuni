package com.suse.manager.webui.controllers.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.suse.manager.webui.controllers.SSOController;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import spark.Request;

public class SSOControllerTest extends BaseControllerTestCase {
    public void setUp() throws Exception {
        super.setUp();
        Map<String, Object> samlData = new HashMap<>();
        SettingsBuilder builder = new SettingsBuilder();
        samlData.put("onelogin.saml2.sp.entityid", "https://localhost/metadata.jsp");
        samlData.put("onelogin.saml2.sp.assertion_consumer_service.url", new URL("https://localhost/acs.jsp"));
        samlData.put("onelogin.saml2.security.want_xml_validation", true);
        samlData.put("onelogin.saml2.idp.entityid", "https://idp");
        samlData.put("onelogin.saml2.idp.single_sign_on_service.url", "https://idp/sso");
        samlData.put("onelogin.saml2.idp.x509cert", "-----BEGIN CERTIFICATE-----\n" +
                "MIICNDCCAZ2gAwIBAgIBADANBgkqhkiG9w0BAQ0FADA3MQswCQYDVQQGEwJ1czEM\n" +
                "MAoGA1UECAwDZm9vMQwwCgYDVQQKDANiYXIxDDAKBgNVBAMMA3llczAeFw0xOTA1\n" +
                "MDkxNjI5MjlaFw0yMDA1MDgxNjI5MjlaMDcxCzAJBgNVBAYTAnVzMQwwCgYDVQQI\n" +
                "DANmb28xDDAKBgNVBAoMA2JhcjEMMAoGA1UEAwwDeWVzMIGfMA0GCSqGSIb3DQEB\n" +
                "AQUAA4GNADCBiQKBgQDDxirCp0Fyr3lM+qciXW1oOKegScth2uVzCbah9+JyEB4S\n" +
                "dFSPdsT9BB5Jj2/BZlQVHTr9C3TXaow79tSg1IDVjGwhSDQLnkfkXRr3h+reQFlj\n" +
                "/zCS7gi2Yv+KJG9/ZODDSUp/YrDWuGLQfScR3KGZxxPd//vPLaE/yocuK3kdzQID\n" +
                "AQABo1AwTjAdBgNVHQ4EFgQU2nQoIcw2rwCVj1Mxh7PYnUs4qjIwHwYDVR0jBBgw\n" +
                "FoAU2nQoIcw2rwCVj1Mxh7PYnUs4qjIwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0B\n" +
                "AQ0FAAOBgQAkGZg7TM7DCKLFM1E7rcPfg5SLPGueNbDK3i5oizrMa//L7auVRM+r\n" +
                "jHaIbhGK5KlF5vaabSygxRTfgtI4Npv6aF3Bs57sqKsIVnxaOm+w7VUAB4Yv9Riz\n" +
                "FHQbixAeSxYR8QKSjSvQKdrCrbksUUOudq0eB+Wfir+HFIIW1tgh1g==\n" +
                "-----END CERTIFICATE-----");
        Saml2Settings settings = builder.fromValues(samlData).build();
        SSOController.setSsoConfig(Optional.of(settings));
    }

    public void testACSWithoutSSO() {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");
        assertNull(SSOController.getACS(getRequestWithCsrf("/manager/sso/acs"), response));
    }

    public void testMetadataWithSSO() throws IOException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "true");
        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        SSOController.getMetadata(requestWithCsrf, response);
        assertTrue(response.raw().getOutputStream().toString().contains("entityID=\"https://localhost/metadata.jsp\""));
    }

    public void testMetadataWithoutSSO() throws IOException {
        Config.get().setBoolean(ConfigDefaults.SINGLE_SIGN_ON_ENABLED, "false");
        Request requestWithCsrf = getRequestWithCsrf("/manager/sso/metadata");
        SSOController.getMetadata(requestWithCsrf, response);
        assertTrue(response.raw().getOutputStream().toString().equals(StringUtils.EMPTY));
    }
}
