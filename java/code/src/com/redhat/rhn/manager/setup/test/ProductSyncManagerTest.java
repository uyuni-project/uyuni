/*
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
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.product.test.SUSEProductTestUtils;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto;
import com.redhat.rhn.frontend.dto.SetupWizardProductDto.SyncStatus;
import com.redhat.rhn.manager.setup.ProductSyncManager;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.model.products.Channel;
import com.suse.manager.model.products.MandatoryChannels;
import com.suse.manager.model.products.OptionalChannels;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Tests for ProductSyncManager.
 */
public class ProductSyncManagerTest extends BaseTestCaseWithUser {

    /**
     * Verify product sync status for a given product: NOT_MIRRORED
     * Product is NOT_MIRRORED if any of the channels is not mirrored (= exists in the DB).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusNotMirrored() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("...", channelByLabel);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.NOT_MIRRORED, status.getStage());

        // Repeat test with another product with only one channel not mirrored
        product = createFakeProduct("PP.", channelByLabel);
        status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.NOT_MIRRORED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * All channels are mirrored, but no metadata is there yet and no schedule either.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailed() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * A product is FAILED if there is FAILED tasko runs (even after FINISHED).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailedTasko() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoRun(product, TaskoRun.STATUS_FINISHED);
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FAILED
     * A product is FAILED if there is FINISHED tasko runs, but no metadata.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusFailedNoMetadata() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoRun(product, TaskoRun.STATUS_FINISHED);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is schedules for all mandatory channels.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressScheduled() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoSchedule(product);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
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
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.FAILED, status.getStage());
        // Avoid time collision
        Thread.sleep(1_000);
        productInsertTaskoSchedule(product);
        status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is STATUS_READY_TO_RUN tasko runs.
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressReady() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoRun(product, TaskoRun.STATUS_READY_TO_RUN);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: IN_PROGRESS
     * A product is IN_PROGRESS if there is STATUS_RUNNING tasko runs (even after FAILED).
     *
     * @throws Exception if something goes wrong
     */
    public void testGetProductSyncStatusInProgressRunning() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

        SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
        productInsertTaskoRun(product, TaskoRun.STATUS_FAILED);
        productInsertTaskoRun(product, TaskoRun.STATUS_RUNNING);
        SyncStatus status = getProductSyncStatus(product, channelByLabel);
        assertEquals(SyncStatus.SyncStage.IN_PROGRESS, status.getStage());
    }

    /**
     * Verify product sync status for a given product: FINISHED
     * Product is FINISHED if metadata is available for all mandatory channels.
     *
     * @throws Exception if anything goes wrong
     */
    public void testGetProductSyncStatusFinished() throws Exception {
        Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel = new HashMap<>();

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
            SetupWizardProductDto product = createFakeProduct("PPP", channelByLabel);
            generateFakeMetadataForProduct(product);

            // Check the product sync status
            SyncStatus status = getProductSyncStatus(product, channelByLabel);
            assertEquals(SyncStatus.SyncStage.FINISHED, status.getStage());
            assertNotNull((status.getLastSyncDate()));
        }
        finally {
            // Clean up and restore the old cache path
            FileUtils.deleteDirectory(tempMountPoint);
            Config.get().setString(ConfigDefaults.REPOMD_CACHE_MOUNT_POINT, oldMountPoint);
        }
    }

    public void testCanVerifyVendorChannelConflictsWithCustomChannel() {
        ProductSyncManager manager = new ProductSyncManager();

        final User user = UserTestUtils.findNewUser("testUser", "testOrg");
        ChannelFamily family = getOrCreateChannelFamily();
        String uniquePrefix = RandomStringUtils.randomAlphanumeric(6);

        // Create SUSE sles product
        Map<String, SUSEProduct> suseProductsMap = createSUSEProducts(uniquePrefix, family);
        createSUSEProductSCCRepositories(uniquePrefix, suseProductsMap);

        // Create two conflicting custom channels
        createConflictingChannel(uniquePrefix + "-sles", family);
        createConflictingChannel(uniquePrefix + "-sles-updates", family);
        createConflictingChannel(uniquePrefix + "-sle-containers", family);

        List<Long> productIds = new ArrayList<>();
        suseProductsMap.forEach((name, product) -> productIds.add(product.getProductId()));

        Map<Long, List<String>> conflictsMap = manager.verifyChannelConflicts(productIds);

        assertEquals(2, conflictsMap.size());

        // Conflict for sles
        List<String> slesConflicts = conflictsMap.get(suseProductsMap.get("sles").getId());
        assertNotNull(slesConflicts);
        assertEquals(2, slesConflicts.size());
        assertTrue(slesConflicts.contains(uniquePrefix + "-sles"));
        assertTrue(slesConflicts.contains(uniquePrefix + "-sles-updates"));

        // Conflict for sles
        List<String> containersConflict = conflictsMap.get(suseProductsMap.get("containers").getId());
        assertNotNull(containersConflict);
        assertEquals(1, containersConflict.size());
        assertEquals(uniquePrefix + "-sle-containers", containersConflict.get(0));
    }

    public void testCanRetrieveTreeStructureForProducts() {
        ProductSyncManager manager = new ProductSyncManager();

        ChannelFamily family = getOrCreateChannelFamily();
        String uniquePrefix = RandomStringUtils.randomAlphanumeric(6);

        final Map<String, SUSEProduct> suseProductsMap = createSUSEProducts(uniquePrefix, family);
        createSUSEProductExtensions(suseProductsMap);

        List<Long> productIds = new ArrayList<>();
        suseProductsMap.forEach((name, product) -> productIds.add(product.getProductId()));

        final Map<Long, Long> productTreeMap = manager.getProductTreeMap(productIds);

        var sles = suseProductsMap.get("sles");
        var baseSystem = suseProductsMap.get("baseSystem");
        var appModule = suseProductsMap.get("applications");
        var cntModule = suseProductsMap.get("containers");

        assertNull(productTreeMap.get(sles.getProductId()));
        assertEquals(sles.getId(), (long) productTreeMap.get(baseSystem.getId()));
        assertEquals(baseSystem.getId(), (long) productTreeMap.get(appModule.getId()));
        assertEquals(baseSystem.getId(), (long) productTreeMap.get(cntModule.getId()));
    }

    /**
     * Create fake product with channels as described in channelDesc, e.g. "P..P".
     * For every "P" (= provided) a real channel will be created in the database.
     *
     * @param channelDesc description of a set of channels and their status
     * @param channelByLabel map of channels, indexed by their label
     * @return {@link SetupWizardProductDto} fake product
     * @throws Exception if something goes wrong
     */
    private SetupWizardProductDto createFakeProduct(String channelDesc,
                                                    Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel)
        throws Exception {
        String ident = "product-" + TestUtils.randomString();
        SetupWizardProductDto p = new SetupWizardProductDto(1L, 1L, "x86_64", ident, "Product " + ident, "",
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
                channelByLabel.put(dbChannel.getLabel(), dbChannel);
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
        Map<String, Object> params = new HashMap<>();
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
     * Call the private method getProductSyncStatus() of {@link ProductSyncManager}.
     *
     * @param product the product
     * @return syncStatus the sync status of the product
     */
    private SyncStatus getProductSyncStatus(SetupWizardProductDto product,
                                            Map<String, com.redhat.rhn.domain.channel.Channel> channelByLabel)
        throws Exception {
        Method method = ProductSyncManager.class.getDeclaredMethod(
                "getProductSyncStatus", SetupWizardProductDto.class, Map.class);
        method.setAccessible(true);

        return (SyncStatus) method.invoke(new ProductSyncManager(), new Object[]{product, channelByLabel});
    }

    private void createConflictingChannel(String channel, ChannelFamily family) {
        ChannelArch arch = (ChannelArch) TestUtils.lookupFromCacheById(500L, "ChannelArch.findById");

        try {
            ChannelFactoryTest.createTestChannel(channel + "-conflict", channel, user.getOrg(), arch,
                family);
        }
        catch (Exception ex) {
            fail("Unable to create a custom channel");
        }
    }

    private Map<String, SUSEProduct> createSUSEProducts(String prefix, ChannelFamily family) {
        var sles = createProduct(prefix, "sles", "15.3", "SUSE Linux Enterprise Server", family, true);
        var baseSystem = createProduct(prefix, "sle-basesystem", "15.3", "Basesystem Module", family, false);
        var appModule = createProduct(prefix, "sle-applications", "15.3", "Applications Module", family, false);
        var cntModule = createProduct(prefix, "sle-containers", "15.3", "Containers Module", family, false);

        return Map.of("sles", sles, "baseSystem", baseSystem, "applications", appModule, "containers", cntModule);
    }

    public static void createSUSEProductSCCRepositories(String prefix, Map<String, SUSEProduct> suseProductsMap) {

        var sles = suseProductsMap.get("sles");
        var baseSystem = suseProductsMap.get("baseSystem");
        var appModule = suseProductsMap.get("applications");
        var cntModule = suseProductsMap.get("containers");

        createProductSCCRepository(prefix, sles, sles, "SLES", "sles", null);
        createProductSCCRepository(prefix, sles, sles, "SLES", "sles-updates", null);

        createProductSCCRepository(prefix, baseSystem, sles, "Module-Basesystem", "sle-basesystem", "sles");
        createProductSCCRepository(prefix, appModule, sles, "Module-Applications", "sle-applications", "sles");
        createProductSCCRepository(prefix, cntModule, sles, "Module-Containers", "sle-containers", "sles");
    }

    private static void createSUSEProductExtensions(Map<String, SUSEProduct> suseProductsMap) {

        var sles = suseProductsMap.get("sles");
        var baseSystem = suseProductsMap.get("baseSystem");
        var appModule = suseProductsMap.get("applications");
        var cntModule = suseProductsMap.get("containers");

        createProductExtension(baseSystem, sles, sles);
        createProductExtension(appModule, baseSystem, sles);
        createProductExtension(cntModule, baseSystem, sles);
    }

    private static ChannelFamily getOrCreateChannelFamily() {
        ChannelFamily family = ChannelFamilyFactory.lookupByLabel("7261", null);
        if (family == null) {
            family = new ChannelFamily();
            family.setLabel("7261");
            family.setName("SUSE Linux Enterprise Server");
            TestUtils.saveAndFlush(family);
        }
        return family;
    }

    private static void createProductSCCRepository(String prefix, SUSEProduct product, SUSEProduct root,
                                                                       String channelName, String channelLabel,
                                                                       String rootChannelLabel) {
        var repo = SUSEProductTestUtils.createSCCRepository();

        var productSSCRepo = new SUSEProductSCCRepository();
        productSSCRepo.setProduct(product);
        productSSCRepo.setRootProduct(root);
        productSSCRepo.setRepository(repo);
        productSSCRepo.setChannelLabel(prefix + "-" + channelLabel);
        productSSCRepo.setParentChannelLabel(prefix + "-" + Objects.requireNonNullElse(rootChannelLabel, channelLabel));
        productSSCRepo.setChannelName(channelName);
        productSSCRepo.setMandatory(true);
        TestUtils.saveAndReload(productSSCRepo);
    }

    private static void createProductExtension(SUSEProduct product, SUSEProduct parent, SUSEProduct root) {
        var productExtension = new SUSEProductExtension();
        productExtension.setExtensionProduct(product);
        productExtension.setBaseProduct(parent);
        productExtension.setRootProduct(root);
        productExtension.setRecommended(true);
        TestUtils.saveAndReload(productExtension);
    }

    private static SUSEProduct createProduct(String prefix, String name, String version, String desc,
                                             ChannelFamily family, boolean isBase) {
        SUSEProduct product = new SUSEProduct();
        product.setName(prefix + "-" + name);
        product.setVersion(version);
        product.setFriendlyName(desc);
        product.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        product.setProductId(RandomUtils.nextInt(0, 10000));
        product.setChannelFamily(family);
        product.setBase(isBase);
        product.setReleaseStage(ReleaseStage.released);
        TestUtils.saveAndReload(product);
        return product;
    }
}
