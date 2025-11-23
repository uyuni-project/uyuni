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

import java.util.List;

/**
 * Describes a product migration for the given systems.
 * @param commonBaseProduct true if all the systems in the system data have the same source product and can be migrated
 * @param migrationSource the most common migration source, computed among the given systems
 * @param migrationTargets the possible migration targets, computed starting from the migration source
 * @param systemsData the description of the systems involved in the product migration
 */
public record MigrationTargetSelection(
    boolean commonBaseProduct,
    MigrationProduct migrationSource,
    List<MigrationTarget> migrationTargets,
    List<MigrationSystemData> systemsData
) { }
