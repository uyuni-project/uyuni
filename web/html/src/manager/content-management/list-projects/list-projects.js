// @flow
import React, {useEffect} from 'react';
import {TopPanel} from 'components/panels/TopPanel';
import {Table, Column, SearchField} from 'components/table';
import Functions from 'utils/functions';
import {LinkButton} from 'components/buttons';
import { showSuccessToastr } from 'components/toastr/toastr';
import withPageWrapper from 'components/general/with-page-wrapper';

type ContentProjectOverviewType = {
  properties: {
    label: String,
    name: String,
    description: String,
  },
  environments: Array<String>,
  needRebuild: Boolean
};

type Props = {
  projects: Array<ContentProjectOverviewType>,
  flashMessage: String,
};

const ListProjects = (props: Props) => {

  useEffect(()=> {
    if(props.flashMessage) {
      showSuccessToastr(props.flashMessage)
    }
  }, [])

  const searchData = (row, criteria) => {
    const keysToSearch = ['name', 'description', 'environmentPath'];
    if (criteria) {
      return keysToSearch.map(key => row[key]).join().toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  const normalizedProjects = props.projects.map(project => ({
    label: project.properties.label,
    name: project.properties.name,
    description: project.properties.description,
    environmentPath: project.environments.join(' - ')
  }));

  const panelButtons = (
    <div className="pull-right btn-group">
      <LinkButton
        id="createcontentproject"
        icon="fa-plus"
        className="btn-link"
        title={t('Create a new content project')}
        text={t('Create Project')}
        href="/rhn/manager/contentmanagement/project"
      />
    </div>
  );

  return (
      <TopPanel title={t('Content Projects')} icon="spacewalk-icon-software-channels" button={panelButtons}>
        <Table
          data={normalizedProjects}
          identifier={row => row.label}
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
            cell={row =>
              <a href={`/rhn/manager/contentmanagement/project/${row.label}`}>
                {row.name}
              </a>
            }
          />
          <Column
            columnKey="description"
            comparator={Functions.Utils.sortByText}
            header={t('Description')}
            cell={row => row.description}
          />
          <Column
            columnKey="environmentPath"
            header={t('Environment Path')}
            cell={row => row.environmentPath}
          />
          <Column
            columnKey="name"
            cell={
              row => row.needRebuild
                && (
                  <LinkButton
                    className="btn-primary"
                    text="Re-Build"
                    href={`/rhn/manager/contentmanagement/project/${row.id}`}
                  />
                )
            }
          />
        </Table>
      </TopPanel>
    );
}

export default withPageWrapper<Props>(ListProjects);
