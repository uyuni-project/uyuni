import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import { Dialog } from "components/dialog/Dialog";
import { ModalLink } from "components/dialog/ModalLink";

import { Panel } from "./Panel";

type Props = {
  id: string;
  title: string;
  creatingText: string;
  panelLevel: string;
  onSave: Function;
  renderCreationContent: Function;
  renderContent: Function;
  disableEditing?: boolean;
  className?: string;
  onCancel?: Function;
  onOpen?: Function;
  onDelete?: Function;
  disableDelete?: boolean;
  disableOperations?: boolean;
  collapsible?: boolean;
  icon?: string;
  customIconClass?: string;
};

const panelLevels = {
  "1": "h1",
  "2": "h2",
  "3": "h3",
  "4": "h4",
};

const CreatorPanel = (props: Props) => {
  const [open, setOpen] = useState(false);
  const [item, setItem] = useState({});
  const [errors, setErrors] = useState(null);

  const setStateItem = (item: any) => setItem(item);
  const setStateErrors = (errors: any) => setErrors(errors);
  const modalNameId = `${props.id}-modal`;
  const panelCollapseId = props.collapsible ? `${props.id}-panel` : null;

  return (
    <React.Fragment>
      <Panel
        headingLevel={panelLevels[props.panelLevel]}
        collapseId={panelCollapseId}
        customIconClass={props.customIconClass ? props.customIconClass : ""}
        title={props.title}
        className={props.className}
        buttons={
          !props.disableEditing && (
            <ModalLink
              id={`${props.id}-modal-link`}
              icon={props.icon ? props.icon : "fa-plus"}
              text={props.creatingText}
              target={modalNameId}
              onClick={() => {
                setOpen(true);
                props.onOpen &&
                  props.onOpen({
                    setItem: setStateItem,
                    setErrors: setStateErrors,
                  });
              }}
            />
          )
        }
      >
        {props.renderContent()}
      </Panel>

      {!props.disableEditing && (
        <Dialog
          id={modalNameId}
          isOpen={open}
          title={props.title}
          className="modal-lg"
          content={props.renderCreationContent({
            open,
            item,
            setItem: setStateItem,
            errors,
          })}
          onClose={() => setOpen(false)}
          footer={
            <React.Fragment>
              <div className="btn-group col-lg-6">
                {props.onDelete && (
                  <Button
                    id={`${props.id}-modal-delete-button`}
                    className="btn-danger"
                    text={t("Delete")}
                    disabled={props.disableDelete || props.disableOperations}
                    handler={() =>
                      props.onDelete &&
                      props.onDelete({
                        item,
                        closeDialog: () => setOpen(false),
                      })
                    }
                  />
                )}
              </div>
              <div className="col-lg-6">
                <div className="pull-right btn-group">
                  <Button
                    id={`${props.id}-modal-cancel-button`}
                    className="btn-default"
                    text={t("Cancel")}
                    handler={() => {
                      if (props.onCancel) {
                        props.onCancel();
                      }
                      setOpen(false);
                    }}
                  />
                  <Button
                    id={`${props.id}-modal-save-button`}
                    className="btn-primary"
                    text={t("Save")}
                    disabled={props.disableOperations}
                    handler={() =>
                      props.onSave({
                        item,
                        closeDialog: () => setOpen(false),
                        setErrors: setStateErrors,
                      })
                    }
                  />
                </div>
              </div>
            </React.Fragment>
          }
        />
      )}
    </React.Fragment>
  );
};

export default CreatorPanel;
