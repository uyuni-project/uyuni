//@flow
import _isEmpty from "lodash/isEmpty";
import filtersEnum from "../shared/business/filters.enum";
import type {FilterFormType, FilterServerType} from "../shared/type/filter.type";

export function mapFilterFormToRequest(filterForm: FilterFormType, projectLabel: string): FilterServerType {
  const requestForm = {};
  requestForm.projectLabel = projectLabel;
  requestForm.name = filterForm.name;
  requestForm.deny = filterForm.deny;
  requestForm.entityType = filtersEnum.findByKey(filterForm.type).entityType;
  requestForm.matcher = filtersEnum.findByKey(filterForm.type).matcher;
  if (filterForm.type === filtersEnum.enum.PACKAGE_NEVRA.key) {
    const epochName = !_isEmpty(filterForm.epoch) ? `${filterForm.epoch}:` : '';
    if(_isEmpty(filterForm.architecture)){
      requestForm.criteriaKey = "nevr"
      requestForm.criteriaValue =
        `${filterForm.packageName || ""}-${epochName}${filterForm.version|| ""}-${filterForm.release|| ""}`;
    } else {
      requestForm.criteriaKey = "nevra"
      requestForm.criteriaValue =
        `${filterForm.packageName || ""}-${epochName}${filterForm.version || ""}-${filterForm.release || ""}.${filterForm.architecture}`;
    }
  } else if (filterForm.type === filtersEnum.enum.ERRATUM.key) {
    requestForm.criteriaKey = "advisory_name";
    requestForm.criteriaValue = filterForm.advisoryName;
  } else {
    requestForm.criteriaKey = "name";
    requestForm.criteriaValue = filterForm.criteria;
  }
  return requestForm;
}

export function mapResponseToFilterForm(filtersResponse: Array<FilterServerType> = []): Array<FilterFormType> {
  return filtersResponse.map(filterResponse => {
    let filterForm = {};
    filterForm.id = filterResponse.id;
    filterForm.name = filterResponse.name;
    filterForm.deny = filterResponse.deny;
    filterForm.matcher = filterResponse.matcher;
    filterForm.projects = filterResponse.projects;

    if(filterResponse.criteriaKey === "nevr") {
      filterForm.type = filtersEnum.enum.PACKAGE_NEVRA.key;
      if(!_isEmpty(filterResponse.criteriaValue)) {
        const [
          ,
          packageName,
          ,
          epoch,
          version,
          release
        ] = filterResponse.criteriaValue.match(/(.*)-((.*):)?(.*)-(.*)/);

        filterForm.packageName = packageName;
        filterForm.epoch = epoch;
        filterForm.version = version;
        filterForm.release = release;
      }
    } else if (filterResponse.criteriaKey === "nevra") {
      filterForm.type = filtersEnum.enum.PACKAGE_NEVRA.key;

      if(!_isEmpty(filterResponse.criteriaValue)) {
        const [
          ,
          packageName,
          ,
          epoch,
          version,
          release,
          architecture
        ] = filterResponse.criteriaValue.match(/(.*)-((.*):)?(.*)-(.*)\.(.*)/);

        filterForm.packageName = packageName;
        filterForm.epoch = epoch;
        filterForm.version = version;
        filterForm.release = release;
        filterForm.architecture = architecture;
      }
    } else if (filterResponse.criteriaKey === "advisory_name") {
      filterForm.type = filtersEnum.enum.ERRATUM.key;
      filterForm["advisoryName"] = filterResponse.criteriaValue;
    } else if (filterResponse.criteriaKey === "name") {
      filterForm.type = filtersEnum.enum.PACKAGE.key;
      filterForm.criteria = filterResponse.criteriaValue;
    }

    return filterForm;
  })
}
