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
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.TaskoBunch;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.TaskoRun;
import com.redhat.rhn.taskomatic.TaskoSchedule;
import com.redhat.rhn.taskomatic.TaskoTemplate;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;
import com.suse.manager.model.products.Product;
import com.suse.manager.model.products.ProductList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

/**
 * Tests ProductSyncManager.
 */
public class ProductSyncManagerTest extends BaseTestCaseWithUser {

    private final ProductList products = new ProductList();
    private String inProgressProductIdent;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // lazy init
        products.getProducts();

        Product p = createFakeProduct("PPPP");
        inProgressProductIdent = p.getIdent();
        products.getProducts().add(p);

        products.getProducts().add(createFakeProduct("PP.."));
        products.getProducts().add(createFakeProduct("PPP"));
        products.getProducts().add(createFakeProduct("PPP"));
        products.getProducts().add(createFakeProduct("PPP"));

        // make one of the products an addon
        products.getProducts().get(3).setBaseProductIdent(
                products.getProducts().get(2).getIdent());
    }

    /**
     * Creates a fake product with the channels as described in
     * channelDesc eg: "P..P"
     * @param channelDesc
     */
    private Product createFakeProduct(String channelDesc)  throws Exception {
        String ident = "product-" + TestUtils.randomString();
        Product p = new Product("x86_64",
            ident,
            "Product " + ident,
            "", // ident
            new MandatoryChannels(),
            new OptionalChannels());

        // lazy init
        p.getMandatoryChannels();
        p.getOptionalChannels();

        for (int k = 0; k < channelDesc.length(); k++) {
            char descChar = channelDesc.charAt(k);

            com.suse.manager.model.products.Channel ch;
            ch = new com.suse.manager.model.products.Channel();
            // for the first product have all channels provided
            // for the rest only half of them
            if (!(descChar == '.' || descChar == 'P' )) {
                throw new IllegalArgumentException(
                        "Ilegal channel description char " + descChar);
            }
            ch.setStatus(String.valueOf(descChar));
            p.getMandatoryChannels().add(ch);
            // if the channel is Provided, create a real channel
            // in the database
            if (ch.isProvided()) {
                Channel chObj = ChannelFactoryTest.createTestChannel(user);
                ch.setLabel(chObj.getLabel());
            }
            else {
                ch.setLabel(p.getIdent() + "-channel-" + k);
            }
        }
        return p;
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * builds the xml of mgr-ncc-sync from the created
     * test channels in setUp()
     */
    private String getTestProductXml() {
        try {
            Serializer s = new Persister();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            s.write(products, os);
            String ret = new String(os.toByteArray(), "UTF-8");
            return ret;
        }
        catch (Exception e) {
            fail("Can't get the xml fixture data:" + e.getMessage());
            return null;
        }
    }

   /**
     * Fake product data, but static
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private String getFixtureProductXml() {
        try {
            return TestUtils.readAll(TestUtils.findTestData("mgr_ncc_sync_products.xml"));
        }
        catch (Exception e) {
            fail(e.getMessage());
            return null;
        }
    }

    /**
     * Returns an executor that outputs the xml from the created
     * products in setUp()
     */
    private Executor getTestExecutor() {
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
        return executor;
    }

    public void testProductStatusNotMirrored() throws Exception {
        ProductSyncManager productSyncManager = new ProductSyncManager(getTestExecutor());
        List<Product> parsedProds = productSyncManager.getBaseProducts();

        for (Product p : parsedProds) {
            if (!p.isProvided()) {
                assertEquals(Product.SyncStatus.NOT_MIRRORED,
                    p.getSyncStatus());
            }
        }
    }

    private Product getProductWithAllChannelsProvided() throws Exception {
        ProductSyncManager productSyncManager = new ProductSyncManager(getTestExecutor());
        List<Product> parsedProds = productSyncManager.getBaseProducts();

        Product prod = null;
        for (Product p : parsedProds) {
            if (p.getIdent().equals(inProgressProductIdent)) {
                prod = p;
            }
        }
        return prod;
    }

    /**
     * A product with all channels on P without previous download runs
     * is in-progress
     * @throws Exception
     */
    public void testNewProductStatusInProgress() throws Exception {
        Product prod = getProductWithAllChannelsProvided();

        assertEquals(Product.SyncStatus.IN_PROGRESS,
                        prod.getSyncStatus());
    }

    public void testOldProductStatusInProgress() throws Exception {
        // in this case the product is not new, it had
        // some old downloads
        productScheduleFakeDownload(inProgressProductIdent, TaskoRun.STATUS_FAILED);
        productScheduleFakeDownload(inProgressProductIdent, TaskoRun.STATUS_RUNNING);
        Product prod = getProductWithAllChannelsProvided();

        assertEquals(Product.SyncStatus.IN_PROGRESS,
                        prod.getSyncStatus());
    }

    public void testProductStatusFailed() throws Exception {
        // in this case the product is not new, it had
        // some old downloads
        productScheduleFakeDownload(inProgressProductIdent, TaskoRun.STATUS_FAILED);
        Product prod = getProductWithAllChannelsProvided();

        assertEquals(Product.SyncStatus.FAILED,
                prod.getSyncStatus());
    }

    public void testProductStatusDownloadCompletedNoMetadata() throws Exception {
        productScheduleFakeDownload(inProgressProductIdent, TaskoRun.STATUS_FINISHED);
        Product prod = getProductWithAllChannelsProvided();

        // in this case the product is not new, it had
        // some old downloads
        assertEquals(Product.SyncStatus.FAILED,
                        prod.getSyncStatus());
    }

    /**
     * Generate fake metadata for a product
     * @param prodIdent identifier
     */
    private void productGenerateFakeMetadata(String prodIdent) throws IOException {
        for (Product prod : products.getProducts()) {
            if (prodIdent.equals(prod.getIdent())) {
                for (com.suse.manager.model.products.Channel ch : prod.getMandatoryChannels()) {
                    channelGenerateFakeMetadata(ch);
                }
            }
        }
    }

    /**
     * Generate fake metadata for a channel
     * @param ch channel DTO
     */
    private void channelGenerateFakeMetadata(
            com.suse.manager.model.products.Channel ch) throws IOException {
        if (ch.isProvided()) {
            Channel chObj = ChannelFactory.lookupByLabel(ch.getLabel());
            if (chObj == null) {
                throw new IllegalArgumentException("Channel" + ch.getLabel() + " is not P");
            }
            if (chObj.getId() == null) {
                throw new IllegalArgumentException("Channel" + ch.getLabel() + " has null id");
            }
            channelGenerateFakeMetadata(chObj);
        }
    }

    /**
     * Generate fake metadata for a channel
     * @param chObj channel bean
     */
    private void channelGenerateFakeMetadata(Channel chObj) throws IOException {
        String prefix = Config.get().getString(
                ConfigDefaults.REPOMD_PATH_PREFIX, "rhn/repodata");
        String mountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        File repoPath = new File(mountPoint + File.separator + prefix +
                File.separator + chObj.getLabel());
        if (!repoPath.mkdirs()) {
            throw new IOException("Can't create directories for " + repoPath.getAbsolutePath());
        }
        File repomd = new File(repoPath, "repomd.xml");
        repomd.createNewFile();
    }

    public void testProductStatusDownloadCompletedAndMetadata() throws Exception {
        String oldMountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        // temporary repodata directory
        File tempMountPoint = File.createTempFile("folder-name","");
        tempMountPoint.delete();
        tempMountPoint.mkdir();
        // change the default mount point
        Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT,
                tempMountPoint.getAbsolutePath());

        // download finished
        productScheduleFakeDownload(inProgressProductIdent, TaskoRun.STATUS_FINISHED);
        productGenerateFakeMetadata(inProgressProductIdent);

        Product prod = getProductWithAllChannelsProvided();

        assertEquals(Product.SyncStatus.FINISHED,
                        prod.getSyncStatus());
        // restore old cache path
        Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, oldMountPoint);
    }

    /**
     * Schedules download to all provided channels
     */
    private void productScheduleFakeDownload(String prodIdent, String status) throws Exception {
        for (Product prod : products.getProducts()) {
            if (prodIdent.equals(prod.getIdent())) {
                for (com.suse.manager.model.products.Channel ch : prod.getMandatoryChannels()) {
                    scheduleFakeDownload(ch, status);
                }
            }
        }
    }

    private void scheduleFakeDownload(
            com.suse.manager.model.products.Channel ch, String status) throws Exception {
        if (ch.isProvided()) {
            Channel chObj = ChannelFactory.lookupByLabel(ch.getLabel());
            if (chObj == null) {
                throw new RuntimeException("Channel" + ch.getLabel() + " is not P");
            }
            if (chObj.getId() == null) {
                throw new RuntimeException("Channel" + ch.getLabel() + " has null id");
            }
            scheduleFakeDownload(chObj, status);
        }
    }

    private void scheduleFakeDownload(Channel chObj, String status) throws Exception {
        // now schedule it
        String bunchName = "repo-sync-bunch";
        Integer orgId = user.getOrg().getId().intValue();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_id", chObj.getId().toString());
        TaskoBunch bunch = TaskoFactory.lookupBunchByName(bunchName);
        String jobLabel = chObj.getLabel() + "-job-" + TestUtils.randomString();
        TaskoSchedule schedule = new TaskoSchedule(
                orgId, bunch, jobLabel, params, new Date(), null, null);
        TaskoFactory.save(schedule);
        TaskoTemplate template = bunch.getTemplates().get(0);
        assertNotNull(template);
        TaskoRun taskRun = new TaskoRun(schedule.getOrgId(), template, schedule.getId());
        taskRun.setStatus(status);
        TaskoFactory.save(taskRun);
        TaskoFactory.getSession().flush();
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
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.LIST_PRODUCT_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        List<Product> output = productSyncManager.getBaseProducts();

        assertEquals(output.get(0).getIdent(), "res-4-i386-1321-rhel-i386-es-4");
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
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.REFRESH_SWITCH);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        productSyncManager.refreshProducts();
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
                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.ADD_PRODUCT_SWITCH);
                assertEquals(argsIn[3], productIdent);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);

        productSyncManager.addProduct(productIdent);
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

                assertEquals(argsIn[0], ProductSyncManager.PRODUCT_SYNC_COMMAND[0]);
                assertEquals(argsIn[1], ProductSyncManager.PRODUCT_SYNC_COMMAND[1]);
                assertEquals(argsIn[2], ProductSyncManager.ADD_PRODUCT_SWITCH);
                assertEquals(argsIn[3], expected);
                return 0;
            }
        };

        ProductSyncManager productSyncManager = new ProductSyncManager(executor);
        productSyncManager.addProducts(idents);
        assertTrue(expectedIdents.isEmpty());
    }
}
