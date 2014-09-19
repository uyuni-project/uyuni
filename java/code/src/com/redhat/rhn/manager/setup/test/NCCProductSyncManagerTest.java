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

package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.setup.NCCProductSyncManager;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.model.products.Product;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 *
 * @author bo
 */
public class NCCProductSyncManagerTest extends BaseProductSyncManagerTestCase {
    /**
     * {@inheritDoc}}
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }


   /**
     * Fake product data, but static
     * @return
     */
    protected String getFixtureProductXml() {
        try {
            return TestUtils.readAll(TestUtils.findTestData("mgr_ncc_sync_products.xml"));
        }
        catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    /**
     * builds the xml of mgr-ncc-sync from the created
     * test channels in setUp()
     */
    protected String getTestProductXml() {
        try {
            Serializer s = new Persister();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            s.write(this.getProducts(), os);
            String ret = new String(os.toByteArray(), "UTF-8");
            return ret;
        }
        catch (Exception e) {
            fail("Can't get the xml fixture data:" + e.getMessage());
            return null;
        }
    }

    protected Product getProductWithAllChannelsProvided() throws Exception {
        ProductSyncManager productSyncManager = new NCCProductSyncManager(getTestExecutor());
        List<Product> parsedProds = productSyncManager.getBaseProducts();

        Product prod = null;
        for (Product p : parsedProds) {
            if (p.getIdent().equals(this.getProvidedProductIdent())) {
                prod = p;
            }
        }
        return prod;
    }

    /**
     * Returns an executor that outputs the xml from the created
     * products in setUp()
     * @return {@link Executor}
     */
    protected Executor getTestExecutor() {
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
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };
        return executor;
    }

    /**
     * See if we get NOT_MIRRORED in case product status is not parsed as P.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusNotMirrored() throws Exception {
        ProductSyncManager productSyncManager = new NCCProductSyncManager(getTestExecutor());
        List<Product> parsedProds = productSyncManager.getBaseProducts();

        for (Product p : parsedProds) {
            if (!p.isProvided()) {
                assert(p.isStatusNotMirrored());
            }
        }
    }

    /**
     * Repo sync finished and metadata is there: FINISHED.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusDownloadCompletedAndMetadata() throws Exception {
        String oldMountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        // temporary repodata directory
        File tempMountPoint = File.createTempFile("folder-name", "");
        tempMountPoint.delete();
        tempMountPoint.mkdir();
        // change the default mount point
        Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT,
                tempMountPoint.getAbsolutePath());

        // download finished
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_FINISHED);
        productGenerateFakeMetadata(this.getProvidedProductIdent());

        Product prod = getProductWithAllChannelsProvided();

        assert(prod.getSyncStatus().isFinished());
        assertNotNull((prod.getSyncStatus().getLastSyncDate()));
        // restore old cache path
        Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, oldMountPoint);
    }

    /**
     * Tests getBaseProducts().
     * @throws Exception if anything goes wrong
     */
    public void testGetBaseProducts() throws Exception {
        Executor executor = new Executor() {
            @Override
            public String getLastCommandOutput() {
                return getFixtureProductXml();
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new NCCProductSyncManager(executor);

        List<Product> output = productSyncManager.getBaseProducts();

        assertEquals(output.get(0).getIdent(), "res-4-i386-1321-rhel-i386-es-4");
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
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };

        NCCProductSyncManager productSyncManager = new NCCProductSyncManager(executor);

        productSyncManager.readProducts();
    }

    /**
     * Tests addProducts().
     * @throws Exception if anything goes wrong
     */
    public void testAddProducts() throws Exception {
        final List<String> idents = new LinkedList<String>();
        idents.add("first_ident");
        idents.add("second_ident");

        // This list is used by the custom executor to make some assertions.
        // Items are removed from this list, hence we cannot pass idents to it,
        // we have to make a copy of it.
        final List<String> expectedIdents = new LinkedList<String>(idents);

        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return "Done";
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                String expected = expectedIdents.get(0);
                expectedIdents.remove(0);

                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.ADD_PRODUCT_SWITCH);
                assertEquals(argsIn[3], expected);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new NCCProductSyncManager(executor);
        productSyncManager.addProducts(idents);
        assertTrue(expectedIdents.isEmpty());
    }

    /**
     * Tests addProduct().
     * @throws Exception if anything goes wrong
     */
    public void testAddProduct() throws Exception {
        final String productIdent = "test";

        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return "Done";
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.ADD_PRODUCT_SWITCH);
                assertEquals(argsIn[3], productIdent);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new NCCProductSyncManager(executor);

        productSyncManager.addProduct(productIdent);
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
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], "daisan");
                return 0;
            }
        };

        NCCProductSyncManager productSyncManager = new NCCProductSyncManager(executor);

        String output = productSyncManager.runProductSyncCommand("daisan");

        assertEquals("expected test output", output);
    }

    /**
     * Tests parsing of SUSE products
     * @throws Exception if anything goes wrong
     */
    public void testParseProducts() throws Exception {
        NCCProductSyncManager productSyncManager = new NCCProductSyncManager();
        List<Product> baseProducts =
                productSyncManager.parseBaseProducts(getFixtureProductXml());

        assertEquals(3, baseProducts.size());
        assertEquals("res-4-i386-1321-rhel-i386-es-4", baseProducts.get(0).getIdent());
        assertEquals("res-4-x86_64-1321-rhel-x86_64-as-4", baseProducts.get(1).getIdent());
        assertEquals("res-6-x86_64-2580-rhel-x86_64-server-6", baseProducts.get(2)
                .getIdent());

        assertTrue(baseProducts.get(0).getAddonProducts().isEmpty());
        assertTrue(baseProducts.get(1).getAddonProducts().isEmpty());

        List<Product> res6Addons = baseProducts.get(2).getAddonProducts();
        assertEquals(1, res6Addons.size());
        assertEquals("rhel-6-expanded-support-x86_64-3200-rhel-x86_64-server-6", res6Addons
                .get(0).getIdent());
    }

    /**
     * Tests refreshProducts().
     * @throws Exception if anything goes wrong
     */
    public void testRefreshProducts() throws Exception {
        Executor executor = new Executor() {

            @Override
            public String getLastCommandOutput() {
                return "Done";
            }

            @Override
            public String getLastCommandErrorMessage() {
                return null;
            }

            @Override
            public int execute(String[] argsIn) {
                assertEquals(argsIn[0], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], NCCProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], NCCProductSyncManager.REFRESH_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = ProductSyncManager.createInstance(executor);

        productSyncManager.refreshProducts();
    }
}
