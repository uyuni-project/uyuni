// @flow
import React from 'react';
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import EnvironmentView from "./environment-view";
import EnvironmentForm from "./environment-form";
import {Loading} from "components/loading/loading";
import Promote from "../promote/promote";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import {mapAddEnvironmentRequest, mapUpdateEnvironmentRequest} from './environment.utils';

import type {ProjectEnvironmentType} from '../../../type/project.type.js';
import type {ProjectHistoryEntry} from "../../../type/project.type";
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";

type Props = {
  projectId: string,
  environments: Array<ProjectEnvironmentType>,
  historyEntries: Array<ProjectHistoryEntry>,
  onChange: Function,
};

const EnvironmentLifecycle = (props: Props) => {

  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({
    resource: 'projects', nestedResource:"environments"
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  return (
    <CreatorPanel
      id="environmentLifecycle"
      title={t("Environment Lifecycle")}
      creatingText={t("Add Environment")}
      panelLevel="2"
      disableEditing={!hasEditingPermissions}
      collapsible
      customIconClass="fa-small"
      disableOperations={isLoading}
      onSave={({ item, closeDialog }) =>
        onAction(mapAddEnvironmentRequest(item, props.environments, props.projectId), "create", props.projectId)
          .then((projectWithCreatedEnvironment) => {
            closeDialog();
            showSuccessToastr(t("Environment created successfully"));
            props.onChange(projectWithCreatedEnvironment)
          })
          .catch((error) => {
            showErrorToastr(error);
          })}
      onOpen={({ setItem }) => setItem({})}
      onCancel={() => cancelAction()}
      renderCreationContent={({ open, item, setItem }) => {

        if (!open) {
          return null;
        }

        if (isLoading) {
          return (
            <Loading text={t('Creating the environment...')}/>
          )
        }

        return (
          <EnvironmentForm
            environment={{...item}}
            environments={props.environments}
            onChange={(item) => setItem(item)}/>
        )
      }}
      renderContent={() =>
        <div className="min-height-panel">
          <>
            {
              props.environments.length === 0 &&
              <h4>{t("No environments created")}</h4>
            }
            {
              props.environments.map((environment, i) =>
                <React.Fragment key={environment.label}>
                  <div className="row">
                    <CreatorPanel
                      id={`environment${environment.label}`}
                      title={environment.name}
                      creatingText="Edit"
                      panelLevel="3"
                      disableEditing={!hasEditingPermissions}
                      disableOperations={isLoading}
                      onSave={({ item, closeDialog }) =>
                        onAction(mapUpdateEnvironmentRequest(item, props.projectId), "update", props.projectId)
                          .then((projectWithUpdatedEnvironment) => {
                            props.onChange(projectWithUpdatedEnvironment)
                            closeDialog();
                            showSuccessToastr(t("Environment updated successfully"));
                          })
                          .catch((error) => {
                            showErrorToastr(error);
                          })}
                      onOpen={({ setItem }) => setItem(environment)}
                      onCancel={() => cancelAction()}
                      onDelete={({ item, closeDialog }) => {
                        return onAction(item, "delete", props.projectId)
                          .then((projectWithDeleteddEnvironment) => {
                            closeDialog()
                              .then(() => {
                                props.onChange(projectWithDeleteddEnvironment);
                              });
                            showSuccessToastr(t("Environment deleted successfully"));
                          })
                          .catch((error) => {
                            showErrorToastr(error);
                          })
                      }}
                      renderCreationContent={({ open, item, setItem }) => {
                        if (!open) {
                          return null;
                        }

                        if (isLoading) {
                          return (
                            <Loading text='Editing the environment..'/>
                          )
                        }

                        return (
                          <EnvironmentForm
                            environment={{...item}}
                            environments={props.environments}
                            onChange={(item) => setItem(item)}
                            editing
                          />
                        )
                      }}
                      renderContent={() => <EnvironmentView
                        environment={environment}
                        historyEntries={props.historyEntries}
                      />}/>
                  </div>
                  {
                    props.environments.length - 1 !== i &&
                    <Promote
                      projectId={props.projectId}
                      environmentPromote={environment}
                      environmentTarget={props.environments[i + 1]}
                      historyEntries={props.historyEntries}
                      versionToPromote={environment.version}
                      onChange={props.onChange}
                    />
                  }
                </React.Fragment>
              )
            }
          </>
        </div>
      }
    />
  )
}

export default EnvironmentLifecycle;
