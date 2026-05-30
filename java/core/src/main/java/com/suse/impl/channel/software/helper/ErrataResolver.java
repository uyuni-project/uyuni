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
package com.suse.impl.channel.software.helper;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static com.suse.utils.Predicates.isProvided;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.org.Org;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Resolves erratas using cascading organization priority for channel software sync operations.
 */
public final class ErrataResolver {

    /**
     * Lookup erratas using cascading org priority.
     * Priority: originalChannel.org > userOrg > vendor
     * Used for clone operations where we want to prefer organization-specific versions.
     *
     * @param originalChannel Original channel (may be null)
     * @param userOrg User's organization
     * @param advisoryNames Optional filter by advisory names
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @return Set of erratas with highest priority versions
     */
    public static Set<Errata> resolveFromCascadingOrg(
            Channel originalChannel,
            Org userOrg,
            List<String> advisoryNames,
            Date startDate,
            Date endDate
    ) {
        // Fetch all erratas from all three sources we consider
        Set<Errata> fromOriginalOrg = originalChannel != null && originalChannel.getOrg() != null ?
                ErrataFactory.lookupErrataByOrg(originalChannel.getOrg(), advisoryNames, startDate, endDate) :
                new HashSet<>();

        Set<Errata> fromUserOrg = ErrataFactory.lookupErrataByOrg(userOrg, advisoryNames, startDate, endDate);
        Set<Errata> fromVendor = ErrataFactory.lookupErrataFromVendor(advisoryNames, startDate, endDate);

        // Build an errata map with the highest priority errata versions
        // Priority: original org > user org > vendor (later puts override earlier ones)
        Map<String, Errata> errataMap = new HashMap<>();
        fromVendor.forEach(e -> errataMap.put(e.getAdvisoryName(), e));
        fromUserOrg.forEach(e -> errataMap.put(e.getAdvisoryName(), e));
        fromOriginalOrg.forEach(e -> errataMap.put(e.getAdvisoryName(), e));

        if (isProvided(advisoryNames)) {
            return advisoryNames.stream()
                    .map(errataMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        else {
            return new HashSet<>(errataMap.values());
        }
    }

    private ErrataResolver() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
