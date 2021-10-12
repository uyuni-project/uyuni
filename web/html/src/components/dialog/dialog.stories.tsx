import * as React from "react";
import { storiesOf } from "@storybook/react";
import { DeleteDialog } from "./DeleteDialog";
import { ActionConfirm } from "./ActionConfirm";
import { ModalLink } from "./ModalLink";
import { Button } from "../buttons";

storiesOf("Dialogs", module)
  .add("delete dialog", () => (
    <div>
      <div style={{ height: "100px", display: "flex", alignItems: "center", justifyContent: "center" }}>
        <ModalLink id={`delete-modal-link`} text="delete (Click me for Testing the modal!!)" target="delete-modal" />
      </div>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete project")}
        content={
          <span>
            {t("Are you sure you want to delete project ")}
            <strong>{"CLM2"}</strong>
          </span>
        }
        onConfirm={() => alert("deleted pressed")}
        onClosePopUp={() => alert("modal closed")}
      />
    </div>
  ))
  .add("Action confirmation dialog", () => {
    const [open, setOpen] = React.useState(false);
    const items = [{ name: "Item 1" }, { name: "Item 2" }, { name: "Item 3" }];
    const doAction = (type, toAct, params) => {
      const names = toAct.map((obj) => obj.name).join();
      alert(`doAction: ${type}, ${names}, force? ${params.force || false}`);
    };
    return (
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
    );
  });
