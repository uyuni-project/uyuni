//@flow
import _find from "lodash/find";

type FilterEntityType = { key: string, text: string };
type FilterEntityEnumType = { [key: string]: FilterEntityType };

export type FilterMatcherType =  {key: string, text: string, longDescription: string};
type FilterMatcherEnumType = { [key: string]: FilterMatcherType };

export type ClmFilterOptionType = {key: string, entityType: FilterEntityType, matchers: Array<FilterMatcherType>, text: string}
type ClmFilterOptionsEnumType = { [key: string]: ClmFilterOptionType }

export const filterEntity : FilterEntityEnumType = {
  PACKAGE: {
    key: 'package',
    text: t('Package')
  },
  ERRATUM: {
    key: 'erratum',
    text: t('Patch')
  }
};

const filterMatchers : FilterMatcherEnumType = {
  CONTAINS: {
    key: 'contains',
    text: t('contains'),
    longDescription: t('containing')
  },
  CONTAINS_PKG_NAME: {
    key: 'contains_pkg_name',
    text: t('contains'),
    longDescription: t('contains package name')
  },
  CONTAINS_PKG_LT_EVR: {
    key: 'contains_pkg_lt_evr',
    text: t('version lower than (<)'),
    longDescription: t('contains package with version lower than')
  },
  CONTAINS_PKG_LE_EVR: {
    key: 'contains_pkg_le_evr',
    text: t('version lower or equal than (<=)'),
    longDescription: t('contains package with version lower or equal than')
  },
  CONTAINS_PKG_EQ_EVR: {
    key: 'contains_pkg_eq_evr',
    text: t('version equal (=)'),
    longDescription: t('contains package with version equal than')
  },
  CONTAINS_PKG_GE_EVR: {
    key: 'contains_pkg_ge_evr',
    text: t('version greater or equal than (>=)'),
    longDescription: t('contains package with version greater or equal than')
  },
  CONTAINS_PKG_GT_EVR: {
    key: 'contains_pkg_gt_evr',
    text: 'version greater than (>)',
    longDescription: t('contains package with version greater than')
  },
  EQUALS: {
    key: 'equals',
    text: t('matches (=)'),
    longDescription: t('matching')
  },
  GREATEREQ: {
    key: 'greatereq',
    text: t('greater or equal (>=)'),
    longDescription: t('greater or equal than')
  },

};

export const clmFilterOptions : ClmFilterOptionsEnumType = {
  NAME: {
    key: 'name',
    text: t('Name'),
    entityType: filterEntity.PACKAGE,
    matchers: [filterMatchers.CONTAINS],
  },
  NEVRA: {
    key: 'nevra',
    text: t('NEVRA'),
    entityType: filterEntity.PACKAGE,
    matchers: [filterMatchers.EQUALS]
  },
  ADVISORY_NAME: {
    key: 'advisory_name',
    text: t('Advisory Name'),
    entityType: filterEntity.ERRATUM,
    matchers: [filterMatchers.EQUALS]
  },
  ADVISORY_TYPE: {
    key: 'advisory_type',
    text: t('Advisory Type'),
    entityType: filterEntity.ERRATUM,
    matchers: [filterMatchers.EQUALS]
  },
  SYNOPSIS: {
    key: 'synopsis',
    text: t('Synopsis'),
    entityType: filterEntity.ERRATUM,
    matchers: [filterMatchers.EQUALS, filterMatchers.CONTAINS]
  },
  ISSUE_DATE: {
    key: 'issue_date',
    text: t('Issue date'),
    entityType: filterEntity.ERRATUM,
    matchers: [filterMatchers.GREATEREQ],
  },
  PACKAGE_NAME: {
    key: 'package_name',
    text: t('Contains Package Name'),
    entityType: filterEntity.ERRATUM,
    matchers: [filterMatchers.CONTAINS_PKG_NAME]
  },
  PACKAGE_NEVR: {
    key: 'package_nevr',
    text: t('Contains Package'),
    entityType: filterEntity.ERRATUM,
    matchers: [
      filterMatchers.CONTAINS_PKG_LT_EVR, filterMatchers.CONTAINS_PKG_LE_EVR,
      filterMatchers.CONTAINS_PKG_EQ_EVR, filterMatchers.CONTAINS_PKG_GE_EVR,
      filterMatchers.CONTAINS_PKG_GT_EVR
    ]
  }
};

export function findClmFilterByKey(key: ?string): ?ClmFilterOptionType  {
  return _find(clmFilterOptions, entry => entry.key === key);
}

export function getClmFiltersOptions(): Array<ClmFilterOptionType> {
  return (Object.values(clmFilterOptions): any);
}

function findFilterMatcherByKey(key: ?string): FilterMatcherType {
  return _find(filterMatchers, entry => entry.key === key) || {};
}

export function getClmFilterDescription (filter: Object): string {
  const filterMatcher = findFilterMatcherByKey(filter.matcher);
  return `${filter.name}: ${filter.rule} ${filter.entityType} ${filterMatcher.longDescription || ''} ${filter.criteriaValue} (${filter.criteriaKey})`;
}
