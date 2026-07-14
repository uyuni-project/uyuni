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
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.channel.access.ChannelAccessHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class ChannelAccessHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "channel.access";
    }

    @Override
    protected Class<ChannelAccessHandler> getHandlerClass() {
        return ChannelAccessHandler.class;
    }

    private ChannelAccessHandler handler() {
        return (ChannelAccessHandler) handlerMock;
    }

    @Test
    public void testEnableUserRestrictions() throws Exception {
        var channelLabel = "test-channel";

        context.checking(new Expectations() {{
            oneOf(handler()).enableUserRestrictions(with(mockUser), with(channelLabel));
            will(returnValue(1));
        }});

        validateApiContract("/channel.access/enableUserRestrictions", "POST")
                .withBody(Map.of("channelLabel", channelLabel))
                .onHandlerMethod("enableUserRestrictions", User.class, String.class);
    }

    @Test
    public void testDisableUserRestrictions() throws Exception {
        var channelLabel = "test-channel";

        context.checking(new Expectations() {{
            oneOf(handler()).disableUserRestrictions(with(mockUser), with(channelLabel));
            will(returnValue(1));
        }});

        validateApiContract("/channel.access/disableUserRestrictions", "POST")
                .withBody(Map.of("channelLabel", channelLabel))
                .onHandlerMethod("disableUserRestrictions", User.class, String.class);
    }

    @Test
    public void testSetOrgSharing() throws Exception {
        var channelLabel = "test-channel";
        var access = "public";

        context.checking(new Expectations() {{
            oneOf(handler()).setOrgSharing(with(mockUser), with(channelLabel), with(access));
            will(returnValue(1));
        }});

        validateApiContract("/channel.access/setOrgSharing", "POST")
                .withBody(Map.of("channelLabel", channelLabel, "access", access))
                .onHandlerMethod("setOrgSharing", User.class, String.class, String.class);
    }

    @Test
    public void testGetOrgSharing() throws Exception {
        var channelLabel = "test-channel";

        context.checking(new Expectations() {{
            oneOf(handler()).getOrgSharing(with(mockUser), with(channelLabel));
            will(returnValue("public"));
        }});

        validateApiContract("/channel.access/getOrgSharing", "GET")
                .withParams(Map.of("channelLabel", new String[]{channelLabel}))
                .onHandlerMethod("getOrgSharing", User.class, String.class);
    }
}
