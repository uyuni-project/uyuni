/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.errata;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

/**
 * ErrataAction - Class representation of the table rhnAction.
 */
@Entity
@DiscriminatorValue("5")
public class ErrataAction extends Action {

    @ManyToMany
    @JoinTable(
            name = "rhnActionErrataUpdate",
            joinColumns = @JoinColumn(name = "action_id"),
            inverseJoinColumns = @JoinColumn(name = "errata_id"))
    private Set<Errata> errata;

    @OneToOne(mappedBy = "parentAction", cascade = CascadeType.ALL)
    private ActionPackageDetails details;

    /**
     * @return Returns ActionPackageDetails
     */
    public ActionPackageDetails getDetails() {
        return details;
    }

    /**
     * @param detailsIn ActionPackageDetails
     */
    public void setDetails(ActionPackageDetails detailsIn) {
        this.details = detailsIn;
    }

    /**
     * @return Returns the errata.
     */
    public Set<Errata> getErrata() {
        return errata;
    }

    /**
     * @param errataIn The errata to set.
     */
    public void setErrata(Set<Errata> errataIn) {
        this.errata = errataIn;
    }

    /**
     * Add an Errata to this action.
     * @param e Errata to add
     */
    public void addErrata(Errata e) {
        if (errata == null) {
            errata = new HashSet<>();
        }
        errata.add(e);
    }

    /**
     * Get the Formatter for this class but in this case we use
     * ErrataActionFormatter.
     *
     * {@inheritDoc}
     */
    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ErrataActionFormatter(this);
        }
        return formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        retval.append(ls.getMessage("system.event.affectedErrata"));
        retval.append("</br>");
        retval.append("<ul>");
        for (Errata e : this.getErrata()) {
            retval.append("<li>");
            retval.append(e.getAdvisoryName());
            retval.append(" - ");
            retval.append(e.getSynopsis());
            retval.append("</li>");
        }
        retval.append("</ul>");
        return retval.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Set<Long> errataIds = getErrata().stream()
                .map(Errata::getId).collect(Collectors.toSet());
        boolean allowVendorChange = getDetails().getAllowVendorChange();

        Map<Boolean, List<MinionSummary>> byUbuntu = minionSummaries.stream()
                .collect(partitioningBy(m -> m.getOs().equals("Ubuntu")));

        Map<LocalCall<Map<String, State.ApplyResult>>, List<MinionSummary>> ubuntuErrataInstallCalls =
                errataToPackageInstallCalls(byUbuntu.get(true), errataIds);

        Set<Long> minionServerIds = byUbuntu.get(false).stream()
                .map(MinionSummary::getServerId)
                .collect(Collectors.toSet());

        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos = ServerFactory
                .listErrataNamesForServers(minionServerIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionSummary>> collect = byUbuntu.get(false).stream()
                .collect(Collectors.groupingBy(minionId -> errataInfos.get(minionId.getServerId())
                        .values().stream()
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
                ));

        // Convert errata names to LocalCall objects of type State.apply
        Map<LocalCall<?>, List<MinionSummary>> patchableCalls = collect.entrySet().stream()
                .collect(Collectors.toMap(entry -> {
                            Map<String, Object> params = new HashMap<>();
                            params.put(SaltParameters.PARAM_REGULAR_PATCHES,
                                    entry.getKey().stream()
                                            .filter(e -> !e.isUpdateStack())
                                            .map(ErrataInfo::getName)
                                            .sorted()
                                            .collect(toList())
                            );
                            params.put(SaltParameters.ALLOW_VENDOR_CHANGE, allowVendorChange);
                            params.put(SaltParameters.PARAM_UPDATE_STACK_PATCHES,
                                    entry.getKey().stream()
                                            .filter(ErrataInfo::isUpdateStack)
                                            .map(ErrataInfo::getName)
                                            .sorted()
                                            .collect(toList())
                            );
                            if (entry.getKey().stream().anyMatch(ErrataInfo::includeSalt)) {
                                params.put("include_salt_upgrade", true);
                            }
                            return State.apply(
                                    List.of(SaltParameters.PACKAGES_PATCHINSTALL),
                                    Optional.of(params)
                            );
                        },
                        Map.Entry::getValue));
        patchableCalls.putAll(ubuntuErrataInstallCalls);
        return patchableCalls;
    }

    private Map<LocalCall<Map<String, State.ApplyResult>>, List<MinionSummary>> errataToPackageInstallCalls(
            List<MinionSummary> minions,
            Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionSummary::getServerId).collect(Collectors.toSet());
        Map<Long, Map<String, Tuple2<String, String>>> longMapMap =
                ServerFactory.listNewestPkgsForServerErrata(minionIds, errataIds);

        // group minions by packages that need to be updated
        Map<Map<String, Tuple2<String, String>>, List<MinionSummary>> nameArchVersionToMinions =
                minions.stream().collect(
                        Collectors.groupingBy(minion -> longMapMap.get(minion.getServerId()))
                );

        return nameArchVersionToMinions.entrySet().stream().collect(toMap(
                entry -> State.apply(
                        singletonList(SaltParameters.PACKAGES_PKGINSTALL),
                        Optional.of(singletonMap(SaltParameters.PARAM_PKGS,
                                entry.getKey().entrySet()
                                        .stream()
                                        .map(e -> List.of(
                                                e.getKey(),
                                                e.getValue().getA().replaceAll("-deb$", ""),
                                                e.getValue().getB().endsWith("-X") ?
                                                        e.getValue().getB()
                                                                .substring(0, e.getValue().getB().length() - 2) :
                                                        e.getValue().getB()))
                                        .collect(Collectors.toList())))
                ),
                Map.Entry::getValue
        ));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        final List<Map<String, String>> additionalInfo = new ArrayList<>();

        // retrieve the errata that were associated with the action...
        DataResult<Row> errataList = ActionManager.getErrataList(getId());
        for (Row erratum : errataList) {
            String detail = (String) erratum.get("advisory");
            detail += " (" + erratum.get("synopsis") + ")";

            Map<String, String> info = new HashMap<>();
            info.put("detail", detail);
            additionalInfo.add(info);
        }

        return additionalInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalCall<?> prepareStagingTargets(List<MinionSummary> minionSummaries) {
        Set<Long> errataIds = getErrata().stream()
                .map(Errata::getId).collect(Collectors.toSet());
        Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                .listErrataNamesForServers(minionSummaries.stream().map(MinionSummary::getServerId)
                        .collect(Collectors.toSet()), errataIds);
        List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                        .flatMap(f -> f.getValue().stream()
                                .map(ErrataInfo::getName)
                        )
                )
                .toList();

        LOG.info("Executing staging of patches");
        return State.apply(List.of(SaltParameters.PACKAGES_PATCHDOWNLOAD),
                Optional.of(Collections.singletonMap(SaltParameters.PARAM_PATCHES, errataArgs)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean clientExecutionReturnsYamlFormat() {
        return true;
    }
}
