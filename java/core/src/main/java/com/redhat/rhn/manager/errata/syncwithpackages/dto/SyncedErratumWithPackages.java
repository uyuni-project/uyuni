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
package com.redhat.rhn.manager.errata.syncwithpackages.dto;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;

import java.util.List;

/**
 * The representative erratum in the target channel and the packages the channel serves for it.
 * @param errata the representative erratum in the target channel
 * @param packages the packages the target channel serves for the advisory
 */
public record SyncedErratumWithPackages(Errata errata, List<Package> packages) { }
