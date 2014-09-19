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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests ProductSyncManager.
 */
public abstract class BaseProductSyncManagerTestCase extends BaseTestCaseWithUser {

    private final ProductList products = new ProductList();
    private String providedProductIdent;

    /**
     * Returns list of products as an XML element.
     * @return {@link ProductList}
     */
    protected ProductList getProducts() {
        return products;
    }

    /**
     * Returns provided product ident.
     * @return Product ident.
     */
    protected String getProvidedProductIdent() {
        return providedProductIdent;
    }


    /**
     * {@inheritDoc}}
     * @throws java.lang.Exception if anything goes wrong
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // lazy init
        products.getProducts();

        Product p = createFakeProduct("PPPP");
        providedProductIdent = p.getIdent();
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
     * @return {@link Product}
     * @throws java.lang.Exception
     */
    protected Product createFakeProduct(String channelDesc)  throws Exception {
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
            if (!(descChar == '.' || descChar == 'P')) {
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
                chObj.setLastSynced(new Date());
            }
            else {
                ch.setLabel(p.getIdent() + "-channel-" + k);
            }
        }
        return p;
    }


    /**
     * Generate fake metadata for a given product.
     * @param prodIdent ident of a product
     * @throws java.io.IOException
     */
    protected void productGenerateFakeMetadata(String prodIdent) throws IOException {
        for (Product prod : products.getProducts()) {
            if (prodIdent.equals(prod.getIdent())) {
                for (com.suse.manager.model.products.Channel ch : prod
                        .getMandatoryChannels()) {
                    channelGenerateFakeMetadata(ch);
                }
            }
        }
    }

    /**
     * Generate fake metadata for a given channel.
     * @param ch channel DTO
     * @throws java.io.IOException
     */
    protected void channelGenerateFakeMetadata(
            com.suse.manager.model.products.Channel ch) throws IOException {
        if (ch.isProvided()) {
            Channel chObj = ChannelFactory.lookupByLabel(ch.getLabel());
            if (chObj == null) {
                throw new IllegalArgumentException("Channel" + ch.getLabel() + " is not P");
            }
            if (chObj.getId() == null) {
                throw new IllegalArgumentException("Channel" + ch.getLabel()
                        + " has null id");
            }
            channelGenerateFakeMetadata(chObj);
        }
    }

    /**
     * Generate fake metadata for a {@link Channel} object.
     * @param chObj channel bean
     * @throws java.io.IOException
     */
    protected void channelGenerateFakeMetadata(Channel chObj) throws IOException {
        String prefix = Config.get().getString(
                ConfigDefaults.REPOMD_PATH_PREFIX, "rhn/repodata");
        String mountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        File repoPath = new File(mountPoint + File.separator + prefix +
                File.separator + chObj.getLabel());
        if (!repoPath.mkdirs()) {
            throw new IOException("Can't create directories for "
                    + repoPath.getAbsolutePath());
        }
        File repomd = new File(repoPath, "repomd.xml");
        repomd.createNewFile();
    }

    /**
     * Insert {@link TaskoSchedule} rows for a product given by ident. Will create schedules
     * for all channels with status P, while those with . will be ignored.
     * @param prodIdent
     */
    protected void productInsertTaskoSchedule(String prodIdent) {
        for (Product prod : products.getProducts()) {
            if (prodIdent.equals(prod.getIdent())) {
                for (com.suse.manager.model.products.Channel ch : prod
                        .getMandatoryChannels()) {
                    insertTaskoSchedule(ch);
                }
            }
        }
    }

    protected void insertTaskoSchedule(com.suse.manager.model.products.Channel ch) {
        if (ch.isProvided()) {
            Channel chObj = ChannelFactory.lookupByLabel(ch.getLabel());
            insertTaskoSchedule(chObj);
        }
    }

    private TaskoSchedule insertTaskoSchedule(Channel chObj) {
        String bunchName = "repo-sync-bunch";
        Integer orgId = user.getOrg().getId().intValue();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_id", chObj.getId().toString());
        TaskoBunch bunch = TaskoFactory.lookupBunchByName(bunchName);
        String jobLabel = chObj.getLabel() + "-job-" + TestUtils.randomString();
        TaskoSchedule schedule = new TaskoSchedule(
                orgId, bunch, jobLabel, params, new Date(), null, null);
        TaskoFactory.save(schedule);
        return schedule;
    }

    /**
     * Insert {@link TaskoRun} rows for a given product and status. This will create runs
     * and schedules for all channels with status P, while those with . will be ignored.
     * @param prodIdent ident of a product
     * @param status status
     * @throws java.lang.Exception
     */
    protected void productInsertTaskoRun(String prodIdent, String status) throws Exception {
        for (Product prod : products.getProducts()) {
            if (prodIdent.equals(prod.getIdent())) {
                for (com.suse.manager.model.products.Channel ch : prod
                        .getMandatoryChannels()) {
                    insertTaskoRun(ch, status);
                }
            }
        }
    }

    private void insertTaskoRun(
            com.suse.manager.model.products.Channel ch, String status) throws Exception {
        if (ch.isProvided()) {
            Channel chObj = ChannelFactory.lookupByLabel(ch.getLabel());
            insertTaskoRun(chObj, status);
        }
    }

    private void insertTaskoRun(Channel chObj, String status) throws Exception {
        TaskoSchedule schedule = insertTaskoSchedule(chObj);
        TaskoTemplate template = schedule.getBunch().getTemplates().get(0);
        assertNotNull(template);
        TaskoRun taskRun = new TaskoRun(schedule.getOrgId(), template, schedule.getId());
        taskRun.setStatus(status);
        if (status.equals(TaskoRun.STATUS_FAILED) ||
            status.equals(TaskoRun.STATUS_FINISHED) ||
            status.equals(TaskoRun.STATUS_INTERRUPTED) ||
            status.equals(TaskoRun.STATUS_SKIPPED)) {
            taskRun.setEndTime(new Date());
        }
        TaskoFactory.save(taskRun);
        TaskoFactory.getSession().flush();
    }

    /**
     * Actual implementation should feed getProducts() fixtures into the real
     * product sync manager backend getBaseProducts(). As an example NCC backend uses
     * a test {@link Executor} which outputs the fixtures to the XML.
     *
     * @return {@link Product}
     * @throws Exception
     */
    protected abstract Product getProductWithAllChannelsProvided() throws Exception;

    /**
     * A product with all channels on P without previous download runs
     * is in progress if there is schedules for all those channels.
     * @throws Exception if anything goes wrong
     */
    public void testNewProductStatusInProgress() throws Exception {
        productInsertTaskoSchedule(this.getProvidedProductIdent());
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.IN_PROGRESS,
                        prod.getSyncStatus());
    }

    /**
     * There is no runs and no schedules: FAILED.
     * @throws Exception if anything goes wrong
     */
    public void testNewProductStatusFailed() throws Exception {
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.FAILED,
                        prod.getSyncStatus());
    }

    /**
     * There is a run with status RUNNING (even after FAILED ones), so IN_PROGRESS.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusInProgress() throws Exception {
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_FAILED);
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_RUNNING);
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.IN_PROGRESS,
                        prod.getSyncStatus());
    }

    /**
     * Repo sync run has FAILED and there is no new schedule: FAILED.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusFailed() throws Exception {
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_FAILED);
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.FAILED,
                prod.getSyncStatus());
    }

    /**
     * Repo sync run has FAILED and there is a new schedule (retry): IN_PROGRESS.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusAfterRetry() throws Exception {
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_FAILED);
        productInsertTaskoSchedule(this.getProvidedProductIdent());
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.IN_PROGRESS,
                prod.getSyncStatus());
    }

    /**
     * Repo sync finished, but no metadata: FAILED.
     * @throws Exception if anything goes wrong
     */
    public void testProductStatusDownloadCompletedNoMetadata() throws Exception {
        productInsertTaskoRun(this.getProvidedProductIdent(), TaskoRun.STATUS_FINISHED);
        Product prod = getProductWithAllChannelsProvided();
        assertEquals(Product.SyncStatus.FAILED,
                        prod.getSyncStatus());
    }
}
