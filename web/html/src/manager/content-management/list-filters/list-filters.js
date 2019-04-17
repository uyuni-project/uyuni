// @flow
import React, {useEffect, useState} from 'react';
import {TopPanel} from 'components/panels/TopPanel';
import {Column, SearchField, Table} from 'components/table';
import Functions from 'utils/functions';
import {showSuccessToastr} from 'components/toastr/toastr';
import withPageWrapper from 'components/general/with-page-wrapper';
import type {FilterType} from '../shared/type/filter.type.js';
import {hot} from 'react-hot-loader';
import FilterEdit from "./filter-edit";

type Props = {
  filters: Array<FilterType>,
  openFilterId: number,
  flashMessage: string,
};

const ListFilters = (props: Props) => {

  const [displayedFilters, setDisplayedFilters] = useState(props.filters);

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
      <FilterEdit
        id="create-filter-button"
        filter={{type: 'package', deny: true}}
        icon='fa-plus'
        buttonText='Create Filter'
        openFilterId={props.openFilterId}
        onChange={setDisplayedFilters}
      />
    </div>
  );

  return (
    <TopPanel title={t('Filter')} icon="spacewalk-icon-software-channels" button={panelButtons}>
      <Table
        data={displayedFilters}
        identifier={row => row.name}
        searchField={(
          <SearchField
            filter={searchData}
            placeholder={t('Filter by any value')}
          />
        )}
      >
        <Column
          columnKey="name"
          comparator={Functions.Utils.sortByText}
          header={t('Name')}
          cell={row => row.name}
        />
        <Column
          columnKey="projects"
          header={t('Projects in use')}
          cell={row => row.projects.map(p =>
            <a className="project-tag-link" href={`/rhn/manager/contentmanagement/project/${p}`}>
              {p}
            </a>
          )}
        />
        <Column
          columnKey="action-buttons"
          header={t('Action buttons')}
          cell={ row =>
            <FilterEdit
              id={`edit-filter-button-${row.id}`}
              filter={row}
              icon='fa-edit'
              buttonText='Edit Filter'
              onChange={setDisplayedFilters}
              openFilterId={props.openFilterId}
              editing
            />
          }

        />
      </Table>
    </TopPanel>
  );
}

export default hot(module)(withPageWrapper<Props>(ListFilters));
