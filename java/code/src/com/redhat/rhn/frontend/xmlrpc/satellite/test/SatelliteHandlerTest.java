/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.satellite.test;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.proxy.ProxyHandler;
import com.redhat.rhn.frontend.xmlrpc.satellite.SatelliteHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;

import java.util.Map;

public class SatelliteHandlerTest extends BaseHandlerTestCase {

    private SystemQuery systemQuery = new SaltService();
    private RegularMinionBootstrapper regularMinionBootstrapper = RegularMinionBootstrapper.getInstance(systemQuery);
    private SSHMinionBootstrapper sshMinionBootstrapper = SSHMinionBootstrapper.getInstance(systemQuery);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private SatelliteHandler handler = new SatelliteHandler(new ProxyHandler(xmlRpcSystemHelper));

    public void testListProxies() throws Exception {
        Server server = ServerFactoryTest.createTestProxyServer(admin, false);
        Object[] list = handler.listProxies(admin);
        assertEquals(1, list.length);
        assertEquals(server.getId(), ((Map)list[0]).get("id"));
    }
}
