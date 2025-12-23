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

package com.suse.proxy;

import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_ADMIN_MAIL;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_1;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_INTERMEDIATE_CA_2;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_MAX_CACHE;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_PARENT_FQDN;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_PROXY_CERT;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_PROXY_FQDN;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_PROXY_KEY;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_PROXY_PORT;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_ROOT_CA;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_SSH_KEY;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_SSH_PARENT;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.DUMMY_SSH_PUB;
import static com.suse.proxy.ProxyConfigUpdateTestUtils.assertExpectedErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.proxycontainerconfig.ProxyContainerConfigCreateFacade;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.ssl.SSLCertGenerationException;
import com.suse.manager.ssl.SSLCertManager;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.TestSaltApi;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateFileAcquisitor;
import com.suse.proxy.update.ProxyConfigUpdateValidation;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link ProxyConfigUpdateFileAcquisitor}
 * Assumes {@link ProxyConfigUpdateValidation} step is executed before, not registering any errors.
 */
public class ProxyConfigUpdateFileAcquisitorTest extends BaseTestCaseWithUser {

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemManager systemManager =
            new SystemManager(ServerFactory.SINGLETON, ServerGroupFactory.SINGLETON, saltApi);

    @SuppressWarnings({"java:S1171", "java:S3599"})
    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Test a scenario where all expected file configuration values are returned
     * by the {@link ProxyContainerConfigCreateFacade#createFiles} method.
     */
    @Test
    public void testWhenCreateFilesReturnsAllValues() {
        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        Map<String, Object> expectedProxyConfigFiles = Map.of(
                "server", "dummyServer",
                "ca_crt", "dummyCaCrt",
                "proxy_fqdn", "dummyProxyFqdn",
                "max_cache_size_mb", 100L,
                "server_version", "dummyServerVersion",
                "email", "dummyEmail",
                "httpd", Map.of(
                        "system_id", "dummySystemId",
                        "server_crt", "dummyServerCrt",
                        "server_key", "dummyServerKey"
                ),
                "ssh", Map.of(
                        "server_ssh_key_pub", "dummyServerSshKeyPub",
                        "server_ssh_push", "dummyServerSshPush",
                        "server_ssh_push_pub", "dummyServerSshPushPub"
                ),
                "replace_fqdns", List.of("masterFqdn")
        );
        mockProxyContainerConfigCreateFacadeMockCreateFiles(expectedProxyConfigFiles);

        new ProxyConfigUpdateFileAcquisitor().handle(proxyConfigUpdateContext);
        assertEquals(expectedProxyConfigFiles, proxyConfigUpdateContext.getProxyConfigFiles());
        assertFalse(proxyConfigUpdateContext.getErrorReport().hasErrors());
    }

    @Test
    public void testWhenCreateFilesReturnsNoContents() {
        final String[] expectedErrorMessages = {
                "proxy container configuration did not generate required entry: server_version",
                "proxy container configuration did not generate required entry: max_cache_size_mb",
                "proxy container configuration did not generate required entry: ssh",
                "proxy container configuration did not generate required entry: proxy_fqdn",
                "proxy container configuration did not generate required entry: ca_crt",
                "proxy container configuration did not generate required entry: server",
                "proxy container configuration did not generate required entry: httpd",
                "proxy container configuration did not generate required entry: email",
                "proxy container configuration did not generate required entry: replace_fqdns"
        };

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        mockProxyContainerConfigCreateFacadeMockCreateFiles(new HashMap<>());

        new ProxyConfigUpdateFileAcquisitor().handle(proxyConfigUpdateContext);
        assertNotNull(proxyConfigUpdateContext.getProxyConfigFiles());
        assertTrue(proxyConfigUpdateContext.getProxyConfigFiles().isEmpty());
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    @Test
    public void testWhenCreateFilesReturnsNull() {
        final String[] expectedErrorMessages = {"proxy container configuration files were not created"};

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        mockProxyContainerConfigCreateFacadeMockCreateFiles(null);

        new ProxyConfigUpdateFileAcquisitor().handle(proxyConfigUpdateContext);
        assertNull(proxyConfigUpdateContext.getProxyConfigFiles());
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    @Test
    public void testWhenCreateFilesReturnsUnexpectedValues() {
        final String[] expectedErrorMessages = {
                "proxy container configuration generated an unexpected value for entry: server",
                "proxy container configuration generated an unexpected value for entry: ca_crt",
                "proxy container configuration did not generate required entry: proxy_fqdn",
                "proxy container configuration did not generate required entry: max_cache_size_mb",
                "proxy container configuration did not generate required entry: server_version",
                "proxy container configuration did not generate required entry: email",
                "proxy container configuration did not generate required entry: httpd.system_id",
                "proxy container configuration generated an unexpected value for entry: httpd.server_key",
                "proxy container configuration generated an unexpected value for entry: ssh"
        };

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        Map<String, Object> expectedProxyConfigFiles = Map.of(
                "server", 1L,
                "ca_crt", Map.of("not", "expected"),
                "not", "expected_key",
                "httpd", Map.of(
                        "not", "a_key",
                        "server_crt", "dummyServerCrt",
                        "server_key", Map.of("unexpected", "map")
                ),
                "ssh", "not_a_map",
                "replace_fqdns", List.of("masterFqdn")
        );
        mockProxyContainerConfigCreateFacadeMockCreateFiles(expectedProxyConfigFiles);

        new ProxyConfigUpdateFileAcquisitor().handle(proxyConfigUpdateContext);
        assertEquals(expectedProxyConfigFiles, proxyConfigUpdateContext.getProxyConfigFiles());
        assertExpectedErrors(expectedErrorMessages, proxyConfigUpdateContext);
    }

    @SuppressWarnings({"java:S1171", "java:S3599"})
    @Test
    public void testWhenCreateFilesThrowsSSLCertGenerationException() {
        final String expectedErrorMessage = "Failed to create proxy container configuration";

        ProxyConfigUpdateContext proxyConfigUpdateContext = getProxyConfigUpdateContext();

        try {
            ProxyContainerConfigCreateFacade proxyContainerConfigCreateFacadeMock =
                    getProxyContainerConfigCreateFacadeMock();
            context.checking(new Expectations() {{
                allowing(proxyContainerConfigCreateFacadeMock).createFiles(
                        with(any(SaltApi.class)),
                        with(any(SystemEntitlementManager.class)),
                        with(any(User.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(Integer.class)),
                        with(any(Long.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(List.class)),
                        with(any(SSLCertPair.class)),
                        with.is(anything()),
                        with.is(anything()),
                        with.is(anything()),
                        with(any(SSLCertManager.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(String.class))
                );
                will(throwException(new SSLCertGenerationException(expectedErrorMessage)));
            }});
        }
        catch (Exception e) {
            fail("Failed to mock ProxyContainerConfigCreateFacade#createFiles method: " + e.getMessage());
        }


        new ProxyConfigUpdateFileAcquisitor().handle(proxyConfigUpdateContext);
        assertExpectedErrors(new String[]{expectedErrorMessage}, proxyConfigUpdateContext);
    }

    /**
     * Creates a dummy {@link ProxyConfigUpdateContext} with placeholder values.
     * These values are not meaningful but are necessary for the method to function.
     * It is assumed that the {@link ProxyConfigUpdateValidation} step has already
     * been executed successfully, ensuring their existence.
     *
     * @return a dummy {@link ProxyConfigUpdateContext} instance
     */
    private ProxyConfigUpdateContext getProxyConfigUpdateContext() {
        ProxyConfigUpdateJson request = new ProxyConfigUpdateJsonBuilder()
                .proxyPort(DUMMY_PROXY_PORT)
                .parentFqdn(DUMMY_PARENT_FQDN)
                .maxCache(DUMMY_MAX_CACHE)
                .email(DUMMY_ADMIN_MAIL)
                .sshParentPub(DUMMY_SSH_PARENT)
                .sshPub(DUMMY_SSH_PUB)
                .sshKey(DUMMY_SSH_KEY)
                .build();
        ProxyConfigUpdateContext proxyConfigUpdateContext =
                new ProxyConfigUpdateContext(request, systemManager, user);
        proxyConfigUpdateContext.setProxyFqdn(DUMMY_PROXY_FQDN);
        proxyConfigUpdateContext.setRootCA(DUMMY_ROOT_CA);
        proxyConfigUpdateContext.setIntermediateCAs(List.of(DUMMY_INTERMEDIATE_CA_1, DUMMY_INTERMEDIATE_CA_2));
        proxyConfigUpdateContext.setProxyCert(DUMMY_PROXY_CERT);
        proxyConfigUpdateContext.setProxyKey(DUMMY_PROXY_KEY);
        return proxyConfigUpdateContext;
    }


    /**
     * Mocks the {@link ProxyContainerConfigCreateFacade#createFiles} method to return a given value
     * .
     *
     * @param expectedProxyConfigFiles the value to be returned by the mocked method
     */
    @SuppressWarnings({"java:S1171", "java:S3599"})
    private void mockProxyContainerConfigCreateFacadeMockCreateFiles(Map<String, Object> expectedProxyConfigFiles) {
        try {
            ProxyContainerConfigCreateFacade proxyContainerConfigCreateFacadeMock =
                    getProxyContainerConfigCreateFacadeMock();
            context.checking(new Expectations() {{
                allowing(proxyContainerConfigCreateFacadeMock).createFiles(
                        with(any(SaltApi.class)),
                        with(any(SystemEntitlementManager.class)),
                        with(any(User.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(Integer.class)),
                        with(any(Long.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(List.class)),
                        with(any(SSLCertPair.class)),
                        with.is(anything()),
                        with.is(anything()),
                        with.is(anything()),
                        with(any(SSLCertManager.class)),
                        with(any(String.class)),
                        with(any(String.class)),
                        with(any(String.class))
                );
                will(returnValue(expectedProxyConfigFiles));
            }});
        }
        catch (Exception e) {
            fail("Failed to mock ProxyContainerConfigCreateFacade#createFiles method: " + e.getMessage());
        }
    }

    /**
     * Create a mock {@link ProxyContainerConfigCreateFacade} and replace it in systemManager.
     *
     * @return the mocked {@link ProxyContainerConfigCreateFacade}
     * @throws NoSuchFieldException   if the field is not found
     * @throws IllegalAccessException if the field is not accessible
     */
    @SuppressWarnings("squid:S3011")
    private ProxyContainerConfigCreateFacade getProxyContainerConfigCreateFacadeMock()
            throws NoSuchFieldException, IllegalAccessException {
        ProxyContainerConfigCreateFacade mockFacade = context.mock(ProxyContainerConfigCreateFacade.class);
        Field field = SystemManager.class.getDeclaredField("proxyContainerConfigCreateFacade");
        field.setAccessible(true);
        field.set(systemManager, mockFacade);
        return mockFacade;
    }
}
