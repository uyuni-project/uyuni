import { TableFilter } from "components/table/TableFilter";

import { SEARCH_FIELD_OPTIONS } from "./recurring-actions-search-utils";

export const RecurringActionsSearch = (props) => {
  return <TableFilter filterOptions={SEARCH_FIELD_OPTIONS} {...props} />;
};
