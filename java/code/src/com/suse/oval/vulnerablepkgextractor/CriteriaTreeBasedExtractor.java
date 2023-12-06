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
import java.util.stream.Collectors;

/**
 * An abstract implementation that provides utility methods to extract vulnerable package information from
 * OVAL criteria trees
 */
public abstract class CriteriaTreeBasedExtractor implements VulnerablePackagesExtractor {
    protected final DefinitionType definition;
    protected final CriteriaType criteriaRoot;

    protected CriteriaTreeBasedExtractor(DefinitionType definitionIn) {
        assertDefinitionIsValid(definitionIn);

        this.definition = definitionIn;
        this.criteriaRoot = definitionIn.getCriteria();
    }

    protected abstract List<ProductVulnerablePackages> extractItem(BaseCriteria criteriaType);

    /**
     * Tests whether the extractor can extract package vulnerability information from the given criteria node or not
     */
    protected abstract boolean test(BaseCriteria criteria);

    /**
     * Walk the criteria tree and extact product vulnerable packages based on the implementation of
     * {@code test()} and {@code extractItem()}
     *
     * @return a list of ProductVulnerablePackages. It's a list because an OVAL definition could encapsulate
     * vulnerability information for multiple products. In that case, each item in the returned list gives the list of
     * vulnerable packages for one product.
     * */
    public final List<ProductVulnerablePackages> extract() {
        List<BaseCriteria> matchedCriteriaList = walkCriteriaTree();

        return matchedCriteriaList.stream().map(this::extractItem)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * A helper method to recursively collect the children criterions contained at most {@code maxNestingLevel} levels
     * inside the given {@code criteria} or returns the actual {@code criteria} if it's of type {@link CriterionType}
     *
     * @param criteria the criteria to collect criterions from
     * @param maxNestingLevel the maximum level to reach when recursively collecting criterions
     * @return the list of criterions inside {@code criteria}
     * */
    public List<CriterionType> collectCriterions(BaseCriteria criteria, int maxNestingLevel) {
        List<CriterionType> result = new ArrayList<>();

        collectCriterionsHelper(criteria, 0, maxNestingLevel, result);

        return result;
    }

    /**
     * A helper method to collect the children criterions contained directly inside the given {@code criteria}
     * or returns the actual {@code criteria} if it's of type {@link CriterionType}
     *
     * @param criteria the criteria to collect criterions from
     * @return the list of criterions inside {@code criteria}
     * */
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
    public void assertDefinitionIsValid(DefinitionType definitionIn) {
        assert definitionIn != null;
        assert definitionIn.getCriteria() != null;
    }
}
