import { useState } from "react";

import { Select } from "./Select";

export default () => {
  const [value, setValue] = useState<string | undefined>();
  const options = [
    {
      fooLabelValue: t("Beginner"),
      nested: {
        value: "beginner",
      },
    },
    {
      fooLabelValue: t("Normal"),
      nested: {
        value: "normal",
      },
    },
    {
      fooLabelValue: t("Expert"),
      nested: {
        value: "expert",
      },
    },
  ];

  return (
    <>
      <p>
        Get values and labels from custom fields: <code>{value ? JSON.stringify(value) : typeof value}</code>
      </p>

      <Select
        value={value}
        onChange={(newValue) => setValue(newValue)}
        options={options}
        getOptionLabel={(item) => item.fooLabelValue}
        getOptionValue={(item) => item.nested.value}
        isClearable
      />
    </>
  );
};
