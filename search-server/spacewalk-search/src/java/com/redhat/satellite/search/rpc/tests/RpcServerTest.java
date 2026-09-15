/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.satellite.search.rpc.tests;

import com.redhat.satellite.search.rpc.RpcServer;
import com.redhat.satellite.search.tests.BaseTestCase;
import com.redhat.satellite.search.tests.TestUtil;

public class RpcServerTest extends BaseTestCase {
    @SuppressWarnings("unchecked")
    @Override
    protected Class[] getComponentClasses() {
        return TestUtil.buildComponentsList(RpcServer.class);
    }

    public void testStopServer() {
        RpcServer server = (RpcServer)
            container.getComponentInstanceOfType(RpcServer.class);
        server.stop();
    }
}
