import { useState } from "react";

import { Button } from "components/buttons";
import { Check } from "components/input";

export default () => {
  const [checked, setChecked] = useState(false);
  const [indeterminate, setIndeterminate] = useState(true);

  return (
    <div style={{ display: "grid", gap: "12px", justifyItems: "start" }}>
      <label>
        <Check checked={checked} indeterminate={indeterminate} onChange={setChecked} /> click to toggle
      </label>

      <div className="btn-group">
        <Button
          className="btn-default"
          text={indeterminate ? "clear indeterminate" : "set indeterminate"}
          handler={() => setIndeterminate((prev) => !prev)}
        />
        <Button
          className="btn-default"
          text={checked ? "uncheck" : "check"}
          handler={() => setChecked((prev) => !prev)}
        />
      </div>

      <p>
        State: <code>checked={String(checked)}</code> <code>indeterminate={String(indeterminate)}</code>
      </p>
    </div>
  );
};
