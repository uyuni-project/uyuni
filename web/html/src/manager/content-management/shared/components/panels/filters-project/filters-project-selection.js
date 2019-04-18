// @flow
import React, {useEffect, useState} from 'react';
import {LinkButton} from "components/buttons";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import type {FilterType} from "../../../type/filter.type";
import {Loading} from "components/loading/loading";
import _xor from "lodash/xor";
import type {Node} from 'react';

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
  const [allFilters, setAllFilters]: [Array<FilterType>, Function] = useState([]);
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
              {`${filter.name}: deny ${filter.type} containing ${filter.criteria} in the name`}
            </label>
          </div>
      )
    }
    <LinkButton
        id={`create-new-filter-link`}
        icon='fa-plus'
        text={t("Create new Filter")}
        href="/rhn/manager/contentmanagement/filters?openFilterId=-1"
    />
    </React.Fragment>
  );
};

export default FiltersProjectSelection;
