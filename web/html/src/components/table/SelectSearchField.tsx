import { useState } from "react";

import { Select } from "components/input";

const ALL_OPTION = { value: "ALL", label: t("All") };

export const SelectSearchField = (props) => {
  const [searchValue, setSearchValue] = useState(props.criteria || "");

  const handleSearchValueChange = (value) => {
    setSearchValue(value);
    props.onSearch?.(value === ALL_OPTION.value ? "" : value);
  };

  const options = [ALL_OPTION].concat(props.options);

  return (
    <Select
      name="selectSearchField"
      placeholder={props.label}
      defaultValue={searchValue}
      options={options}
      onChange={(_name, value) => handleSearchValueChange(value)}
    />
  );
};
