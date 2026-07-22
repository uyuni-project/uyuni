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
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.messaging.MessageQueueHolder;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.frontend.events.SyncFromOriginalErrataEvent;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.impl.channel.software.helper.ErrataResolver;
import com.suse.spec.channel.software.SyncFromOriginalService;
import com.suse.spec.channel.software.dto.SyncRequest;
import com.suse.spec.channel.software.dto.SyncResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of SyncFromOriginalService that clones erratas from original channels
 * to cloned channels with package inheritance.
 */
public class SyncFromOriginalServiceImpl implements SyncFromOriginalService {

    private static final Logger LOG = LogManager.getLogger(SyncFromOriginalServiceImpl.class);

    @Override
    public SyncResponse sync(User user, String targetChannelLabel, SyncRequest request) {
        // Acquire data and validate
        UyuniErrorReport errorReport = ChannelSoftwareValidationHelper.validateRequestFields(
                targetChannelLabel, null, request, false
        );
        errorReport.report(logReportingStrategy(this));
        errorReport.report(rpcValidationReportingStrategy());

        // Validate target channel and permissions
        Channel targetChannel = ChannelSoftwareValidationHelper.validateAndLookupChannel(user, targetChannelLabel);
        ChannelSoftwareValidationHelper.validateChannelIsCloned(targetChannel);
        ChannelSoftwareValidationHelper.validateUserHasPermission(user, targetChannel);

        // Get and validate original channel
        Channel originalChannel = ChannelFactory.lookupOriginalChannel(targetChannel);
        ChannelSoftwareValidationHelper.validateOriginalChannelAccessible(originalChannel, targetChannelLabel);

        // Reschedule if async
        if (request.async()) {
            SyncFromOriginalErrataEvent event = new SyncFromOriginalErrataEvent(
                    user, targetChannelLabel, request
            );
            MessageQueueHolder.publish(event);
            return new SyncResponse(emptySet(), emptySet());
        }

        // Synchronous execution
        Set<Errata> erratas = emptySet();
        Set<Package> packages = emptySet();

        // Check for pending async jobs
        ChannelSoftwareValidationHelper.validateChannelHasNoPendingAsyncCloneJobs(targetChannel);

        // Resolve matching erratas in original channel based on criteria
        Set<Errata> errataToClone = ErrataResolver.resolveFromCascadingOrg(
                originalChannel, user.getOrg(),
                request.criteria().advisoryNames(),
                request.criteria().startDate(),
                request.criteria().endDate()
        );

        if (errataToClone.isEmpty()) {
            LOG.debug("No erratas to sync");
            return new SyncResponse(erratas, packages);
        }

        if (request.operation().includesErratas()) {
            // Clone erratas, reusing existing clones
            erratas = ErrataManager.cloneErrataForOrg(errataToClone, user.getOrg());

            // Link erratas to channel
            ErrataManager.linkErrataToChannel(erratas, targetChannel);
        }

        if (request.operation().includesPackages()) {
            packages = syncPackages(errataToClone, targetChannel, originalChannel, user, request.forceRefresh());
        }

        return new SyncResponse(erratas, packages);
    }

    /**
     * Adds packages to a channel for given errata using original channel hierarchy.
     * Uses listErrataChannelPacks strategy (all packages from original channel).
     * Handles the package ↔ channel relationship and creates ErrataFile records.
     *
     * @param erratas        Set of errata to process packages for
     * @param channel        Target channel (must be a cloned channel)
     * @param original       Original channel to lookup packages from
     * @param user           User performing the operation
     * @param forceRefresh   Refresh the channel with newest packages
     * @return the Set of cloned packages
     */
    private Set<Package> syncPackages(
        Set<Errata> erratas, Channel channel, Channel original, User user, boolean forceRefresh
    ) {
        Set<Package> packagesToSync = new HashSet<>();

        for (Errata errata : erratas) {
            Set<Package> packagesToPush = new HashSet<>();

            // Traverse clone hierarchy to find original channel with errata
            Channel origChannel = original;
            Set<Channel> associatedChannels = errata.getChannels();
            while (origChannel.isCloned() && !associatedChannels.contains(origChannel)) {
                origChannel = ChannelFactory.lookupOriginalChannel(origChannel);
            }

            DataResult<PackageOverview> packs = ErrataManager.listErrataChannelPacks(origChannel, errata, user);

            for (PackageOverview packOver : packs) {
                Package pack = PackageFactory.lookupByIdAndUser(packOver.getId(), user);
                packagesToPush.add(pack);
            }

            ErrataManager.addPackagesToChannel(channel, packagesToPush, errata, user);
            packagesToSync.addAll(packagesToPush);
        }

        ChannelFactory.save(channel);
        if (forceRefresh) {
            ChannelManager.refreshWithNewestPackages(channel, "java::syncErratasAndPackagesFromOriginal");
        }
        return packagesToSync;
    }


}
