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
import com.redhat.rhn.frontend.xmlrpc.sync.content.ContentSyncHandler;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContentSyncHandlerContractTest extends BaseOpenApiTest {

    private static final String CHANNEL_LABEL = "sle-product-sles15-sp6-pool-x86_64";
    private static final String MIRROR_URL = "https://mirror.example.com/repo";

    @Override
    protected String getApiNamespace() {
        return "sync.content";
    }

    @Override
    protected Class<ContentSyncHandler> getHandlerClass() {
        return ContentSyncHandler.class;
    }

    private ContentSyncHandler handler() {
        return (ContentSyncHandler) handlerMock;
    }

    /**
     * Mirrors the output of {@code MgrSyncChannelDtoSerializer}, which cannot be exercised directly
     * here because it resolves the "noarch" package architecture through the database. The lookup
     * runs even for a channel that has an architecture, since it is the argument of an
     * {@code Optional.orElse} and Java evaluates it eagerly.
     *
     * @return the serialized form of a channel
     */
    private Map<String, Object> channel() {
        Map<String, Object> channel = new LinkedHashMap<>();
        channel.put("arch", "x86_64");
        channel.put("description", "Basesystem Module for SUSE Linux Enterprise 15");
        channel.put("family", "SLE-Module-Basesystem");
        channel.put("is_signed", true);
        channel.put("label", CHANNEL_LABEL);
        channel.put("name", "Basesystem Module");
        channel.put("optional", false);
        channel.put("parent", "sle-product-sles15-sp6-pool-x86_64");
        channel.put("product_name", "SUSE Linux Enterprise Server");
        channel.put("product_version", "15.6");
        channel.put("source_url", MIRROR_URL);
        channel.put("status", "installed");
        channel.put("summary", "Basesystem Module summary");
        channel.put("update_tag", "SLE-Module-Basesystem-15-SP6");
        channel.put("installer_updates", false);
        return channel;
    }

    /**
     * Mirrors the output of {@code MgrSyncProductDtoSerializer}, whose channels are serialized by
     * the channel serializer described above.
     *
     * @return the serialized form of a product
     */
    private Map<String, Object> product() {
        Map<String, Object> extension = new LinkedHashMap<>();
        extension.put("friendly_name", "Containers Module");
        extension.put("arch", "x86_64");
        extension.put("status", "available");
        extension.put("channels", List.of(channel()));

        Map<String, Object> product = new LinkedHashMap<>();
        product.put("friendly_name", "SUSE Linux Enterprise Server");
        product.put("arch", "x86_64");
        product.put("status", "installed");
        product.put("channels", List.of(channel()));
        product.put("extensions", List.of(extension));
        product.put("recommended", true);
        return product;
    }

    private MirrorCredentialsDto credentials() {
        MirrorCredentialsDto credentials = new MirrorCredentialsDto("mirror-user", "secret");
        credentials.setId(1L);
        credentials.setPrimary(true);
        return credentials;
    }

    @Test
    public void testListProducts() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listProducts(with(mockUser));
            will(returnValue(List.of(product())));
        }});

        validateApiContract("/sync.content/listProducts", "GET")
                .onHandlerMethod("listProducts", User.class);
    }

    @Test
    public void testListChannels() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listChannels(with(mockUser));
            will(returnValue(List.of(channel())));
        }});

        validateApiContract("/sync.content/listChannels", "GET")
                .onHandlerMethod("listChannels", User.class);
    }

    @Test
    public void testListCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCredentials(with(mockUser));
            will(returnValue(List.of(credentials())));
        }});

        validateApiContract("/sync.content/listCredentials", "GET")
                .onHandlerMethod("listCredentials", User.class);
    }

    @Test
    public void testSynchronizeChannelFamilies() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).synchronizeChannelFamilies(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/synchronizeChannelFamilies", "POST")
                .onHandlerMethod("synchronizeChannelFamilies", User.class);
    }

    @Test
    public void testSynchronizeProducts() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).synchronizeProducts(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/synchronizeProducts", "POST")
                .onHandlerMethod("synchronizeProducts", User.class);
    }

    @Test
    public void testSynchronizeSubscriptions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).synchronizeSubscriptions(with(mockUser));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/synchronizeSubscriptions", "POST")
                .onHandlerMethod("synchronizeSubscriptions", User.class);
    }

    @Test
    public void testSynchronizeRepositories() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).synchronizeRepositories(with(mockUser), with(MIRROR_URL));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/synchronizeRepositories", "POST")
                .withBody(Map.of("mirrorUrl", MIRROR_URL))
                .onHandlerMethod("synchronizeRepositories", User.class, String.class);
    }

    @Test
    public void testAddChannel() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("channelLabel", CHANNEL_LABEL);
        body.put("mirrorUrl", MIRROR_URL);

        context.checking(new Expectations() {{
            oneOf(handler()).addChannel(with(mockUser), with(CHANNEL_LABEL), with(MIRROR_URL));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/addChannel", "POST")
                .withBody(body)
                .onHandlerMethod("addChannel", User.class, String.class, String.class);
    }

    @Test
    public void testAddChannels() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("channelLabel", CHANNEL_LABEL);
        body.put("mirrorUrl", MIRROR_URL);

        context.checking(new Expectations() {{
            oneOf(handler()).addChannels(with(mockUser), with(CHANNEL_LABEL), with(MIRROR_URL));
            will(returnValue(new Object[] {CHANNEL_LABEL}));
        }});

        validateApiContract("/sync.content/addChannels", "POST")
                .withBody(body)
                .onHandlerMethod("addChannels", User.class, String.class, String.class);
    }

    @Test
    public void testAddCredentials() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("username", "mirror-user");
        body.put("password", "secret");
        body.put("primary", true);

        context.checking(new Expectations() {{
            oneOf(handler()).addCredentials(with(mockUser), with("mirror-user"), with("secret"), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/addCredentials", "POST")
                .withBody(body)
                .onHandlerMethod("addCredentials", User.class, String.class, String.class, boolean.class);
    }

    @Test
    public void testDeleteCredentials() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteCredentials(with(mockUser), with("mirror-user"));
            will(returnValue(1));
        }});

        validateApiContract("/sync.content/deleteCredentials", "POST")
                .withBody(Map.of("username", "mirror-user"))
                .onHandlerMethod("deleteCredentials", User.class, String.class);
    }
}
