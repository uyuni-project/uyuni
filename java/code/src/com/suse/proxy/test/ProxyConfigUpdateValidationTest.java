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

package com.suse.proxy.test;

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_ADVANCED;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_REGISTRY;
import static com.suse.proxy.test.ProxyConfigUpdateUtils.assertExpectedErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.update.ProxyConfigUpdateAcquisitor;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateValidation;
import com.suse.utils.Json;

import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Tests for the {@link ProxyConfigUpdateValidation} class
 * These will assume the previous step in the chain of responsibility {@link ProxyConfigUpdateAcquisitor}
 * has been executed and, no errors have been added to the context.
 */
@ExtendWith(JUnit5Mockery.class)
public class ProxyConfigUpdateValidationTest extends MockObjectTestCase {

    /**
     * Test a scenario where ProxyConfigUpdateJson is resolved as being empty
     */
    @Test
    public void testFailureWhenBlankRequest() {
        // setup
        final String[] expectedErrorMessages = {
                "serverId is required",
                "parentFQDN is required",
                "proxyPort is required",
                "maxSquidCacheSize is required",
                "proxyAdminEmail is required",
                "rootCA is required",
                "proxyCertificate is required",
                "proxyKey is required",
                "sourceMode is required"
        };

        ProxyConfigUpdateJson request = Json.GSON.fromJson("{}", ProxyConfigUpdateJson.class);
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);

        // execution
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);

        //assertions
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test scenario where minimal required data is provided.
     * Certificates are set to keep and source mode set to use rpm.
     * However, proxyFqdn is not resolved and parentFqdn is invalid.
     */
    @Test
    public void testFailureWhenProxyFqdnNotResolvedAndParentFqdnIsInvalid() {
        final String[] expectedErrorMessages = {
                "proxyFQDN for the server was not resolved",
                "parentFQDN is invalid"
        };

        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .serverId(123L)
                .parentFqdn("invalid@fqdn")
                .proxyPort(3182)
                .maxCache(1000)
                .email("admin@suse.com")
                .sourceRPM()
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setRootCA("rootCA");
        proxyConfigUpdateContext.setProxyCert("proxyCert");
        proxyConfigUpdateContext.setProxyKey("proxyKey");

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test the successful scenario when replacing certificates and using rpm source mode
     */
    @Test
    public void testSuccessWhenReplaceCerts() {
        ProxyConfigUpdateJson request = getBaseRequestWithRpm()
                .replaceCerts(null, null, null, null)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setRootCA("rootCA");
        proxyConfigUpdateContext.setProxyCert("proxyCert");
        proxyConfigUpdateContext.setProxyKey("proxyKey");
        proxyConfigUpdateContext.setParentServer(new Server(123L, "parent.fqdn.com"));
        proxyConfigUpdateContext.setProxyFqdn("proxy.fqdn.com");

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    /**
     * Test the successful scenario when keeping existing certificates and using rpm source mode
     */
    @Test
    public void testSuccessWhenKeepCerts() {
        ProxyConfigUpdateJson request = getBaseRequestWithRpm()
                .keepCerts(null, null, null, null)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    /**
     * Test a scenario when keeping existing certificates and using rpm source mode.
     * However, no existing proxy configuration has been retrieved.
     */
    @Test
    public void testFailWhenKeepCertsButNoExistingProxyConfig() {
        final String[] expectedErrorMessages = {
                "No current proxy configuration found to keep certificates"
        };

        ProxyConfigUpdateJson request = getBaseRequestWithRpm()
                .keepCerts(null, null, null, null)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(123L, "parent.fqdn.com"));
        proxyConfigUpdateContext.setProxyFqdn("proxy.fqdn.com");

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test a scenario when keeping existing certificates and using rpm source mode.
     * However, existing proxy config certificates are have no content.
     */
    @Test
    public void testFailWhenKeepCertsButNoExistingCertificates() {
        final String[] expectedErrorMessages = {
                "rootCA not found on current proxy configuration",
                "proxyCertificate not found on current proxy configuration",
                "proxyKey not found on current proxy configuration",
        };

        ProxyConfigUpdateJson request = getBaseRequestWithRpm()
                .keepCerts(null, null, null, null)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(123L, "parent.fqdn.com"));
        proxyConfigUpdateContext.setProxyFqdn("proxy.fqdn.com");
        proxyConfigUpdateContext.setProxyConfig(new ProxyConfig());

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }


    /**
     * Test the successful scenario when replacing existing certificates.
     * However, the provided source mode is invalid.
     */
    @Test
    public void testFailWhenInvalidSourceMode() {
        final String[] expectedErrorMessages = {
                "sourceMode unknown is invalid. Must be either 'registry' or 'rpm'",
        };

        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceMode("unknown")
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }


    /**
     * Test a scenario when replacing existing certificates and using registry source.
     * However, the provided registry mode is invalid.
     */
    @Test
    public void testFailWhenInvalidRegistryMode() {
        final String[] expectedErrorMessages = {
                "sourceRegistryMode unknown is invalid. Must be either 'simple' or 'advanced'",
        };

        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode("unknown")
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test a scenario when replacing existing certificates and using registry source in simple mode.
     * However, no registry url or tag is provided.
     */
    @Test
    public void testFailWhenSimpleRegistryModeButNoUrlOrTagProvided() {
        final String[] expectedErrorMessages = {
                "registryBaseURL is required",
                "registryBaseTag is required",
        };

        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode(REGISTRY_MODE_SIMPLE)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test the successful scenario when replacing existing certificates and using registry source in simple mode.
     */
    @Test
    public void testSuccessWhenSimpleRegistryMode() {
        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode(REGISTRY_MODE_SIMPLE)
                .registryBaseURL("http://registry.suse.com")
                .registryBaseTag("latest")
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    /**
     * Test a scenario when replacing existing certificates and using registry source in advanced mode.
     * However, no registry urls or tags are provided.
     */
    @Test
    public void testFailWhenAdvancedRegistryModeButNoUrlsOrTagsProvided() {
        final String[] expectedErrorMessages = {
                "registryHttpdURL is required",
                "registryHttpdTag is required",
                "registrySaltbrokerURL is required",
                "registrySaltbrokerTag is required",
                "registrySquidURL is required",
                "registrySquidTag is required",
                "registrySshURL is required",
                "registrySshTag is required",
                "registryTftpdURL is required",
                "registryTftpdTag is required"
        };

        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceMode(SOURCE_MODE_REGISTRY)
                .registryMode(REGISTRY_MODE_ADVANCED)
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    /**
     * Test the successful scenario when replacing existing certificates and using registry source in advanced mode.
     */
    @Test
    public void testSuccessWhenAdvancedRegistryMode() {
        ProxyConfigUpdateJson request = getBaseRequestWithReplaceCerts()
                .sourceRegistryAdvanced(
                        "http://registry.suse.com/httpd", "latest",
                        "http://registry.suse.com/saltbroker", "latest",
                        "http://registry.suse.com/squid", "latest",
                        "http://registry.suse.com/ssh", "latest",
                        "http://registry.suse.com/tftpd", "latest"
                )
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext(request);

        //
        new ProxyConfigUpdateValidation().handle(proxyConfigUpdateContext);
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }


    /**
     * Creates a {@link ProxyConfigUpdateContext} with the provided request and assuming previous step
     * {@link ProxyConfigUpdateAcquisitor} resolved the proxy minion, certificates, parent server and proxy fqdn.
     *
     * @param request the request
     * @return the context
     */
    private static ProxyConfigUpdateContext getProxyConfigUpdateContext(ProxyConfigUpdateJson request) {
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(123L, "parent.fqdn.com"));
        proxyConfigUpdateContext.setProxyFqdn("proxy.fqdn.com");
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setProxyConfig(new ProxyConfig());
        proxyConfigUpdateContext.setRootCA("rootCA");
        proxyConfigUpdateContext.setProxyCert("proxyCert");
        proxyConfigUpdateContext.setProxyKey("proxyKey");
        return proxyConfigUpdateContext;
    }

    /**
     * Creates a base request with the required fields for the proxy configuration using rpm as source
     *
     * @return the builder
     */
    private ProxyConfigUpdateJsonBuilder getBaseRequestWithRpm() {
        return new ProxyConfigUpdateJsonBuilder()
                .serverId(123L)
                .parentFqdn("proxy.suse.com")
                .proxyPort(3182)
                .maxCache(1000)
                .email("admin@suse.com")
                .sourceRPM();
    }

    /**
     * Creates a base request with the required fields for the proxy configuration replacing certificates
     *
     * @return the builder
     */
    private ProxyConfigUpdateJsonBuilder getBaseRequestWithReplaceCerts() {
        return new ProxyConfigUpdateJsonBuilder()
                .serverId(123L)
                .parentFqdn("proxy.suse.com")
                .proxyPort(3182)
                .maxCache(1000)
                .email("admin@suse.com")
                .replaceCerts(null, null, null, null);
    }
}
