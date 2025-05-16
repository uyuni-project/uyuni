import { useState } from "react";

import { Select } from "./Select";

export default () => {
  const [value, setValue] = useState<string[]>([]);
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
      <p>
        Multiple values: <code>{value ? JSON.stringify(value) : typeof value}</code>
      </p>

      <Select value={value} onChange={(newValue) => setValue(newValue)} options={options} isClearable />
    </>
  );
};
