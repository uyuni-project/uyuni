import * as React from "react";
import { useState } from "react";
import yaml from 'js-yaml';

import { ModalButton } from "components/dialog/ModalButton";
import { Dialog } from "components/dialog/Dialog";
import { Button } from "components/buttons";
import AnsibleVarYamlEditor from "./ansible-var-yaml-editor"

type Props = {
  id: string;
  title?: string;
  creatingText?: string;
  onSave?: Function;
  renderContent?: React.ReactNode;
  disableEditing?: boolean;
  onCancel?: Function;
  onOpen?: Function;
  collapsible?: boolean;
  icon?: string;
  customIconClass?: string;
};

const EditAnsibleVarsModal = (props: Props) => {
  const data = yaml.load(props.renderContent);
  const varsObject = data[0].vars;

  const [open, setOpen] = useState(false);
  const onCreateToken = () => {
    console.log(varsObject);
  }
  return (
    <>
      <ModalButton
        id="edit-playbbok-vars"
        text={t("Edit variables")}
        target="playbbok-vars"
        className="btn-default"
        onClick={() => {
          setOpen(true);
        }}
      />
      <Dialog
        id={props.id}
        isOpen={open}
        title="Edit Variables"
        className="modal-lg"
        content={<AnsibleVarYamlEditor data={varsObject} />}
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
                  id={`modal-save-button`}
                  className="btn-primary"
                  text={t("Save")}
                  handler={() => {
                    setOpen(false);
                    onCreateToken()
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

export default EditAnsibleVarsModal;

