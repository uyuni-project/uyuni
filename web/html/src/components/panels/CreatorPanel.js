// @flow
import React, {useState} from 'react';
import {Panel} from "./Panel";
import {ModalLink} from "../dialog/ModalLink";
import {closeDialog, Dialog} from "../dialog/Dialog";
import {Button} from "../buttons";

type Props = {
  id: string,
  title: string,
  creatingText: string,
  panelLevel: string,
  onSave: Function,
  renderCreationContent: Function,
  renderContent: Function,
  disableEditing?: boolean,
  className?: string,
  onCancel?: Function,
  onOpen?: Function,
  onDelete?: Function,
  disableOperations?: boolean,
  collapsible?: boolean,
  customIconClass?: string,
};

const panelLevels = {
  "1": "h1",
  "2": "h2",
  "3": "h3",
  "4": "h4",
}

const CreatorPanel = (props: Props) => {

  const [open, setOpen] = useState(false);
  const [item, setItem] = useState({});

  const setStateItem = (item: Object) => setItem(item);
  const modalNameId = `${props.id}-modal`;
  const panelCollapseId = props.collapsible && `${props.id}-panel`;

  return (
    <React.Fragment>
      <Panel
        headingLevel={panelLevels[props.panelLevel]}
        collapseId={panelCollapseId}
        customIconClass={props.customIconClass ? props.customIconClass : ""}
        title={props.title}
        className={props.className}
        buttons={
          !props.disableEditing &&
          <ModalLink
            id={`${props.id}-modal-link`}
            icon="fa-plus"
            className="btn-link"
            text={props.creatingText}
            target={modalNameId}
            onClick={() => {
              setOpen(true)
              props.onOpen && props.onOpen({setItem: setStateItem})
            }}
          />
        }>

        {props.renderContent()}

      </Panel>


      {
        !props.disableEditing &&
        <Dialog id={modalNameId}
                title={props.title}
                closableModal={false}
                className="modal-lg"
                content={
                  props.renderCreationContent({
                    open,
                    item,
                    setItem: setStateItem
                  })
                }
                onClosePopUp={() => setOpen(false)}
                buttons={
                  <React.Fragment>
                    <div className="btn-group col-lg-6">
                      {
                        props.onDelete && <Button
                          id={`${props.id}-modal-delete-button`}
                          className="btn-danger"
                          text={t('Delete')}
                          disabled={props.disableOperations}
                          handler={() => props.onDelete && props.onDelete({
                            item,
                            closeDialog: () => closeDialog(modalNameId)
                          })}
                        />
                      }
                    </div>
                    <div className="col-lg-6">
                      <div className="pull-right btn-group">
                        <Button
                          id={`${props.id}-modal-cancel-button`}
                          className="btn-default"
                          text={t('Cancel')}
                          handler={() => {
                            if (props.onCancel) {
                              props.onCancel();
                            }
                            closeDialog(modalNameId);
                          }}
                        />
                        <Button
                          id={`${props.id}-modal-save-button`}
                          className="btn-primary"
                          text={t('Save')}
                          disabled={props.disableOperations}
                          handler={() => props.onSave({
                            item,
                            closeDialog: () => closeDialog(modalNameId)
                          })}
                        />
                      </div>
                    </div>
                  </React.Fragment>
                }/>
      }
    </React.Fragment>
  );
};

export default CreatorPanel;
