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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.distchannel.DistChannelHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DistChannelHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "distchannel";
    }

    @Override
    protected Class<DistChannelHandler> getHandlerClass() {
        return DistChannelHandler.class;
    }

    private DistChannelHandler handler() {
        return (DistChannelHandler) handlerMock;
    }

    /**
     * Builds a dist channel map serialized by the registered DistChannelMapSerializer,
     * so the response is validated against the documented snake_case schema.
     */
    private DistChannelMap distChannelMap() {
        var arch = new ChannelArch();
        arch.setName("x86_64");

        var channel = new Channel();
        channel.setLabel("test-channel");

        var map = new DistChannelMap();
        map.setOs("SLES");
        map.setRelease("15.5");
        map.setChannelArch(arch);
        map.setChannel(channel);
        map.setOrg(new Org());

        return map;
    }

    @Test
    public void testListDefaultMaps() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listDefaultMaps(with(mockUser));
            will(returnValue(new Object[]{distChannelMap()}));
        }});

        validateApiContract("/distchannel/listDefaultMaps", "GET")
                .onHandlerMethod("listDefaultMaps", User.class);
    }

    @Test
    public void testListMapsForOrg() throws Exception {
        var orgId = 1;

        context.checking(new Expectations() {{
            oneOf(handler()).listMapsForOrg(with(mockUser), with(orgId));
            will(returnValue(new Object[]{distChannelMap()}));
        }});

        validateApiContract("/distchannel/listMapsForOrg", "GET")
                .withParams(Map.of("orgId", new String[]{String.valueOf(orgId)}))
                .onHandlerMethod("listMapsForOrg", User.class, Integer.class);
    }

    /**
     * The orgId parameter is optional, so the call without it must dispatch to the
     * overload that lists the maps of the user's own organization.
     */
    @Test
    public void testListMapsForOrgWithoutOrgId() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listMapsForOrg(with(mockUser));
            will(returnValue(new Object[]{distChannelMap()}));
        }});

        validateApiContract("/distchannel/listMapsForOrg", "GET")
                .onHandlerMethod("listMapsForOrg", User.class);
    }

    @Test
    public void testSetMapForOrg() throws Exception {
        var os = "SLES";
        var release = "15.5";
        var archName = "x86_64";
        var channelLabel = "test-channel";

        context.checking(new Expectations() {{
            oneOf(handler()).setMapForOrg(with(mockUser), with(os), with(release), with(archName),
                    with(channelLabel));
            will(returnValue(1));
        }});

        validateApiContract("/distchannel/setMapForOrg", "POST")
                .withBody(Map.of("os", os, "release", release, "archName", archName,
                        "channelLabel", channelLabel))
                .onHandlerMethod("setMapForOrg", User.class, String.class, String.class, String.class,
                        String.class);
    }
}
