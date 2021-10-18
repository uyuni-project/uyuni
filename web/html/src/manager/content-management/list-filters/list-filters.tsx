import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useEffect, useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { Utils } from "utils/functions";
import { showSuccessToastr, showErrorToastr } from "components/toastr/toastr";
import withPageWrapper from "components/general/with-page-wrapper";
import FilterEdit from "./filter-edit";
import { mapFilterFormToRequest, mapResponseToFilterForm } from "./filter.utils";
import { FilterFormType, FilterServerType } from "../shared/type/filter.type";
import useRoles from "core/auth/use-roles";
import { isOrgAdmin } from "core/auth/auth.utils";
import { getValue } from "utils/data";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import { Button } from "components/buttons";
import { getUrlParam } from "utils/url";

type Props = {
  filters: Array<FilterServerType>;
  flashMessage: string;
};

const ListFilters = (props: Props) => {
  const { onAction } = useLifecycleActionsApi({ resource: "filters" });

  const [displayedFilters, setDisplayedFilters] = useState<FilterFormType[]>(mapResponseToFilterForm(props.filters));
  const [selectedIdentifiers, setSelectedIdentifiers] = useState<string[]>([]);
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useEffect(() => {
    if (props.flashMessage) {
      showSuccessToastr(props.flashMessage);
    }
  }, [props.flashMessage]);

  const searchData = (row: FilterFormType, criteria?: string) => {
    const keysToSearch = ["filter_name", "projects.right"];
    if (criteria) {
      return keysToSearch
        .map((key) => getValue(row, key))
        .filter(Boolean)
        .join()
        .toLowerCase()
        .includes(criteria.toLowerCase());
    }
    return true;
  };

  const onSelect = (identifiers: string[]) => {
    setSelectedIdentifiers(identifiers);
  };

  const onSelectUnused = () => {
    const unused = displayedFilters.filter((row) => !row.projects?.length);
    setSelectedIdentifiers(unused.map(identifier));
  };

  const isDeletable = (row: FilterFormType) => {
    return !row.projects?.length;
  };

  const deleteRow = async (row: FilterFormType) => {
    try {
      const remainingFilters = await onAction(mapFilterFormToRequest(row), "delete", row.id?.toString());
      setDisplayedFilters(mapResponseToFilterForm(remainingFilters));
      const remainingSelection = selectedIdentifiers.filter((item) => item !== identifier(row));
      setSelectedIdentifiers(remainingSelection);
    } catch (error) {
      showErrorToastr(error?.messages ?? error);
    }
  };

  const deleteSelectedRows = async () => {
    const rows = displayedFilters.filter((row) => selectedIdentifiers.includes(identifier(row)));
    if (!rows.every(isDeletable)) {
      showErrorToastr(t("Some of the selected filters are used in projects and can not be deleted."));
      return;
    }
    try {
      await Promise.all(rows.map((row) => onAction(mapFilterFormToRequest(row), "delete", row.id?.toString())));
      setSelectedIdentifiers([]);

      const remainingFilters = await onAction(undefined, "get");
      setDisplayedFilters(mapResponseToFilterForm(remainingFilters));
    } catch (error) {
      showErrorToastr(error?.messages ?? error);
    }
  };

  const identifier = (row) => row.filter_name;

  const sortProjects = (row: FilterFormType) => {
    return row.projects?.sort((a, b) => a.right?.toLowerCase().localeCompare(b.right?.toLowerCase())) ?? [];
  };

  const openFilterId = getUrlParam("openFilterId", Number);
  const projectLabel = getUrlParam("projectLabel");
  const openTemplate = getUrlParam("openTemplate");
  const systemId = getUrlParam("systemId", Number);
  const systemName = getUrlParam("systemName");
  const kernelName = getUrlParam("kernelName");

  const initialFilterForm = {
    rule: "deny",
    labelPrefix: projectLabel,
    template: openTemplate,
    systemId,
    systemName,
    kernelName,
  };

  const panelButtons = (
    <div className="pull-right btn-group">
      {hasEditingPermissions && (
        <FilterEdit
          id="create-filter-button"
          initialFilterForm={initialFilterForm}
          icon="fa-plus"
          buttonText="Create Filter"
          openFilterId={openFilterId}
          projectLabel={projectLabel}
          onChange={(responseFilters) => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
        />
      )}
    </div>
  );

  const unusedFilter = <Button className="btn-link" handler={onSelectUnused} text={t("Select unused")}></Button>;

  const deleteSelected = (
    <Button
      className={`${selectedIdentifiers.length ? "btn-danger" : "btn-disabled"}`}
      disabled={!selectedIdentifiers.length}
      handler={deleteSelectedRows}
      text={t("Delete selected")}
    />
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
        selectedItems={selectedIdentifiers}
        deletable={isDeletable}
        onDelete={deleteRow}
        additionalFilters={[unusedFilter, deleteSelected]}
      >
        <Column
          columnKey="filter_name"
          comparator={Utils.sortByText}
          header={t("Name")}
          cell={(row) => row.filter_name}
        />
        <Column
          columnKey="projects"
          header={t("Projects in use")} // {left: project label, right: project name}
          comparator={(aRow: FilterFormType, bRow: FilterFormType, _, sortDirection) => {
            const aProjects = sortProjects(aRow)
              .map((project) => project.right)
              .join();
            const bProjects = sortProjects(bRow)
              .map((project) => project.right)
              .join();
            return aProjects.localeCompare(bProjects) * sortDirection;
          }}
          cell={(row) =>
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
          cell={(row) =>
            hasEditingPermissions && (
              <FilterEdit
                id={`edit-filter-button-${row.id}`}
                initialFilterForm={row}
                icon="fa-edit"
                buttonText="Edit Filter"
                onChange={(responseFilters) => setDisplayedFilters(mapResponseToFilterForm(responseFilters))}
                openFilterId={openFilterId}
                projectLabel={projectLabel}
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
