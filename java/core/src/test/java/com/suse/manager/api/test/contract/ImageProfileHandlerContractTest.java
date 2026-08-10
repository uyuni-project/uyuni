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

import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.image.profile.ImageProfileHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ImageProfileHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "image.profile";
    }

    @Override
    protected Class<ImageProfileHandler> getHandlerClass() {
        return ImageProfileHandler.class;
    }

    private ImageProfileHandler handler() {
        return (ImageProfileHandler) handlerMock;
    }

    /**
     * Builds a profile serialized by the registered ImageProfileSerializer. A Dockerfile
     * profile is used because it is the variant carrying the documented "path" property.
     *
     * @return the image profile
     */
    private ImageProfile imageProfile() {
        var store = new ImageStore();
        store.setLabel("test-store");

        var profile = new DockerfileProfile();
        profile.setLabel("test-profile");
        profile.setTargetStore(store);
        profile.setPath("git://example.com/dockerfile#master:/");
        return profile;
    }

    @Test
    public void testListImageProfileTypes() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImageProfileTypes(with(mockUser));
            will(returnValue(List.of("dockerfile", "kiwi")));
        }});

        validateApiContract("/image.profile/listImageProfileTypes", "GET")
                .onHandlerMethod("listImageProfileTypes", User.class);
    }

    @Test
    public void testListImageProfiles() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listImageProfiles(with(mockUser));
            will(returnValue(List.of(imageProfile())));
        }});

        validateApiContract("/image.profile/listImageProfiles", "GET")
                .onHandlerMethod("listImageProfiles", User.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with("test-profile"));
            will(returnValue(imageProfile()));
        }});

        validateApiContract("/image.profile/getDetails", "GET")
                .withParams(Map.of("label", new String[]{"test-profile"}))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testCreate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with("test-profile"), with("dockerfile"),
                    with("test-store"), with("git://example.com/dockerfile#master:/"),
                    with("activation-key"), with("--set-repo=value"));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/create", "POST")
                .withBody(Map.of(
                        "label", "test-profile",
                        "type", "dockerfile",
                        "storeLabel", "test-store",
                        "path", "git://example.com/dockerfile#master:/",
                        "activationKey", "activation-key",
                        "kiwiOptions", "--set-repo=value"))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class, String.class,
                        String.class, String.class);
    }

    /**
     * kiwiOptions is optional, so a request omitting it must dispatch to the shorter overload.
     */
    @Test
    public void testCreateWithoutKiwiOptions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with("test-profile"), with("dockerfile"),
                    with("test-store"), with("git://example.com/dockerfile#master:/"),
                    with("activation-key"));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/create", "POST")
                .withBody(Map.of(
                        "label", "test-profile",
                        "type", "dockerfile",
                        "storeLabel", "test-store",
                        "path", "git://example.com/dockerfile#master:/",
                        "activationKey", "activation-key"))
                .onHandlerMethod("create", User.class, String.class, String.class, String.class, String.class,
                        String.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with("test-profile"));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/delete", "POST")
                .withBody(Map.of("label", "test-profile"))
                .onHandlerMethod("delete", User.class, String.class);
    }

    @Test
    public void testSetDetails() throws Exception {
        var details = Map.<String, Object>of(
                "storeLabel", "test-store",
                "path", "git://example.com/dockerfile#master:/",
                "activationKey", "activation-key");

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with("test-profile"), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/setDetails", "POST")
                .withBody(Map.of("label", "test-profile", "details", details))
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }

    @Test
    public void testGetCustomValues() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getCustomValues(with(mockUser), with("test-profile"));
            will(returnValue(Map.of("custom info label", "test-key", "value", "test-value")));
        }});

        validateApiContract("/image.profile/getCustomValues", "GET")
                .withParams(Map.of("label", new String[]{"test-profile"}))
                .onHandlerMethod("getCustomValues", User.class, String.class);
    }

    @Test
    public void testSetCustomValues() throws Exception {
        var values = Map.of("custom info label", "test-key", "value", "test-value");

        context.checking(new Expectations() {{
            oneOf(handler()).setCustomValues(with(mockUser), with("test-profile"), with(values));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/setCustomValues", "POST")
                .withBody(Map.of("label", "test-profile", "values", values))
                .onHandlerMethod("setCustomValues", User.class, String.class, Map.class);
    }

    @Test
    public void testDeleteCustomValues() throws Exception {
        var keys = List.of("test-key");

        context.checking(new Expectations() {{
            oneOf(handler()).deleteCustomValues(with(mockUser), with("test-profile"), with(keys));
            will(returnValue(1));
        }});

        validateApiContract("/image.profile/deleteCustomValues", "POST")
                .withBody(Map.of("label", "test-profile", "keys", keys))
                .onHandlerMethod("deleteCustomValues", User.class, String.class, List.class);
    }
}
