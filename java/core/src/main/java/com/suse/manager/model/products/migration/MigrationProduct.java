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

import java.util.List;

/**
 * A simple product description for the product migration UI
 * @param id the id of the product
 * @param name the display name of the product
 * @param addons the list of extensions of this product
 */
public record MigrationProduct(long id, String name, List<MigrationProduct> addons) { }
