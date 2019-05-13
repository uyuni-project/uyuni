// @flow
import React from 'react';
import {LinkButton} from "components/buttons";

import styles from "./filters-project.css"
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import CreatorPanel from "components/panels/CreatorPanel";
import type {ProjectFilterServerType} from "../../../type/project.type";
import FiltersProjectSelection from "./filters-project-selection";
import statesEnum from "../../../business/states.enum";
import filtersEnum from "../../../business/filters.enum";
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";

type FiltersProps = {
  projectId: string,
  selectedFilters: Array<ProjectFilterServerType>,
  onChange: Function,
};

const renderFilterEntry = (filter, projectId) => {
  const descr = filtersEnum.getFilterDescription(filter);
  const filterButton =
    <div className={styles.icon_wrapper_vertical_center}>
      <LinkButton
        id={`edit-filter-${filter.id}`}
        icon='fa-edit'
        title={t(`Edit Filter ${filter.name}`)}
        className='pull-right'
        text={t("Edit")}
        href={`/rhn/manager/contentmanagement/filters?openFilterId=${filter.id}&projectLabel=${projectId}`}
      />
    </div>;

  if (filter.state === statesEnum.enum.ATTACHED.key) {
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
  if (filter.state === statesEnum.enum.DETACHED.key) {
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
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const displayingFilters = [...props.selectedFilters];
  displayingFilters.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));

  return (

    <CreatorPanel
      id="filters"
      title={t('Filters')}
      creatingText="Attach/Detach Filters"
      panelLevel="2"
      collapsible
      customIconClass="fa-small"
      disableEditing={!hasEditingPermissions}
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
        return (
          <FiltersProjectSelection
            isUpdatingFilter={isLoading}
            projectId={props.projectId}
            initialSelectedFiltersIds={
              props.selectedFilters
                .filter(filter => !statesEnum.isDeletion(filter.state))
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
              displayingFilters.map(filter => renderFilterEntry(filter, props.projectId))
            }
          </ul>
        </div>
      }
    />

  );
};

export default FiltersProject;
