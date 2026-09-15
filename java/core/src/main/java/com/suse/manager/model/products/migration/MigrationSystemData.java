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
 * Record describing the migration eligibility for for a system.
 * @param id the id of the system
 * @param name the name of the system
 * @param installedProduct the set pf products installed on the system
 * @param eligible if the system is eligible for the product migration feature
 * @param reason  a short description explaining the value of the eligibility
 * @param details a long description providing more in depth details on the value of the eligibility
 */
public record MigrationSystemData(
    long id,
    String name,
    MigrationProduct installedProduct,
    boolean eligible,
    String reason,
    String details
) { }
