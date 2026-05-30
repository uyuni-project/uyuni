/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.spec.channel.software.dto;

import com.suse.utils.Predicates;

import java.util.Date;
import java.util.List;

/**
 * Filter criteria for selecting erratas and their associated packages.
 *
 * @param advisoryNames Optional list of advisory names to filter by
 * @param startDate Optional start date for errata issue date range
 * @param endDate Optional end date for errata issue date range
 */
public record ErrataCriteria(List<String> advisoryNames, Date startDate, Date endDate) {

    /**
     * Check if any filter criteria are specified.
     * @return true if at least one filter is present
     */
    public boolean hasFilters() {
        return Predicates.anyProvided(advisoryNames, startDate, endDate);
    }
}
