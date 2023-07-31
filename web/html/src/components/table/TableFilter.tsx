import { useState } from "react";

import { Select } from "components/input";
import { Form } from "components/input/Form";
import { SelectSearchField } from "components/table/SelectSearchField";

const renderSearchField = (props) => {
  const { field } = props;
  const selectedOption = props.filterOptions.find((it) => it.value === field);
  if (selectedOption?.filterOptions) {
    return <SelectSearchField label={selectedOption.label} options={selectedOption.filterOptions} {...props} />;
  }
  return (
    <div className="form-group">
      <input
        className="form-control"
        value={props.criteria || ""}
        placeholder={props.placeholder}
        type="text"
        onChange={(e) => props.onSearch?.(e.target.value)}
        name={props.name}
      />
    </div>
  );
};

export const TableFilter = (props) => {
  // Dummy model and onChange to reuse the Select component as it requires a Form
  let model = {};
  const onChange = () => {};

  const [selectedFilter, setSelectedFilter] = useState(props.field || "");
  const handleChangeFilter = (value) => {
    setSelectedFilter(value);
    props.onSearchField?.(value);
  };

  return (
    <Form model={model} onChange={onChange} title={t("Filter")} className="row">
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
