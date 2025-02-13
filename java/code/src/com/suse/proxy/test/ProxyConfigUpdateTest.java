/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy.test;

import static org.junit.Assert.assertEquals;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.MockObjectTestCase;
import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.gson.ProxyConfigUpdateJson;
import com.suse.proxy.update.ProxyConfigUpdate;
import com.suse.proxy.update.ProxyConfigUpdateContext;
import com.suse.proxy.update.ProxyConfigUpdateContextHandler;

import org.jmock.Expectations;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(JUnit5Mockery.class)
public class ProxyConfigUpdateTest extends MockObjectTestCase {

    private ProxyConfigUpdate proxyConfigUpdate;
    private ProxyConfigUpdateJson request;
    private SystemManager systemManager;
    private SaltApi saltApi;
    private User user;
    private ProxyConfigUpdateContextHandler handler1;
    private ProxyConfigUpdateContextHandler handler2;

    @Before
    public void setUp() {
        request = new ProxyConfigUpdateJson();
        systemManager = context.mock(SystemManager.class);
        saltApi = context.mock(SaltApi.class);
        user = context.mock(User.class);
        handler1 = context.mock(ProxyConfigUpdateContextHandler.class, "handler1");
        handler2 = context.mock(ProxyConfigUpdateContextHandler.class, "handler2");

        proxyConfigUpdate = null; // new ProxyConfigUpdate(List.of(handler1, handler2));
    }

    @Test
    public void testUpdate() {
        context.checking(new Expectations() {{
            oneOf(handler1).handle(with(any(ProxyConfigUpdateContext.class)));
            oneOf(handler2).handle(with(any(ProxyConfigUpdateContext.class)));
        }});

        proxyConfigUpdate.update(request, systemManager, saltApi, user);
    }

    @Test
    public void testUpdateWithError() {
        context.checking(new Expectations() {{
            oneOf(handler1).handle(with(any(ProxyConfigUpdateContext.class)));
            will(throwException(new RuntimeException("Handler error")));
        }});

        try {
            proxyConfigUpdate.update(request, systemManager, saltApi, user);
        } catch (RuntimeException e) {
            assertEquals("Handler error", e.getMessage());
        }

        context.assertIsSatisfied();
    }
}