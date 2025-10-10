import { useState } from "react";

import { useAsyncState } from "@etheryte/react-hooks";

import { Column, Table } from "components/table";

import { useDebounce } from "utils/hooks";

import { getPlaceholderDataWithSearch, PlaceholderRow } from "./search.example.placeholderData";

export default () => {
  const [criteria, setCriteria] = useState("");
  const data = useAsyncState(() => getPlaceholderDataWithSearch(criteria), [criteria]) ?? [];

  const onSearch = useDebounce((newCriteria) => setCriteria(newCriteria), 200);
  const identifier = (row: PlaceholderRow) => row.id;

  return (
    <Table data={data} identifier={identifier} onSearch={onSearch}>
      <Column columnKey="id" header={t("Item id")} cell={(row) => row.id} />
    </Table>
  );
};
