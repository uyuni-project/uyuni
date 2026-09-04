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
package com.suse.manager.action;

import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.server.MinionServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Immutable transactional formula execution plan for a minion.
 *
 * @param transactionalFormulas formulas to include in the transactional phase
 * @param postTransactionalFormulas formulas to freeze and execute in full in the live post-transactional phase
 * @param liveStateIds state IDs to exclude during the transactional phase
 * @param unsupportedFormulas executable formulas that must fail fast on transactional systems
 */
public record FormulaTransactionalPlan(
        List<String> transactionalFormulas,
        List<String> postTransactionalFormulas,
        List<String> liveStateIds,
        List<String> unsupportedFormulas) {

    private static final Map<String, FormulaTransactionalPolicy> BUILT_IN_POLICY = Map.ofEntries(
            Map.entry("locale", FormulaTransactionalPolicy.transactionalThenLive(List.of(
                    "mgr_timezone_setting",
                    "mgr_kb_settings",
                    "mgr_language_settings"))),
            Map.entry("pxe", FormulaTransactionalPolicy.transactionalThenLive(List.of(
                    "srv_tftpboot_default",
                    "srv_tftpboot_default_efi",
                    "srv_tftpboot_base_efi",
                    "pxe_copy",
                    "pxe_copy_grub_efi",
                    "pxe_copy_shim_efi",
                    "pxe_copy_efi_dir",
                    "pxe_copy_grub_arm64_efi",
                    "pxe_copy_arm64_efi_dir",
                    "srv_tftpboot_default_arm64_efi"))),
            Map.entry("tftpd", FormulaTransactionalPolicy.transactionalThenLive(List.of(
                    "enable_and_start_tftpd"))),
            Map.entry("bind", FormulaTransactionalPolicy.transactional()),
            Map.entry("dhcpd", FormulaTransactionalPolicy.transactional()),
            Map.entry("branch-network", FormulaTransactionalPolicy.transactional()),
            Map.entry("image-synchronize", FormulaTransactionalPolicy.unsupported()),
            Map.entry("prometheus-exporters", FormulaTransactionalPolicy.unsupported()),
            Map.entry("vsftpd", FormulaTransactionalPolicy.unsupported()),
            Map.entry("cpu-mitigations", FormulaTransactionalPolicy.unsupported()),
            Map.entry("grafana", FormulaTransactionalPolicy.unsupported()),
            Map.entry("prometheus", FormulaTransactionalPolicy.unsupported()),
            Map.entry("saline-grafana", FormulaTransactionalPolicy.unsupported()),
            Map.entry("saline-prometheus", FormulaTransactionalPolicy.unsupported()),
            Map.entry("uyuni-config", FormulaTransactionalPolicy.unsupported()),
            Map.entry("liberate", FormulaTransactionalPolicy.unsupported()));

    /**
     * Create a new immutable plan.
     *
     * @param transactionalFormulas formulas to include in the transactional phase
     * @param postTransactionalFormulas formulas to freeze and execute in full in the live post-transactional phase
     * @param liveStateIds state IDs to exclude during the transactional phase
     * @param unsupportedFormulas executable formulas that must fail fast on transactional systems
     */
    public FormulaTransactionalPlan {
        transactionalFormulas = List.copyOf(transactionalFormulas);
        postTransactionalFormulas = List.copyOf(postTransactionalFormulas);
        liveStateIds = List.copyOf(liveStateIds);
        unsupportedFormulas = List.copyOf(unsupportedFormulas);
    }

    /**
     * Build a transactional formula execution plan for a minion.
     *
     * @param minion minion whose assigned formulas should be planned
     * @return immutable transactional formula execution plan
     */
    public static FormulaTransactionalPlan fromMinion(MinionServer minion) {
        return fromAssignedFormulas(
                FormulaFactory.getFormulasByMinion(minion),
                FormulaFactory.getGroupFormulasByServer(minion),
                FormulaFactory::getMetadata,
                FormulaFactory::isCustomFormula);
    }

    static FormulaTransactionalPlan fromAssignedFormulas(
            List<String> directFormulas,
            List<String> inheritedFormulas,
            Function<String, Map<String, Object>> metadataProvider) {
        return fromAssignedFormulas(directFormulas, inheritedFormulas, metadataProvider, formula -> false);
    }

    static FormulaTransactionalPlan fromAssignedFormulas(
            List<String> directFormulas,
            List<String> inheritedFormulas,
            Function<String, Map<String, Object>> metadataProvider,
            Predicate<String> customFormulaPredicate) {
        return fromAssignedFormulas(directFormulas, inheritedFormulas, metadataProvider, customFormulaPredicate,
                BUILT_IN_POLICY);
    }

    static FormulaTransactionalPlan fromAssignedFormulas(
            List<String> directFormulas,
            List<String> inheritedFormulas,
            Function<String, Map<String, Object>> metadataProvider,
            Map<String, FormulaTransactionalPolicy> policy) {
        return fromAssignedFormulas(directFormulas, inheritedFormulas, metadataProvider, formula -> false, policy);
    }

    static FormulaTransactionalPlan fromAssignedFormulas(
            List<String> directFormulas,
            List<String> inheritedFormulas,
            Function<String, Map<String, Object>> metadataProvider,
            Predicate<String> customFormulaPredicate,
            Map<String, FormulaTransactionalPolicy> policy) {
        List<String> formulas = new ArrayList<>();
        formulas.addAll(directFormulas);
        formulas.addAll(inheritedFormulas);
        return fromFormulas(formulas, metadataProvider, customFormulaPredicate, policy);
    }

    static FormulaTransactionalPlan fromFormulas(
            List<String> formulas,
            Function<String, Map<String, Object>> metadataProvider,
            Map<String, FormulaTransactionalPolicy> policy) {
        return fromFormulas(formulas, metadataProvider, formula -> false, policy);
    }

    static FormulaTransactionalPlan fromFormulas(
            List<String> formulas,
            Function<String, Map<String, Object>> metadataProvider,
            Predicate<String> customFormulaPredicate,
            Map<String, FormulaTransactionalPolicy> policy) {
        FormulaTransactionalPolicyResolver resolver = new FormulaTransactionalPolicyResolver(
                policy, metadataProvider, customFormulaPredicate);
        return fromFormulas(formulas, metadataProvider, resolver);
    }

    static FormulaTransactionalPlan fromFormulas(
            List<String> formulas,
            Function<String, Map<String, Object>> metadataProvider,
            FormulaTransactionalPolicyResolver policyResolver) {
        List<String> orderedFormulas = orderedFormulas(executableFormulas(formulas, metadataProvider));

        List<String> transactional = new ArrayList<>();
        List<String> postTransactional = new ArrayList<>();
        List<String> unsupported = new ArrayList<>();
        LinkedHashSet<String> liveStateIds = new LinkedHashSet<>();

        for (String formula : orderedFormulas) {
            FormulaTransactionalPolicy formulaPolicy = policyResolver.resolve(formula);
            FormulaTransactionalMode mode = formulaPolicy.mode();
            switch (mode) {
                case TRANSACTIONAL -> transactional.add(formula);
                case LIVE -> postTransactional.add(formula);
                case TRANSACTIONAL_THEN_LIVE -> {
                    transactional.add(formula);
                    postTransactional.add(formula);
                    liveStateIds.addAll(formulaPolicy.liveStateIds());
                }
                case UNSUPPORTED -> unsupported.add(formula);
                default -> throw new IllegalStateException("Unsupported formula transactional mode: " + mode);
            }
        }

        return new FormulaTransactionalPlan(transactional, postTransactional, List.copyOf(liveStateIds), unsupported);
    }

    private static List<String> executableFormulas(
            List<String> assignedFormulas,
            Function<String, Map<String, Object>> metadataProvider) {
        LinkedHashSet<String> formulas = new LinkedHashSet<>();
        formulas.addAll(assignedFormulas);

        return formulas.stream()
                .filter(formula -> !Boolean.TRUE.equals(
                        metadataProvider.apply(formula).getOrDefault("pillar_only", Boolean.FALSE)))
                .toList();
    }

    private static List<String> orderedFormulas(List<String> formulas) {
        List<String> ordered = new ArrayList<>(FormulaFactory.orderFormulas(formulas));
        // Assigned formulas must not disappear from classification, especially from unsupportedFormulas.
        formulas.stream()
                .filter(formula -> !ordered.contains(formula))
                .forEach(ordered::add);
        return ordered;
    }

    static Map<String, FormulaTransactionalPolicy> builtInPolicy() {
        return Collections.unmodifiableMap(BUILT_IN_POLICY);
    }
}
