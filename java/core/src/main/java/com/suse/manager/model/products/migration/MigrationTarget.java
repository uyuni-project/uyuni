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
 * Represent a possible migration target.
 * @param id the unique identifier of this target
 * @param targetProduct the target products of this possible migration.
 * @param missingChannels the list of the channels that are currently missing (not synced in current configuration). Any
 * value different from an empty list will make this target not usable.
 */
public record MigrationTarget(String id, MigrationProduct targetProduct, List<String> missingChannels) { }
