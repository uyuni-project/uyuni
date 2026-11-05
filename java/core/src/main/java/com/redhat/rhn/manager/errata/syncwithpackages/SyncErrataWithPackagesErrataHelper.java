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
package com.redhat.rhn.manager.errata.syncwithpackages;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.manage.ErrataHelper;

import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Errata logic of the errata synchronization: finding the errata of interest and choosing the representative
 * erratum in the target channel.
 */
public class SyncErrataWithPackagesErrataHelper {

    /**
     * Finds errata in a source channel by advisory name.
     * If an empty list is provided, then return all errata of the channel.
     * @param source the source channel
     * @param advisoryNames the advisory names
     * @return the errata found, mapped to their source channel (null when found without one)
     */
    public static Map<Errata, Channel> findInSource(Channel source, List<String> advisoryNames) {
        if (advisoryNames.isEmpty()) {
            return withSource(source.getErratas(), source);
        }
        Map<Errata, Channel> found = new HashMap<>();
        for (String name : advisoryNames) {
            ErrataFactory.lookupByAdvisoryAndChannel(name, source).ifPresent(e -> found.put(e, source));
        }
        return found;
    }

    /**
     * Finds the errata of a source channel last modified within a date range, bounds included.
     * @param source the source channel
     * @param startDate the start date
     * @param endDate the end date
     * @return the errata found, mapped to their source channel (null when found without one)
     */
    public static Map<Errata, Channel> findInSource(Channel source, Date startDate, Date endDate) {
        return withSource(ErrataFactory.listByChannelLastModifiedBetween(source, startDate, endDate), source);
    }

    /**
     * Resolves each advisory name independently, using the first matching erratum in the following order:
     * 1) Clone ancestors of the target channel accessible to the user, nearest first;
     * 2) Errata belonging to the target channel's org;
     * 3) Errata belonging to the user's org;
     * 4) Vendor errata.
     * The source channel is the ancestor where the erratum was found, or {@code null} for errata found through the
     * fallback lookups.
     * @param user the user
     * @param target the target channel
     * @param advisoryNames the advisory names
     * @return the errata found, mapped to their source channel (null when found without one)
     */
    public static Map<Errata, Channel> findByCriteria(User user, Channel target, List<String> advisoryNames) {
        // Collect (user accessible) channel ancestors
        List<Channel> ancestors = target.originChain()
                .skip(1)
                .filter(c -> ChannelFactory.lookupByIdAndUser(c.getId(), user) != null)
                .toList();

        // Match each advisory to a channel
        Map<Errata, Channel> found = new HashMap<>();
        for (String name : advisoryNames) {
            Channel source = null;
            Errata errata = null;

            // 1) Try walk up the clone hierarchy
            for (Channel ancestor : ancestors) {
                errata = ErrataFactory.lookupByAdvisoryAndChannel(name, ancestor).orElse(null);
                if (errata != null) {
                    source = ancestor;
                    break;
                }
            }

            // Look for the errata according to the following fallback strategy:
            // 2) Errata belonging to the target channel's org
            // 3) Errata belonging to the user's org
            // 4) Vendor errata
            if (errata == null) {
                errata = Optional.ofNullable(ErrataFactory.lookupByAdvisoryAndOrg(name, target.getOrg()))
                        .or(() -> Optional.ofNullable(ErrataFactory.lookupByAdvisoryAndOrg(name, user.getOrg())))
                        .or(() -> Optional.ofNullable(ErrataFactory.lookupByAdvisoryAndOrg(name, null)))
                        .orElse(null);
            }
            if (errata != null) {
                found.put(errata, source);
            }
        }
        return found;
    }

    /**
     * Maps every erratum to the same source channel.
     * @param errata the errata
     * @param source the source channel they are taken from
     * @return the errata, mapped to the source channel
     */
    private static Map<Errata, Channel> withSource(Collection<Errata> errata, Channel source) {
        Map<Errata, Channel> found = new HashMap<>();
        errata.forEach(e -> found.put(e, source));
        return found;
    }

    /**
     * Finds the representative of an erratum that the target channel already holds: the erratum itself, a clone of it,
     * or a sibling (another clone of the same original).
     * @param target the target channel
     * @param errata the erratum of interest
     * @return the representative, if the target channel holds one
     */
    public static Optional<Errata> findRepresentativeErrataInTarget(Channel target, Errata errata) {
        // If target channel holds the erratum itself, return it
        if (errata.getChannels().contains(target)) {
            return Optional.of(errata);
        }

        // Otherwise, if the target channel holds a clone of the erratum, return that clone
        Optional<Errata> clone = ErrataFactory.lookupCloneInChannel(errata, target);
        if (clone.isPresent()) {
            return clone;
        }

        // Otherwise, look for a sibling (ie, another clone of the original this erratum was cloned from).
        // An erratum that is not a clone has no original, so no siblings: return empty
        Errata original = getOriginalErrataOf(errata);
        if (original == null) {
            return Optional.empty();
        }

        // Return the sibling held by the target channel, or empty if it holds none
        return ErrataFactory.lookupCloneInChannel(original, target);
    }

    /**
     * Returns the original an erratum was cloned from, used to find its siblings (other clones of the same original).
     * Only {@link ClonedErrata} has an original. Hibernate may hand out a lazy proxy of {@link Errata} for a cloned
     * erratum: it forwards calls such as isCloned() to the real object, but it is never an instance of
     * {@link ClonedErrata}, so it is unwrapped before the type check.
     * @param errata the erratum
     * @return the original erratum, or null when the erratum is not a clone
     */
    private static Errata getOriginalErrataOf(Errata errata) {
        return HibernateFactory.unproxy(errata) instanceof ClonedErrata cloned ? cloned.getOriginal() : null;
    }

    /**
     * Returns a representative of an erratum owned by an org, in this order:
     * - the erratum itself, when the org owns it;
     * - otherwise an existing clone of it owned by the org, the oldest one (lowest id) when there are several,
     * so repeated runs pick the same one;
     * - otherwise a new clone of it created for the org.
     * The representative is not linked to any channel here.
     * @param org the org owning the target channel
     * @param errata the erratum of interest
     * @return the representative
     */
    public static Errata getRepresentativeErrataOwnedBy(Org org, Errata errata) {
        if (Objects.equals(errata.getOrg(), org)) {
            return errata;
        }
        return ErrataFactory.lookupByOriginal(org, errata).stream()
                .min(Comparator.comparing(Errata::getId))
                .orElseGet(() -> ErrataHelper.cloneErrataFast(errata, org));
    }

    private SyncErrataWithPackagesErrataHelper() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
