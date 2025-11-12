import * as React from "react";
import { useState, useRef } from "react";

import yaml from "js-yaml";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { ModalButton } from "components/dialog/ModalButton";
import { showErrorToastr } from "components/toastr/toastr";

import AnsibleVarYamlEditor from "./ansible-var-yaml-editor";
import styles from "./Ansible.module.scss";

type Props = {
  id: string;
  className?: string;
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
  updatePlaybookContent?: Function;
};

const EditAnsibleVarsModal = (props: Props) => {
  const data = yaml.load(props.renderContent);
  const varsObject = data[0].vars;

  // State to store updated YAML and extra added variables
  const [extraVars, setExtraVars] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const editorRef = useRef<any>(null);

  const onSave = () => {
    try {
      const savedVars = editorRef.current?.getValues?.();
      if (extraVars) {
        const parsedExtraVars = yaml.load(extraVars);
        if (typeof parsedExtraVars !== "object" || parsedExtraVars === null) {
          showErrorToastr("Additonal Variables must be a valid YAML object.", {
            autoHide: false,
            containerId: "extra-var",
          });
          setOpen(true);
          return;
        }
      }
      props.updatePlaybookContent?.(savedVars, extraVars);
      setOpen(false);
    } catch (error) {
      if (error instanceof Error) {
        showErrorToastr(`YAML Error: ${error.message}`, {
          autoHide: false,
          containerId: "extra-var",
        });
      }
    }
  };
  return (
    <>
      <ModalButton
        id="edit-playbook-vars"
        text={t("Edit variables")}
        target="playbook-vars"
        className="btn-default"
        onClick={() => {
          setOpen(true);
        }}
      />
      <Dialog
        id={props.id}
        isOpen={open}
        title="Edit Variables"
        className={`modal-lg ${styles.ansibleModal} ${props.className}`}
        content={
          <AnsibleVarYamlEditor ref={editorRef} data={varsObject} onExtraVarChange={setExtraVars} />
        }
        onClose={() => setOpen(false)}
        footer={
          <React.Fragment>
            <div className="btn-group col-lg-6"></div>
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
                    onSave();
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
