// @flow
import React from 'react';
import {LinkButton} from "components/buttons";

import styles from "./filters-project.css"
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import CreatorPanel from "components/panels/CreatorPanel";
import type {FilterType} from "../../../type/filter.type";
import FiltersProjectSelection from "./filters-project-selection";

type FiltersProps = {
  projectId: string,
  selectedFilters: Array<FilterType>,
  onChange: Function,
};

const renderFilterEntry = (filter) => {
  const descr = `${filter.name}: deny ${filter.type} containing ${filter.criteria} in the name`;
  const filterButton =
      <div className={styles.icon_wrapper_vertical_center}>
        <LinkButton
          id={`edit-filter-${filter.id}`}
          icon='fa-edit'
          title={t(`Edit Filter ${filter.name}`)}
          className='pull-right'
          text={t("Edit")}
          href={`/rhn/manager/contentmanagement/filters?openFilterId=${filter.id}`}
        />
      </div>;

  if (filter.state === 'ATTACHED') {
    return (
      <li
        key={`filter_list_item_${filter.id}`}
        className={`list-group-item text-success ${styles.wrapper}`}>
        <i className='fa fa-plus'/>
        <b>{descr}</b>
        {filterButton}
      </li>
    );
  }
  if (filter.state === 'DETACHED') {
    return (
      <li
        key={`filter_list_item_${filter.id}`}
        className={`list-group-item text-danger ${styles.wrapper} ${styles.dettached}`}>
        <i className='fa fa-minus'/>
        <b>{descr}</b>
        {filterButton}
      </li>
    );
  }
  return (
    <li
      key={`filter_list_item_${filter.id}`}
      className={`list-group-item text-sucess ${styles.wrapper}`}>
      {descr}
      {filterButton}
    </li>
  );
}

const FiltersProject = (props:  FiltersProps) => {

  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({
    resource: 'projects', nestedResource: "filters"
  });

  return (

    <CreatorPanel
      id="filters"
      title={t('Filters')}
      creatingText="Attach/Detach Filters"
      panelLevel="2"
      collapsible
      customIconClass="fa-small"
      onCancel={() => cancelAction()}
      onOpen={({setItem}) => setItem(props.selectedFilters.map(filter => filter.id))}
      onSave={({closeDialog, item}) => {
        const requestParam = {
          projectLabel: props.projectId,
          filtersIds: item,
        };

        onAction(requestParam, "update", props.projectId)
          .then((projectWithUpdatedSources) => {
            closeDialog();
            showSuccessToastr(t("Filters edited successfully"));
            props.onChange(projectWithUpdatedSources)
          })
          .catch((error) => {
            showErrorToastr(error);
          });
      }}
      renderCreationContent={({setItem}) => {
        // TODO: [LuNeves] transform this in an enum and reuse in sources.js as well
        const selectedStates = ["ATTACHED","BUILT"];

        return (
          <FiltersProjectSelection
            isUpdatingFilter={isLoading}
            projectId={props.projectId}
            initialSelectedFiltersIds={
              props.selectedFilters
                .filter(filter => selectedStates.includes(filter.state))
                .map(filter => filter.id)
            }
            onChange={setItem}
          />
        )
      }}
      renderContent={() =>
        <div className="min-height-panel">
          <ul className="list-group">
            {
              props.selectedFilters.map(filter => renderFilterEntry(filter))
            }
          </ul>
        </div>
      }
    />

  );
};

export default FiltersProject;
