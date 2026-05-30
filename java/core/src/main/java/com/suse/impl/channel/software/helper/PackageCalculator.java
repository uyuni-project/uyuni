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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.user.User;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Calculates packages for channel software sync operations.
 */
public final class PackageCalculator {

    /**
     * Calculate packages from clone tree.
     * Walks up clone hierarchy to find the original channel, then gets ALL packages
     * associated with each errata from that channel.
     *
     * @param targetChannel Target channel (must be cloned)
     * @param erratas Erratas to process
     * @param user User performing the operation
     * @param excludedPackages Packages to exclude from the result
     * @return Map of errata to packages from original channel
     */
    public static Map<Errata, List<Package>> calculateFromCloneTree(
        Channel targetChannel, Set<Errata> erratas, User user, Set<Package> excludedPackages
    ) {
        Map<Errata, List<Package>> packagesPerErrata = new HashMap<>();

        for (Errata errata : erratas) {
            // Walk up parent clones until finding channel containing the errata
            Channel original = targetChannel.getOriginal();
            Set<Channel> associatedChannels = errata.getChannels();

            // BZ 805714: handle clone-of-clone scenarios
            while (original.isCloned() && !associatedChannels.contains(original)) {
                original = ChannelFactory.lookupOriginalChannel(original);
            }

            // Get all packages from this errata in the original channel
            List<Package> packages = PackageFactory.findByErrataInChannel(original, errata, user.getOrg());
            packages.removeAll(excludedPackages);
            packagesPerErrata.put(errata, packages);
        }

        return packagesPerErrata;
    }

    /**
     * Calculate packages from vendor match.
     * Returns packages from erratas that target channel already has a match (by name+arch).
     *
     * @param targetChannel Target channel
     * @param erratas Erratas to process
     * @param user User performing the operation
     * @return Set of packages matching existing name+arch in target
     */
    public static Set<Package> calculateFromVendorMatch(Channel targetChannel, Set<Errata> erratas, User user) {
        Set<Package> allPackages = new HashSet<>();
        for (Errata errata : erratas) {
            allPackages.addAll(
                    PackageFactory.findByErrataWithNameArchMatchInChannelForUser(targetChannel, errata, user)
            );
        }
        return allPackages;
    }

    private PackageCalculator() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
