import { useState } from "react";

import { DEPRECATED_Select } from "components/input";
import { Form } from "components/input/form/Form";
import { SelectSearchField } from "components/table/SelectSearchField";

import { NumericSearchField } from "./NumericSearchField";

export enum FilterOptionType {
  TEXT,
  SELECT,
  NUMERIC,
}

type FilterOption = {
  label: string;
  value: string;
  type?: FilterOptionType;
  filterOptions?: any[];
};

type SearchFieldProps = {
  filterOptions: FilterOption[];
  field: any;
  criteria: any;
  onSearch: any;
  placeholder: any;
  name: any;
};

const renderSearchField = (props: SearchFieldProps) => {
  const { filterOptions, field, criteria, onSearch, placeholder, name } = props;
  const selectedOption = filterOptions.find((it) => it.value === field);
  if (selectedOption?.type === FilterOptionType.SELECT) {
    return (
      <SelectSearchField
        label={selectedOption.label}
        options={selectedOption.filterOptions}
        criteria={criteria}
        onSearch={onSearch}
      />
    );
  }

  if (selectedOption?.type === FilterOptionType.NUMERIC) {
    return <NumericSearchField name={name} criteria={criteria} onSearch={onSearch} />;
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
        <DEPRECATED_Select
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
