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
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_ADMIN_MAIL;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_MAX_CACHE;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PARENT_FQDN;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_CERT;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_FQDN;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_KEY;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_PORT;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_ROOT_CA;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_SERVER_ID;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_TAG;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.assertExpectedErrors;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.MockObjectTestCase;

import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.model.ProxyConfig;
import com.suse.proxy.update.ProxyConfigUpdateAcquisitor;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateValidation;
import com.suse.utils.Json;

import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link ProxyConfigUpdateValidation} class
 * These will assume the previous step in the chain of responsibility {@link ProxyConfigUpdateAcquisitor}
 * has been executed and, no errors have been added to the context.
 */
public class ProxyConfigUpdateValidationTest extends MockObjectTestCase {

    public static final String UNKNOWN = "unknown";

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
                "rootCA is required",
                "proxyCertificate is required",
                "proxyKey is required",
                "sourceMode is required"
        };

        ProxyConfigUpdateJson request = Json.GSON.fromJson("{}", ProxyConfigUpdateJson.class);
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);

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
                .serverId(DUMMY_SERVER_ID)
                .parentFqdn("invalid@fqdn")
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .sourceRPM()
                .build();

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setRootCA(DUMMY_ROOT_CA);
        proxyConfigUpdateContext.setProxyCert(DUMMY_PROXY_CERT);
        proxyConfigUpdateContext.setProxyKey(DUMMY_PROXY_KEY);

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

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setRootCA(DUMMY_ROOT_CA);
        proxyConfigUpdateContext.setProxyCert(DUMMY_PROXY_CERT);
        proxyConfigUpdateContext.setProxyKey(DUMMY_PROXY_KEY);
        proxyConfigUpdateContext.setParentServer(new Server(DUMMY_SERVER_ID, DUMMY_PARENT_FQDN));
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);

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

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(DUMMY_SERVER_ID, DUMMY_PARENT_FQDN));
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);

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

        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(DUMMY_SERVER_ID, DUMMY_PARENT_FQDN));
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);
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
                .sourceMode(UNKNOWN)
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
                .registryMode(UNKNOWN)
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
                .registryBaseTag(DUMMY_TAG)
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
                        "http://registry.suse.com/httpd", DUMMY_TAG,
                        "http://registry.suse.com/saltbroker", DUMMY_TAG,
                        "http://registry.suse.com/squid", DUMMY_TAG,
                        "http://registry.suse.com/ssh", DUMMY_TAG,
                        "http://registry.suse.com/tftpd", DUMMY_TAG
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
        ProxyConfigUpdateContext proxyConfigUpdateContext = new ProxyConfigUpdateContext(request, null, null);
        proxyConfigUpdateContext.setParentServer(new Server(DUMMY_SERVER_ID, DUMMY_PARENT_FQDN));
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);
        // certificate content is handled in {@link ProxyConfigUpdateAcquisitor} and set directly in the context
        proxyConfigUpdateContext.setProxyConfig(new ProxyConfig());
        proxyConfigUpdateContext.setRootCA(DUMMY_ROOT_CA);
        proxyConfigUpdateContext.setProxyCert(DUMMY_PROXY_CERT);
        proxyConfigUpdateContext.setProxyKey(DUMMY_PROXY_KEY);
        return proxyConfigUpdateContext;
    }

    /**
     * Creates a base request with the required fields for the proxy configuration using rpm as source
     *
     * @return the builder
     */
    private ProxyConfigUpdateJsonBuilder getBaseRequestWithRpm() {
        return new ProxyConfigUpdateJsonBuilder()
                .serverId(DUMMY_SERVER_ID)
                .parentFqdn(DUMMY_PARENT_FQDN)
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .sourceRPM();
    }

    /**
     * Creates a base request with the required fields for the proxy configuration replacing certificates
     *
     * @return the builder
     */
    private ProxyConfigUpdateJsonBuilder getBaseRequestWithReplaceCerts() {
        return new ProxyConfigUpdateJsonBuilder()
                .serverId(DUMMY_SERVER_ID)
                .parentFqdn(DUMMY_PARENT_FQDN)
                .proxyPort(DUMMY_PROXY_PORT)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .replaceCerts(null, null, null, null);
    }
}
