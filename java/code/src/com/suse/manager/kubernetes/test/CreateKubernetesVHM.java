/**
 * Copyright (c) 2017 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.kubernetes.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by matei on 7/18/17.
 */
public class CreateKubernetesVHM extends RhnJmockBaseTestCase {

    public void test() {
        VirtualHostManager mgr = VirtualHostManagerFactory.getInstance().lookupByLabel("Cluster 1");
        VirtualHostManagerFactory.getInstance().delete(mgr);
        HibernateFactory.getSession().flush();

        VirtualHostManager cluster1 = createVirtHostManager("Cluster 1");
    }

    private VirtualHostManager createVirtHostManager(String label, String... kubeconfig) {
        User user = UserFactory.lookupByLogin("admin");
        Map params = new HashMap<>();
        if (kubeconfig.length > 0) {
            params.put("kubeconfig", kubeconfig[0]);
        } else {
            params.put("kubeconfig", "/srv/susemanager/virt_host_mgr/kubeconfig-123");
        }
        params.put("context", "local-context");
        VirtualHostManager virtualHostManager = VirtualHostManagerFactory.getInstance().createVirtualHostManager(
                label,
                user.getOrg(),
                "Kubernetes",
                params
        );
        TestUtils.saveAndFlush(virtualHostManager);
        return virtualHostManager;
    }

    @Override
    protected void tearDown() throws Exception {
        HibernateFactory.commitTransaction();
    }

}
