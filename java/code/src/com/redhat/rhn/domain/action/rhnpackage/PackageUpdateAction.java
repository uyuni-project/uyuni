/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.rhnpackage;

import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * PackageUpdateAction
 */
public class PackageUpdateAction extends PackageAction {
    public static final String PACKAGES_PKGUPDATE = "packages.pkgupdate";
    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";


    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> packagesUpdateAction(
            List<MinionSummary> minionSummaries, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        List<Long> sids = minionSummaries.stream().map(MinionSummary::getServerId).collect(toList());

        List<String> nevraStrings = action.getDetails().stream().map(details -> {
            PackageName name = details.getPackageName();
            PackageEvr evr = details.getEvr();
            PackageArch arch = details.getArch();
            return name.getName() + "-" + evr.toUniversalEvrString() + "." + arch.getLabel();
        }).collect(toList());

        List<Tuple2<Long, Long>> retractedPidSidPairs = ErrataFactory.retractedPackagesByNevra(nevraStrings, sids);
        Map<Long, List<Long>> retractedPidsBySid = retractedPidSidPairs.stream()
                .collect(groupingBy(Tuple2::getB, mapping(Tuple2::getA, toList())));
        action.getServerActions().forEach(sa -> {
            List<Long> packageIds = retractedPidsBySid.get(sa.getServerId());
            if (packageIds != null) {
                sa.fail("contains retracted packages: " +
                        packageIds.stream().map(Object::toString).collect(joining(",")));
            }
        });
        List<MinionSummary> filteredMinions = minionSummaries.stream()
                .filter(ms -> retractedPidsBySid.get(ms.getServerId()) == null ||
                        retractedPidsBySid.get(ms.getServerId()).isEmpty())
                .collect(toList());

        List<List<String>> pkgs = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .toList();
        if (pkgs.isEmpty()) {
            // Full system package update using update state
            ret.put(State.apply(List.of(PACKAGES_PKGUPDATE), Optional.empty()), filteredMinions);
        }
        else {
            ret.put(State.apply(List.of(PACKAGES_PKGINSTALL),
                    Optional.of(singletonMap(PARAM_PKGS, pkgs))), filteredMinions);
        }
        return ret;
    }

}
