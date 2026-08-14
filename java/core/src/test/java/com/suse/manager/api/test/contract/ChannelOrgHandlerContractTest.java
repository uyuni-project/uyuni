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
import com.redhat.rhn.frontend.xmlrpc.channel.org.ChannelOrgHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ChannelOrgHandlerContractTest extends BaseOpenApiTest {

    private static final String LABEL = "test-channel";
    private static final Integer ORG_ID = 1;

    @Override
    protected String getApiNamespace() {
        return "channel.org";
    }

    @Override
    protected Class<ChannelOrgHandler> getHandlerClass() {
        return ChannelOrgHandler.class;
    }

    private ChannelOrgHandler handler() {
        return (ChannelOrgHandler) handlerMock;
    }

    @Test
    public void testList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).list(with(mockUser), with(LABEL));
            will(returnValue(List.of(Map.of(
                    "org_id", ORG_ID,
                    "org_name", "Test Organization",
                    "access_enabled", Boolean.TRUE
            ))));
        }});

        validateApiContract("/channel.org/list", "POST")
                .withBody(Map.of("label", LABEL))
                .onHandlerMethod("list", User.class, String.class);
    }

    @Test
    public void testEnableAccess() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).enableAccess(with(mockUser), with(LABEL), with(ORG_ID));
            will(returnValue(1));
        }});

        validateApiContract("/channel.org/enableAccess", "POST")
                .withBody(Map.of("label", LABEL, "orgId", ORG_ID))
                .onHandlerMethod("enableAccess", User.class, String.class, Integer.class);
    }

    @Test
    public void testDisableAccess() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).disableAccess(with(mockUser), with(LABEL), with(ORG_ID));
            will(returnValue(1));
        }});

        validateApiContract("/channel.org/disableAccess", "POST")
                .withBody(Map.of("label", LABEL, "orgId", ORG_ID))
                .onHandlerMethod("disableAccess", User.class, String.class, Integer.class);
    }
}
