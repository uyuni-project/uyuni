/**
 * Copyright (c) 2014 SUSE LLC
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
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tests for ProductSyncManager.
 */
public class ProductSyncManagerTest extends BaseTestCaseWithUser {

    /**
     * Test for the convertProduct() method of {@link ProductSyncManager}:
     * Convert a product with an extension into displayable objects.
     *
     * @throws Exception if something goes wrong
     */
    /*
    public void testConvertProduct() throws Exception {
        // Setup some test data
        XMLChannel baseChannel = new XMLChannel();
        baseChannel.setArch("x86_64");
        baseChannel.setLabel("baseChannel");
        baseChannel.setStatus(MgrSyncStatus.INSTALLED);
        MgrSyncProductDto baseProduct =
                new MgrSyncProductDto("baseProduct", 100L, "11", baseChannel);
        baseProduct.addChannel(baseChannel);

        // Setup an addon
        XMLChannel childChannel = new XMLChannel();
        childChannel.setArch("x86_64");
        childChannel.setLabel("childChannel");
        childChannel.setOptional(true);
        childChannel.setStatus(MgrSyncStatus.AVAILABLE);
        MgrSyncProductDto extension =
                new MgrSyncProductDto("extensionProduct", 200L, "12", baseChannel);
        extension.addChannel(childChannel);
        baseProduct.addExtension(extension);

        // Call the private method
        Method method = ProductSyncManager.class.getDeclaredMethod(
                "convertProduct", MgrSyncProductDto.class);
        method.setAccessible(true);
        SetupWizardProductDto productOut = (SetupWizardProductDto) method.invoke(
                new ProductSyncManager(), baseProduct);

        // Verify the base product attributes
        assertEquals("x86_64", productOut.getArch());
        assertEquals("baseProduct", productOut.getName());
        assertEquals("100-baseChannel", productOut.getIdent());
        assertEquals("", productOut.getBaseProductIdent());
        assertNotEmpty(productOut.getMandatoryChannels());
        for (Channel c: productOut.getMandatoryChannels()) {
            assertEquals("P", c.getStatus());
        }
        assertTrue(productOut.getOptionalChannels().isEmpty());
        assertNotEmpty(productOut.getAddonProducts());

        // Verify the addon product
        SetupWizardProductDto addonOut = productOut.getAddonProducts().get(0);
        assertEquals("x86_64", addonOut.getArch());
        assertEquals("extensionProduct", addonOut.getName());
        assertEquals("200-baseChannel", addonOut.getIdent());
        assertEquals("100-baseChannel", addonOut.getBaseProductIdent());
        assertNotEmpty(addonOut.getOptionalChannels());
        for (Channel c: addonOut.getOptionalChannels()) {
            assertEquals(".", c.getStatus());
        }
    }
    */

    /**
     * Verify product sync status for a given product: NOT_MIRRORED
     * Product is NOT_MIRRORED if any of the channels is not mirrored (= exists in the DB).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusNotMirrored() throws Exception {
        SetupWizardProductDto product = createFakeProduct("...");
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.NOT_MIRRORED, status.getStage());

        // Repeat test with another product with only one channel not mirrored
        product = createFakeProduct("PP.");
        status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.NOT_MIRRORED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * All channels are mirrored, but no metadata is there yet and no schedule either.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailed() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * A product is FAILED if there is FAILED tasko runs (even after FINISHED).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailedTasko() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoRun(product, TaskoRun.STATUS_FINISHED);
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * A product is FAILED if there is FINISHED tasko runs, but no metadata.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailedNoMetadata() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoRun(product, TaskoRun.STATUS_FINISHED);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is schedules for all mandatory channels.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressScheduled() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoSchedule(product);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is schedules for all mandatory channels, even
     * after a previous STATUS_FAILED.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressRescheduled() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        productInsertTaskoSchedule(product);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is STATUS_READY_TO_RUN tasko runs.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressReady() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoRun(product, TaskoRun.STATUS_READY_TO_RUN);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is STATUS_RUNNING tasko runs (even after FAILED).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressRunning() throws Exception {
        SetupWizardProductDto product = createFakeProduct("PPP");
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        productInsertTaskoRun(product, TaskoRun.STATUS_RUNNING);
        SyncStatus status = getProductSyncStatus(product);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FINISHED
     * Product is FINISHED if metadata is available for all mandatory channels.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetProductSyncStatusFinished() throws Exception {
        // Change default mount point for temporary repodata directory
        String oldMountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        File tempMountPoint = File.createTempFile("repomd-test", "");
        tempMountPoint.delete();
        tempMountPoint.mkdir();
        try {
            Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT,
                    tempMountPoint.getAbsolutePath());

            // Download finished and metadata available
            SetupWizardProductDto product = createFakeProduct("PPP");
            generateFakeMetadataForProduct(product);

            // Check the product sync status
            SyncStatus status = getProductSyncStatus(product);
            assertEquals(SyncStatus.SyncStage.FINISHED, status.getStage());
            assertNotNull((status.getLastSyncDate()));
        }
        finally {
            // Clean up and restore the old cache path
            FileUtils.deleteDirectory(tempMountPoint);
            Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, oldMountPoint);
        }
    }

    /**
     * Create fake product with channels as described in channelDesc, e.g. "P..P".
     * For every "P" (= provided) a real channel will be created in the database.
     *
     * @param channelDesc description of a set of channels and their status
     * @return {@link SetupWizardProductDto} fake product
     * @throws Exception if something goes wrong
     */
    private SetupWizardProductDto createFakeProduct(String channelDesc) throws Exception {
        String ident = "product-" + TestUtils.randomString();
        SetupWizardProductDto p = new SetupWizardProductDto("x86_64", ident, "Product " + ident, "",
                new MandatoryChannels(), new OptionalChannels());

        for (int k = 0; k < channelDesc.length(); k++) {
            char descChar = channelDesc.charAt(k);
            Channel channel = new Channel();
            if (!(descChar == '.' || descChar == 'P')) {
                throw new IllegalArgumentException(
                        "Ilegal channel description char " + descChar);
            }
            channel.setStatus(String.valueOf(descChar));
            p.getMandatoryChannels().add(channel);

            // If the channel is "Provided" create a real channel in the database
            if (channel.isProvided()) {
                com.redhat.rhn.domain.channel.Channel dbChannel =
                        ChannelFactoryTest.createTestChannel(user);
                channel.setLabel(dbChannel.getLabel());
                dbChannel.setLastSynced(new Date());
            }
            else {
                channel.setLabel(p.getIdent() + "-channel-" + k);
            }
        }

        return p;
    }

    /**
     * Insert a {@link TaskoSchedule} rows for a product. Will create schedules for all
     * mandatory product channels.
     *
     * @param product the product
     */
    private void productInsertTaskoSchedule(SetupWizardProductDto product) {
        List<String> channelIds = new LinkedList<>();
        for (Channel channel : product.getMandatoryChannels()) {
            String channelId = ChannelFactory.lookupByLabel(channel.getLabel())
                .getId().toString();
            channelIds.add(channelId);
        }
        insertTaskoSchedule(channelIds);
    }

    /**
     * Insert a {@link TaskoSchedule} for a given {@link Channel}.
     *
     * @param channelIds the channel ids
     * @return the schedule
     */
    private TaskoSchedule insertTaskoSchedule(List<String> channelIds) {
        String bunchName = "repo-sync-bunch";
        Integer orgId = user.getOrg().getId().intValue();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("channel_ids", channelIds);
        TaskoBunch bunch = TaskoFactory.lookupBunchByName(bunchName);
        String jobLabel = "channels-job-" + TestUtils.randomString();
        TaskoSchedule schedule = new TaskoSchedule(
                orgId, bunch, jobLabel, params, new Date(), null, null);
        TaskoFactory.save(schedule);
        return schedule;
    }

    /**
    * Insert {@link TaskoRun} rows for a given product and status. This will create runs
    * and schedules for all mandatory channels.
    *
    * @param product the product
    * @param status the status
    */
    private void productInsertTaskoRun(SetupWizardProductDto product, String status) {
        List<String> channelIds = new LinkedList<>();
        for (Channel channel : product.getMandatoryChannels()) {
            String channelId = ChannelFactory.lookupByLabel(channel.getLabel())
                    .getId().toString();
                channelIds.add(channelId);
        }
        insertTaskoRun(channelIds, status);
    }

    /**
     * Insert a {@link TaskoRun} for a given {@link com.redhat.rhn.domain.channel.Channel}.
     *
     * @param dbChannel the channel
     * @param status the tasko run status
     */
    private void insertTaskoRun(List<String> channelIds,
            String status) {
        TaskoSchedule schedule = insertTaskoSchedule(channelIds);
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
    }

    /**
     * Generate fake metadata for a given product.
     *
     * @param prodIdent ident of a product
     * @throws java.io.IOException
     */
    private void generateFakeMetadataForProduct(SetupWizardProductDto product) throws IOException {
        for (Channel channel : product.getMandatoryChannels()) {
            generateFakeMetadataForChannel(channel);
        }
    }

    /**
     * Generate fake metadata for a given {@link Channel}.
     *
     * @param channel the channel
     * @throws IOException if something goes wrong
     */
    private void generateFakeMetadataForChannel(Channel channel) throws IOException {
        if (channel.isProvided()) {
            com.redhat.rhn.domain.channel.Channel dbChannel =
                    ChannelFactory.lookupByLabel(channel.getLabel());
            if (dbChannel == null) {
                throw new IllegalArgumentException(
                        "Channel not found: " + channel.getLabel());
            }
            generateFakeMetadataForChannelLabel(dbChannel.getLabel());
        }
    }

    /**
     * Generate fake metadata for a given channel label.
     *
     * @param channelLabel the channel label
     * @throws IOException if something goes wrong
     */
    private void generateFakeMetadataForChannelLabel(String channelLabel)
            throws IOException {
        String prefix = Config.get().getString(
                ConfigDefaults.REPOMD_PATH_PREFIX, "rhn/repodata");
        String mountPoint = Config.get().getString(
                ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, "/pub");
        File repoPath = new File(mountPoint + File.separator + prefix +
                File.separator + channelLabel);
        if (!repoPath.mkdirs()) {
            throw new IOException("Can't create directories for " +
                    repoPath.getAbsolutePath());
        }
        File repomd = new File(repoPath, "repomd.xml");
        repomd.createNewFile();
    }

    /**
     * Call the private method {@link ProductSyncManager#getProductSyncStatus(SetupWizardProductDto)}.
     *
     * @param product the product
     * @return syncStatus the sync status of the product
     */
    private SyncStatus getProductSyncStatus(SetupWizardProductDto product) throws Exception {
        Method method = ProductSyncManager.class.getDeclaredMethod(
                "getProductSyncStatus", SetupWizardProductDto.class);
        method.setAccessible(true);
        return (SyncStatus) method.invoke(new ProductSyncManager(), product);
    }
}
