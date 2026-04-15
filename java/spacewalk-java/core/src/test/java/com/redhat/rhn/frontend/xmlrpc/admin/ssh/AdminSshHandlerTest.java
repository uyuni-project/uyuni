/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.admin.ssh;

import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Optional;

public class AdminSshHandlerTest extends BaseHandlerTestCase {

    @RegisterExtension
    protected final Mockery context = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    private SaltApi saltApi;
    private AdminSshHandler handler;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        saltApi = context.mock(SaltApi.class);
        handler = new AdminSshHandler(saltApi);
    }

    @Test
    public void testRemoveKnownHostSuccess() {
        context.checking(new Expectations() {{
            allowing(saltApi).removeSaltSSHKnownHost(
                    with("testhost.example.com"), with(22));
            will(returnValue(Optional.of(
                    new MgrUtilRunner.RemoveKnowHostResult("removed", ""))));
        }});
        Assertions.assertEquals(1, handler.removeKnownHost(satAdmin, "testhost.example.com", 22));
    }

    @Test
    public void testRemoveKnownHostNotFound() {
        context.checking(new Expectations() {{
            allowing(saltApi).removeSaltSSHKnownHost(
                    with("unknown.example.com"), with(22));
            will(returnValue(Optional.of(
                    new MgrUtilRunner.RemoveKnowHostResult("not_found", "Host not found"))));
        }});
        Assertions.assertEquals(0, handler.removeKnownHost(satAdmin, "unknown.example.com", 22));
    }

    @Test
    public void testRemoveKnownHostEmptyResult() {
        context.checking(new Expectations() {{
            allowing(saltApi).removeSaltSSHKnownHost(
                    with("testhost.example.com"), with(22));
            will(returnValue(Optional.empty()));
        }});
        Assertions.assertEquals(0, handler.removeKnownHost(satAdmin, "testhost.example.com", 22));
    }

    @Test
    public void testRemoveKnownHostNoPermission() {
        Assertions.assertThrows(PermissionCheckFailureException.class,
                () -> handler.removeKnownHost(regular, "testhost.example.com", 22));
    }
}
