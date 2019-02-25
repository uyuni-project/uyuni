//@flow

import React, {useState} from 'react';
import {Button} from "../../../../../../components/buttons";
import {Dialog, closeDialog} from "../../../../../../components/dialog/Dialog";
import {ModalButton} from "../../../../../../components/dialog/ModalButton";

import {Form} from "../../../../../../components/input/Form";
import {Text} from "../../../../../../components/input/Text";
import {Loading} from "../../../../../../components/loading/loading";
import ProjectBuildApi from "../../../api/project-build-api";
import DownArrow from '../../down-arrow/down-arrow';

type Props = {
  versionToBuild: Object,
  onBuild: Function,
  changesToBuild: Array<Object>,
  disabled: boolean,
}

const Build = (props: Props) => {

  const [buildVersionForm, setBuildVersionForm] = useState({});

  const modalNameId = "cm-create-build-modal";

  return (
    <ProjectBuildApi>
      {({
          isLoading,
          message,
          buildProject
        }) =>
        <React.Fragment>
          <DownArrow/>

          <div className="text-center">
            <ModalButton
              id={`build-contentmngt-modal-link`}
              className="btn-success"
              text={`Build (${props.changesToBuild.length})`}
              disabled={props.disabled}
              target={modalNameId}
              onClick={() => setBuildVersionForm({version: props.versionToBuild.version})}
            />
          </div>

          <Dialog id={modalNameId}
                  title="Build Project"
                  closableModal={false}
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
                          <dt className="col-xs-3">Version {buildVersionForm.version} history:</dt>
                          <dd className="col-xs-6">
                            <ul className="list-unstyled">
                              {
                                props.changesToBuild.map((change, index) => {
                                  const versionMessage = `${change.type} ${change.name} ${change.state}`
                                  return (
                                    <li>
                                      {versionMessage}
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
                              buildProject()
                                .then(() => {
                                  const buildVersion = buildVersionForm;
                                  props.onBuild(buildVersion);
                                  closeDialog(modalNameId);
                                });
                            }}
                          />
                        </div>
                      </div>
                    </React.Fragment>
                  }
          />

          <DownArrow/>
        </React.Fragment>
      }
    </ProjectBuildApi>
  );
};

export default Build;
