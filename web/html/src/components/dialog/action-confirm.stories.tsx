import * as React from "react";

import { Button } from "../buttons";
import { ActionConfirm } from "./ActionConfirm";

export default () => {
  const [open, setOpen] = React.useState(false);
  const items = [{ name: "Item 1" }, { name: "Item 2" }, { name: "Item 3" }];
  const doAction = (type, toAct, params) => {
    const names = toAct.map((obj) => obj.name).join();
    alert(`doAction: ${type}, ${names}, force? ${params.force || false}`);
  };

  return (
    <>
      <p>Action confirmation dialog:</p>
      <div>
        <div style={{ height: "100px", display: "flex", alignItems: "center", justifyContent: "center" }}>
          <Button
            id={`confirm-modal-link`}
            text="delete (Click me for Testing the modal!!)"
            handler={() => setOpen(true)}
          />
        </div>
        <ActionConfirm
          id="confirm-modal"
          type="remove"
          name="Remove"
          itemName="item"
          icon="fa-trash"
          selected={items}
          onConfirm={doAction}
          canForce={true}
          forceName="Purge"
          isOpen={open}
          onClose={() => setOpen(false)}
        />
      </div>
    </>
  );
};
