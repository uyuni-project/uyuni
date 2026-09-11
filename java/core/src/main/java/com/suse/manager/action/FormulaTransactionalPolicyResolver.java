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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Resolves the transactional policy for executable formulas.
 */
class FormulaTransactionalPolicyResolver {

    private static final String TRANSACTIONAL = "transactional";
    private static final String MODE = "mode";
    private static final String LIVE_STATE_IDS = "live_state_ids";

    private final Map<String, FormulaTransactionalPolicy> builtInPolicy;
    private final Function<String, Map<String, Object>> metadataProvider;
    private final Predicate<String> customFormulaPredicate;

    FormulaTransactionalPolicyResolver(
            Map<String, FormulaTransactionalPolicy> builtInPolicyIn,
            Function<String, Map<String, Object>> metadataProviderIn,
            Predicate<String> customFormulaPredicateIn) {
        this.builtInPolicy = Map.copyOf(builtInPolicyIn);
        this.metadataProvider = metadataProviderIn;
        this.customFormulaPredicate = customFormulaPredicateIn;
    }

    FormulaTransactionalPolicy resolve(String formula) {
        FormulaTransactionalPolicy policy = builtInPolicy.get(formula);
        if (policy != null) {
            return policy;
        }

        if (customFormulaPredicate.test(formula)) {
            return parseCustomPolicy(formula, metadataProvider.apply(formula));
        }

        return FormulaTransactionalPolicy.transactional();
    }

    private FormulaTransactionalPolicy parseCustomPolicy(String formula, Map<String, Object> metadata) {
        if (!metadata.containsKey(TRANSACTIONAL)) {
            return FormulaTransactionalPolicy.transactional();
        }

        Object config = metadata.get(TRANSACTIONAL);
        if (!(config instanceof Map<?, ?> configMap)) {
            throw invalid(formula, TRANSACTIONAL, "must be a map");
        }

        Set<?> keys = configMap.keySet();
        for (Object key : keys) {
            if (!MODE.equals(key) && !LIVE_STATE_IDS.equals(key)) {
                throw invalid(formula, String.valueOf(key), "unknown transactional field");
            }
        }

        Object modeValue = configMap.get(MODE);
        if (modeValue == null) {
            throw invalid(formula, MODE, "is required");
        }
        if (!(modeValue instanceof String mode)) {
            throw invalid(formula, MODE, "must be a string");
        }

        return switch (mode) {
            case TRANSACTIONAL -> parseModeWithoutLiveStateIds(formula, configMap,
                    FormulaTransactionalPolicy.transactional());
            case "live" -> parseModeWithoutLiveStateIds(formula, configMap, FormulaTransactionalPolicy.live());
            case "transactional_then_live" ->
                    FormulaTransactionalPolicy.transactionalThenLive(parseLiveStateIds(formula, configMap));
            case "disabled" -> parseModeWithoutLiveStateIds(formula, configMap,
                    FormulaTransactionalPolicy.unsupported());
            default -> throw invalid(formula, MODE, "has unsupported value '" + mode + "'");
        };
    }

    private static FormulaTransactionalPolicy parseModeWithoutLiveStateIds(
            String formula,
            Map<?, ?> configMap,
            FormulaTransactionalPolicy policy) {
        if (configMap.containsKey(LIVE_STATE_IDS)) {
            throw invalid(formula, LIVE_STATE_IDS, "is only valid for transactional_then_live");
        }
        return policy;
    }

    private static List<String> parseLiveStateIds(String formula, Map<?, ?> configMap) {
        Object idsValue = configMap.get(LIVE_STATE_IDS);
        if (!(idsValue instanceof List<?> ids)) {
            throw invalid(formula, LIVE_STATE_IDS, "must be a non-empty list");
        }
        if (ids.isEmpty()) {
            throw invalid(formula, LIVE_STATE_IDS, "must contain at least one ID");
        }

        LinkedHashSet<String> seenIds = new LinkedHashSet<>();
        for (Object id : ids) {
            if (!(id instanceof String idString)) {
                throw invalid(formula, LIVE_STATE_IDS, "must contain only strings");
            }
            if (idString.trim().isEmpty()) {
                throw invalid(formula, LIVE_STATE_IDS, "must not contain blank IDs");
            }
            if (!seenIds.add(idString)) {
                throw invalid(formula, LIVE_STATE_IDS, "contains duplicated ID '" + idString + "'");
            }
        }

        return List.copyOf(seenIds);
    }

    private static IllegalArgumentException invalid(String formula, String field, String reason) {
        return new IllegalArgumentException("Invalid transactional metadata for formula '" + formula +
                "': field '" + field + "' " + reason);
    }
}
