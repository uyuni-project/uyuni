import { useState } from "react";

import { Select } from "components/input";

import { ACTION_TYPE_OPTIONS, ALL_OPTION } from "./recurring-actions-search-utils";

export const ActionTypeFilter = (props) => {
  const [actionType, setActionType] = useState(ALL_OPTION.value);
  const handleActionTypeChange = (value) => {
    setActionType(value);
    props.onSearch?.(value === ALL_OPTION.value ? "" : value);
  };
  return (
    <Select
      name="actionTypeFilter"
      placeholder={t("Select an Action Type")}
      defaultValue={actionType}
      options={ACTION_TYPE_OPTIONS}
      onChange={(name, value) => handleActionTypeChange(value)}
    />
  );
};
