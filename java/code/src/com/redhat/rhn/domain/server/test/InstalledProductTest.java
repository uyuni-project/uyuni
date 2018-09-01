/**
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.HashSet;
import java.util.Set;


public class InstalledProductTest extends RhnBaseTestCase {

    public void testInstalledProduct() throws Exception {
        SUSEProductTestUtils.createVendorSUSEProducts();

        InstalledProduct installedPrd = new InstalledProduct();
        installedPrd.setName("SLES");
        installedPrd.setVersion("12.1");
        installedPrd.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        installedPrd.setBaseproduct(true);
        assertNull(installedPrd.getId());

        User u = UserTestUtils.findNewUser("testUser", "testOrgCreateTestInstalledProduct");
        Server server = ServerFactoryTest.createTestServer(u, true);
        assertNotNull(server);
        assertNotNull(server.getId());

        Set<InstalledProduct> products = new HashSet<>();
        products.add(installedPrd);

        server.setInstalledProducts(products);
        TestUtils.saveAndReload(server);

        assertNotNull(server.getInstalledProductSet().orElse(null));

        Set<InstalledProduct> readProducts = server.getInstalledProducts();
        assertNotNull(readProducts);

        readProducts.forEach( p -> {
                assertEquals(installedPrd.getName(), p.getName());
                assertEquals(installedPrd.getVersion(), p.getVersion());
                assertNull(p.getRelease());
                assertEquals(installedPrd.getArch(), p.getArch());
                assertEquals(installedPrd.isBaseproduct(), p.isBaseproduct());
        });
    }
}
