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
import com.redhat.rhn.frontend.xmlrpc.saltkey.SaltKeyHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class SaltKeyHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "saltkey";
    }

    @Override
    protected Class<SaltKeyHandler> getHandlerClass() {
        return SaltKeyHandler.class;
    }

    private SaltKeyHandler handler() {
        return (SaltKeyHandler) handlerMock;
    }

    @Test
    public void testAcceptedList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).acceptedList(with(mockUser));
            will(returnValue(List.of("minion1", "minion2")));
        }});

        validateApiContract("/saltkey/acceptedList", "GET")
                .onHandlerMethod("acceptedList");
    }

    @Test
    public void testPendingList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).pendingList(with(mockUser));
            will(returnValue(List.of("minion1")));
        }});

        validateApiContract("/saltkey/pendingList", "GET")
                .onHandlerMethod("pendingList");
    }

    @Test
    public void testRejectedList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).rejectedList(with(mockUser));
            will(returnValue(List.of()));
        }});

        validateApiContract("/saltkey/rejectedList", "GET")
                .onHandlerMethod("rejectedList");
    }

    @Test
    public void testDeniedList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deniedList(with(mockUser));
            will(returnValue(List.of()));
        }});

        validateApiContract("/saltkey/deniedList", "GET")
                .onHandlerMethod("deniedList");
    }

    @Test
    public void testAccept() throws Exception {
        var minionId = "minion-1";

        context.checking(new Expectations() {{
            oneOf(handler()).accept(with(mockUser), with(minionId));
            will(returnValue(1));
        }});

        validateApiContract("/saltkey/accept", "POST")
                .withBody(Map.of("minionId", minionId))
                .onHandlerMethod("accept", User.class, String.class);
    }

    @Test
    public void testReject() throws Exception {
        var minionId = "minion-2";

        context.checking(new Expectations() {{
            oneOf(handler()).reject(with(mockUser), with(minionId));
            will(returnValue(1));
        }});

        validateApiContract("/saltkey/reject", "POST")
                .withBody(Map.of("minionId", minionId))
                .onHandlerMethod("reject", User.class, String.class);
    }

    @Test
    public void testDelete() throws Exception {
        var minionId = "minion-3";

        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(minionId));
            will(returnValue(1));
        }});

        validateApiContract("/saltkey/delete", "POST")
                .withBody(Map.of("minionId", minionId))
                .onHandlerMethod("delete", User.class, String.class);
    }
}
