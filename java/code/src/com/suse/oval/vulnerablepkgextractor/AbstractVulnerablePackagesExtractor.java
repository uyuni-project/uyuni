package com.suse.oval.vulnerablepkgextractor;

import com.suse.oval.manager.OvalObjectManager;
import com.suse.oval.manager.OvalStateManager;
import com.suse.oval.manager.OvalTestManager;
import com.suse.oval.ovaltypes.BaseCriteria;
import com.suse.oval.ovaltypes.CriteriaType;
import com.suse.oval.ovaltypes.CriterionType;
import com.suse.oval.ovaltypes.DefinitionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class responsible for the extraction of vulnerable packages and their fix versions from an OVAL vulnerability definition.
 * The extraction process depends on the way the vulnerability criteria tree is structured. Therefore, the implementations
 * differ depending on the source of the OVAL definition (SUSE, Ubuntu, etc.)
 */
public abstract class AbstractVulnerablePackagesExtractor {
    protected final DefinitionType vulnerabilityDefinition;
    protected final CriteriaType criteriaRoot;
    protected final OvalObjectManager ovalObjectManager;
    protected final OvalTestManager ovalTestManager;
    protected final OvalStateManager ovalStateManager;

    protected AbstractVulnerablePackagesExtractor(DefinitionType vulnerabilityDefinition,
                                                  OvalObjectManager ovalObjectManager, OvalTestManager ovalTestManager,
                                                  OvalStateManager ovalStateManager) {
        this.vulnerabilityDefinition = vulnerabilityDefinition;
        this.criteriaRoot = vulnerabilityDefinition.getCriteria();
        this.ovalObjectManager = ovalObjectManager;
        this.ovalTestManager = ovalTestManager;
        this.ovalStateManager = ovalStateManager;
    }

    protected abstract List<ProductVulnerablePackages> extractItem(CriteriaType criteriaType);

    /**
     * Tests whether the extractor can extract package vulnerability information from the given criteria node or not
     */
    protected abstract boolean test(CriteriaType criteria);

    public final List<ProductVulnerablePackages> extract() {
        List<CriteriaType> matchedCriteriaList = walkCriteriaTree();

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
        } else {
            if (currentLevel > maxNestingLevel) {
                return;
            }
            for (BaseCriteria child : ((CriteriaType) (criteria)).getChildren()) {
                collectCriterionsHelper(child, currentLevel + 1, maxNestingLevel, criterions);
            }
        }
    }

    private List<CriteriaType> walkCriteriaTree() {
        List<CriteriaType> matches = new ArrayList<>();

        walkCriteriaTreeHelper(criteriaRoot, matches);

        return matches;
    }

    private void walkCriteriaTreeHelper(CriteriaType criteria, List<CriteriaType> matches) {
        if (criteria == null) {
            return;
        } else if (test(criteria)) {
            matches.add(criteria);
        }
        for (BaseCriteria childCriteria : criteria.getChildren()) {
            if (childCriteria instanceof CriteriaType) {
                walkCriteriaTreeHelper((CriteriaType) childCriteria, matches);
            }
        }

    }
}
