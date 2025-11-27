import { useEffect, useRef, useState, Fragment } from "react";

import yaml from "js-yaml";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { ModalButton } from "components/dialog/ModalButton";
import { showErrorToastr } from "components/toastr/toastr";

import styles from "./Ansible.module.scss";
import AnsibleVarYamlEditor from "./ansible-var-yaml-editor";

type Props = {
  id: string;
  className?: string;
  title?: string;
  creatingText?: string;
  onSave?: () => void;
  renderContent?: React.ReactNode;
  disableEditing?: boolean;
  onCancel?: () => void;
  onOpen?: () => void;
  collapsible?: boolean;
  icon?: string;
  customIconClass?: string;
  updatePlaybookContent?: () => void;
};

const EditAnsibleVarsModal = (props: Props) => {
  // State to store updated YAML and extra added variables
  const [extraVars, setExtraVars] = useState<string | null>(null);
  const [open, setOpen] = useState(false);
  const [editorData, setEditorData] = useState<any>(null);

  const editorRef = useRef<any>(null);

  useEffect(() => {
    if (open) {
      try {
        const data = yaml.load(props.renderContent);
        setEditorData(data?.[0]?.vars || {});
        setExtraVars(null);
      } catch (e) {
        showErrorToastr("Invalid playbook YAML", { containerId: "extra-var" });
      }
    }
  }, [open, props.renderContent]);

  const onSave = () => {
    try {
      const savedVars = editorRef.current?.getValues?.();
      if (!savedVars) {
        return;
      }
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
          editorData && <AnsibleVarYamlEditor ref={editorRef} data={editorData} onExtraVarChange={setExtraVars} />
        }
        onClose={() => setOpen(false)}
        footer={
          <Fragment>
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
          </Fragment>
        }
      />
    </>
  );
};

export default EditAnsibleVarsModal;
