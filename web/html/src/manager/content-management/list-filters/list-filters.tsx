import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useEffect, useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";
import { showSuccessToastr } from "components/toastr/toastr";
import withPageWrapper from "components/general/with-page-wrapper";
import FilterEdit from "./filter-edit";
import { mapResponseToFilterForm } from "./filter.utils";
import { FilterFormType, FilterServerType } from "../shared/type/filter.type";
import useRoles from "core/auth/use-roles";
import { isOrgAdmin } from "core/auth/auth.utils";
import { getValue } from "utils/data";

type Props = {
  filters: Array<FilterServerType>;
  openFilterId: number;
  projectLabel: string;
  flashMessage: string;
};

const ListFilters = (props: Props) => {
  const [displayedFilters, setDisplayedFilters] = useState<FilterFormType[]>(mapResponseToFilterForm(props.filters));
  const [selectedItems, setSelectedItems] = useState<string[]>([]);
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, []);

  const searchData = (row: FilterFormType, criteria?: string) => {
    const keysToSearch = ["filter_name", "projects.right"];
    if (criteria) {
      return keysToSearch
        .map(key => getValue(row, key))
        .filter(Boolean)
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  const onSelect = (identifiers: string[]) => {
    setSelectedItems(identifiers);
  };

  const onSelectUnused = () => {
    const unused = displayedFilters.filter(row => !row.projects?.length);
    setSelectedItems(unused.map(identifier));
  };

  const deletable = (row: FilterFormType) => {
    return !row.projects?.length;
  };

  const onDelete = (row: FilterFormType) => {
    console.log("delete row", row);
  };

  const identifier = row => row.filter_name;

  const sortProjects = (row: FilterFormType) => {
    return row.projects?.sort((a, b) => a.right?.toLowerCase().localeCompare(b.right?.toLowerCase())) ?? [];
  };

  const panelButtons = (
    <div className="pull-right btn-group">
      {hasEditingPermissions && (
        <FilterEdit
          id="create-filter-button"
          initialFilterForm={{ rule: "deny" }}
          icon="fa-plus"
          buttonText="Create Filter"
          openFilterId={props.openFilterId}
          projectLabel={props.projectLabel}
          onChange={responseFilters => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
        />
      )}
    </div>
  );

  const unusedFilter = (
    <button className="btn-link" onClick={onSelectUnused}>
      {t("Select unused")}
    </button>
  );

  return (
    <TopPanel
      title={t("Content Lifecycle Filters")}
      icon="fa-filter"
      button={panelButtons}
      helpUrl="reference/clm/clm-filters.html"
    >
      <Table
        data={displayedFilters}
        identifier={identifier}
        initialSortColumnKey="filter_name"
        initialItemsPerPage={window.userPrefPageSize}
        searchField={<SearchField filter={searchData} placeholder={t("Filter by name or project")} />}
        selectable={true}
        onSelect={onSelect}
        selectedItems={selectedItems}
        deletable={deletable}
        onDelete={onDelete}
        additionalFilters={[unusedFilter]}
      >
        <Column
          columnKey="filter_name"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={row => row.filter_name}
        />
        <Column
          columnKey="projects"
          header={t("Projects in use")} // {left: project label, right: project name}
          comparator={(aRow: FilterFormType, bRow: FilterFormType, _, sortDirection) => {
            const aProjects = sortProjects(aRow)
              .map(project => project.right)
              .join();
            const bProjects = sortProjects(bRow)
              .map(project => project.right)
              .join();
            return aProjects.localeCompare(bProjects) * sortDirection;
          }}
          cell={row =>
            sortProjects(row).map((p, index) => (
              <a
                className="project-tag-link js-spa"
                href={`/rhn/manager/contentmanagement/project/${p.left}`}
                key={`project-tag-link-${index}`}
              >
                {p.right}
              </a>
            ))
          }
        />
        <Column
          columnKey="action-buttons"
          header={t("")}
          cell={row =>
            hasEditingPermissions && (
              <FilterEdit
                id={`edit-filter-button-${row.id}`}
                initialFilterForm={row}
                icon="fa-edit"
                buttonText="Edit Filter"
                onChange={responseFilters => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
                openFilterId={props.openFilterId}
                projectLabel={props.projectLabel}
                editing
              />
            )
          }
        />
      </Table>
    </TopPanel>
  );
};

export default hot(withPageWrapper<Props>(ListFilters));
