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

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;

import java.util.Date;
import java.util.List;

/**
 * Synchronizes errata together with their packages into a target channel
 * (channel.software.syncErrataWithPackages).
 * Afterwards, the target channel holds exactly one representative erratum per advisory, owned by the target
 * channel org, which declares every package of the erratum of interest. The target channel serves the packages
 * that the erratum and its source have in common, or all packages of the erratum when it has no source.
 * Packages are never removed.
 */
public interface SyncErrataWithPackagesManager {

    /**
     * Synchronizes errata of a source channel by advisory name.
     * @param user the user
     * @param sourceLabel the label of the source channel
     * @param targetLabel the label of the target channel, the user must administer it
     * @param advisoryNames the advisory names, all errata of the source channel when empty
     * @param strict fail and synchronize nothing when any advisory name is unmatched
     * @param async synchronize asynchronously after validating the request
     * @param forceRefresh refresh the newest package cache and schedule repodata regeneration when the target
     * channel changed
     * @return the result
     */
    SyncErrataWithPackagesResult syncFromSource(
            User user,
            String sourceLabel,
            String targetLabel,
            List<String> advisoryNames,
            boolean strict,
            boolean async,
            boolean forceRefresh
    );

    /**
     * Synchronizes errata of a source channel last modified within a date range, bounds included.
     * @param user the user
     * @param sourceLabel the label of the source channel
     * @param targetLabel the label of the target channel, the user must administer it
     * @param startDate the start date
     * @param endDate the end date, not before the start date
     * @param async synchronize asynchronously after validating the request
     * @param forceRefresh refresh the newest package cache and schedule repodata regeneration when the target
     * channel changed
     * @return the result
     */
    SyncErrataWithPackagesResult syncFromSource(
            User user,
            String sourceLabel,
            String targetLabel,
            Date startDate,
            Date endDate,
            boolean async,
            boolean forceRefresh
    );

    /**
     * Synchronizes errata by advisory name, finding the source of each: the nearest accessible clone ancestor
     * of the target channel that contains it, otherwise no source.
     * @param user the user
     * @param targetLabel the label of the target channel, the user must administer it
     * @param advisoryNames the advisory names, must not be empty
     * @param strict fail and synchronize nothing when any advisory name is unmatched
     * @param async synchronize asynchronously after validating the request
     * @param forceRefresh refresh the newest package cache and schedule repodata regeneration when the target
     * channel changed
     * @return the result
     */
    SyncErrataWithPackagesResult syncByCriteria(
            User user,
            String targetLabel,
            List<String> advisoryNames,
            boolean strict,
            boolean async,
            boolean forceRefresh
    );

}
