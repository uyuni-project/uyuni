/*
 * Copyright (c) 2025 SUSE LLC
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

import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.servlet.http.HttpServletRequest;

/**
 * PackageUpdateAction
 */
@Entity
@DiscriminatorValue("3")
public class PackageUpdateAction extends PackageAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        List<Long> sids = minionSummaries.stream().map(MinionSummary::getServerId).collect(toList());

        List<String> nevraStrings = getDetails().stream().map(details -> {
            PackageName name = details.getPackageName();
            PackageEvr evr = details.getEvr();
            PackageArch arch = details.getArch();
            return name.getName() + "-" + evr.toUniversalEvrString() + "." + arch.getLabel();
        }).collect(toList());

        List<Tuple2<Long, Long>> retractedPidSidPairs = ErrataFactory.retractedPackagesByNevra(nevraStrings, sids);
        Map<Long, List<Long>> retractedPidsBySid = retractedPidSidPairs.stream()
                .collect(groupingBy(Tuple2::getB, mapping(Tuple2::getA, toList())));
        getServerActions().forEach(sa -> {
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

        List<List<String>> pkgs =
                getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .toList();
        if (pkgs.isEmpty()) {
            // Full system package update using update state
            ret.put(State.apply(List.of(SaltParameters.PACKAGES_PKGUPDATE), Optional.empty()), filteredMinions);
        }
        else {
            ret.put(State.apply(List.of(SaltParameters.PACKAGES_PKGINSTALL),
                    Optional.of(singletonMap(SaltParameters.PARAM_PKGS, pkgs))), filteredMinions);
        }
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        return createPackageActionSpecificDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalCall<?> prepareStagingTargets(List<MinionSummary> minionSummaries) {
        List<List<String>> args = getDetails().stream()
                .map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .toList();
        LOG.info("Executing staging of packages");
        return State.apply(List.of(SaltParameters.PACKAGES_PKGDOWNLOAD),
                Optional.of(Collections.singletonMap(SaltParameters.PARAM_PKGS, args)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRequestAttributeTypePackages(HttpServletRequest request) {
        request.setAttribute("type", "packages");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clientExecutionReturnsYamlFormat() {
        return true;
    }
}
