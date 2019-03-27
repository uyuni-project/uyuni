//@flow
import React, {useEffect, useState} from 'react';
import {ModalButton} from "components/dialog/ModalButton";
import DownArrow from '../../down-arrow/down-arrow';
import {closeDialog, Dialog} from "components/dialog/Dialog";
import {Button} from "components/buttons";
import type {ProjectEnvironmentType, ProjectHistoryEntry} from "../../../type/project.type";
import {getVersionMessageByNumber} from "../properties/properties.utils";
import useProjectActionsApi from "../../../api/use-project-actions-api";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import {Loading} from "components/loading/loading";


type Props = {
  projectId: string,
  environmentPromote: ProjectEnvironmentType,
  environmentTarget: ProjectEnvironmentType,
  versionToPromote: number,
  historyEntries: Array<ProjectHistoryEntry>,
  onChange: Function
}

const Promote = (props: Props) => {
  const [open, setOpen] = useState(false);
  const {onAction, cancelAction, isLoading} = useProjectActionsApi({
    projectId: props.projectId, projectResource: "promote"
  });

  useEffect(() => {
    if(!open){
      cancelAction();
    }
  }, [open]);

  const versionMessage = getVersionMessageByNumber(props.environmentPromote.version, props.historyEntries) || "not built";
  const modalNameId = `${props.environmentPromote.label}-cm-promote-env-modal`;
  const disabled = !props.environmentPromote.version
    || props.environmentPromote.version <= props.environmentTarget.version;
  return (
    <React.Fragment>
      <DownArrow/>
      <div className="text-center">
        <ModalButton
          id={`${props.environmentPromote.label}-promote-modal-link`}
          className="btn-default"
          text="Promote"
          disabled={disabled}
          target={modalNameId}
          onClick={() => {
            setOpen(true)
          }}
        />
      </div>

      <Dialog
        id={modalNameId}
        onClosePopUp={() =>
          setOpen(false)
        }
        content={
          isLoading
            ? <Loading text='Promoting project..'/>
            :
            <React.Fragment>
              <dl className="row">
                <dt className="col-xs-4">Version:</dt>
                <dd className="col-xs-8">{versionMessage}</dd>
              </dl>
              <dl className="row">
                <dt className="col-xs-4">Target environment:</dt>
                <dd className="col-xs-8">{props.environmentTarget.name}</dd>
              </dl>
            </React.Fragment>
        }
        title={`Promote version ${props.environmentPromote.version} into ${props.environmentTarget.name}`}
        buttons={
          <div className="col-lg-12">
            <div className="pull-right btn-group">
              <Button
                className="btn-default"
                text={t("Cancel")}
                title={t("Cancel")}
                handler={() => {
                  closeDialog(modalNameId)
                }}
              />
              <Button
                className="btn-success"
                text={t("Promote")}
                title={t("Promote environment")}
                handler={() => {
                  onAction({
                    projectLabel: props.projectId,
                    environmentPromoteLabel: props.environmentPromote.label
                  }, "action")
                    .then((projectWithUpdatedSources) => {
                      closeDialog(modalNameId);
                      showSuccessToastr(`Version ${props.versionToPromote} successfully promoted into ${props.environmentTarget.name}`)
                      props.onChange(projectWithUpdatedSources)
                    })
                    .catch((error) => {
                      showErrorToastr(error);
                      closeDialog();
                    });
                }}
              />
            </div>
          </div>
        }
      />
      <DownArrow/>
    </React.Fragment>
  );
};

export default Promote;
