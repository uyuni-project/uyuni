import { useState } from "react";

import { Select } from "./Select";

export default () => {
  const [value, setValue] = useState<number | undefined>();

  const loadOptions = () => {
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve([
          {
            value: 1,
            label: "Level 1",
          },
          {
            value: 2,
            label: "Level 2",
          },
        ]);
      }, 1000);
    });
  };

  return (
    <>
      <p>
        Async data. To show a prefilled value, use <code>defaultValueOption</code> with a value that matches the
        expected schema.
      </p>
      <p>
        Value: <code>{value ? JSON.stringify(value) : typeof value}</code>
      </p>

      <Select loadOptions={loadOptions} value={value} onChange={(newValue) => setValue(newValue)} />
    </>
  );
};
