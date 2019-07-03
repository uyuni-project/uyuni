//@flow

import React, {useEffect, useState} from 'react';
import {Button} from "../../../../../../components/buttons";
import {closeDialog, Dialog} from "../../../../../../components/dialog/Dialog";
import {ModalButton} from "../../../../../../components/dialog/ModalButton";

import {Form} from "../../../../../../components/input/Form";
import {Text} from "../../../../../../components/input/Text";
import {Loading} from "../../../../../../components/loading/loading";
import DownArrow from '../../down-arrow/down-arrow';

import type {ProjectHistoryEntry} from '../../../type/project.type.js';
import {showErrorToastr, showSuccessToastr} from "../../../../../../components/toastr/toastr";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";

type Props = {
  projectId: string,
  onBuild: Function,
  currentHistoryEntry?: ProjectHistoryEntry,
  changesToBuild: Array<string>,
  disabled?: boolean,
}

const Build = ({projectId, onBuild, currentHistoryEntry = {}, changesToBuild, disabled} : Props) => {

  const [open, setOpen] = useState(false);
  const [buildVersionForm, setBuildVersionForm] = useState({})
  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({
    resource: 'projects', nestedResource: 'build'
  });

  const modalNameId = "cm-create-build-modal";

  useEffect(() => {
    cancelAction();
    // Our modal implementation never destroys the modals, we need this to init the modal state :)
    const currentVersion = currentHistoryEntry.version;
    setBuildVersionForm({version: currentVersion ? currentVersion + 1 : 1, message: ""});
  }, [open]);

  return (
    <div
      {...(disabled ? {title: "Add an environment to build"} : {})}
    >
      <DownArrow/>

      <div className="text-center">
        <ModalButton
          id={`build-contentmngt-modal-link`}
          className="btn-success"
          text={changesToBuild.length > 0 ? `Build (${changesToBuild.length})` : `Build`}
          disabled={disabled}
          target={modalNameId}
          onClick={() => {
            setOpen(true)
          }}
        />
      </div>

      <Dialog id={modalNameId}
              title="Build Project"
              closableModal={false}
              className="modal-lg"
              onClosePopUp={() =>
                setOpen(false)
              }
              content={
                isLoading
                  ? <Loading text='Building project..'/>
                  :
                  <Form
                    model={buildVersionForm}
                    onChange={model => setBuildVersionForm(model)} >
                    <div className="row">
                      <Text
                        name="version"
                        label={t("Version")}
                        labelClass="col-md-3"
                        divClass="col-md-9"
                        disabled
                      />
                    </div>
                    <div className="row">
                      <Text
                        name="message"
                        label={t("Version Message")}
                        labelClass="col-md-3"
                        divClass="col-md-9"/>
                    </div>
                    <dl className="row">
                      <dt className="col-md-3 control-label">Version {buildVersionForm.version} history:</dt>
                      <dd className="col-md-9">
                        <ul className="list-unstyled">
                          {
                            changesToBuild.map((change, index) => {
                              return (
                                <li key={index}>
                                  {change}
                                </li>
                              )
                            })
                          }
                        </ul>
                      </dd>
                    </dl>
                  </Form>
              }
              buttons={
                <React.Fragment>
                  <div className="col-lg-offset-6 col-lg-6">
                    <div className="pull-right btn-group">
                      <Button
                        id={`cm-build-modal-cancel-button`}
                        className="btn-default"
                        text={t('Cancel')}
                        handler={() => closeDialog(modalNameId)}
                      />
                      <Button
                        id={`cm-build-modal-save-button`}
                        className="btn-primary"
                        text={t('Build')}
                        handler={() => {
                          onAction({
                            projectLabel: projectId,
                            message: buildVersionForm.message,
                          }, "action", projectId)
                            .then((projectWithUpdatedSources) => {
                              closeDialog(modalNameId);
                              showSuccessToastr(`Version ${buildVersionForm.version} successfully built into ${projectWithUpdatedSources.environments[0].name}`)
                              onBuild(projectWithUpdatedSources)
                            })
                            .catch((error) => {
                              showErrorToastr(error);
                              closeDialog();
                            });
                        }}
                      />
                    </div>
                  </div>
                </React.Fragment>
              }
      />

      <DownArrow/>
    </div>
  );
};

export default Build;
