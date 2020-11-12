/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.frontend.nav.test;

import org.junit.Test;

import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.AclFactory;
import com.redhat.rhn.common.security.acl.AclHandler;
import com.redhat.rhn.frontend.nav.AclGuard;
import com.redhat.rhn.frontend.nav.NavNode;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import java.util.HashMap;

/**
 * AclGuardTest
 * @version $Rev$
 */
public class AclGuardTest extends RhnBaseTestCase {

    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager();
    private final FormulaManager formulaManager = new FormulaManager(saltApi);
    private final ClusterManager clusterManager = new ClusterManager(
            saltApi, systemQuery, serverGroupManager, formulaManager);

    @Test
    public void testNoAclDefined() {
        NavNode node = new NavNode();
        AclFactory aclFactory = new AclFactory(new Access(clusterManager));
        AclGuard aclGuard = new AclGuard(new HashMap(), aclFactory);
        boolean rc = aclGuard.canRender(node, 0);
        assertTrue(rc);
    }

    @Test
    public void testNullNodeDefined() {
        AclGuard aclGuard = new AclGuard(new HashMap(), new AclFactory(new Access(clusterManager)));
        boolean rc = aclGuard.canRender(null, 0);
        assertTrue(rc);
    }

    @Test
    public void testAclDefinedFailsRender() {
        NavNode node = new NavNode();
        node.setAcl("false_test()");
        AclGuard aclGuard = new AclGuard(new HashMap(),
                MockAclHandler.class.getName(), new AclFactory(new Access(clusterManager)));
        boolean rc = aclGuard.canRender(node, 0);
        assertFalse(rc);
    }

    @Test
    public void testAclDefinedShouldRender() {
        NavNode node = new NavNode();
        node.setAcl("true_test()");
        AclGuard aclGuard = new AclGuard(new HashMap(),
                MockAclHandler.class.getName(), new AclFactory(new Access(clusterManager)));
        boolean rc = aclGuard.canRender(node, 0);
        assertTrue(rc);
    }

    public static class MockAclHandler implements AclHandler {
        /**
         * Always returns true.
         * @param ctx ignored
         * @param params ignored
         * @return true
         */
        public boolean aclTrueTest(Object ctx, String[] params) {
            return true;
        }

        /**
         * Always returns false.
         * @param ctx ignored
         * @param params ignored
         * @return false
         */
        public boolean aclFalseTest(Object ctx, String[] params) {
            return false;
        }
    }
}
