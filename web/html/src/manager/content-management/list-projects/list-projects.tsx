import { hot } from "react-hot-loader/root";

import * as React from "react";
import { useEffect } from "react";

import _truncate from "lodash/truncate";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import { LinkButton } from "components/buttons";
import { FromNow } from "components/datetime/FromNow";
import withPageWrapper from "components/general/with-page-wrapper";
import { ServerMessageType } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { showSuccessToastr } from "components/toastr/toastr";

import { Utils } from "utils/functions";

type ContentProjectOverviewType = {
  properties: {
    label: String;
    name: String;
    description: String;
    lastBuildDate: moment.Moment;
  };
  environments: Array<String>;
  needRebuild: Boolean;
};

type Props = {
  projects: Array<ContentProjectOverviewType>;
  flashMessage?: ServerMessageType;
};

const ListProjects = (props: Props) => {
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  const searchData = (row, criteria) => {
    const keysToSearch = ["name", "description", "environmentLifecycle"];
    if (criteria) {
      return keysToSearch
        .map((key) => row[key])
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  const normalizedProjects = props.projects.map((project) => ({
    label: project.properties.label,
    name: project.properties.name,
    description: project.properties.description,
    lastBuildDate: project.properties.lastBuildDate,
    environmentLifecycle: project.environments.join(" > "),
  }));

  const panelButtons = (
    <div className="pull-right btn-group">
      {hasEditingPermissions && (
        <LinkButton
          id="createcontentproject"
          icon="fa-plus"
          className="btn-link js-spa"
          title={t("Create a new content lifecycle project")}
          text={t("Create Project")}
          href="/rhn/manager/contentmanagement/project"
        />
      )}
    </div>
  );

  return (
    <TopPanel
      title={t("Content Lifecycle Projects")}
      icon="spacewalk-icon-lifecycle"
      button={panelButtons}
      helpUrl="reference/clm/clm-projects.html"
    >
      <Table
        data={normalizedProjects}
        identifier={(row) => row.label}
        initialSortColumnKey="name"
        searchField={<SearchField filter={searchData} placeholder={t("Filter by any value")} />}
      >
        <Column
          columnKey="name"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(row) => (
            <a className="js-spa" href={`/rhn/manager/contentmanagement/project/${row.label}`}>
              {row.name}
            </a>
          )}
        />
        <Column
          columnKey="description"
          comparator={Utils.sortByText}
          header={t("Description")}
          cell={(row) => _truncate(row.description, { length: 120 })}
        />
        <Column
          columnKey="lastBuildDate"
          comparator={Utils.sortByDate}
          header={t("Last Build")}
          cell={(row) => (row.lastBuildDate ? <FromNow value={row.lastBuildDate} /> : t("never"))}
        />
        <Column
          columnKey="environmentLifecycle"
          header={t("Environment Lifecycle")}
          cell={(row) => row.environmentLifecycle}
        />
      </Table>
    </TopPanel>
  );
};

export default hot(withPageWrapper(ListProjects));
