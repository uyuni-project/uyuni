/*
 * Copyright (c) 2026 SUSE LLC
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
package com.redhat.rhn.manager.errata.syncwithpackages;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.SyncErrataWithPackagesEvent;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;
import com.redhat.rhn.manager.user.UserManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrates the errata synchronization: validates the request, then synchronizes or schedules the synchronization.
 */
public class SyncErrataWithPackagesManagerImpl implements SyncErrataWithPackagesManager {

    /** Maximum number of unmatched advisory names listed in the response or in the strict mode fault. */
    private static final int MAX_MISMATCH_OUTPUT = 20;

    @Override
    public SyncErrataWithPackagesResult syncFromSource(
            User user,
            String sourceLabel,
            String targetLabel,
            List<String> advisoryNames,
            boolean strict,
            boolean async,
            boolean forceRefresh
    ) {
        Channel target = lookupTarget(user, targetLabel);
        Channel source = lookupChannel(user, sourceLabel);
        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findInSource(source, advisoryNames);
        List<String> unmatched = unmatched(advisoryNames, found);
        validate(target, found, unmatched, strict);
        return dispatch(user, target, found, unmatched, async, forceRefresh);
    }

    @Override
    public SyncErrataWithPackagesResult syncFromSource(
            User user,
            String sourceLabel,
            String targetLabel,
            Date startDate,
            Date endDate,
            boolean async,
            boolean forceRefresh
    ) {
        Channel target = lookupTarget(user, targetLabel);
        Channel source = lookupChannel(user, sourceLabel);
        if (endDate.before(startDate)) {
            throw new ValidationException("End date cannot be before start date");
        }
        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findInSource(source, startDate, endDate);
        validate(target, found, List.of(), true);
        return dispatch(user, target, found, List.of(), async, forceRefresh);
    }

    @Override
    public SyncErrataWithPackagesResult syncByCriteria(
            User user,
            String targetLabel,
            List<String> advisoryNames,
            boolean strict,
            boolean async,
            boolean forceRefresh
    ) {
        Channel target = lookupTarget(user, targetLabel);
        if (advisoryNames.isEmpty()) {
            throw new InvalidParameterException("advisoryNames must not be empty");
        }
        Map<Errata, Channel> found = SyncErrataWithPackagesErrataHelper.findByCriteria(user, target, advisoryNames);
        List<String> unmatched = unmatched(advisoryNames, found);
        validate(target, found, unmatched, strict);
        return dispatch(user, target, found, unmatched, async, forceRefresh);
    }

    /**
     * Synchronizes errata of interest and their packages.
     * Assumes the request was already validated by one of:
     * - {@link SyncErrataWithPackagesManagerImpl#syncFromSource(User, String, String, Date, Date, boolean, boolean)}
     * - {@link SyncErrataWithPackagesManagerImpl#syncFromSource(User, String, String, List, boolean, boolean, boolean)}
     * - {@link SyncErrataWithPackagesManagerImpl#syncByCriteria(User, String, List, boolean, boolean, boolean)}
     * and does not check it again.
     * @param user the user
     * @param target the target channel
     * @param errata the errata of interest mapped to their source channel (null when found without one)
     * @param forceRefresh refresh the newest package cache and schedule repodata regeneration when the target
     * channel changed
     * @return the synchronized errata
     */
    public List<SyncedErratumWithPackages> syncValidated(
            User user,
            Channel target,
            Map<Errata, Channel> errata,
            boolean forceRefresh
    ) {
        // let's avoid deadlocks, as in ErrataManager.cloneErrata
        ChannelFactory.lock(target);

        List<SyncedErratumWithPackages> synced = new ArrayList<>();
        boolean targetChanged = false;
        for (Map.Entry<Errata, Channel> entry : errata.entrySet()) {
            Errata erratum = entry.getKey();
            Channel source = entry.getValue();

            // Choosing the representative erratum: one the target already holds, otherwise one owned by its org
            Optional<Errata> inTarget =
                    SyncErrataWithPackagesErrataHelper.findRepresentativeErrataInTarget(target, erratum);
            Errata representative = inTarget.orElseGet(() ->
                    SyncErrataWithPackagesErrataHelper.getRepresentativeErrataOwnedBy(target.getOrg(), erratum));

            // Bringing in the packages: add to the representative the packages of the erratum it is missing,
            // then get the ones the target serves: those the source also has (all of them when there is no source)
            List<Package> addedPackages = erratum.getPackages().stream()
                    .filter(p -> !representative.getPackages().contains(p))
                    .toList();
            addedPackages.forEach(representative::addPackage);
            List<Package> served = erratum.getPackages().stream()
                    .filter(p -> source == null || source.getPackages().contains(p))
                    .toList();

            // link only when something is missing, so a repeated request changes nothing (idempotent);
            // linking only adds: packages already in the target are never removed
            boolean missing = !addedPackages.isEmpty() || !target.getPackages().containsAll(served);
            if (inTarget.isEmpty() || missing) {
                // link the representative and its served packages, then queue an errata notification
                ErrataFactory.addToChannel(representative, target, user, new HashSet<>(served), false);
                ErrataManager.replaceChannelNotifications(representative.getId(), target.getId(), new Date());
                targetChanged = true;
            }
            synced.add(new SyncedErratumWithPackages(representative, served));
        }

        // forceRefresh: newest-package cache and repodata regeneration, only when the target changed
        if (targetChanged && forceRefresh) {
            ChannelManager.refreshWithNewestPackages(target, "java::syncErrataWithPackages");
        }
        return synced;
    }

    /**
     * Synchronizes the validated errata or schedules their asynchronous synchronization, and builds the response.
     * @param user the user
     * @param target the target channel
     * @param found the errata of interest mapped to their source channel (null when found without one)
     * @param unmatched the unmatched advisory names
     * @param async schedule the synchronization in the background instead of running it now
     * @param forceRefresh refresh the newest package cache and schedule repodata regeneration when the target
     * channel changed
     * @return the response: the synchronized errata (empty when async) and the unmatched report
     */
    private SyncErrataWithPackagesResult dispatch(
            User user,
            Channel target,
            Map<Errata, Channel> found,
            List<String> unmatched,
            boolean async,
            boolean forceRefresh
    ) {
        List<SyncedErratumWithPackages> synced = List.of();
        if (async) {
            SyncErrataWithPackagesEvent event = new SyncErrataWithPackagesEvent(target, found, forceRefresh, user);
            event.register();
            MessageQueue.publish(event);
        }
        else {
            synced = syncValidated(user, target, found, forceRefresh);
        }
        List<String> unmatchedShown = firstUnmatched(unmatched);
        return new SyncErrataWithPackagesResult(synced, unmatchedShown, unmatched.size() - unmatchedShown.size());
    }

    /**
     * Returns the requested advisory names that match no erratum found, in request order and without duplicates.
     * Every lookup matches on the exact advisory name.
     * @param advisoryNames the requested advisory names
     * @param found the errata found
     * @return the unmatched advisory names
     */
    private static List<String> unmatched(List<String> advisoryNames, Map<Errata, Channel> found) {
        Set<String> matched = found.keySet().stream().map(Errata::getAdvisoryName).collect(Collectors.toSet());
        return advisoryNames.stream().distinct().filter(name -> !matched.contains(name)).toList();
    }

    /**
     * Returns the unmatched advisory names to report, at most MAX_MISMATCH_OUTPUT of them.
     * @param unmatched the unmatched advisory names
     * @return the first ones, in the same order
     */
    private static List<String> firstUnmatched(List<String> unmatched) {
        return List.copyOf(unmatched.subList(0, Math.min(unmatched.size(), MAX_MISMATCH_OUTPUT)));
    }

    /**
     * Looks up a channel the user can access.
     * @param user the user
     * @param label the channel label
     * @return the channel
     * @throws NoSuchChannelException when the channel does not exist or the user cannot access it
     */
    private Channel lookupChannel(User user, String label) {
        Channel channel = ChannelFactory.lookupByLabelAndUser(label, user);
        if (channel == null) {
            throw new NoSuchChannelException(label);
        }
        return channel;
    }

    /**
     * Looks up the target channel, which the user must administer.
     * @param user the user
     * @param label the target channel label
     * @return the target channel
     * @throws NoSuchChannelException when the channel does not exist or the user cannot access it
     * @throws PermissionCheckFailureException when the user is not a channel administrator of it
     */
    private Channel lookupTarget(User user, String label) {
        Channel target = lookupChannel(user, label);
        if (!UserManager.verifyChannelAdmin(user, target)) {
            throw new PermissionCheckFailureException("User " + user.getLogin() +
                    " does not have access to channel " + target.getName());
        }
        return target;
    }

    /**
     * Checks the lookup result: no unmatched advisory name in strict mode, at least one erratum found, and no
     * pending asynchronous errata clone jobs on the target channel.
     * @param target the target channel
     * @param found the errata found
     * @param unmatched the unmatched advisory names
     * @param strict whether any unmatched advisory name fails the request
     * @throws FaultException in strict mode, when some advisory names are unmatched
     * @throws ValidationException when no erratum was found
     * @throws InvalidChannelException when the target channel has pending asynchronous errata clone jobs
     */
    private void validate(Channel target, Map<Errata, Channel> found, List<String> unmatched, boolean strict) {
        if (strict && !unmatched.isEmpty()) {
            List<String> unmatchedShown = firstUnmatched(unmatched);
            int unmatchedRemaining = unmatched.size() - unmatchedShown.size();
            throw new FaultException(-208, "no_such_patch", "Patches not found: " +
                    String.join(", ", unmatchedShown) +
                    (unmatchedRemaining > 0 ? " and " + unmatchedRemaining + " more" : ""));
        }
        if (found.isEmpty()) {
            throw new ValidationException("No erratas were found");
        }
        if (ErrataManager.channelHasPendingAsyncCloneJobs(target)) {
            throw new InvalidChannelException("Channel " + target.getName() +
                    " has pending asynchronous errata clone jobs. You must wait until asynchronous errata clone" +
                    " jobs are done.");
        }
    }
}
