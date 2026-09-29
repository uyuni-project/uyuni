import { useState } from "react";

import { useAsyncState } from "@etheryte/react-hooks";

import { Button } from "components/buttons";
import { CheckFilterGroup, Column, Table } from "components/table";

import { useDebounce } from "utils/hooks";

import { getPlaceholderDataWithSearch, PlaceholderRow } from "./search.example.placeholderData";
const dataNo = [];
export default () => {
  const [criteria, setCriteria] = useState("");
  const [apiNamespace, setApiNamespace] = useState(false);
  const [webNamespace, setWebNamespace] = useState(false);
  const [onlySelected, setOnlySelected] = useState(false);
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
    <CheckFilterGroup
      key="namespaces-filter"
      className="ms-4"
      options={[
        { label: t("API"), checked: apiNamespace, onChange: setApiNamespace },
        { label: t("Web"), checked: webNamespace, onChange: setWebNamespace },
        { label: t("Only selected"), checked: onlySelected, onChange: setOnlySelected },
      ]}
    />
  );

  return (
    <>
      <h4>Table header with search and bottom pagination</h4>
      <Table data={data} identifier={identifier} onSearch={onSearch}>
        <Column columnKey="id" header={t("Item id")} cell={(row) => row.id} />
        <Column columnKey="name" header={t("Item name")} cell={(row) => row.name} />
      </Table>
      <h4 className="mt-5">Table header with search and bulk action buttons</h4>
      <p>
        <code>titleButtons</code> places bulk action buttons in the table header (top-right or top area).
      </p>
      <Table data={dataNo} identifier={(row) => row.id} onSearch={onSearch} titleButtons={actionButtons}>
        <Column columnKey="id" header="Item id" cell={(row) => row.id} />
        <Column columnKey="name" header="Item name" cell={(row) => row.name} />
      </Table>
      <h4 className="mt-5">Table header with search and inline filters</h4>
      <p>
        <code>searchPanelInline</code>controls whether search and additionalFilters appear in one row; otherwise,
        additionalFilters are shown below the search bar.
      </p>
      <Table
        data={dataNo}
        identifier={(row) => row.id}
        onSearch={onSearch}
        searchPanelInline
        additionalFilters={[namespacesFilter]}
      >
        <Column columnKey="id" header="Item id" cell={(row) => row.id} />
        <Column columnKey="name" header="Item name" cell={(row) => row.name} />
      </Table>
    </>
  );
};
