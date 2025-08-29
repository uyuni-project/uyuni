import { useState } from "react";

import { Check } from "components/input";

export default () => {
  const [checked, setChecked] = useState(true);

  return <Check name="foo" checked={checked} onChange={setChecked} />;
};
