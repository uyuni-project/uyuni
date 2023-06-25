import { useState } from "react";

import { Select } from "components/input";
import { Form } from "components/input/Form";

import { ActionTypeFilter } from "./recurring-actions-search-action-type-filter";
import { TargetTypeFilter } from "./recurring-actions-search-target-type-filter";
import { ACTION_TYPE_OPTION, SEARCH_FIELD_OPTIONS, TARGET_TYPE_OPTION } from "./recurring-actions-search-utils";

const renderSearchField = (props) => {
  const { field } = props;
  if (field === TARGET_TYPE_OPTION.value) {
    return <TargetTypeFilter {...props} />;
  } else if (field === ACTION_TYPE_OPTION.value) {
    return <ActionTypeFilter {...props} />;
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

export const RecurringActionsSearch = (props) => {
  // Dummy model and onChange to reuse the Select component as it requires a Form
  let model = {};
  const onChange = () => {};

  const [filterValue, setFilterValue] = useState("");
  const handleChangeSearchField = (value) => {
    setFilterValue(value);
    props.onSearchField?.(value);
  };

  return (
    <Form model={model} onChange={onChange} title={t("Filter")} className="row">
      <div className="col-sm-4">
        <Select
          name="filter"
          placeholder={t("Select a filter")}
          defaultValue={filterValue}
          options={SEARCH_FIELD_OPTIONS}
          inputClass="col-sm-12"
          onChange={(name: string | undefined, value: string) => handleChangeSearchField(value)}
        />
      </div>
      <div className="col-sm-6">{props.field && renderSearchField(props)}</div>
    </Form>
  );
};
