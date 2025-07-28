import { useState } from "react";

import { DEPRECATED_Select } from "components/input";

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

const decodeHTMLEntities = (str) => {
  const txt = document.createElement("textarea");
  txt.innerHTML = str;
  return txt.value;
};

export const NumericSearchField = ({ name, criteria, onSearch }) => {
  let criteriaMatcher = "";
  let criteriaValue = "";
  if (criteria) {
    const criteriaDecoded = decodeHTMLEntities(criteria);
    const bestMatcher = matchers.reduce((bestFound, currentMatcher) => {
      // Find the longest matcher that is a prefix of the criteria string.
      // It prevents to take "<" matcher when it should be "<="
      if (criteriaDecoded.startsWith(currentMatcher.value) && currentMatcher.value.length > bestFound.length) {
        return currentMatcher.value;
      }
      return bestFound;
    }, "");
    criteriaMatcher = bestMatcher || "=";
    criteriaValue = criteriaDecoded.substring(criteriaMatcher.length);
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
        <DEPRECATED_Select
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
