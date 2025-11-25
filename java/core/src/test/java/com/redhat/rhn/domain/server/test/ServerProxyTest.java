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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import static com.redhat.rhn.domain.server.ServerConstants.SLEMICRO;
import static com.redhat.rhn.domain.server.ServerConstants.SLES;
import static com.redhat.rhn.domain.server.ServerConstants.SLMICRO;
import static com.redhat.rhn.domain.server.ServerConstants.UBUNTU;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.MockObjectTestCase;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.stream.Stream;

/**
 * Unit tests for {@link Server}.
 */
public class ServerProxyTest extends MockObjectTestCase {

    private Server server;
    private final ConfigDefaults configDefaults = ConfigDefaults.get();

    /**
     * Set up the test.
     */
    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        server = new Server();
    }

    /**
     * Restore original ConfigDefaults instance to avoid side-effects on other tests
     */
    @AfterEach
    public void tearDown() throws NoSuchFieldException, IllegalAccessException {
        setConfigDefaultsInstance(configDefaults);
    }

    /**
     * Test for {@link Server#isProxy()} when proxy info is null.
     */
    @Test
    public void testIsProxyWhenProxyInfoIsNull() {
        server.setProxyInfo(null);
        assertFalse(server.isProxy());
    }

    /**
     * Test for {@link Server#isProxy()} when a proxy info record exists.
     */
    @Test
    public void testIsProxyWhenProxyInfoIsNotNull() {
        server.setProxyInfo(new ProxyInfo());
        assertTrue(server.isProxy());
    }

    /**
     * Test for {@link Server#hasProxyEntitlement()} when proxy is entitled to proxy.
     */
    @Test
    public void testHasProxyEntitlementWhenEntitled() {
        server.addGroup(createEntitlementServerGroup(EntitlementManager.PROXY.getLabel()));
        assertTrue(server.hasProxyEntitlement());
    }

    /**
     * Test for {@link Server#hasProxyEntitlement()} when proxy doesnt have the proxy entitlement.
     */
    @Test
    public void testHasProxyEntitlementWhenNotEntitled() {
        server.addGroup(createEntitlementServerGroup("some-other-entitlement"));
        assertFalse(server.hasProxyEntitlement());
    }

    /**
     * Test for {@link Server#hasProxyEntitlement()} when proxy doesnt have the proxy entitlement and no groups.
     */
    @Test
    public void testHasProxyEntitlementWhenNoGroups() {
        assertFalse(server.hasProxyEntitlement());
    }

    /**
     * Test {@link Server#isConvertibleToProxy()} for when a server that is already a proxy.
     */
    @Test
    public void testIsNotConvertibleToProxyIfAlreadyAProxy() {
        server.setProxyInfo(new ProxyInfo());
        assertFalse(server.isConvertibleToProxy());
    }

    /**
     * Test for {@link Server#isConvertibleToProxy()} when manager is uyuni.
     */
    @Test
    public void testIsConvertibleToProxyOnUyuni() {
        mockConfigDefaults(true);
        server.setProxyInfo(null);
        assertTrue(server.isConvertibleToProxy(), "Any server should be convertible on Uyuni if it's not a proxy");
    }

    /**
     * Test for {@link Server#isConvertibleToProxy()} for when manager is mlm.
     * @param os the operating system of the server
     * @param release the release of the server's OS
     * @param expected the expected result
     */
    @ParameterizedTest
    @MethodSource("isConvertibleToProxyOnMLMData")
    public void testIsConvertibleToProxyOnMLM(String os, String release, boolean expected) {
        mockConfigDefaults(false);
        server.setProxyInfo(null);
        server.setOs(os);
        server.setRelease(release);
        assertEquals(expected, server.isConvertibleToProxy());
    }

    static Stream<Arguments> isConvertibleToProxyOnMLMData() {
        return Stream.of(
            Arguments.of(SLEMICRO, "6.1", false),
            Arguments.of(SLMICRO, "5.5", false),
            Arguments.of(SLMICRO, "6.0", false),
            Arguments.of(SLMICRO, "6.1", true),
            Arguments.of(SLMICRO, "6.2", false),
            Arguments.of(SLES, "15.8", false),
            Arguments.of(SLES, "15.7", true),
            Arguments.of(SLES, "15.6", false),
            Arguments.of(UBUNTU, "22.04", false)
        );
    }


    @SuppressWarnings({"java:S1171", "java:S3599", "java:S112"})
    private void mockConfigDefaults(boolean isUyuni) {
        ConfigDefaults mockConfigDefaults = context.mock(ConfigDefaults.class);
        context.checking(new Expectations() {{
            allowing(mockConfigDefaults).isUyuni();
            will(returnValue(isUyuni));
        }});
        try {
            setConfigDefaultsInstance(mockConfigDefaults);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to mock ConfigDefaults", e);
        }
    }

    @SuppressWarnings("java:S3011")
    private static void setConfigDefaultsInstance(ConfigDefaults configDefaultsIn)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = ConfigDefaults.class.getDeclaredField("instance");
        field.setAccessible(true);
        field.set(null, configDefaultsIn);
    }

    /**
     * Helper class to expose the protected setGroupType method for testing purposes.
     */
    private static class TestableEntitlementServerGroup extends EntitlementServerGroup {
        @Override
        public void setGroupType(ServerGroupType groupType) {
            super.setGroupType(groupType);
        }
    }

    private EntitlementServerGroup createEntitlementServerGroup(String entitlementLabel) {
        TestableEntitlementServerGroup group = new TestableEntitlementServerGroup();
        ServerGroupType groupType = new ServerGroupType();
        groupType.setLabel(entitlementLabel);
        group.setGroupType(groupType);
        return group;
    }
}
