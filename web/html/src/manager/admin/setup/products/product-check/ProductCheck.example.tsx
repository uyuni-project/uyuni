import { useState } from "react";

import { Button } from "components/buttons";

import { type ProductSelectionState } from "./product-selection.utils";
import { ProductCheck } from "./ProductCheck";

export default () => {
  const [selectionState, setSelectionState] = useState<ProductSelectionState>("partially");
  const [disabled, setDisabled] = useState(false);

  return (
    <div style={{ display: "grid", gap: "12px", justifyItems: "start" }}>
      <label>
        <ProductCheck
          selectionState={selectionState}
          disabled={disabled}
          onChange={() => {
            setSelectionState((prev) => (prev === "checked" ? "unchecked" : "checked"));
          }}
        />{" "}
        click to toggle
      </label>

      <div className="btn-group">
        <Button
          className="btn-default"
          text={selectionState === "checked" ? "set unchecked" : "set checked"}
          handler={() => setSelectionState((prev) => (prev === "checked" ? "unchecked" : "checked"))}
        />
        <Button className="btn-default" text="set partially" handler={() => setSelectionState("partially")} />
        <Button
          className="btn-default"
          text={disabled ? "enable" : "disable"}
          handler={() => setDisabled((prev) => !prev)}
        />
      </div>

      <p>
        State: <code>selectionState={selectionState}</code> <code>disabled={String(disabled)}</code>
      </p>
    </div>
  );
};
