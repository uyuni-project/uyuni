// @flow
import React, {useEffect, useState} from 'react';
import {LinkButton} from "components/buttons";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import type {ProjectFilterServerType} from "../../../type/project.type";
import {Loading} from "components/loading/loading";
import _xor from "lodash/xor";
import type {Node} from 'react';
import filtersEnum from "../../../business/filters.enum";

type FiltersProps = {
  projectId: string,
  initialSelectedFiltersIds: Array<number>,
  onChange: Function,
  isUpdatingFilter: boolean,
};

const FiltersProjectSelection = (props:  FiltersProps): Node => {

  const {onAction: onActionAllFilters, isLoading: isLoadingAllFilters} = useLifecycleActionsApi({
    resource: 'filters'
  });
  const [allFilters, setAllFilters]: [Array<ProjectFilterServerType>, Function] = useState([]);
  const [onGoingSelectedFilters, setOnGoingSelectedFilters] = useState(props.initialSelectedFiltersIds);

  useEffect(() => {
    onActionAllFilters({}, 'get')
      .then(apiAllFilters => setAllFilters(apiAllFilters))
  }, [])

  useEffect(() => {
    props.onChange(onGoingSelectedFilters);
  }, [onGoingSelectedFilters])

  if (isLoadingAllFilters) {
    return (
      <Loading text={t('Loading global filters...')}/>
    )
  }

  if(props.isUpdatingFilter) {
    return (
      <Loading text={t('Updating project filters...')}/>
    )
  }

  return (
    <React.Fragment>
    {
      allFilters && allFilters.length > 0 && allFilters.map(filter =>
          <div key={filter.id} className='checkbox'>
            <input type='checkbox'
                  value={filter.id}
                  id={'child_' + filter.id}
                  name='filterSelection'
                  checked={onGoingSelectedFilters.includes(filter.id)}
                  onChange={(event) => setOnGoingSelectedFilters(
                    _xor(onGoingSelectedFilters, [parseInt(event.target.value, 10)])
                  )}
            />
            <label
              htmlFor={"child_" + filter.id}>
              {filtersEnum.getFilterDescription(filter)}
            </label>
          </div>
      )
    }
    <LinkButton
        id={`create-new-filter-link`}
        icon='fa-plus'
        text={t("Create new Filter")}
        href={`/rhn/manager/contentmanagement/filters?openFilterId=-1&projectLabel=${props.projectId}`}
    />
    </React.Fragment>
  );
};


export default FiltersProjectSelection;
