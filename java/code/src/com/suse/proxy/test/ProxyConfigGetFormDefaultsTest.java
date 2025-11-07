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

import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_TAG;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_BASE_URL;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE;
import static com.suse.proxy.ProxyConfigUtils.REGISTRY_MODE_SIMPLE;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_FIELD;
import static com.suse.proxy.ProxyConfigUtils.SOURCE_MODE_RPM;
import static com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults.DEFAULT_UYUNI_REGISTRY_TAG;
import static com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults.DEFAULT_UYUNI_REGISTRY_URL;
import static com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults.MLM_REGISTRY_URL_EXAMPLE;
import static com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults.UYUNI_REGISTRY_URL_EXAMPLE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDefaults;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Tests for the {@link ProxyConfigGetFormDefaults} class
 */

@SuppressWarnings({"java:S1171", "java:S3599"})
@ExtendWith(JUnit5Mockery.class)
public class ProxyConfigGetFormDefaultsTest extends BaseTestCaseWithUser {

    private MinionServer testMinionServer;
    private final ConfigDefaults configDefaults = ConfigDefaults.get();

    @RegisterExtension
    protected final JUnit5Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
    }};

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        context.setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        testMinionServer.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
    }

    @Override
    @AfterEach
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setConfigDefaultsInstance(configDefaults);
    }

    /**
     * Test registry defaults on Uyuni
     */
    @Test
    public void testDefaultsWhenUyuni() throws NoSuchFieldException, IllegalAccessException {
        // mocking the ConfigDefaults
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);

        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(true));
            allowing(mockConfigDefaults).getProductVersion();
            will(returnValue("2025.10"));
        }});

        setConfigDefaultsInstance(mockConfigDefaults);

        //
        ProxyConfigGetFormDataContext proxyConfigGetFormDataContext =
                new ProxyConfigGetFormDataContext(user, testMinionServer, null);

        //
        new ProxyConfigGetFormDefaults().handle(proxyConfigGetFormDataContext);

        Map<String, Object> actualProxyConfigAsMap = proxyConfigGetFormDataContext.getProxyConfigAsMap();
        assertEquals(SOURCE_MODE_RPM, actualProxyConfigAsMap.get(SOURCE_MODE_FIELD));
        assertEquals(REGISTRY_MODE_SIMPLE, actualProxyConfigAsMap.get(REGISTRY_MODE));
        assertEquals(DEFAULT_UYUNI_REGISTRY_URL, actualProxyConfigAsMap.get(REGISTRY_BASE_URL));
        assertEquals(DEFAULT_UYUNI_REGISTRY_TAG, actualProxyConfigAsMap.get(REGISTRY_BASE_TAG));
        assertEquals(UYUNI_REGISTRY_URL_EXAMPLE, proxyConfigGetFormDataContext.getRegistryUrlExample());
        assertEquals(DEFAULT_UYUNI_REGISTRY_TAG, proxyConfigGetFormDataContext.getRegistryTagExample());
    }

    /**
     * Test registry defaults when mlm
     */
    @Test
    public void testDefaultsWhenMLM() throws NoSuchFieldException, IllegalAccessException {
        //
        final String expectedRegistryBaseUrl = "registry.suse.com/suse/multi-linux-manager/99.98/x86_64";
        final String expectedRegistryBaseTag = "99.98.97";

        // mocking the ConfigDefaults
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);

        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(false));
            allowing(mockConfigDefaults).getProductVersion();
            will(returnValue(expectedRegistryBaseTag));
        }});

        setConfigDefaultsInstance(mockConfigDefaults);


        //
        ProxyConfigGetFormDataContext proxyConfigGetFormDataContext =
                new ProxyConfigGetFormDataContext(user, testMinionServer, null);

        //
        new ProxyConfigGetFormDefaults().handle(proxyConfigGetFormDataContext);

        Map<String, Object> actualProxyConfigAsMap = proxyConfigGetFormDataContext.getProxyConfigAsMap();
        assertEquals(SOURCE_MODE_RPM, actualProxyConfigAsMap.get(SOURCE_MODE_FIELD));
        assertEquals(REGISTRY_MODE_SIMPLE, actualProxyConfigAsMap.get(REGISTRY_MODE));
        assertEquals(expectedRegistryBaseUrl, actualProxyConfigAsMap.get(REGISTRY_BASE_URL));
        assertEquals(expectedRegistryBaseTag, actualProxyConfigAsMap.get(REGISTRY_BASE_TAG));
        assertEquals(MLM_REGISTRY_URL_EXAMPLE, proxyConfigGetFormDataContext.getRegistryUrlExample());
        assertEquals(expectedRegistryBaseTag, proxyConfigGetFormDataContext.getRegistryTagExample());

    }

    /**
     * Overrides the ConfigDefaults instance
     * @param configDefaultsIn the ConfigDefaults instance
     * @throws NoSuchFieldException if a field with the specified name is not found.
     * @throws IllegalAccessException if the field is not accessible.
     */
    @SuppressWarnings("java:S3011")
    private static void setConfigDefaultsInstance(ConfigDefaults configDefaultsIn)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigDefaults.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, configDefaultsIn);
    }
}
