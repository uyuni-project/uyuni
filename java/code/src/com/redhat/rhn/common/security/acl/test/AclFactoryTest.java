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
package com.redhat.rhn.common.security.acl.test; import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.redhat.rhn.common.security.acl.Access;
import com.redhat.rhn.common.security.acl.Acl;
import com.redhat.rhn.common.security.acl.AclFactory;
import com.redhat.rhn.manager.formula.FormulaManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.suse.manager.clusters.ClusterManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

/**
 * AccessTest
 * @version $Rev$
 */
public class AclFactoryTest extends RhnBaseTestCase {

    @Test
    public void testGetAcl() {
        SystemQuery systemQuery = new TestSystemQuery();
        SaltApi saltApi = new TestSaltApi();
        ServerGroupManager serverGroupManager = new ServerGroupManager();
        FormulaManager formulaManager = new FormulaManager(saltApi);
        ClusterManager clusterManager = new ClusterManager(saltApi, systemQuery, serverGroupManager, formulaManager);
        AclFactory aclFactory = new AclFactory(new Access(clusterManager));
        Acl test = aclFactory.getAcl("  com.redhat.rhn.common.security.acl.test.MixinTestHandler  ");
        assertNotNull(test);
    }
}
