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

package com.suse.proxy.get.formdata.test;

import static com.suse.proxy.ProxyConfigUtils.MGRPXY;
import static com.suse.proxy.ProxyConfigUtils.isMgrpxyAvailable;
import static com.suse.proxy.ProxyConfigUtils.isMgrpxyInstalled;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.SERVER_ID;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.assertErrors;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.assertNoErrors;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.setConfigDefaultsInstance;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataPreConditions;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Tests for ProxyConfigGetFormDataProxyInitializer.
 */
public class ProxyConfigGetFormDataPreConditionsTest extends RhnJmockBaseTestCase {

    private static final String JAVA_TEST = "java::test";
    public static final String SERVER_NOT_FOUND = "Server not found";
    public static final String NO_CHANNEL_WITH_MGRPXY_PACKAGE_FOUND = "No channel with mgrpxy package found";
    public static final String CANNOT_ENTITLE_SERVER = "Cannot entitle server";
    public static final String SYSTEM_IS_MGR_SERVER =
            "The system is a Management Server and cannot be converted to a Proxy";

    private final ConfigDefaults configDefaults = ConfigDefaults.get();
    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );
    private final ProxyConfigGetFormDataPreConditions preConditions = new ProxyConfigGetFormDataPreConditions();

    private Server mockServer;
    private User user;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        this.mockServer = mock(Server.class);
        this.user = UserTestUtils.createUser();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        try {
            setConfigDefaultsInstance(configDefaults);
        }
        catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Error resetting ConfigDefaults instance", e);
        }
    }

    /**
     * Test failure when manager is uyuni and server is null
     * Expects matching error
     */
    @Test
    public void testFailureWithNullServer() {
        ProxyConfigGetFormDataContext getFormDataContext =
                new ProxyConfigGetFormDataContext(null, null,  null, null);

        preConditions.handle(getFormDataContext);
        assertErrors(getFormDataContext.getErrorReport(), SERVER_NOT_FOUND);
    }

    /**
     * Test failure when converting a manager server
     * Expects matching error
     */
    @Test
    public void testFailureWithManagerServer() {
        context().checking(new Expectations() {{
            oneOf(mockServer).isMgrServer();
            will(returnValue(true));
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext();

        preConditions.handle(getFormDataContext);
        assertErrors(getFormDataContext.getErrorReport(), SYSTEM_IS_MGR_SERVER);
    }

    // Tests focusing proxy entitlement

    /**
     * Test success scenario where:
     * - manager is uyuni
     * - server has proxy entitlement
     * Expects no errors
     */
    @Test
    public void testSuccessWhenHasEntitlement() {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, true);

        context().checking(new Expectations() {{
            oneOf(mockServer).isMgrServer();
            will(returnValue(false));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(true));
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext();

        preConditions.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test success scenario where:
     * - manager is uyuni
     * - server does not have proxy entitlement
     * - server can be entitled
     * Expects no errors
     */
    @Test
    public void testSuccessWhenCanEntitleServer() {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, true);

        SystemEntitlementManager mockSystemEntitlementManager = mock(SystemEntitlementManager.class);
        context().checking(new Expectations() {{
            oneOf(mockServer).isMgrServer();
            will(returnValue(false));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(false));
            allowing(mockServer).getId();
            will(returnValue(SERVER_ID));
            oneOf(mockSystemEntitlementManager).canEntitleServer(mockServer, EntitlementManager.PROXY);
            will(returnValue(true));
        }});

        ProxyConfigGetFormDataContext getFormDataContext =
                new ProxyConfigGetFormDataContext(user, mockServer, null, mockSystemEntitlementManager);

        preConditions.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test failure when:
     * - manager is MLM
     * - cannot entitle server
     * - no subscribable channels are found
     * Expects matching error messages for both conditions.
     */
    @Test
    public void testFailureWhenCannotEntitleServerAndNoSubscribableChannelsFound() throws Exception {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, false);

        context().checking(new Expectations() {{
            oneOf(mockServer).isMgrServer();
            will(returnValue(false));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(false));
            oneOf(mockServer).getEntitlements();
            will(returnValue(new HashSet<>()));
            allowing(mockServer).getId();
            will(returnValue(SERVER_ID));
            allowing(mockServer).getBaseChannel();
            will(returnValue(ChannelFactoryTest.createTestChannel(user)));
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext();

        preConditions.handle(getFormDataContext);
        assertErrors(getFormDataContext.getErrorReport(), CANNOT_ENTITLE_SERVER, NO_CHANNEL_WITH_MGRPXY_PACKAGE_FOUND);
    }

    // Tests focusing mgrpxy package access

    /**
     * Test success scenario where:
     * - manager is MLM
     * - can entitle server
     * - mgrpxy is installed on server
     * Expects no errors
     */
    @Test
    public void testSuccessWhenMgrpxyIsInstalled() throws Exception {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, false);

        // setup installed package
        Server server = ServerTestUtils.createTestSystem(user);
        Channel channelWithMgrpxy = ChannelFactoryTest.createTestChannel(user);
        PackageManagerTest.addPackageToSystemAndChannel(MGRPXY, server, channelWithMgrpxy);

        // assert preconditions
        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.PROXY));
        assertTrue(isMgrpxyInstalled(server));
        assertFalse(isMgrpxyAvailable(server));

        //
        ProxyConfigGetFormDataContext getFormDataContext = createContext(server);

        preConditions.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test success scenario where:
     * - manager is MLM
     * - can entitle server
     * - mgrpxy can be installed
     * Expects no errors
     */
    @Test
    public void testSuccessWhenMgrpxyIsAvailable() throws Exception {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, false);

        // setup installed package
        Server server = ServerTestUtils.createTestSystem(user);
        Channel channelWithMgrpxy = createChannelWithPackage(MGRPXY, server);
        SystemManager.subscribeServerToChannel(user, server, channelWithMgrpxy);
        ChannelFactory.refreshNewestPackageCache(channelWithMgrpxy, JAVA_TEST);

        // assert preconditions
        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.PROXY));
        assertFalse(isMgrpxyInstalled(server));
        assertTrue(isMgrpxyAvailable(server));

        //
        ProxyConfigGetFormDataContext getFormDataContext = createContext(server);

        preConditions.handle(getFormDataContext);
        assertTrue(getFormDataContext.getSubscribableChannels().isEmpty());
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test success scenario where:
     * - manager is MLM
     * - can entitle server
     * - subscribable channels with mgrpxy found
     * Expects:
     * - no errors
     * - childChannel is in subscribableChannels
     */
    @Test
    public void testSuccessWhenSubscribableChannelsFound() throws Exception {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, false);

        // setup installed package
        Server server = ServerTestUtils.createTestSystem(user);
        Channel channelWithMgrpxy = createChannelWithPackage(MGRPXY, server);
        ChannelFactory.refreshNewestPackageCache(channelWithMgrpxy, JAVA_TEST);

        // assert preconditions
        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.PROXY));
        assertFalse(isMgrpxyInstalled(server));
        assertFalse(isMgrpxyAvailable(server));

        //
        ProxyConfigGetFormDataContext getFormDataContext = createContext(server);

        preConditions.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
        Set<Channel> subscribableChannels = getFormDataContext.getSubscribableChannels();
        assertEquals(1, subscribableChannels.size());
        assertEquals(channelWithMgrpxy, subscribableChannels.iterator().next());
    }

    /**
     * Test failure scenario where:
     * - manager is MLM
     * - can entitle server
     * - subscribable channels but none with mgrpxy
     * Expects:
     * - no errors
     * - childChannel is in subscribableChannels
     */
    @Test
    public void testFailureWhenNoSubscribableChannelsFound() throws Exception {
        ProxyConfigGetFormTestUtils.mockConfigDefaults(context, false);

        // setup installed package
        Server server = ServerTestUtils.createTestSystem(user);
        Channel channelWithMgrpxy = createChannelWithPackage("mgrpxy-lang", server);
        SystemManager.subscribeServerToChannel(user, server, channelWithMgrpxy);

        // assert preconditions
        assertTrue(systemEntitlementManager.canEntitleServer(server, EntitlementManager.PROXY));
        assertFalse(isMgrpxyInstalled(server));
        assertFalse(isMgrpxyAvailable(server));

        //
        ProxyConfigGetFormDataContext getFormDataContext =
                new ProxyConfigGetFormDataContext(user, server,  null, systemEntitlementManager);

        preConditions.handle(getFormDataContext);
        assertNull(getFormDataContext.getSubscribableChannels());
        assertErrors(getFormDataContext.getErrorReport(), NO_CHANNEL_WITH_MGRPXY_PACKAGE_FOUND);
    }

    /**
     * Helper method to set up a base channel and a child channel with the given package, and subscribes the server to
     * the base channel.
     * @param packageName the name of the package
     * @param server the server to subscribe
     * @return the created child channel
     * @throws Exception on errors
     */
    private Channel createChannelWithPackage(String packageName, Server server) throws Exception {
        Package pkg = PackageTest.createTestPackage(user.getOrg(), packageName);
        Channel childChannel = ChannelTestUtils.createChildChannel(user, server.getBaseChannel());
        childChannel.getPackages().add(pkg);
        return childChannel;
    }

    /**
     * Helper method to create ProxyConfigGetFormDataContext with default system entitlement manager
     * @return the created context
     */
    private ProxyConfigGetFormDataContext createContext() {
        return createContext(mockServer);
    }

    /**
     * Helper method to create ProxyConfigGetFormDataContext with custom system entitlement manager
     * @param server the server to use in the context
     * @return the created context
     */
    private ProxyConfigGetFormDataContext createContext(Server server) {
        return new ProxyConfigGetFormDataContext(user, server, null, systemEntitlementManager);
    }
}
