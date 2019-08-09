//@flow
import _find from "lodash/find";
import {objectDefaultValueHandler} from "core/utils/objects";
import type {ProjectFilterServerType} from "../type/project.type";

export type FilterOptionType = {key: string, entityType: string, matcher: string, text: string}
type FiltersOptionEnumType = { [key: string]: FilterOptionType }

const defaultState = {};

export const filtersOptionsEnum : FiltersOptionEnumType = new Proxy({
    PACKAGE: {
      key: 'package',
      entityType: 'package',
      matcher: 'contains',
      text: 'Package (contains Name)',
    },
    PACKAGE_NEVRA: {
      key: 'package_nevra',
      entityType: 'package',
      matcher: 'equals',
      text: 'Package (matches NEVRA)',
    },
    ERRATUM: {
      key: 'erratum',
      entityType: 'erratum',
      matcher: 'equals',
      text: 'Patch (matches Advisory Name)',
    },
    ERRATUM_BYTYPE: {
      key: 'erratum_bytype',
      entityType: 'erratum',
      matcher: 'equals',
      text: 'Patch (matches Advisory Type)',
    },
    ERRATUM_BYDATE: {
      key: 'erratum_bydate',
      entityType: 'erratum',
      matcher: 'greatereq',
      text: 'Patch (issued after)',
    },
    ERRATUM_BYSYNOPSIS: {
      key: 'erratum_bysynopsis',
      entityType: 'erratum',
      matcher: 'equals',
      text: 'Patch (equals Synopsis)',
    },
    ERRATUM_BYSYNOPSIS_CONTAINS: {
      key: 'erratum_bysynopsis_contains',
      entityType: 'erratum',
      matcher: 'contains',
      text: 'Patch (contains Synopsis)',
    },
    ERRATUM_PKG_NAME: {
      key: 'erratum_pkg_name',
      entityType: 'erratum',
      matcher: 'contains_pkg_name',
      text: 'Patch contains package name',
    }
  },
  objectDefaultValueHandler(defaultState)
);

function getFiltersOptions(): Array<FilterOptionType> {
  return (Object.values(filtersOptionsEnum): any);
}

function findByKey(key: string): FilterOptionType {
  return _find(filtersOptionsEnum, entry => entry.key === key) || defaultState;
}

function getFilterDescription (filter: Object): string {
  const matcherDescription = {
    equals: " matching ",
    contains: " containing ",
    greatereq: " greater or equal ",
    contains_pkg_name: " contains package name "
  }

  return `${filter.name}: deny ${filter.entityType} ${matcherDescription[filter.matcher] || ""} ${filter.criteriaValue} (${filter.criteriaKey})`;
}

export default ({
  enum: filtersOptionsEnum,
  findByKey,
  getFiltersOptions,
  getFilterDescription
}: {
  enum: FiltersOptionEnumType,
  findByKey: (string) => FilterOptionType,
  getFiltersOptions: () => Array<FilterOptionType>,
  getFilterDescription: (ProjectFilterServerType) => string
});
