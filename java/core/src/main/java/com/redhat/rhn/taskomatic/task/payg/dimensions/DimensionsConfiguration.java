/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg.dimensions;

import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.BaseProductRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.EntitlementRule;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.RequirementType;
import com.redhat.rhn.taskomatic.task.payg.dimensions.rules.SUSEManagerToolsRule;

import com.suse.cloud.domain.BillingDimension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * Configuration for the PAYG dimension computation
 */
public class DimensionsConfiguration {

    private final Map<BillingDimension, List<DimensionRule>> configurationMap;

    /**
     * Default PAYG configuration. Used by the {@link com.redhat.rhn.taskomatic.task.payg.PaygComputeDimensionsTask}
     */
    public static final DimensionsConfiguration DEFAULT_CONFIGURATION = new DimensionsConfiguration(
        Map.of(
            BillingDimension.MANAGED_SYSTEMS, List.of(
                new EntitlementRule(RuleType.INCLUDE, RequirementType.ANY,
                    Set.of(EntitlementManager.SALT, EntitlementManager.MANAGEMENT)),
                new BaseProductRule(RuleType.EXCLUDE, Set.of("sles_sap"), true),
                new SUSEManagerToolsRule()
            ),

            BillingDimension.MONITORING, List.of(
                new EntitlementRule(RuleType.INCLUDE, RequirementType.ANY, Set.of(EntitlementManager.MONITORING))
            )
        )
    );

    private DimensionsConfiguration(Map<BillingDimension, List<DimensionRule>> configurationMapIn) {
        this.configurationMap = configurationMapIn;
    }

    public Set<BillingDimension> getDimensions() {
        return Collections.unmodifiableSet(configurationMap.keySet());
    }

    /**
     * Retrieves the rule configured for the given dimension
     * @param dimension the dimension to retrieve
     * @return a list of rules, or an empty list if the requested dimension is not configured
     */
    public List<DimensionRule> getRulesForDimension(BillingDimension dimension) {
        return Collections.unmodifiableList(configurationMap.getOrDefault(dimension, Collections.emptyList()));
    }

    /**
     * Executes the given operation for each configured dimensions.
     * @param consumer a consumer that receives the dimensions and the list of rules
     */
    public void forEachDimension(BiConsumer<BillingDimension, List<DimensionRule>> consumer) {
        configurationMap.forEach(consumer);
    }
}
