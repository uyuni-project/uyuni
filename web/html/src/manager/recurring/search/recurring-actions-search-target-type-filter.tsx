import { useState } from "react";

import { Select } from "components/input";

import { ALL_OPTION, TARGET_TYPE_OPTIONS } from "./recurring-actions-search-utils";

export const TargetTypeFilter = (props) => {
  const [targetType, setTargetType] = useState(ALL_OPTION.value);

  const handleTargetTypeChange = (value) => {
    setTargetType(value);
    props.onSearch?.(value === ALL_OPTION.value ? "" : value);
  };

  return (
    <Select
      name="targetTypeFilter"
      placeholder={t("Select a Target Type")}
      defaultValue={targetType}
      options={TARGET_TYPE_OPTIONS}
      onChange={(name, value) => handleTargetTypeChange(value)}
    />
  );
};
