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

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.image.store.ImageStoreHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ImageStoreHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "image.store";
    }

    @Override
    protected Class<ImageStoreHandler> getHandlerClass() {
        return ImageStoreHandler.class;
    }

    private ImageStoreHandler handler() {
        return (ImageStoreHandler) handlerMock;
    }

    /**
     * Builds a store serialized by the registered ImageStoreSerializer. Credentials are left
     * null so that "hasCredentials" is false and "username" is the empty string, which keeps
     * the fixture from reaching the credentials tables.
     *
     * @return the image store
     */
    private ImageStore imageStore() {
        var store = new ImageStore();
        store.setLabel("test-store");
        store.setUri("registry.example.com");
        store.setStoreType(imageStoreType());
        return store;
    }

    private ImageStoreType imageStoreType() {
        var type = new ImageStoreType();
        type.setId(1L);
        type.setLabel("registry");
        type.setName("Registry");
        return type;
    }

    @Test
    public void testListImageStoreTypes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImageStoreTypes(with(mockUser));
            will(returnValue(List.of(imageStoreType())));
        }});

        validateApiContract("/image.store/listImageStoreTypes", "GET")
                .onHandlerMethod("listImageStoreTypes", User.class);
    }

    @Test
    public void testListImageStores() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImageStores(with(mockUser));
            will(returnValue(List.of(imageStore())));
        }});

        validateApiContract("/image.store/listImageStores", "GET")
                .onHandlerMethod("listImageStores", User.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with("test-store"));
            will(returnValue(imageStore()));
        }});

        validateApiContract("/image.store/getDetails", "GET")
                .withParams(Map.of("label", new String[]{"test-store"}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testCreate() throws Exception {
        var credentials = Map.of("username", "test-user", "password", "test-password");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with("test-store"),
                    with("registry.example.com"), with("registry"), with(credentials));
            will(returnValue(1));
        }});

        validateApiContract("/image.store/create", "POST")
                .withBody(Map.of(
                        "label", "test-store",
                        "uri", "registry.example.com",
                        "storeType", "registry",
                        "credentials", credentials))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class, Map.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with("test-store"));
            will(returnValue(1));
        }});

        validateApiContract("/image.store/delete", "POST")
                .withBody(Map.of("label", "test-store"))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testSetDetails() throws Exception {
        var details = Map.<String, Object>of(
                "uri", "registry.example.com",
                "username", "test-user",
                "password", "test-password");

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with("test-store"), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/image.store/setDetails", "POST")
                .withBody(Map.of("label", "test-store", "details", details))
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }
}
