/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import static java.util.stream.Collectors.partitioningBy;

import com.redhat.rhn.domain.action.TransactionalAction;
import com.redhat.rhn.domain.action.TransactionalFlow;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.servlet.http.HttpServletRequest;

/**
 * PackageRemoveAction
 */
@Entity
@DiscriminatorValue("4")
public class PackageRemoveAction extends PackageAction implements TransactionalAction {

    /**
     * {@inheritDoc}
     */
    @Override
    public TransactionalFlow getTransactionalFlow() {
        return TransactionalFlow.APPLY_THEN_COMPLETE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<List<String>> pkgsAll =
                getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .toList();

        List<List<String>> uniquePkgs = new ArrayList<>();
        pkgsAll.forEach(d -> {
            if (!uniquePkgs.stream().map(p -> p.get(0))
                    .toList()
                    .contains(d.get(0))) {
                uniquePkgs.add(d);
            }
        });
        List<List<String>> duplicatedPkgs = pkgsAll.stream()
                .filter(p -> !uniquePkgs.contains(p)).collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put(SaltParameters.PARAM_PKGS, uniquePkgs);
        params.put("param_pkgs_duplicates", duplicatedPkgs);

        Map<Boolean, List<MinionSummary>> minionsByTransactionalUpdate = minionSummaries.stream()
                .collect(partitioningBy(MinionSummary::isTransactionalUpdate));
        addPackageRemoveCall(ret, State.apply(List.of(SaltParameters.PACKAGES_PKGREMOVE),
                Optional.of(params)), minionsByTransactionalUpdate.get(false));
        addPackageRemoveCall(ret, TransactionalUpdateCalls.apply(List.of(SaltParameters.PACKAGES_PKGREMOVE),
                Optional.of(params)), minionsByTransactionalUpdate.get(true));
        return ret;
    }

    private static void addPackageRemoveCall(
            Map<LocalCall<?>, List<MinionSummary>> calls, LocalCall<?> call, List<MinionSummary> minions) {
        if (!minions.isEmpty()) {
            calls.put(call, minions);
        }
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
    public void setRequestAttributeTypePackages(HttpServletRequest request) {
        request.setAttribute("type", "packages");
    }
}
