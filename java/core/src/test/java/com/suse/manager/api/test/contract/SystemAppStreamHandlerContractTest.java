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

import com.redhat.rhn.domain.channel.AppStream;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.appstreams.SystemAppStreamHandler;

import com.suse.manager.webui.controllers.appstreams.response.ChannelAppStreamsResponse;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SystemAppStreamHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "system.appstreams";
    }

    @Override
    protected Class<SystemAppStreamHandler> getHandlerClass() {
        return SystemAppStreamHandler.class;
    }

    private SystemAppStreamHandler handler() {
        return (SystemAppStreamHandler) handlerMock;
    }

    private ChannelAppStreamsResponse channelAppStreams() {
        Channel channel = new Channel();
        channel.setLabel("sle-module-basesystem15-sp6-pool-x86_64");
        channel.setName("Basesystem Module");

        AppStream appStream = new AppStream();
        appStream.setName("postgresql");
        appStream.setStream("15");
        appStream.setArch("x86_64");

        return new ChannelAppStreamsResponse(channel, List.of(appStream), (module, stream) -> true);
    }

    private Map<String, Object> moduleStream() {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("module", "postgresql");
        entry.put("stream", "15");
        return entry;
    }

    @Test
    public void testEnable() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", 1000010000);
        body.put("moduleStreams", List.of(moduleStream()));
        body.put("earliestOccurrence", "2026-08-15T10:00:00Z");

        context.checking(new Expectations() {{
            oneOf(handler()).enable(with(mockUser), with(1000010000),
                    with(any(List.class)), with(any(Date.class)));
            will(returnValue(42));
        }});

        validateApiContract("/system.appstreams/enable", "POST")
                .withBody(body)
                .onHandlerMethod("enable", User.class, Integer.class, List.class, Date.class);
    }

    @Test
    public void testDisable() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", 1000010000);
        body.put("moduleStreams", List.of(moduleStream()));
        body.put("earliestOccurrence", "2026-08-15T10:00:00Z");

        context.checking(new Expectations() {{
            oneOf(handler()).disable(with(mockUser), with(1000010000),
                    with(any(List.class)), with(any(Date.class)));
            will(returnValue(43));
        }});

        validateApiContract("/system.appstreams/disable", "POST")
                .withBody(body)
                .onHandlerMethod("disable", User.class, Integer.class, List.class, Date.class);
    }

    @Test
    public void testListModuleStreams() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listModuleStreams(with(mockUser), with(1000010000));
            will(returnValue(List.of(channelAppStreams())));
        }});

        validateApiContract("/system.appstreams/listModuleStreams", "GET")
                .withParams(Map.of("sid", new String[] {"1000010000"}))
                .onHandlerMethod("listModuleStreams", User.class, Integer.class);
    }

    @Test
    public void testSsmEnable() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("channelId", 101);
        body.put("moduleStreams", List.of(moduleStream()));
        body.put("earliestOccurrence", "2026-08-15T10:00:00Z");

        context.checking(new Expectations() {{
            oneOf(handler()).ssmEnable(with(mockUser), with(101),
                    with(any(List.class)), with(any(Date.class)));
            will(returnValue(44));
        }});

        validateApiContract("/system.appstreams/ssmEnable", "POST")
                .withBody(body)
                .onHandlerMethod("ssmEnable", User.class, Integer.class, List.class, Date.class);
    }

    @Test
    public void testSsmDisable() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("channelId", 101);
        body.put("moduleNames", List.of("postgresql"));
        body.put("earliestOccurrence", "2026-08-15T10:00:00Z");

        context.checking(new Expectations() {{
            oneOf(handler()).ssmDisable(with(mockUser), with(101),
                    with(any(List.class)), with(any(Date.class)));
            will(returnValue(45));
        }});

        validateApiContract("/system.appstreams/ssmDisable", "POST")
                .withBody(body)
                .onHandlerMethod("ssmDisable", User.class, Integer.class, List.class, Date.class);
    }
}
