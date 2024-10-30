import { useEffect, useState } from "react";

import { Select } from "components/input";

const ALL_OPTION = { value: "ALL", label: t("All") };

export const SelectSearchField = ({ label, criteria, options, onSearch }) => {
  const [searchValue, setSearchValue] = useState(criteria || "");

  const handleSearchValueChange = (value) => {
    setSearchValue(value);
    onSearch?.(value === ALL_OPTION.value ? "" : value);
  };

  const allOptions = [ALL_OPTION].concat(options);

  // Avoid invalid value selected when changing field.
  useEffect(() => {
    if (!allOptions.some((it) => it.value === searchValue)) {
      handleSearchValueChange(ALL_OPTION.value);
    }
  }, [searchValue, allOptions, onSearch]);

  return (
    <Select
      name="selectSearchField"
      placeholder={label}
      defaultValue={searchValue}
      options={allOptions}
      onChange={(_name, value) => handleSearchValueChange(value)}
    />
  );
};
