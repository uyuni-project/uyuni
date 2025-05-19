import { useState } from "react";

import { Select } from "./Select";

export default () => {
  const [value, setValue] = useState<string[] | undefined>(["beginner", "expert"]);
  const options = [
    {
      label: t("Beginner"),
      value: "beginner",
    },
    {
      label: t("Normal"),
      value: "normal",
    },
    {
      label: t("Expert"),
      value: "expert",
    },
  ];

  return (
    <>
      <p>Multiple values</p>
      <p>
        Value: <code>{value ? JSON.stringify(value) : typeof value}</code>
      </p>

      <Select isMulti value={value} onChange={(newValue) => setValue(newValue)} options={options} isClearable />
    </>
  );
};
