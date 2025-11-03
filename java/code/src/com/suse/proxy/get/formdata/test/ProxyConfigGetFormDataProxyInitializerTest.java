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

package com.suse.proxy.get.formdata.test;

import static com.suse.proxy.get.ProxyConfigGetFacadeImpl.MGRPXY;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.SERVER_ID;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.assertErrors;
import static com.suse.proxy.get.formdata.test.ProxyConfigGetFormTestUtils.assertNoErrors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataContext;
import com.suse.proxy.get.formdata.ProxyConfigGetFormDataProxyInitializer;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.lib.action.CustomAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for ProxyConfigGetFormDataProxyInitializer.
 */
public class ProxyConfigGetFormDataProxyInitializerTest extends RhnJmockBaseTestCase {

    private static final String FAILED_TO_ADD_PROXY_ENTITLEMENT = "Failed to add proxy entitlement to server";
    private static final String FAILED_TO_SUBSCRIBE_CHANNEL =
            "Failed to subscribe to appropriate proxy extension channel for server";

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );
    private final ProxyConfigGetFormDataProxyInitializer initializer = new ProxyConfigGetFormDataProxyInitializer();

    private Server mockServer;
    private User user;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        this.mockServer = mock(Server.class);
        this.user = UserTestUtils.createUser();
    }


    /**
     * Test success scenario where:
     * - server has a ProxyInfo
     * - server has proxy entitlement
     * - no subscribableChannels retrieved
     * Expects no errors
     */
    @Test
    public void testSuccessWhenExistingProxy() {
        context().checking(new Expectations() {{
            oneOf(mockServer).getProxyInfo();
            will(returnValue(new ProxyInfo()));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(true));
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext();

        initializer.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test success scenario where:
     * - server does not have a ProxyInfo
     * Expects:
     * - to create a ProxyInfo for the server
     * - no errors
     */
    @Test
    public void testSuccessWhenCreateProxyInfo() {
        context().checking(new Expectations() {{
           oneOf(mockServer).getProxyInfo();
           will(returnValue(null));
           oneOf(mockServer).hasProxyEntitlement();
           will(returnValue(true));
           allowing(mockServer).getId();
           will(returnValue(SERVER_ID));

            // assert setProxyInfo is invoked once
            oneOf(mockServer).setProxyInfo(with(aNonNull(ProxyInfo.class)));
       }});
        ProxyConfigGetFormDataContext getFormDataContext = createContext();

        initializer.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test success scenario where:
     * - server does not have a proxy entitlement
     * Expects:
     * - to add the proxy entitlement to the server
     * - no errors
     */
    @Test
    public void testSuccessWhenEntitlementAddedToServer() {
        SystemEntitlementManager mockSystemEntitlementManager = mock(SystemEntitlementManager.class);
        context().checking(new Expectations() {{
            oneOf(mockServer).getProxyInfo();
            will(returnValue(new ProxyInfo()));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(false));
            allowing(mockServer).getId();
            will(returnValue(SERVER_ID));

            // assert addEntitlementToServer is invoked once
            oneOf(mockSystemEntitlementManager).addEntitlementToServer(mockServer, EntitlementManager.PROXY);
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext(mockSystemEntitlementManager);

        initializer.handle(getFormDataContext);
        assertFalse(getFormDataContext.getErrorReport().hasErrors());
    }

    /**
     * Test failure over a scenario where:
     * - server does not have a proxy entitlement
     * Expects:
     * - fails while adding the proxy entitlement to the server
     * - matching error
     */
    @Test
    public void testFailureWhenEntitlementAddedToServer() {
        ValidatorResult expectedAddEntitlementToServerResult = new ValidatorResult();
        expectedAddEntitlementToServerResult.addError("dummy");

        SystemEntitlementManager mockSystemEntitlementManager = mock(SystemEntitlementManager.class);
        context().checking(new Expectations() {{
            oneOf(mockServer).getProxyInfo();
            will(returnValue(new ProxyInfo()));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(false));
            allowing(mockServer).getId();
            will(returnValue(SERVER_ID));

            // assert addEntitlementToServer is invoked once
            oneOf(mockSystemEntitlementManager).addEntitlementToServer(mockServer, EntitlementManager.PROXY);
            will(returnValue(expectedAddEntitlementToServerResult));
        }});

        ProxyConfigGetFormDataContext getFormDataContext = createContext(mockSystemEntitlementManager);

        initializer.handle(getFormDataContext);
        assertErrors(getFormDataContext.getErrorReport(), FAILED_TO_ADD_PROXY_ENTITLEMENT);
    }

    /**
     * Test a scenario where:
     * - mgrpxy was identified in a subscribable channel
     * Expects:
     * - server successfully subscribes the channel
     * - no errors
     */
    @Test
    public void testSuccessWhenChannelIsSubscribed() throws Exception {
        Channel channelWithMgrpxy = setupChannelWithMgrpxy();
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleSubscribeChannels(
                    with(user),
                    with(any(SubscribeChannelsAction.class))
            );
            will(new CustomAction("capture SubscribeChannelsAction") {
                @Override
                public Object invoke(Invocation invocation) {
                    SubscribeChannelsAction action = (SubscribeChannelsAction) invocation.getParameter(1);
                    // assert the captured SubscribedChannelsAction content channel matches the generated channel
                    assertEquals(
                            channelWithMgrpxy.getId(),
                            action.getDetails().getChannels().iterator().next().getId()
                    );
                    return null;
                }
            });
        }});


        ProxyConfigGetFormDataContext getFormDataContext = createContext();
        getFormDataContext.getSubscribableChannels().add(channelWithMgrpxy);

        initializer.handle(getFormDataContext);
        assertNoErrors(getFormDataContext.getErrorReport());
    }

    /**
     * Test a scenario where:
     * - mgrpxy was identified in a subscribable channel
     * Expects:
     * - server fails while subscribing the channel
     * - matching error
     */
    @Test
    public void testFailureWhenChannelSubscriptionFails() throws Exception {
        Channel channelWithMgrpxy = setupChannelWithMgrpxy();
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() {{
            allowing(taskomaticMock).scheduleSubscribeChannels(
                    with(user),
                    with(any(SubscribeChannelsAction.class))
            );
            will(throwException(new TaskomaticApiException(new RuntimeException())));
        }});


        ProxyConfigGetFormDataContext getFormDataContext = createContext();
        getFormDataContext.getSubscribableChannels().add(channelWithMgrpxy);

        initializer.handle(getFormDataContext);
        assertErrors(getFormDataContext.getErrorReport(), FAILED_TO_SUBSCRIBE_CHANNEL);
    }

    /**
     * Helper method to create ProxyConfigGetFormDataContext with default system entitlement manager
     * @return the created context
     */
    private ProxyConfigGetFormDataContext createContext() {
        return createContext(systemEntitlementManager);
    }

    /**
     * Helper method to create ProxyConfigGetFormDataContext with custom system entitlement manager
     * @param customManager the custom system entitlement manager
     * @return the created context
     */
    private ProxyConfigGetFormDataContext createContext(SystemEntitlementManager customManager) {
        return new ProxyConfigGetFormDataContext(user, mockServer, null, customManager);
    }

    /**
     * Helper method to set up a subscribable channel containing mgrpxy package
     */
    private Channel setupChannelWithMgrpxy() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user);
        Package pkg = PackageTest.createTestPackage(user.getOrg(), MGRPXY);
        Channel channelWithMgrpxy = ChannelTestUtils.createChildChannel(user, server.getBaseChannel());
        channelWithMgrpxy.getPackages().add(pkg);

        context().checking(new Expectations() {{
            oneOf(mockServer).getProxyInfo();
            will(returnValue(new ProxyInfo()));
            oneOf(mockServer).hasProxyEntitlement();
            will(returnValue(true));
            allowing(mockServer).getId();
            will(returnValue(server.getId()));

            oneOf(mockServer).getBaseChannel();
            will(returnValue(server.getBaseChannel()));
            oneOf(mockServer).getChildChannels();
            will(returnValue(server.getChildChannels()));
        }});
        return channelWithMgrpxy;
    }

}
