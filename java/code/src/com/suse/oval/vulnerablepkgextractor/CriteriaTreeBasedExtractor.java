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

package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An abstract implementation that provides utility methods to extract vulnerable package information from
 * OVAL criteria trees
 */
public abstract class CriteriaTreeBasedExtractor implements VulnerablePackagesExtractor {
    protected final DefinitionType definition;
    protected final CriteriaType criteriaRoot;

    protected CriteriaTreeBasedExtractor(DefinitionType definition) {
        assertDefinitionIsValid(definition);

        this.definition = definition;
        this.criteriaRoot = definition.getCriteria();
    }

    protected abstract List<ProductVulnerablePackages> extractItem(BaseCriteria criteriaType);

    /**
     * Tests whether the extractor can extract package vulnerability information from the given criteria node or not
     */
    protected abstract boolean test(BaseCriteria criteria);

    public final List<ProductVulnerablePackages> extract() {
        List<BaseCriteria> matchedCriteriaList = walkCriteriaTree();

        return matchedCriteriaList.stream().map(this::extractItem)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<CriterionType> collectCriterions(BaseCriteria criteria, int maxNestingLevel) {
        List<CriterionType> result = new ArrayList<>();

        collectCriterionsHelper(criteria, 0, maxNestingLevel, result);

        return result;
    }

    public List<CriterionType> collectCriterions(BaseCriteria criteria) {
        return collectCriterions(criteria, 0);
    }

    private void collectCriterionsHelper(BaseCriteria criteria, int currentLevel, int maxNestingLevel,
                                         List<CriterionType> criterions) {
        if (criteria instanceof CriterionType) {
            criterions.add((CriterionType) criteria);
        }
        else {
            if (currentLevel > maxNestingLevel) {
                return;
            }
            for (BaseCriteria child : ((CriteriaType) (criteria)).getChildren()) {
                collectCriterionsHelper(child, currentLevel + 1, maxNestingLevel, criterions);
            }
        }
    }

    private List<BaseCriteria> walkCriteriaTree() {
        List<BaseCriteria> matches = new ArrayList<>();

        walkCriteriaTreeHelper(criteriaRoot, matches);

        return matches;
    }

    private void walkCriteriaTreeHelper(BaseCriteria criteria, List<BaseCriteria> matches) {
        if (criteria == null) {
            return;
        }
        else if (test(criteria)) {
            matches.add(criteria);
        }
        if (criteria instanceof CriteriaType) {
            for (BaseCriteria childCriteria : ((CriteriaType) criteria).getChildren()) {
                walkCriteriaTreeHelper(childCriteria, matches);
            }
        }
    }

    @Override
    public void assertDefinitionIsValid(DefinitionType definition) {
        assert definition != null;
        assert definition.getCriteria() != null;
    }
}
