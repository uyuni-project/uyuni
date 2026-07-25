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

import com.redhat.rhn.domain.kickstart.cobbler.CobblerSnippet;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.kickstart.snippet.SnippetHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SnippetHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "kickstart.snippet";
    }

    @Override
    protected Class<SnippetHandler> getHandlerClass() {
        return SnippetHandler.class;
    }

    private SnippetHandler handler() {
        return (SnippetHandler) handlerMock;
    }

    /**
     * Builds a snippet serialized by the registered SnippetSerializer, so the response is
     * validated against the documented schema. CobblerSnippet has no public constructor and
     * its getters read from the cobbler snippets directory, so it is mocked instead.
     */
    private CobblerSnippet snippet() {
        CobblerSnippet snippet = context.mock(CobblerSnippet.class, "snippet");
        context.checking(new Expectations() {{
            allowing(snippet).getName();
            will(returnValue("test-snippet"));
            allowing(snippet).getContents();
            will(returnValue("echo test"));
            allowing(snippet).getFragment();
            will(returnValue("$SNIPPET('spacewalk/1/test-snippet')"));
            allowing(snippet).getPath();
            will(returnValue(new File("/var/lib/cobbler/snippets/spacewalk/1/test-snippet")));
        }});
        return snippet;
    }

    @Test
    public void testListAll() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAll(with(mockUser));
            will(returnValue(List.of(snippet())));
        }});

        validateApiContract("/kickstart.snippet/listAll", "GET")
                .onHandlerMethod("listAll", User.class);
    }

    @Test
    public void testListCustom() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCustom(with(mockUser));
            will(returnValue(List.of(snippet())));
        }});

        validateApiContract("/kickstart.snippet/listCustom", "GET")
                .onHandlerMethod("listCustom", User.class);
    }

    @Test
    public void testListDefault() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listDefault(with(mockUser));
            will(returnValue(List.of(snippet())));
        }});

        validateApiContract("/kickstart.snippet/listDefault", "GET")
                .onHandlerMethod("listDefault", User.class);
    }

    @Test
    public void testCreateOrUpdate() throws Exception {
        String name = "test-snippet";
        String contents = "echo test";

        context.checking(new Expectations() {{
            oneOf(handler()).createOrUpdate(with(mockUser), with(name), with(contents));
            will(returnValue(snippet()));
        }});

        validateApiContract("/kickstart.snippet/createOrUpdate", "POST")
                .withBody(Map.of("name", name, "contents", contents))
                .onHandlerMethod("createOrUpdate", User.class, String.class, String.class);
    }

    @Test
    public void testDelete() throws Exception {
        String name = "test-snippet";

        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(name));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.snippet/delete", "POST")
                .withBody(Map.of("name", name))
                .onHandlerMethod("delete", User.class, String.class);
    }
}
