/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.impl.channel.software;

import static com.redhat.rhn.common.ErrorReportingStrategies.logReportingStrategy;
import static com.redhat.rhn.common.ErrorReportingStrategies.rpcValidationReportingStrategy;
import static java.util.Collections.emptySet;

import com.redhat.rhn.common.UyuniErrorReport;
import com.redhat.rhn.common.messaging.MessageQueueHolder;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.SyncFromSourceErrataEvent;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;

import com.suse.spec.channel.software.SyncFromSourceService;
import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of SyncFromSourceService that syncs erratas and/or packages from source channel to target channel.
 */
public class SyncFromSourceServiceImpl implements SyncFromSourceService {

    private static final Logger LOG = LogManager.getLogger(SyncFromSourceServiceImpl.class);

    @Override
    public SyncResponse sync(
            User user,
            String sourceChannelLabel,
            String targetChannelLabel,
            SyncRequest request
    ) {
        // Acquire data and validate
        UyuniErrorReport errorReport = ChannelSoftwareValidationHelper.validateRequestFields(
                targetChannelLabel, sourceChannelLabel, request, true
        );
        errorReport.report(logReportingStrategy(this));
        errorReport.report(rpcValidationReportingStrategy());

        // Validate channels and permissions
        Channel sourceChannel = ChannelSoftwareValidationHelper.validateAndLookupChannel(user, sourceChannelLabel);
        Channel targetChannel = ChannelSoftwareValidationHelper.validateAndLookupChannel(user, targetChannelLabel);
        ChannelSoftwareValidationHelper.validateUserHasPermission(user, targetChannel);

        // Reschedule if async
        if (request.async()) {
            SyncFromSourceErrataEvent syncFromSourceErrataEvent = new SyncFromSourceErrataEvent(
                    user, sourceChannelLabel, targetChannelLabel, request
            );
            MessageQueueHolder.publish(syncFromSourceErrataEvent);
            return new SyncResponse(emptySet(), emptySet());
        }

        // Actual execution
        Set<Errata> erratas = emptySet();
        Set<Package> packages = emptySet();

        // All erratas from source that match the search criteria, regardless of whether they already exist in target
        // Set null later to consider all
        Set<Errata> matchingErratas = ErrataFactory.lookupErrataByChannel(
                    sourceChannel,
                    request.criteria().advisoryNames(),
                    request.criteria().startDate(),
                    request.criteria().endDate()
        );

        if (request.operation().includesErratas()) {
            erratas = syncErratas(user, sourceChannel, targetChannel, request, matchingErratas);
        }

        if (request.operation().includesPackages()) {
            packages = syncPackages(sourceChannel, targetChannel, request, matchingErratas);
        }

        return new SyncResponse(erratas, packages);
    }

    /**
     * Sync erratas from source to target channel.
     * Returns set of synced erratas
     */
    private Set<Errata> syncErratas(
            User user, Channel sourceChannel, Channel targetChannel, SyncRequest request, Set<Errata> matchingErratas
    ) {
        LOG.debug("Syncing erratas from {} to {}", sourceChannel.getLabel(), targetChannel.getLabel());

        // Filter out erratas already in target
        Set<Errata> erratasToSync =
                ErrataManager.filterErrataRequiringMerge(matchingErratas, sourceChannel, targetChannel);

        if (erratasToSync.isEmpty()) {
            LOG.debug("No erratas to sync");
            return emptySet();
        }

        Set<Long> errataIds = erratasToSync.stream().map(Errata::getId).collect(Collectors.toSet());
        ErrataManager.cloneErrata(targetChannel.getId(), errataIds, request.forceRefresh(), user);
        return erratasToSync;
    }

    /**
     * Sync packages from source to target channel.
     * For ERRATA_AND_PACKAGES mode, only syncs packages from the given erratas.
     * For PACKAGES_ONLY mode, syncs all packages from the source channel.
     */
    private Set<Package> syncPackages(
            Channel sourceChannel, Channel targetChannel, SyncRequest request, Set<Errata> matchingErratas
    ) {
        LOG.debug("Syncing packages from {} to {}", sourceChannel.getLabel(), targetChannel.getLabel());

        Set<Package> packagesToSync = getPackagesToSync(request, matchingErratas, sourceChannel);

        // Exclude the packages that are already in target
        packagesToSync.removeAll(targetChannel.getPackages());

        if (packagesToSync.isEmpty()) {
            LOG.debug("No packages to sync");
            return emptySet();
        }

        targetChannel.addPackages(packagesToSync);
        ChannelFactory.save(targetChannel);
        ChannelManager.refreshWithNewestPackages(targetChannel, "SyncFromSourceService");

        if (request.alignModules() && sourceChannel.isModular()) {
            LOG.debug("Aligning modular metadata");
            targetChannel.cloneModulesFrom(sourceChannel);
        }

        if (request.forceRefresh()) {
            List<Long> packagesToSyncIds = packagesToSync.stream().map(Package::getId).toList();
            ErrataCacheManager.insertCacheForChannelPackagesAsync(List.of(targetChannel.getId()), packagesToSyncIds);
        }

        return packagesToSync;
    }


    /**
     * Returns what packages to sync.
     */
    private Set<Package> getPackagesToSync(SyncRequest request, Set<Errata> matchingErratas, Channel sourceChannel) {
        // If no filters were applied, consider all packages on source channel
        if (!request.criteria().hasFilters()) {
            return new HashSet<>(sourceChannel.getPackages());
        }

        // Otherwise return the packages associated with matching erratas
        return matchingErratas.stream()
                .flatMap(e -> e.getPackages().stream())
                .collect(Collectors.toSet());
    }

}
