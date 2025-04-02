import * as React from "react";
import { useState } from "react";

import { ModalButton } from "components/dialog/ModalButton";
import { Dialog } from "components/dialog/Dialog";
import { Button } from "components/buttons";

type Props = {
  id?: string;
  title?: string;
  creatingText?: string;
  onSave?: Function;
  renderContent?: React.ReactNode;
  disableEditing?: boolean;
  className?: string;
  onCancel?: Function;
  onOpen?: Function;
  collapsible?: boolean;
  icon?: string;
  customIconClass?: string;
};



const EditAnsibleVars = (props: Props) => {
  const [open, setOpen] = useState(false);
  const [errors, setErrors] = useState(null);

  return (
    <>
      <ModalButton
        id="edit-playbbok-vars"
        text={t("Edit variables Component")}
        target="playbbok-vars"
        className="btn-default"
        onClick={() => {
          setOpen(true);
        }}
      />
      <Dialog
        id="playbbok-vars"
        isOpen={open}
        title="Edit Variables Component"
        className="modal-lg"
        content={props.renderContent}
        onClose={() => setOpen(false)}
        footer={
          <React.Fragment>
            <div className="btn-group col-lg-6">
            </div>
            <div className="col-lg-6">
              <div className="pull-right btn-group">
                <Button
                  id={`modal-cancel-button`}
                  className="btn-default"
                  text={t("Cancel")}
                  handler={() => {
                    setOpen(false);
                  }}
                />
                <Button
                  id={`modal-cancel-button`}
                  className="btn-default"
                  text={t("Save")}
                  handler={() => {
                    setOpen(false);
                  }}
                />
              </div>
            </div>
          </React.Fragment>
        }
      />
    </>
  );
};

export default EditAnsibleVars;
