// @flow
import React, {useEffect, useState} from 'react';
import {TopPanel} from 'components/panels/TopPanel';
import {Column} from 'components/table/Column';
import {SearchField} from 'components/table/SearchField';
import {Table} from 'components/table/Table';
import Functions from 'utils/functions';
import {showSuccessToastr} from 'components/toastr/toastr';
import withPageWrapper from 'components/general/with-page-wrapper';
import {hot} from 'react-hot-loader';
import FilterEdit from "./filter-edit";
import {mapResponseToFilterForm} from "./filter.utils";
import type {FilterFormType, FilterServerType} from "../shared/type/filter.type";
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";

type Props = {
  filters: Array<FilterServerType>,
  openFilterId: number,
  projectLabel: string,
  flashMessage: string,
};

const ListFilters = (props: Props) => {
  const [displayedFilters, setDisplayedFilters]: [Array<FilterFormType>, Function] =
    useState(mapResponseToFilterForm(props.filters));
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useEffect(()=> {
    if(props.flashMessage) {
      showSuccessToastr(props.flashMessage)
    }
  }, [])

  const searchData = (row, criteria) => {
    const keysToSearch = ['name'];
    if (criteria) {
      return keysToSearch.map(key => row[key]).join().toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  const panelButtons = (
    <div className="pull-right btn-group">
      {
        hasEditingPermissions &&
        <FilterEdit
          id="create-filter-button"
          initialFilterForm={{rule: "deny"}}
          icon='fa-plus'
          buttonText='Create Filter'
          openFilterId={props.openFilterId}
          projectLabel={props.projectLabel}
          onChange={responseFilters => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
        />
      }
    </div>
  );

  return (
    <TopPanel title={t('Content Lifecycle Filters')} icon="fa-filter" button={panelButtons} helpUrl="/docs/reference/clm/clm-filters.html">
      <Table
        data={displayedFilters}
        identifier={row => row.filter_name}
        initialSortColumnKey="filter_name"
        searchField={(
          <SearchField
            filter={searchData}
            placeholder={t('Filter by any value')}
          />
        )}
      >
        <Column
          columnKey="filter_name"
          comparator={Functions.Utils.sortByText}
          header={t('Name')}
          cell={row => row.filter_name}
        />
        <Column
          columnKey="projects"
          header={t('Projects in use')}
          // {left: project label, right: project name}
          cell={row => row.projects.sort((a, b) => a.right.toLowerCase().localeCompare(b.right.toLowerCase())).map(p =>
            <a className="project-tag-link js-spa" href={`/rhn/manager/contentmanagement/project/${p.left}`}>
              {p.right}
            </a>
          )}
        />
        <Column
          columnKey="action-buttons"
          header={t('')}
          cell={ row =>
            hasEditingPermissions &&
            <FilterEdit
              id={`edit-filter-button-${row.id}`}
              initialFilterForm={row}
              icon='fa-edit'
              buttonText='Edit Filter'
              onChange={responseFilters => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
              openFilterId={props.openFilterId}
              projectLabel={props.projectLabel}
              editing
            />
          }
        />
      </Table>
    </TopPanel>
  );
}

export default hot(module)(withPageWrapper<Props>(ListFilters));
