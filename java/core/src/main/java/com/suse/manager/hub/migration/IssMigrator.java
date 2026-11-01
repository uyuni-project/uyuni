/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.hub.migration;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.iss.IssSlave;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.hub.HubManager;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.migration.MigrationItem;
import com.suse.manager.model.hub.migration.MigrationMessageLevel;
import com.suse.manager.model.hub.migration.MigrationResult;
import com.suse.manager.model.hub.migration.MigrationResultCode;
import com.suse.manager.model.hub.migration.SlaveMigrationData;
import com.suse.utils.CustomCollectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IssMigrator {

    private static final Logger LOGGER = LogManager.getLogger(IssMigrator.class);

    private static final LocalizationService LOC = LocalizationService.getInstance();

    private final HubManager hubManager;

    private final User user;

    private Map<String, Channel> localChannelsMap;

    private MigrationResult result;

    /**
     * Default constructor
     * @param userIn the user performing the migration
     */
    public IssMigrator(User userIn) {
        this(new HubManager(), userIn);
    }

    /**
     * Constractor with explicit dependencies for testing
     * @param hubManagerIn the hub manager to perform the operations
     * @param userIn the user performing the migration
     * @throws PermissionException if the user is not a {@link RoleFactory#SAT_ADMIN}.
     */
    public IssMigrator(HubManager hubManagerIn, User userIn) {
        this.hubManager = hubManagerIn;
        this.user = userIn;

        // These will be initialized at the start of the migration process
        this.result = null;
        this.localChannelsMap = null;

        // ensure the user has the correct role
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            throw new PermissionException(RoleFactory.SAT_ADMIN);
        }
    }

    /**
     * Migrate ISSv1 slaves and configure them as peripheral servers
     * @param migrationDataMap a map containing the mandatory information for migrating an existing slave
     * @return the migration result
     */
    public MigrationResult migrateFromV1(Map<String, SlaveMigrationData> migrationDataMap) {
        // initialize the result
        result = new MigrationResult();
        // Load and cache all the available channels
        localChannelsMap = ChannelFactory.listAllChannels().stream()
            .collect(Collectors.toMap(Channel::getLabel, Function.identity()));
        // Load and cache all the ISS Slaves currently configured
        Map<String, IssSlave> slavesMap = IssFactory.listAllIssSlaves().stream()
            .collect(Collectors.toMap(IssSlave::getSlave, Function.identity()));

        if (slavesMap.isEmpty()) {
            LOGGER.warn("This server does not have any register slaves, nothing to migrate");

            result.setResultCode(MigrationResultCode.FAILURE);
            result.addMessage(MigrationMessageLevel.ERROR, LOC.getMessage("hub.migration.no_slaves"));

            return result;
        }

        // Check if some of the specified migration data does not match with the existing slaves
        migrationDataMap.keySet().stream()
            .filter(fqdn -> !slavesMap.containsKey(fqdn))
            .forEach(fqdn -> {
                LOGGER.warn("Migration data for {} is invalid, such slave does not exist", fqdn);
                result.addMessage(MigrationMessageLevel.WARN, LOC.getMessage("hub.migration.slave_not_exit", fqdn));
            });

        // The migration item is immutable so each step returns the updated version
        List<MigrationItem> migratedItems = slavesMap.values().stream()
            // Step 1 - Create a migration item from the slave
            .map(slave -> createMigrationItem(migrationDataMap, slave))
            // Step 2 - Exclude the items that are not part of the migration data
            .filter(item -> migrationDataAvailable(item))
            // Step 3 - Register the slave as peripheral
            .map(item -> registerSlaveAsPeripheral(item))
            // Step 4 - Configure the channels comparing what the peripheral has with the hub
            .map(item -> setupChannelConfiguration(item))
            // Last step - Cleanup the data that is no longer needed
            .map(item -> cleanupDanglingData(item))
            // Collect all the migrated items for determining the final result
            .toList();

        // Adjust the result code
        result.setResultCode(computeResultCode(migratedItems));

        return result;
    }

    /**
     * Migrate ISSv2 peripherals and configure them as ISSv3 peripheral servers
     * @param migrationData a map containing the mandatory information for migrating an existing ISSv2 peripheral
     * @return the migration result
     */
    public MigrationResult migrateFromV2(List<SlaveMigrationData> migrationData) {
        // initialize the result
        result = new MigrationResult();

        // Load and cache all the available channels
        localChannelsMap = ChannelFactory.listAllChannels().stream()
            .collect(Collectors.toMap(Channel::getLabel, Function.identity()));

        // The migration item is immutable so each step returns the updated version
        List<MigrationItem> migratedItems = migrationData.stream()
            // Step 1 - Create a migration item from the ISSv2 peripheral
            .map(data -> new MigrationItem(data))
            // Step 2 - Register as ISSv3 peripheral
            .map(item -> registerPeripheral(item))
            // Step 4 - Configure the channels comparing what the peripheral has with the hub
            .map(item -> setupChannelConfiguration(item))
            // Last step - Remove the peripheral if the migration failed
            .map(item -> item.ifFailed(this::rollbackPeripheral))
            // Collect all the migrated items for determining the final result
            .toList();

        // Adjust the result code
        result.setResultCode(computeResultCode(migratedItems));

        return result;
    }

    private static MigrationItem createMigrationItem(Map<String, SlaveMigrationData> migrationDataMap, IssSlave slave) {
        LOGGER.debug(
            "Creating item for slave {} (migration data available: {})",
            () -> slave, () -> migrationDataMap.containsKey(slave.getSlave())
        );

        return new MigrationItem(slave, migrationDataMap.get(slave.getSlave()));
    }

    private boolean migrationDataAvailable(MigrationItem item) {
        if (item.migrationData() == null) {
            LOGGER.info("Migration data is missing for slave {}. It will be skipped.", item.slave());

            String message = LOC.getMessage("hub.migration.no_migration_data", item.slave().getSlave());
            result.addMessage(MigrationMessageLevel.INFO, message);

            return false;
        }

        return true;
    }

    private MigrationItem registerSlaveAsPeripheral(MigrationItem item) {
        // Do not migrate the slave if it is disabled
        if (!"Y".equals(item.slave().getEnabled())) {
            LOGGER.info("Slave {} is disabled. It will be skipped.", item.slave());

            String message = LOC.getMessage("hub.migration.slave_disabled", item.slave().getSlave());
            result.addMessage(MigrationMessageLevel.WARN, message);

            return item.fail();
        }

        return registerPeripheral(item);
    }

    private MigrationItem registerPeripheral(MigrationItem item) {
        try {
            LOGGER.debug("Registering {} as peripheral", item.slave());
            var peripheral = hubManager.register(user, item.fqdn(), item.token(), item.rootCA());
            return item.withPeripheral(peripheral);
        }
        catch (Exception ex) {
            LOGGER.error("Unable to register server {}", item.fqdn(), ex);

            String message = LOC.getMessage("hub.migration.unable_to_migrate", item.fqdn(), ex.getLocalizedMessage());
            result.addMessage(MigrationMessageLevel.ERROR, message);

            return item.fail();
        }
    }

    private MigrationItem setupChannelConfiguration(MigrationItem item) {
        // Nothing to do if the item is already failed in previous steps
        if (!item.success()) {
            return item;
        }

        try {
            List<ChannelInfoJson> peripheralChannelsInfo = hubManager.getAllPeripheralChannels(user, item.fqdn());

            // Extract only the channels that also exist on this hub server, group by them by organization and create
            // a map id -> list of channel labels
            Map<Long, List<String>> matchingChannelLabelsByOrganization = peripheralChannelsInfo.stream()
                .filter(channelInfo -> localChannelsMap.containsKey(channelInfo.getLabel()))
                .collect(CustomCollectors.nullSafeGroupingBy(ChannelInfoJson::getOrgId, ChannelInfoJson::getLabel));

            LOGGER.debug("For peripheral {} these channels are available on this hub and will be added for sync: {}",
                () -> item.fqdn(), () -> matchingChannelLabelsByOrganization);

            // For each entry in the map, configure the peripheral channel
            matchingChannelLabelsByOrganization.forEach((orgId, channelLabels) ->
                hubManager.addPeripheralChannelsToSync(user, item.fqdn(), channelLabels, orgId)
            );

            // Now sync the channels to the peripherals
            hubManager.syncPeripheralChannels(user, item.fqdn());
        }
        catch (Exception ex) {
            LOGGER.error("Unable to configure channels for peripheral {}", item.peripheral(), ex);

            String message = LOC.getMessage("hub.migration.fail_channel_config", item.fqdn(), ex.getLocalizedMessage());
            result.addMessage(MigrationMessageLevel.ERROR, message);

            return item.fail();
        }

        return item;
    }

    private MigrationItem cleanupDanglingData(MigrationItem item) {
        // Remove the ISSv1 data if the item was successfully migrated
        if (item.success()) {
            return removeIssConfiguration(item);
        }

        // The item was not migrated, rollback what was created during the process
        return rollbackPeripheral(item);
    }

    private static MigrationResultCode computeResultCode(List<MigrationItem> migratedItems) {
        long successCount = migratedItems.stream().filter(MigrationItem::success).count();
        if (successCount == migratedItems.size()) {
            return MigrationResultCode.SUCCESS;
        }

        if (successCount == 0) {
            return MigrationResultCode.FAILURE;
        }

        return MigrationResultCode.PARTIAL;
    }

    private MigrationItem removeIssConfiguration(MigrationItem item) {
        try {
            LOGGER.debug("Slave {} was migrated to a peripheral. Removing ISSv1 configuration", item.slave());
            hubManager.deleteIssV1Slave(user, item.fqdn(), false);
        }
        catch (Exception ex) {
            LOGGER.error("Unable to remove ISSv1 data for {}", item.slave(), ex);

            String message = LOC.getMessage("hub.migration.removal_slave", item.fqdn(), ex.getLocalizedMessage());
            result.addMessage(MigrationMessageLevel.ERROR, message);
        }

        return item;
    }

    private MigrationItem rollbackPeripheral(MigrationItem item) {
        if (item.peripheral() == null) {
            // The peripheral was not created, nothing to rollback
            return item;
        }

        try {
            LOGGER.debug("Peripheral {} was not correctly migrated. Deregistering.", item.peripheral());
            hubManager.deregister(user, item.fqdn(), IssRole.PERIPHERAL, false);
        }
        catch (Exception ex) {
            LOGGER.error("Unable to deregister peripheral {}", item.peripheral(), ex);

            String message = LOC.getMessage("hub.migration.removal_peripheral", item.fqdn(), ex.getLocalizedMessage());
            result.addMessage(MigrationMessageLevel.ERROR, message);
        }

        return item;
    }
}
