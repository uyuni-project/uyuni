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

package com.suse.manager.model.products.migration;

/**
 * Describe if a system base product can be upgraded and migrated to a new version
 *
 * @param serverId the id of the server this eligibility refers to
 * @param eligible if the server base product can be migrated to an updated product
 * @param reason  a short description explaining the value of the eligibility
 * @param details a long description providing more in depth details on the value of the eligibility
 */
public record MigrationEligibility(long serverId, boolean eligible, String reason, String details) {

    /**
     * Builds an instance with empty details.
     *
     * @param serverIdIn the id of the server this eligibility refers to
     * @param eligibleIn if the server base product can be migrated to an updated product
     * @param reasonIn  a short description explaining the value of the eligibility
     */
    public MigrationEligibility(long serverIdIn, boolean eligibleIn, String reasonIn) {
        this(serverIdIn, eligibleIn, reasonIn, null);
    }
}
