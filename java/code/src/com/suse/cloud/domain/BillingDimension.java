/*
 * Copyright (c) 2023--2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.cloud.domain;

import com.redhat.rhn.domain.Labeled;

public enum BillingDimension implements Labeled {
    MANAGED_SYSTEMS,
    MONITORING;

    public String getLabel() {
        return this.name().toLowerCase();
    }
}
