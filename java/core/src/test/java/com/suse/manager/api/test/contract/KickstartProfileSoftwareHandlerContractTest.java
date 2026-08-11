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
import com.redhat.rhn.frontend.xmlrpc.kickstart.profile.software.SoftwareHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class KickstartProfileSoftwareHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "kickstart.profile.software";
    }

    @Override
    protected Class<SoftwareHandler> getHandlerClass() {
        return SoftwareHandler.class;
    }

    private SoftwareHandler handler() {
        return (SoftwareHandler) handlerMock;
    }

    @Test
    public void testGetSoftwareList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getSoftwareList(with(mockUser), with("test-profile"));
            will(returnValue(List.of("vim", "emacs")));
        }});

        validateApiContract("/kickstart.profile.software/getSoftwareList", "GET")
                .withParams(Map.of("ksLabel", new String[]{"test-profile"}))
                .onHandlerMethod("getSoftwareList", User.class, String.class);
    }

    @Test
    public void testSetSoftwareList() throws Exception {
        var packageList = List.of("vim", "emacs");

        context.checking(new Expectations() {{
            oneOf(handler()).setSoftwareList(with(mockUser), with("test-profile"), with(packageList),
                    with(Boolean.TRUE), with(Boolean.FALSE));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.software/setSoftwareList", "POST")
                .withBody(Map.of(
                        "ksLabel", "test-profile",
                        "packageList", packageList,
                        "ignoreMissing", true,
                        "noBase", false))
                .onHandlerMethod("setSoftwareList", User.class, String.class, List.class,
                        Boolean.class, Boolean.class);
    }

    /**
     * ignoreMissing and noBase are optional, so a request omitting them must dispatch to the
     * shorter overload.
     */
    @Test
    public void testSetSoftwareListWithoutFlags() throws Exception {
        var packageList = List.of("vim", "emacs");

        context.checking(new Expectations() {{
            oneOf(handler()).setSoftwareList(with(mockUser), with("test-profile"), with(packageList));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.software/setSoftwareList", "POST")
                .withBody(Map.of("ksLabel", "test-profile", "packageList", packageList))
                .onHandlerMethod("setSoftwareList", User.class, String.class, List.class);
    }

    @Test
    public void testAppendToSoftwareList() throws Exception {
        var packageList = List.of("vim");

        context.checking(new Expectations() {{
            oneOf(handler()).appendToSoftwareList(with(mockUser), with("test-profile"), with(packageList));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.software/appendToSoftwareList", "POST")
                .withBody(Map.of("ksLabel", "test-profile", "packageList", packageList))
                .onHandlerMethod("appendToSoftwareList", User.class, String.class, List.class);
    }

    @Test
    public void testSetSoftwareDetails() throws Exception {
        var params = Map.<String, Object>of("noBase", Boolean.TRUE, "ignoreMissing", Boolean.FALSE);

        context.checking(new Expectations() {{
            oneOf(handler()).setSoftwareDetails(with(mockUser), with("test-profile"), with(params));
            will(returnValue(1));
        }});

        validateApiContract("/kickstart.profile.software/setSoftwareDetails", "POST")
                .withBody(Map.of("ksLabel", "test-profile", "params", params))
                .onHandlerMethod("setSoftwareDetails", User.class, String.class, Map.class);
    }

    @Test
    public void testGetSoftwareDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getSoftwareDetails(with(mockUser), with("test-profile"));
            will(returnValue(Map.of("noBase", Boolean.TRUE, "ignoreMissing", Boolean.FALSE)));
        }});

        validateApiContract("/kickstart.profile.software/getSoftwareDetails", "GET")
                .withParams(Map.of("ksLabel", new String[]{"test-profile"}))
                .onHandlerMethod("getSoftwareDetails", User.class, String.class);
    }
}
