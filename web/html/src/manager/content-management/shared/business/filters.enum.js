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
      text: 'package (Contains name)',
    },
    PACKAGE_NEVRA: {
      key: 'package_nevra',
      entityType: 'package',
      matcher: 'equals',
      text: 'package (nevra)',
    },
    ERRATUM: {
      key: 'erratum',
      entityType: 'erratum',
      matcher: 'equals',
      text: 'patch (Matches advisory name)',
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
    contains: " containing "
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
