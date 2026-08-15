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
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.channel.appstreams.ChannelAppStreamHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChannelAppStreamHandlerContractTest extends BaseOpenApiTest {

    private static final String CHANNEL_LABEL = "sle-module-basesystem15-sp6-pool-x86_64";

    @Override
    protected String getApiNamespace() {
        return "channel.appstreams";
    }

    @Override
    protected Class<ChannelAppStreamHandler> getHandlerClass() {
        return ChannelAppStreamHandler.class;
    }

    private ChannelAppStreamHandler handler() {
        return (ChannelAppStreamHandler) handlerMock;
    }

    private AppStream appStream() {
        AppStream appStream = new AppStream();
        appStream.setName("postgresql");
        appStream.setStream("15");
        appStream.setArch("x86_64");
        return appStream;
    }

    /**
     * Mirrors the output of {@code ChannelSerializer}, which cannot be exercised directly here
     * because it looks up the original channel and the synchronisation status in the database.
     *
     * @return the serialized form of a modular channel
     */
    private Map<String, Object> modularChannel() {
        Map<String, Object> contentSource = new LinkedHashMap<>();
        contentSource.put("id", 42);
        contentSource.put("label", "External - basesystem");
        contentSource.put("sourceUrl", "https://updates.suse.com/SUSE/Updates/basesystem");
        contentSource.put("type", "yum");

        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("id", 101);
        channel.put("label", CHANNEL_LABEL);
        channel.put("name", "Basesystem Module 15 SP6 x86_64");
        channel.put("arch_name", "x86_64");
        channel.put("arch_label", "channel-x86_64");
        channel.put("summary", "Basesystem Module");
        channel.put("description", "Basesystem Module for SUSE Linux Enterprise 15 SP6");
        channel.put("checksum_label", "sha256");
        channel.put("last_modified", new Date());
        channel.put("maintainer_name", "SUSE");
        channel.put("maintainer_email", "maintainer@suse.com");
        channel.put("maintainer_phone", "");
        channel.put("support_policy", "");
        channel.put("gpg_key_url", "https://updates.suse.com/gpg");
        channel.put("gpg_key_id", "50A3DD1C");
        channel.put("gpg_key_fp", "FEAB 5027 60A8 5A9F");
        channel.put("gpg_check", true);
        channel.put("yumrepo_last_sync", new Date());
        channel.put("contentSources", List.of(contentSource));
        channel.put("end_of_life", "");
        channel.put("parent_channel_label", "sle-product-sles15-sp6-pool-x86_64");
        channel.put("clone_original", "");
        channel.put("sync_status", "R");
        return channel;
    }

    @Test
    public void testListModuleStreams() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listModuleStreams(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(List.of(appStream())));
        }});

        validateApiContract("/channel.appstreams/listModuleStreams", "GET")
                .withParams(Map.of("channelLabel", new String[] {CHANNEL_LABEL}))
                .onHandlerMethod("listModuleStreams", User.class, String.class);
    }

    @Test
    public void testIsModular() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).isModular(with(mockUser), with(CHANNEL_LABEL));
            will(returnValue(true));
        }});

        validateApiContract("/channel.appstreams/isModular", "GET")
                .withParams(Map.of("channelLabel", new String[] {CHANNEL_LABEL}))
                .onHandlerMethod("isModular", User.class, String.class);
    }

    @Test
    public void testListModular() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listModular(with(mockUser));
            will(returnValue(List.of(modularChannel())));
        }});

        validateApiContract("/channel.appstreams/listModular", "GET")
                .onHandlerMethod("listModular", User.class);
    }
}
