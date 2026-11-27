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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.frontend.action.channel.manage.ErrataHelper;

/**
 * Helpers shared by the syncErrataWithPackages tests.
 */
final class SyncErrataWithPackagesTestUtils {

    private SyncErrataWithPackagesTestUtils() { }

    /**
     * Creates an erratum with a generated advisory name and the default description.
     *
     * @param org the organization owning the erratum, null for a vendor erratum
     * @param channel the channel holding the erratum, null for none
     * @param packages the packages of the erratum
     * @return the erratum
     */
    static Errata createErrata(Org org, Channel channel, Package... packages) {
        return buildErrataWithPackages(new ErrataTestBuilder(), org, channel, packages);
    }

    /**
     * Creates an erratum.
     *
     * @param advisoryName the advisory name, null to generate one
     * @param description the description, null for the default one
     * @param org the organization owning the erratum, null for a vendor erratum
     * @param channel the channel holding the erratum, null for none
     * @param packages the packages of the erratum
     * @return the erratum
     */
    static Errata createErrata(String advisoryName, String description, Org org, Channel channel,
                                      Package... packages) {
        ErrataTestBuilder builder = new ErrataTestBuilder().advisory(advisoryName);
        if (description != null) {
            builder.description(description);
        }
        return buildErrataWithPackages(builder, org, channel, packages);
    }

    /**
     * Clones an erratum into an organization.
     *
     * @param org the organization owning the clone
     * @param original the erratum to clone
     * @param channel the channel holding the clone, null for none
     * @return the clone
     */
    static Errata createCloneOf(Org org, Errata original, Channel channel) {
        Errata clone = ErrataHelper.cloneErrataFast(original, org);
        if (channel != null) {
            channel.addErrata(clone);
        }
        return clone;
    }

    private static Errata buildErrataWithPackages(
            ErrataTestBuilder builder, Org org, Channel channel, Package... packages
    ) {
        builder.orgId(org == null ? null : org.getId()).addPackages(packages);
        if (channel != null) {
            builder.addChannel(channel);
        }
        return builder.buildAndSave();
    }
}
