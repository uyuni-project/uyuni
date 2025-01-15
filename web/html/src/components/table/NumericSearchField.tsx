import { useState } from "react";

import { Select } from "components/input";

type Matcher = {
  label: string;
  value: string;
};

const matchers: Matcher[] = [
  { label: t("less than"), value: "<" },
  { label: t("less than or equal to"), value: "<=" },
  { label: t("equal to"), value: "=" },
  { label: t("greater than or equal to"), value: ">=" },
  { label: t("greater than"), value: ">" },
  { label: t("not equal to"), value: "!=" },
];

const resultingCriteria = (matcher: string, value: string) => {
  return matcher && value && value.trim() !== "" ? matcher + value : null;
};

export const NumericSearchField = ({ name, criteria, onSearch }) => {
  let criteriaMatcher = "";
  let criteriaValue = "";
  if (criteria) {
    criteriaMatcher = matchers.find((it) => criteria.startsWith(it.value))?.value ?? "=";
    criteriaValue = criteria.includes(criteriaMatcher) ? criteria.split(criteriaMatcher)[1] : criteria;
  }

  const [matcher, setMatcher] = useState<string>(criteriaMatcher);
  const [value, setValue] = useState<string>(criteriaValue);

  const handleMatcherChange = (selectedMatcher: string) => {
    setMatcher(selectedMatcher);
    onSearch(resultingCriteria(selectedMatcher, value));
  };
  const handleValueChange = (newValue: string) => {
    setValue(newValue);
    onSearch(resultingCriteria(matcher, newValue));
  };
  return (
    <div className="row">
      <div className="col-sm-6">
        <Select
          name="matcher"
          placeholder={t("Matcher")}
          defaultValue={matcher}
          options={matchers}
          onChange={(_name: string | undefined, value: string) => handleMatcherChange(value)}
        />
      </div>

      <div className="col">
        <input
          className="form-control"
          value={value || ""}
          type="number"
          onChange={(e) => handleValueChange(e.target.value)}
          name={name}
        />
      </div>
    </div>
  );
};
