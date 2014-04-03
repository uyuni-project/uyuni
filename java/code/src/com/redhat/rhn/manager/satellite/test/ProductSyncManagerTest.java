/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.satellite.test;

import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.products.Product;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests ProductSyncManager.
 */
public class ProductSyncManagerTest extends TestCase {

    /**
     * Tests getProductsHierarchy().
     * @throws Exception if anything goes wrong
     */
    public void testGetProductsHierarchy() throws Exception {
        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return getTestProductXml();
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        Map<Product, List<Product>> output = productSyncManager.getProductsHierarchy();

        assertEquals(output.keySet().iterator().next().getIdent(),
                "res-4-i386-1321-rhel-i386-es-4");
    }

    /**
     * Tests runProductSyncCommand().
     * @throws Exception if anything goes wrong
     */
    public void testRunProductSyncCommand() throws Exception {
        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return "expected test output";
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], "daisan");
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        String output = productSyncManager.runProductSyncCommand("daisan");

        assertEquals("expected test output", output);
    }

    /**
     * Tests parsing of SUSE products
     * @throws Exception if anything goes wrong
     */
    public void testParseProducts() throws Exception {
        ProductSyncManager productSyncManager = new ProductSyncManager();
        Map<Product, List<Product>> productHierarchy =
                productSyncManager.parseProducts(getTestProductXml());

        List<Product> baseProducts = new LinkedList<Product>(productHierarchy.keySet());
        assertEquals(3, baseProducts.size());
        assertEquals("res-4-i386-1321-rhel-i386-es-4", baseProducts.get(0).getIdent());
        assertEquals("res-4-x86_64-1321-rhel-x86_64-as-4", baseProducts.get(1).getIdent());
        assertEquals("res-6-x86_64-2580-rhel-x86_64-server-6", baseProducts.get(2)
                .getIdent());

        assertTrue(productHierarchy.get(baseProducts.get(0)).isEmpty());
        assertTrue(productHierarchy.get(baseProducts.get(1)).isEmpty());

        List<Product> res6Addons = productHierarchy.get(baseProducts.get(2));
        assertEquals(1, res6Addons.size());
        assertEquals("rhel-6-expanded-support-x86_64-3200-rhel-x86_64-server-6", res6Addons
                .get(0).getIdent());
    }

    /**
     * Tests readProducts().
     * @throws Exception if anything goes wrong
     */
    public void testReadProducts() throws Exception {
        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return getTestProductXml();
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        productSyncManager.readProducts();
    }

    /**
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private String getTestProductXml() {
        try {
            return TestUtils.readAll(TestUtils.findTestData("mgr_ncc_sync_products.xml"));
        }
        catch (Exception e) {
            fail();
            return null;
        }
    }
}
