import _isEmpty from "lodash/isEmpty";
import { clmFilterOptions, findClmFilterByKey } from "../shared/business/filters.enum";
import { FilterFormType, FilterServerType } from "../shared/type/filter.type";
import { localizedMoment } from "utils";

export function mapFilterFormToRequest(
  filterForm: Partial<FilterFormType>,
  projectLabel?: string,
): FilterServerType {
  const requestForm: any = {};
  requestForm.projectLabel = projectLabel;
  requestForm.name = filterForm.filter_name;
  requestForm.rule = filterForm.rule;
  requestForm.matcher = filterForm.matcher;

  // If we're using a prebuilt filter
  if (Object.prototype.hasOwnProperty.call(filterForm, "template")) {
    requestForm.prefix = filterForm.labelPrefix;
    requestForm.kernelEvrId = filterForm.kernelId;
    requestForm.template = filterForm['template'];
  }

  const selectedFilterOption = findClmFilterByKey(filterForm.type);
  if (selectedFilterOption) {
    // By default the enum KEY is used either for the criteriaKey and to map the form field into the criteriaValue
    requestForm.entityType = selectedFilterOption.entityType.key;
    requestForm.criteriaKey = selectedFilterOption.key;
    requestForm.criteriaValue = filterForm[selectedFilterOption.key];
  } else {
    Loggerhead.error(`${filterForm.filter_name}: We couldn't find a matching filter for the form ${filterForm.type}`);
  }

  // Custom filters mappers for complex filter forms
  // If this starts growing we could define mapper functions in the enum itself, for now it's enough. (ex: mapCriteriaValueToRequest())
  if (filterForm.type === clmFilterOptions.ISSUE_DATE.key) {
    const formDateValue = filterForm[clmFilterOptions.ISSUE_DATE.key];
    requestForm.criteriaValue = formDateValue
      ? localizedMoment(formDateValue).toAPIValue()
      : "";
  } else if (filterForm.type === clmFilterOptions.NEVRA.key) {
    // UI filter NEVRA form can map either into nevr or nevra
    const epochName = !_isEmpty(filterForm.epoch) ? `${filterForm.epoch}:` : "";
    if (_isEmpty(filterForm.architecture)) {
      requestForm.criteriaKey = "nevr";
      requestForm.criteriaValue = `${filterForm.packageName || ""}-${epochName}${filterForm.version ||
        ""}-${filterForm.release || ""}`;
    } else {
      requestForm.criteriaKey = "nevra";
      requestForm.criteriaValue = `${filterForm.packageName || ""}-${epochName}${filterForm.version ||
        ""}-${filterForm.release || ""}.${filterForm.architecture}`;
    }
  } else if (filterForm.type === clmFilterOptions.PACKAGE_NEVR.key) {
    const epochName = !_isEmpty(filterForm.epoch) ? `${filterForm.epoch}:` : "";
    requestForm.criteriaValue = `${filterForm.packageName || ""} ${epochName}${filterForm.version ||
      ""}-${filterForm.release || ""}`;
  } else if (filterForm.type === clmFilterOptions.STREAM.key) {
    const streamName = !_isEmpty(filterForm.moduleStream) ? `:${filterForm.moduleStream}` : "";
    requestForm.criteriaValue = `${filterForm.moduleName || ""}${streamName}`;
    requestForm.rule = "allow";
  }

  return requestForm;
}

export function mapResponseToFilterForm(filtersResponse: Array<FilterServerType> = []): Array<FilterFormType> {
  return filtersResponse.map(filterResponse => {
    let filterForm: any = {};
    filterForm.id = filterResponse.id;
    filterForm.filter_name = filterResponse.name;
    filterForm.rule = filterResponse.rule;
    filterForm.matcher = filterResponse.matcher;
    filterForm.projects = filterResponse.projects;

    const selectedFilterOption = findClmFilterByKey(filterResponse.criteriaKey);
    // If we can find a filter option using the CriteriaKey we assume the default behavior
    if (selectedFilterOption) {
      filterForm.type = selectedFilterOption && selectedFilterOption.key;
      filterForm[selectedFilterOption.key] = filterResponse.criteriaValue;
    } else {
      Loggerhead.error(`${filterResponse.name}: We couldn't find a matching filter for ${filterResponse.criteriaKey}`);
    }

    // Custom filters mappers for complex filter forms
    // If this starts growing we could define mapper functions in the enum itself, for now it's enough. (ex: mapCriteriaValueToRequest())
    if (filterResponse.criteriaKey === clmFilterOptions.ISSUE_DATE.key) {
      filterForm[clmFilterOptions.ISSUE_DATE.key] = localizedMoment(filterResponse.criteriaValue);
    } else if (filterResponse.criteriaKey === "nevr") {
      // NEVR filter is mapped into NEVRA in the UI
      filterForm.type = clmFilterOptions.NEVRA.key;
      if (!_isEmpty(filterResponse.criteriaValue)) {
        const [, packageName, , epoch, version, release] =
          filterResponse.criteriaValue.match(/(.*)-((.*):)?(.*)-(.*)/) || [];

        filterForm.packageName = packageName;
        filterForm.epoch = epoch;
        filterForm.version = version;
        filterForm.release = release;
      }
    } else if (filterResponse.criteriaKey === clmFilterOptions.PACKAGE_NEVR.key) {
      if (!_isEmpty(filterResponse.criteriaValue)) {
        const [, packageName, , epoch, version, release] =
          filterResponse.criteriaValue.match(/(.*) ((.*):)?(.*)-(.*)/) || [];

        filterForm.packageName = packageName;
        filterForm.epoch = epoch;
        filterForm.version = version;
        filterForm.release = release;
      }
    } else if (filterResponse.criteriaKey === clmFilterOptions.NEVRA.key) {
      if (!_isEmpty(filterResponse.criteriaValue)) {
        const [, packageName, , epoch, version, release, architecture] =
          filterResponse.criteriaValue.match(/(.*)-((.*):)?(.*)-(.*)\.(.*)/) || [];

        filterForm.packageName = packageName;
        filterForm.epoch = epoch;
        filterForm.version = version;
        filterForm.release = release;
        filterForm.architecture = architecture;
      }
    } else if (filterResponse.criteriaKey === clmFilterOptions.STREAM.key) {
      if (!_isEmpty(filterResponse.criteriaValue)) {
        const [, module, stream] = filterResponse.criteriaValue.match(/([^:]*)(?::(.*))?/) || [];
        filterForm.moduleName = module;
        filterForm.moduleStream = stream;
      }
    }

    return filterForm;
  });
}
