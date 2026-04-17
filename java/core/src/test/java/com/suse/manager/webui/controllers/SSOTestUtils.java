/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.controllers;

import com.onelogin.saml2.settings.Saml2Settings;
import com.onelogin.saml2.settings.SettingsBuilder;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class SSOTestUtils {

    private SSOTestUtils() {
        // Prevent instantiation
    }

    public static Optional<Saml2Settings> getSaml2Settings()
            throws MalformedURLException {
        Map<String, Object> samlData = new HashMap<>();
        SettingsBuilder builder = new SettingsBuilder();
        samlData.put("onelogin.saml2.sp.entityid", "https://localhost/metadata.jsp");
        samlData.put("onelogin.saml2.sp.assertion_consumer_service.url", "https://localhost/acs.jsp");
        samlData.put("onelogin.saml2.security.want_xml_validation", true);
        samlData.put("onelogin.saml2.idp.entityid", "https://idp");
        samlData.put("onelogin.saml2.idp.single_sign_on_service.url", "https://idp/sso");
        samlData.put("onelogin.saml2.idp.x509cert", """
            -----BEGIN CERTIFICATE-----
            MIICNDCCAZ2gAwIBAgIBADANBgkqhkiG9w0BAQ0FADA3MQswCQYDVQQGEwJ1czEM
            MAoGA1UECAwDZm9vMQwwCgYDVQQKDANiYXIxDDAKBgNVBAMMA3llczAeFw0xOTA1
            MDkxNjI5MjlaFw0yMDA1MDgxNjI5MjlaMDcxCzAJBgNVBAYTAnVzMQwwCgYDVQQI
            DANmb28xDDAKBgNVBAoMA2JhcjEMMAoGA1UEAwwDeWVzMIGfMA0GCSqGSIb3DQEB
            AQUAA4GNADCBiQKBgQDDxirCp0Fyr3lM+qciXW1oOKegScth2uVzCbah9+JyEB4S
            dFSPdsT9BB5Jj2/BZlQVHTr9C3TXaow79tSg1IDVjGwhSDQLnkfkXRr3h+reQFlj
            /zCS7gi2Yv+KJG9/ZODDSUp/YrDWuGLQfScR3KGZxxPd//vPLaE/yocuK3kdzQID
            AQABo1AwTjAdBgNVHQ4EFgQU2nQoIcw2rwCVj1Mxh7PYnUs4qjIwHwYDVR0jBBgw
            FoAU2nQoIcw2rwCVj1Mxh7PYnUs4qjIwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0B
            AQ0FAAOBgQAkGZg7TM7DCKLFM1E7rcPfg5SLPGueNbDK3i5oizrMa//L7auVRM+r
            jHaIbhGK5KlF5vaabSygxRTfgtI4Npv6aF3Bs57sqKsIVnxaOm+w7VUAB4Yv9Riz
            FHQbixAeSxYR8QKSjSvQKdrCrbksUUOudq0eB+Wfir+HFIIW1tgh1g==
            -----END CERTIFICATE-----""");
        return Optional.of(builder.fromValues(samlData).build());
    }
}
