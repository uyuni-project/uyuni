import * as React from "react";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import { Messages, Utils as MsgUtils } from "components/messages";
import CreatorPanel from "components/panels/CreatorPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";
import { Loading } from "components/utils/Loading";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import { ProjectEnvironmentType, ProjectHistoryEntry, ProjectMessageType } from "../../../type";
import getRenderedMessages from "../../messages/messages";
import Promote from "../promote/promote";
import { mapAddEnvironmentRequest, mapUpdateEnvironmentRequest } from "./environment.utils";
import EnvironmentForm from "./environment-form";
import EnvironmentView from "./environment-view";

type Props = {
  projectId: string;
  environments: Array<ProjectEnvironmentType>;
  historyEntries: Array<ProjectHistoryEntry>;
  onChange: Function;
  messages?: Array<ProjectMessageType>;
};

const EnvironmentLifecycle = (props: Props) => {
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({
    resource: "projects",
    nestedResource: "environments",
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const messages = getRenderedMessages(props.messages || []);

  return (
    <CreatorPanel
      id="environmentLifecycle"
      title={t("Environment Lifecycle")}
      creatingText={t("Add Environment")}
      className={messages.panelClass}
      panelLevel="2"
      disableEditing={!hasEditingPermissions}
      collapsible
      customIconClass="fa-small"
      disableOperations={isLoading}
      onSave={({ item, closeDialog, setErrors }) =>
        onAction(mapAddEnvironmentRequest(item, props.environments, props.projectId), "create", props.projectId)
          .then((projectWithCreatedEnvironment) => {
            closeDialog();
            showSuccessToastr(t("Environment created successfully"));
            props.onChange(projectWithCreatedEnvironment);
          })
          .catch((error) => {
            setErrors(error.errors);
            showErrorToastr(error.messages, { autoHide: false });
          })
      }
      onOpen={({ setItem }) => setItem({})}
      onCancel={() => cancelAction()}
      renderCreationContent={({ open, item, setItem, errors }) => {
        if (!open) {
          return null;
        }

        if (isLoading) {
          return <Loading text={t("Creating the environment...")} />;
        }

        return (
          <EnvironmentForm
            environment={{ ...item }}
            errors={errors}
            environments={props.environments}
            onChange={(item) => setItem(item)}
          />
        );
      }}
      renderContent={() => (
        <div className="min-height-panel">
          <>
            {messages.messages}
            {props.environments.length === 0 && <h4>{t("No environments created")}</h4>}
            {props.environments.map((environment, i) => (
              <React.Fragment key={environment.label}>
                <CreatorPanel
                  id={`environment${environment.label}`}
                  title={environment.name}
                  icon="fa-pencil"
                  creatingText="Edit"
                  panelLevel="3"
                  disableEditing={!hasEditingPermissions}
                  disableOperations={isLoading}
                  onSave={({ item, closeDialog, setErrors }) =>
                    onAction(mapUpdateEnvironmentRequest(item, props.projectId), "update", props.projectId)
                      .then((projectWithUpdatedEnvironment) => {
                        props.onChange(projectWithUpdatedEnvironment);
                        closeDialog();
                        showSuccessToastr(t("Environment updated successfully"));
                      })
                      .catch((error) => {
                        setErrors(error.errors);
                        showErrorToastr(error.messages, { autoHide: false });
                      })
                  }
                  onOpen={({ setItem }) => setItem(environment)}
                  onCancel={() => cancelAction()}
                  disableDelete={environment.hasProfiles}
                  onDelete={({ item, closeDialog }) => {
                    return onAction(item, "delete", props.projectId)
                      .then((projectWithDeleteddEnvironment) => {
                        closeDialog().then(() => {
                          props.onChange(projectWithDeleteddEnvironment);
                        });
                        showSuccessToastr(t("Environment {name} deleted successfully", { name: environment.label }));
                      })
                      .catch((error) => {
                        showErrorToastr(error.messages, { autoHide: false });
                      });
                  }}
                  renderCreationContent={({ open, item, setItem, errors }) => {
                    if (!open) {
                      return null;
                    }

                    if (isLoading) {
                      return <Loading text="Editing the environment.." />;
                    }

                    return (
                      <>
                        {item.hasProfiles && (
                          <Messages
                            items={MsgUtils.warning(
                              <>
                                {t(
                                  "This environment cannot be deleted since it is being used in an <link>autoinstallation distribution</link>.",
                                  {
                                    link: (str) => (
                                      <a target="_blank" href="/rhn/kickstart/ViewTrees.do">
                                        {str}
                                      </a>
                                    ),
                                  }
                                )}
                              </>
                            )}
                          />
                        )}
                        <EnvironmentForm
                          environment={{ ...item }}
                          errors={errors}
                          environments={props.environments}
                          onChange={(item) => setItem(item)}
                          editing
                        />
                      </>
                    );
                  }}
                  renderContent={() => (
                    <EnvironmentView environment={environment} historyEntries={props.historyEntries} />
                  )}
                />
                {props.environments.length - 1 !== i && (
                  <Promote
                    projectId={props.projectId}
                    environmentPromote={environment}
                    environmentTarget={props.environments[i + 1]}
                    environmentNextTarget={props.environments[i + 2]}
                    historyEntries={props.historyEntries}
                    versionToPromote={environment.version}
                    onChange={props.onChange}
                  />
                )}
              </React.Fragment>
            ))}
          </>
        </div>
      )}
    />
  );
};

export default EnvironmentLifecycle;
