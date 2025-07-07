/*
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


import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PackageRemoveAction
 */
public class PackageRemoveAction extends PackageAction {
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";

    /**
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    public static Map<LocalCall<?>, List<MinionSummary>> packagesRemoveAction(
            List<MinionSummary> minionSummaries, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<List<String>> pkgsAll = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
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
        params.put(PARAM_PKGS, uniquePkgs);
        params.put("param_pkgs_duplicates", duplicatedPkgs);

        ret.put(State.apply(List.of(PACKAGES_PKGREMOVE),
                Optional.of(params)), minionSummaries);
        return ret;
    }

}
