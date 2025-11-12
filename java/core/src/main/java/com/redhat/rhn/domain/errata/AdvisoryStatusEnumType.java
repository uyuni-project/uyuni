/*
 * Copyright (c) 2021--2024 SUSE LLC
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
package com.redhat.rhn.domain.errata;

import com.redhat.rhn.domain.CustomEnumType;

/**
 * AdvisoryStatusEnumType
 */
public class AdvisoryStatusEnumType extends CustomEnumType<AdvisoryStatus, String> {
    /**
     * Constructor
     */
    public AdvisoryStatusEnumType() {
        super(AdvisoryStatus.class, String.class, AdvisoryStatus::getMetadataValue,
            s -> AdvisoryStatus.fromMetadata(s).orElse(null));
    }
}
