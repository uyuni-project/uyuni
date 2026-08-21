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
import com.redhat.rhn.frontend.dto.ChannelTreeNode;
import com.redhat.rhn.frontend.xmlrpc.channel.ChannelHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ChannelHandlerContractTest extends BaseOpenApiTest {

    private static final Integer POPULARITY_COUNT = 10;

    @Override
    protected String getApiNamespace() {
        return "channel";
    }

    @Override
    protected Class<ChannelHandler> getHandlerClass() {
        return ChannelHandler.class;
    }

    private ChannelHandler handler() {
        return (ChannelHandler) handlerMock;
    }

    /**
     * Builds a channel tree node whose registered serializer produces every documented property.
     *
     * @param name a unique mock name, so several nodes can coexist in one test run
     * @return the channel tree node
     */
    private ChannelTreeNode channelTreeNode(String name) {
        ChannelTreeNode node = context.mock(ChannelTreeNode.class, name);

        context.checking(new Expectations() {{
            allowing(node).getId();
            will(returnValue(101L));
            allowing(node).getChannelLabel();
            will(returnValue("test-channel"));
            allowing(node).getName();
            will(returnValue("Test Channel"));
            allowing(node).getOrgId();
            will(returnValue(1L));
            allowing(node).getOrgName();
            will(returnValue("Test Organization"));
            allowing(node).getPackageCount();
            will(returnValue(42L));
            allowing(node).getSystemCount();
            will(returnValue(7L));
            allowing(node).getArchName();
            will(returnValue("x86_64"));
        }});

        return node;
    }

    private Object[] channelTree(String name) {
        return new Object[]{channelTreeNode(name)};
    }

    @Test
    public void testListSoftwareChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSoftwareChannels(with(mockUser));
            will(returnValue(List.of(Map.of(
                    "label", "test-channel",
                    "name", "Test Channel",
                    "parent_label", "",
                    "end_of_life", "",
                    "arch", "x86_64"
            ))));
        }});

        validateApiContract("/channel/listSoftwareChannels", "GET")
                .onHandlerMethod("listSoftwareChannels", User.class);
    }

    @Test
    public void testListSoftwareChannelsByAutoSync() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSoftwareChannelsByAutoSync(with(mockUser), with(Boolean.TRUE));
            will(returnValue(List.of("test-channel")));
        }});

        validateApiContract("/channel/listSoftwareChannelsByAutoSync", "GET")
                .withParams(Map.of("autoSync", new String[]{"true"}))
                .onHandlerMethod("listSoftwareChannelsByAutoSync", User.class, Boolean.class);
    }

    @Test
    public void testListAllChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllChannels(with(mockUser));
            will(returnValue(channelTree("allChannels")));
        }});

        validateApiContract("/channel/listAllChannels", "GET")
                .onHandlerMethod("listAllChannels", User.class);
    }

    @Test
    public void testListVendorChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listVendorChannels(with(mockUser));
            will(returnValue(channelTree("vendorChannels")));
        }});

        validateApiContract("/channel/listVendorChannels", "GET")
                .onHandlerMethod("listVendorChannels", User.class);
    }

    @Test
    public void testListPopularChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listPopularChannels(with(mockUser), with(POPULARITY_COUNT));
            will(returnValue(channelTree("popularChannels")));
        }});

        validateApiContract("/channel/listPopularChannels", "GET")
                .withParams(Map.of("popularityCount", new String[]{POPULARITY_COUNT.toString()}))
                .onHandlerMethod("listPopularChannels", User.class, Integer.class);
    }

    @Test
    public void testListMyChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listMyChannels(with(mockUser));
            will(returnValue(channelTree("myChannels")));
        }});

        validateApiContract("/channel/listMyChannels", "GET")
                .onHandlerMethod("listMyChannels", User.class);
    }

    @Test
    public void testListSharedChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listSharedChannels(with(mockUser));
            will(returnValue(channelTree("sharedChannels")));
        }});

        validateApiContract("/channel/listSharedChannels", "GET")
                .onHandlerMethod("listSharedChannels", User.class);
    }

    @Test
    public void testListRetiredChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listRetiredChannels(with(mockUser));
            will(returnValue(channelTree("retiredChannels")));
        }});

        validateApiContract("/channel/listRetiredChannels", "GET")
                .onHandlerMethod("listRetiredChannels", User.class);
    }

    @Test
    public void testListManageableChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listManageableChannels(with(mockUser));
            will(returnValue(channelTree("manageableChannels")));
        }});

        validateApiContract("/channel/listManageableChannels", "GET")
                .onHandlerMethod("listManageableChannels", User.class);
    }
}
