import { useState } from "react";

import { Check } from "components/input";

export default () => {
  const [checked, setChecked] = useState(true);

  return (
    <>
      <p>
        value: <code>{JSON.stringify(checked)}</code>
      </p>
      <Check checked={checked} onChange={setChecked} />
    </>
  );
};
