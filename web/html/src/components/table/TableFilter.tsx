import { useState } from "react";

import { Select } from "components/input";
import { Form } from "components/input/form/Form";
import { SelectSearchField } from "components/table/SelectSearchField";

const renderSearchField = ({ filterOptions, field, criteria, onSearch, placeholder, name }) => {
  const selectedOption = filterOptions.find((it) => it.value === field);
  if (selectedOption?.filterOptions) {
    return (
      <SelectSearchField
        label={selectedOption.label}
        options={selectedOption.filterOptions}
        criteria={criteria}
        onSearch={onSearch}
      />
    );
  }
  return (
    <div className="form-group">
      <input
        className="form-control"
        value={criteria || ""}
        placeholder={placeholder}
        type="text"
        onChange={(e) => onSearch?.(e.target.value)}
        name={name}
      />
    </div>
  );
};

export const TableFilter = (props) => {
  // Dummy model and onChange to reuse the Select component as it requires a Form
  const model = {};
  const onChange = () => {};

  const [selectedFilter, setSelectedFilter] = useState(props.field || "");
  const handleChangeFilter = (value) => {
    setSelectedFilter(value);
    props.onSearchField?.(value);
  };

  return (
    <Form model={model} onChange={onChange} title={t("Filter")} divClass="row">
      <div className="col-sm-4">
        <Select
          name="filter"
          placeholder={t("Select a filter")}
          defaultValue={selectedFilter}
          options={props.filterOptions}
          onChange={(_name: string | undefined, value: string) => handleChangeFilter(value)}
        />
      </div>
      <div className="col-sm-6">{props.field && renderSearchField(props)}</div>
    </Form>
  );
};
