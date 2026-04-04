/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.products.migration;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.manager.webui.controllers.channels.ChannelsUtils;
import com.suse.manager.webui.utils.gson.ChannelsJson;
import com.suse.utils.Lists;
import com.suse.utils.Maps;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Factory to create the API objects for the product migration feature.
 */
public class MigrationDataFactory {

    private final LocalizationService localizer;

    /**
     * Builds an instance of the factory.
     */
    public MigrationDataFactory() {
        this.localizer = LocalizationService.getInstance();
    }

    /**
     * Builds a product migration data from the given pieces of information for the target selection step.
     *
     * @param serverList the list of servers under migration
     * @param sourceProductSet the set of products from which the migration is starting
     * @param targetSets the possible targets of the migration
     *
     * @return an instance of {@link MigrationTargetSelection}
     */
    public MigrationTargetSelection toMigrationTargetSelection(List<MinionServer> serverList,
                                                               Optional<SUSEProductSet> sourceProductSet,
                                                               List<SUSEProductSet> targetSets) {
        // Check if all the servers share the same base product, if the source product was computed
        SUSEProduct sourceBase = sourceProductSet.map(SUSEProductSet::getBaseProduct).orElse(null);
        boolean allCompatible = sourceBase != null && serverList.stream()
            .map(server -> server.getInstalledProductSet().map(SUSEProductSet::getBaseProduct).orElse(null))
            .allMatch(baseProduct -> baseProduct != null && baseProduct.equals(sourceBase));

        var migrationSource = sourceProductSet.map(sourceSet -> toMigrationProduct(sourceSet)).orElse(null);

        var migrationTargets = targetSets.stream()
            .map(targetSet -> toMigrationTarget(targetSet))
            .sorted(Comparator.comparing(targetSet -> targetSet.targetProduct().name()))
            .toList();

        var systemsData = serverList.stream()
            .map(server -> this.toSystemData(server, sourceProductSet.map(SUSEProductSet::getBaseProduct), targetSets))
            .toList();

        return new MigrationTargetSelection(allCompatible, migrationSource, migrationTargets, systemsData);
    }

    /**
     * Builds a product migration data from the previous dry run action
     * @param action the action
     * @param user the user performing the migration
     * @return an instance of {@link MigrationDryRunConfirmation}
     */
    public MigrationDryRunConfirmation toMigrationDryRunConfirmation(DistUpgradeAction action, User user) {

        // Extract the server list from the action
        List<MinionServer> serverList = action.getServerActions().stream()
            .flatMap(serverAction -> serverAction.getServer().asMinionServer().stream())
            .toList();

        // List all the newly subscribed channels
        Set<Channel> newChannels = action.getDetailsMap().values().stream()
            .flatMap(detail -> detail.getChannelTasks().stream())
            .filter(channelTask -> channelTask.getTask() == DistUpgradeChannelTask.SUBSCRIBE)
            .map(DistUpgradeChannelTask::getChannel)
            .collect(Collectors.toSet());

        // AllowVendorChange should be the same for all details, ensure we compute the correct value anyway
        boolean allowVendorChange = action.getDetailsMap().values().stream()
            .map(DistUpgradeActionDetails::isAllowVendorChange)
            .allMatch(BooleanUtils::isTrue);

        Channel targetBaseChannel = findBaseChannel(newChannels);

        // Get name of original base channel if channel is cloned
        String originalName = ChannelManager.getOriginalChannel(targetBaseChannel).getName();
        SUSEProduct targetBaseProduct = SUSEProductFactory.lookupByChannelName(originalName).get(0).getRootProduct();

        // Extract the source product from the first system, they must be matching
        var sourceSet = serverList.get(0).getInstalledProductSet();

        // Compute the targets from the common base, considering only the one that matches the computed target
        var targetSet = DistUpgradeManager.getTargetProductSets(user, serverList, sourceSet).stream()
            .filter(productSet -> targetBaseProduct.getId() == productSet.getBaseProduct().getId())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(""));

        var systemsData = serverList.stream()
            .map(server -> toSystemData(server, sourceSet.map(SUSEProductSet::getBaseProduct), List.of(targetSet)))
            .toList();

        ChannelsJson selectedChannels = ChannelsJson.fromChannelSet(newChannels);

        var targetProduct = toMigrationProduct(targetSet);

        return new MigrationDryRunConfirmation(systemsData, targetProduct, selectedChannels, allowVendorChange);
    }

    private Channel findBaseChannel(Set<Channel> channels) {
        Set<Channel> baseChannelSet = channels.stream()
            .filter(Channel::isBaseChannel)
            .collect(Collectors.toSet());

        if (baseChannelSet.size() != 1) {
            throw new IllegalStateException("Expected exactly one base channel. Got " + baseChannelSet.size());
        }

        return baseChannelSet.iterator().next();
    }

    /**
     * Create a {@link MigrationSystemData} from the information of a system and the source and target products.
     * @param server the system
     * @param sourceProduct the source product of the migration
     * @param migrationTargets the possible targets of the migration
     * @return an instance of {@link MigrationSystemData}
     */
    public MigrationSystemData toSystemData(Server server, Optional<SUSEProduct> sourceProduct,
                                            List<SUSEProductSet> migrationTargets) {

        var migrationProduct = server.getInstalledProductSet()
            .filter(set -> set.getBaseProduct() != null)
            .map(this::toMigrationProduct)
            .orElse(null);

        var eligibility = computeEligibilityOf(server, migrationTargets, sourceProduct);

        return new MigrationSystemData(
            server.getId(),
            server.getName(),
            migrationProduct,
            eligibility.eligible(),
            eligibility.reason(),
            eligibility.details()
        );
    }

    /**
     * Create a {@link MigrationTarget} from a set of products and the servers involved in the migration.
     * @param targetProductSet the product set
     * @return an instance of {@link MigrationTarget}
     */
    public MigrationTarget toMigrationTarget(SUSEProductSet targetProductSet) {
        MigrationProduct targetProduct = toMigrationProduct(targetProductSet);
        List<String> missingChannels = Collections.unmodifiableList(targetProductSet.getMissingChannels());

        // Get the unique serialized id of the product set
        String serializedId = targetProductSet.getSerializedProductIDs();

        return new MigrationTarget(serializedId, targetProduct, missingChannels);
    }

    /**
     * Create a {@link MigrationProduct} from a set of products.
     * @param productSet the product set
     * @return an instance of {@link MigrationProduct}
     */
    public MigrationProduct toMigrationProduct(SUSEProductSet productSet) {

        var extensionsMap = SUSEProductFactory.allExtensionsOfRoot(productSet.getBaseProduct())
            .stream()
            .collect(Collectors.toMap(
                ext -> ext.getBaseProduct().getProductId(),
                ext -> List.of(ext.getExtensionProduct().getProductId()),
                Lists::union
            ));

        var addonsMap = productSet.getAddonProducts().stream()
            .collect(Collectors.toMap(SUSEProduct::getProductId, Function.identity()));

        return toMigrationProduct(productSet.getBaseProduct(), addonsMap, extensionsMap);
    }

    /**
     * Converts a {@link MigrationProduct} to a {@link SUSEProductSet}
     * @param migrationProduct the migration product
     * @return an instance of {@link SUSEProductSet} describing the given product
     */
    public SUSEProductSet toSUSEProductSet(MigrationProduct migrationProduct) {
        // Retrieve the base product
        SUSEProduct baseProduct = SUSEProductFactory.lookupByProductId(migrationProduct.id());

        // Process all the addons in the migration product, recursively
        List<SUSEProduct> addons = new ArrayList<>();
        lookupAddons(addons, migrationProduct.addons());

        return new SUSEProductSet(baseProduct, addons);
    }

    private static void lookupAddons(List<SUSEProduct> suseAddons, List<MigrationProduct> addonsProduct) {
        if (CollectionUtils.isEmpty(addonsProduct)) {
            return;
        }

        for (MigrationProduct product : addonsProduct) {
            suseAddons.add(SUSEProductFactory.lookupByProductId(product.id()));
            lookupAddons(suseAddons, product.addons());
        }
    }

    /**
     * Extract the list of channel ids from a {@link ChannelsJson} instance
     * @param channelsJson the channels
     * @return the list of ids of all the channels described by the given instance.
     */
    public List<Long> toChannelIds(ChannelsJson channelsJson) {
        List<Long> result = new ArrayList<>();

        result.add(channelsJson.getBase().getId());
        channelsJson.getChildren().forEach(child -> result.add(child.getId()));

        return result;
    }

    /**
     * Evaluate if the given server is eligible for a product migration from the current system base product.
     * @param server the server to check.
     * @param migrationTargets the possible migration targets
     * @return the migration eligibility descriptor for the given system.
     */
    public MigrationEligibility computeEligibilityOf(Server server, List<SUSEProductSet> migrationTargets) {
        var baseProduct = server.getInstalledProductSet().map(SUSEProductSet::getBaseProduct);

        return computeEligibilityOf(server, migrationTargets, baseProduct);
    }
    /**
     * Evaluate if the given server is eligible for a product migration from the given base product.
     * @param server the server to check.
     * @param migrationTargets the possible migration targets
     * @param baseProduct the base product, source of the migration.
     * @return the migration eligibility descriptor for the given system.
     */
    public MigrationEligibility computeEligibilityOf(Server server, List<SUSEProductSet> migrationTargets,
                                                     Optional<SUSEProduct> baseProduct) {
        // Ensure the os family is supported
        String osFamily = server.getOsFamily();
        if (!osFamily.equals("Suse") && !osFamily.equals("RedHat")) {
            return new MigrationEligibility(server.getId(), false,
                localizer.getMessage("product_migration.os_not_supported"));
        }

        // Extract what's installed on the system
        Optional<SUSEProductSet> installedProductSet = server.getInstalledProductSet();
        Optional<SUSEProduct> systemBaseProduct = installedProductSet.map(SUSEProductSet::getBaseProduct);

        // Check if the base product is known
        if (baseProduct.isEmpty() || systemBaseProduct.isEmpty()) {
            return new MigrationEligibility(server.getId(), false,
                localizer.getMessage("product_migration.source_unknown"));
        }

        // Check if the base product matches with the other systems
        if (!systemBaseProduct.equals(baseProduct)) {
            return new MigrationEligibility(server.getId(), false,
                localizer.getMessage("product_migration.source_mismatch"));
        }

        // Check if the salt package on the minion is up to date
        String saltPackage = "salt";
        if (PackageFactory.lookupByNameAndServer("venv-salt-minion", server) != null) {
            saltPackage = "venv-salt-minion";
        }

        if (PackageManager.getServerNeededUpdatePackageByName(server.getId(), saltPackage) != null) {
            return new MigrationEligibility(server.getId(), false,
                localizer.getMessage("product_migration.salt_needs_update"),
                localizer.getMessage("product_migration.salt_needs_upadate.details", saltPackage));
        }

        Set<SUSEProduct> missingSuccessors = new HashSet<>();
        DistUpgradeManager.removeIncompatibleTargets(installedProductSet, migrationTargets, missingSuccessors);

        var installedAddons = DistUpgradeManager.listInstalledTargetAddons(installedProductSet, migrationTargets);

        if (!missingSuccessors.isEmpty() || !installedAddons.isEmpty()) {
            String prefix = migrationTargets.size() > 1 ?  "product_migration" : "product_migration.single";
            return new MigrationEligibility(server.getId(), true,
                localizer.getMessage(prefix + ".addons_affected"),
                getAffectedAddonsDetail(installedAddons, missingSuccessors, prefix));
        }

        return new MigrationEligibility(server.getId(), true, null);
    }

    /**
     * Builds the target channels data from the given pieces of information of this product migration.
     * @param serverList the list of servers to migrate
     * @param user the user performing the migration
     * @param target the target of the migration
     * @param source the source of the migration
     * @return the {@link MigrationChannelsSelection} describing the selectable channels for the migration
     */
    public MigrationChannelsSelection toMigrationChannelsSelection(List<MinionServer> serverList, User user,
                                                                   SUSEProductSet target, SUSEProductSet source) {

        ChannelArch commonArch = serverList.stream()
            .map(server -> server.getServerArch().getCompatibleChannelArch())
            .distinct()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Unable to extract common server architecture"));

        // Get the base channel and its mandatory channels
        var baseChannel = DistUpgradeManager.getProductBaseChannel(target.getBaseProduct().getId(), commonArch, user);
        List<Long> baseMandatoryChannels = DistUpgradeManager.getRequiredChannels(target, baseChannel.getId()).stream()
            .map(EssentialChannelDto::getId)
            .toList();

        // Compute all the alternatives from existing cloned channel
        SortedMap<ClonedChannel, List<Long>> clonesMap = DistUpgradeManager.getAlternatives(target, commonArch, user);

        Map<Long, List<Long>> mandatoryMap = new HashMap<>();
        mandatoryMap.put(baseChannel.getId(), baseMandatoryChannels);
        clonesMap.forEach((key, value) -> mandatoryMap.put(key.getId(), value));

        var possibleChannels = Stream.concat(Stream.of(baseChannel), clonesMap.keySet().stream())
            .map(channel -> ChannelsUtils.generateChannelJson(channel, user))
            .toList();

        // Compute the mandatory flag for all the children
        Stream.concat(Stream.of(baseChannel), clonesMap.keySet().stream())
            .flatMap(channel -> channel.getAccessibleChildrenFor(user).stream())
            .distinct()
            .forEach(channel -> {
                List<Long> mandatory = SUSEProductFactory.findSyncedMandatoryChannels(channel.getLabel())
                    .map(Channel::getId)
                    .toList();

                mandatoryMap.put(channel.getId(), mandatory);
            });

        var systemsData = serverList.stream()
            .map(server -> this.toSystemData(server, Optional.of(source.getBaseProduct()), List.of(target)))
            .toList();

        return new MigrationChannelsSelection(
            possibleChannels,
            Maps.mapToEntryList(mandatoryMap),
            Maps.mapToEntryList(Maps.invertMultimap(mandatoryMap)),
            systemsData
        );
    }

    private String getAffectedAddonsDetail(Set<SUSEProduct> installedAddons, Set<SUSEProduct> missingSuccessors,
                                           String prefix) {
        StringBuilder result = new StringBuilder();

        if (!installedAddons.isEmpty()) {
            StringJoiner listBuilder = new StringJoiner("</li><li>", "<ul><li>", "</li></ul>");

            installedAddons.stream()
                // Aggregate what's going to be installed by internal name
                .collect(Collectors.groupingBy(SUSEProduct::getName))
                .forEach((name, variants) -> {
                    if (variants.size() == 1) {
                        // If there is only one, just add it
                        listBuilder.add(variants.get(0).getFriendlyName());
                    }
                    else {
                        // If there are multiple with different versions, just show "lowest product or higher"
                        variants.stream()
                            .min(DistUpgradeManager.PRODUCT_VERSION_COMPARATOR)
                            .map(SUSEProduct::getFriendlyName)
                            .map(product -> localizer.getMessage("product_migration.addon_or_higher", product))
                            .ifPresent(listBuilder::add);
                    }
                });

            result.append(
                localizer.getMessage(prefix + ".addons_affected.installed_addons", listBuilder.toString())
            );
        }

        if (!missingSuccessors.isEmpty()) {
            StringJoiner listBuilder = new StringJoiner("</li><li>", "<ul><li>", "</li></ul>");
            missingSuccessors.stream().map(SUSEProduct::getFriendlyName)
                .forEach(listBuilder::add);

            result.append(
                localizer.getMessage(prefix + ".addons_affected.missing_successors", listBuilder.toString())
            );
        }

        return result.toString();
    }

    private static MigrationProduct toMigrationProduct(SUSEProduct product, Map<Long, SUSEProduct> addonsMap,
                                                       Map<Long, List<Long>> extensionsMap) {
        List<MigrationProduct> extensions = extensionsMap.getOrDefault(product.getProductId(), List.of()).stream()
            .flatMap(addonId -> Optional.ofNullable(addonsMap.get(addonId)).stream())
            .map(addonProduct -> toMigrationProduct(addonProduct, addonsMap, extensionsMap))
            .toList();

        return new MigrationProduct(product.getProductId(), product.getFriendlyName(), extensions);
    }

}
