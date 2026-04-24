import { useState } from "react";

import { useAsyncState } from "@etheryte/react-hooks";

import { Column, Table } from "components/table";

import { useDebounce } from "utils/hooks";

import { getPlaceholderDataWithSearch, PlaceholderRow } from "./search.example.placeholderData";
import { Button } from "components/buttons";
import { DEPRECATED_Check } from "components/input";
const dataNo = [];
export default () => {
  const [criteria, setCriteria] = useState("");
  const data = useAsyncState(() => getPlaceholderDataWithSearch(criteria), [criteria]) ?? [];

  const onSearch = useDebounce((newCriteria) => setCriteria(newCriteria), 50);
  const identifier = (row: PlaceholderRow) => row.id;

  const actionButtons = [
    <div key="filter-action-buttons" className="btn-group">
      <Button className="btn-default" text={t("Add")}></Button>
      <Button className="btn-danger" text={t("Delete")}></Button>
    </div>,
  ];

  const namespacesFilter = (
    <div className="d-flex">
      <div className="ms-4">
        <div className="d-flex">
          {/* <span className="control-label me-3">Filter by:</span> */}
          <span className="me-4">
            <DEPRECATED_Check label={t("API")} name="apiNamespace" key="apiNamespace" />
          </span>
          <span className="me-4">
            <DEPRECATED_Check label={t("Web")} name="webNamespace" key="webNamespace" />
          </span>
          <span>
            <DEPRECATED_Check label={t("Only selected")} name="showOnlySelected" key="showOnlySelected" />
          </span>
        </div>
      </div>
    </div>
  );

  return (
    <>
      <h4>Table header with search and bottom pagination</h4>
      <Table data={data} identifier={identifier} onSearch={onSearch}>
        <Column columnKey="id" header={t("Item id")} cell={(row) => row.id} />
        <Column columnKey="name" header={t("Item name")} cell={(row) => row.name} />
      </Table>
      <h4>Table header with search and bulk action buttons</h4>
      <p>
        <code>titleButtons</code> places bulk action buttons in the table header (top-right or top area).
      </p>{" "}
      <Table data={dataNo} onSearch={onSearch} titleButtons={actionButtons}></Table>
      <h4>Table header with search and inline filters</h4>
      <p>
        <code>searchPanelInline</code>controls whether search and additionalFilters appear in one row; otherwise,
        additionalFilters are shown below the search bar.
      </p>
      <Table data={dataNo} onSearch={onSearch} searchPanelInline additionalFilters={[namespacesFilter]}></Table>
    </>
  );
};
