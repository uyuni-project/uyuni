import * as React from "react";
import { useEffect, useState } from "react";

import _xor from "lodash/xor";

import { LinkButton } from "components/buttons";
import { Loading } from "components/utils/loading/Loading";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import { getClmFilterDescription } from "../../../business/filters.enum";
import { ProjectFilterServerType } from "../../../type";

type FiltersProps = {
  projectId: string;
  initialSelectedFiltersIds: Array<number>;
  onChange: Function;
  isUpdatingFilter: boolean;
};

const FiltersProjectSelection = (props: FiltersProps) => {
  const { onAction: onActionAllFilters, isLoading: isLoadingAllFilters } = useLifecycleActionsApi({
    resource: "filters",
  });
  const [allFilters, setAllFilters]: [Array<ProjectFilterServerType>, Function] = useState([]);
  const [onGoingSelectedFilters, setOnGoingSelectedFilters] = useState(props.initialSelectedFiltersIds);

  useEffect(() => {
    onActionAllFilters({}, "get").then((apiAllFilters) => setAllFilters(apiAllFilters));
  }, []);

  if (isLoadingAllFilters) {
    return <Loading text={t("Loading global filters...")} />;
  }

  if (props.isUpdatingFilter) {
    return <Loading text={t("Updating project filters...")} />;
  }

  return (
    <React.Fragment>
      <LinkButton
        id={`create-new-filter-link`}
        icon="fa-plus"
        className="btn btn-default js-spa"
        text={t("Create New Filter")}
        href={`/rhn/manager/contentmanagement/filters?openFilterId=-1&projectLabel=${props.projectId}`}
      />

      {allFilters &&
        allFilters.length > 0 &&
        allFilters.map((filter) => (
          <div key={filter.id} className="checkbox">
            <label htmlFor={"child_" + filter.id}>
              <input
                type="checkbox"
                value={filter.id}
                id={"child_" + filter.id}
                name="filterSelection"
                checked={onGoingSelectedFilters.includes(filter.id)}
                onChange={(event) => {
                  const newFilters = _xor(onGoingSelectedFilters, [parseInt(event.target.value, 10)]);
                  setOnGoingSelectedFilters(newFilters);
                  props.onChange(newFilters);
                }}
              />
              {getClmFilterDescription(filter)}
            </label>
          </div>
        ))}
    </React.Fragment>
  );
};

export default FiltersProjectSelection;
