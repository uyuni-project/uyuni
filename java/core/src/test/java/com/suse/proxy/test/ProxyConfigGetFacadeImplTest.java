/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.proxy.test;

import static com.suse.proxy.get.ProxyConfigGetFacadeImpl.MGRPXY;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PARENT_FQDN;
import static com.suse.proxy.test.ProxyConfigUpdateTestUtils.DUMMY_PROXY_FQDN;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Pillar;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.proxy.ProxyConfigUtils;
import com.suse.proxy.get.ProxyConfigGetFacadeImpl;
import com.suse.proxy.model.ProxyConfig;

import com.google.gson.Gson;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for the ProxyConfigGet class
 */
public class ProxyConfigGetFacadeImplTest extends BaseTestCaseWithUser {

    private final ConfigDefaults configDefaults = ConfigDefaults.get();

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

    @Override
    @AfterEach
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setConfigDefaultsInstance(configDefaults);
    }

    /**
     * Tests getProxyConfig when the server is null
     */
    @Test
    public void getProxyConfigWithNullServer() {
        assertNull(new ProxyConfigGetFacadeImpl().getProxyConfig(null));
    }

    /**
     * Tests getProxyConfig when the server provided but no ProxyConfig exists
     */
    @Test
    public void getProxyConfigWithValidServerWhenProxyConfigNotExists() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        ProxyConfig proxyConfig = new ProxyConfigGetFacadeImpl().getProxyConfig(minion);
        assertNull(proxyConfig);
    }

    /**
     * Tests getProxyConfig when the server having an associated ProxyConfig
     */
    @Test
    public void getProxyConfigWithValidServerWhenProxyConfigExists() {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);

        Pillar pillar = new Pillar(ProxyConfigUtils.PROXY_PILLAR_CATEGORY, new HashMap<>(), minion);
        pillar.add(ProxyConfigUtils.PROXY_FQDN_FIELD, DUMMY_PROXY_FQDN);
        pillar.add(ProxyConfigUtils.PARENT_FQDN_FIELD, DUMMY_PARENT_FQDN);
        minion.addPillar(pillar);
        TestUtils.saveAndFlush(minion);

        //
        ProxyConfig proxyConfig = new ProxyConfigGetFacadeImpl().getProxyConfig(minion);
        assertNotNull(proxyConfig);
        assertEquals(DUMMY_PROXY_FQDN, proxyConfig.getProxyFqdn());
        assertEquals(DUMMY_PARENT_FQDN, proxyConfig.getParentFqdn());
    }

    /**
     * Tests getFormData when the provided server is null
     */
    @Test
    public void getFormDataWithNullServer() {
        final String expectedCurrentConfig = "{}";
        final String expectedParents = "[]";
        final String[] expectedValidationErrorMessages = { "Server not found" };

        //
        Map<String, Object> formData = new ProxyConfigGetFacadeImpl().getFormData(user, null,
                GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);

        assertEquals(expectedCurrentConfig, formData.get("currentConfig"));
        assertEquals(expectedParents, formData.get("parents"));

        String validationErrorsJson = (String) formData.get("validationErrors");
        String[] errors = new Gson().fromJson(validationErrorsJson, String[].class);
        assertArrayEquals(expectedValidationErrorMessages, errors);
    }

    /**
     * Tests getFormData when provided server is not associated with any ProxyConfig
     */
    @SuppressWarnings({"java:S3599", "java:S1171"})
    @Test
    public void getFormDataWithWhenNewProxyConfiguration() throws Exception {
        final String expectedCurrentConfig = "{\"sourceMode\":\"rpm\",\"registryMode\":\"simple\"," +
                "\"registryBaseTag\":\"99.98.97\",\"registryBaseURL\":" +
                "\"registry.suse.com/suse/multi-linux-manager/99.98/x86_64\"}";
        final String expectedParents = "[\"" + Config.get().getString(ConfigDefaults.SERVER_HOSTNAME) + "\"]";

        Server server = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeProxyEntitled(),
                ServerFactoryTest.TYPE_SERVER_PROXY);
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        PackageManagerTest.addPackageToSystemAndChannel(MGRPXY, server, channel);

        // mocking the ConfigDefaults
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);

        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(false));
            allowing(mockConfigDefaults).getProductVersion();
            will(returnValue("99.98.97"));
        }});
        setConfigDefaultsInstance(mockConfigDefaults);

        //
        Map<String, Object> formData = new ProxyConfigGetFacadeImpl().getFormData(user, server,
                GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER);

        assertEquals(expectedCurrentConfig, formData.get("currentConfig"));
        assertEquals(expectedParents, formData.get("parents"));
        assertEquals("[]", formData.get("validationErrors"));
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
