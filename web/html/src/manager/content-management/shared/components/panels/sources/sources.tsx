import * as React from "react";
import { useMemo } from "react";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import { Select } from "components/input";
import CreatorPanel from "components/panels/CreatorPanel";
import { Panel } from "components/panels/Panel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import statesEnum from "../../../business/states.enum";
import { ProjectMessageType, ProjectSoftwareSourceType } from "../../../type";
import getRenderedMessages from "../../messages/messages";
import ChannelsSelection from "./channels/channels-selection";
import styles from "./sources.css";

type SourcesProps = {
  projectId: string;
  softwareSources: Array<ProjectSoftwareSourceType>;
  onChange: Function;
  messages?: Array<ProjectMessageType>;
};

const ModalSourceCreationContent = ({ isLoading, softwareSources, onChange }) => {
  const initialSelectedSources = useMemo(
    () => softwareSources.filter((source) => !statesEnum.isDeletion(source.state)),
    [softwareSources]
  );

  return (
    <form className="form-horizontal">
      <div className="row">
        <Select
          name="sourceType"
          label={t("Type")}
          labelClass="col-md-3"
          divClass="col-md-8"
          defaultValue="software"
          options={[{ value: "software", label: t("Channel") }]}
        />
      </div>
      <ChannelsSelection
        isSourcesApiLoading={isLoading}
        initialSelectedSources={initialSelectedSources}
        onChange={(selectedChannelLabels) => {
          onChange(selectedChannelLabels);
        }}
      />
    </form>
  );
};

const renderSourceEntry = (source) => {
  const unsyncedPatches = source.hasUnsyncedPatches ? (
    <a
      target="_blank"
      href={"/rhn/channels/manage/errata/SyncErrata.do?cid=" + source.targetChannelId}
      rel="noopener noreferrer"
    >
      ( {t("has unsynchronized patches")} )
    </a>
  ) : null;

  if (source.state === statesEnum.enum.ATTACHED.key) {
    return (
      // TODO: If you touch this code, please make sure the `href` property here is obsolete and remove it
      // @ts-expect-error
      <div className={`text-success ${styles.attached}`} href="#">
        <i className="fa fa-plus" />
        <b>{source.name}</b>
        &nbsp;
        {unsyncedPatches}
      </div>
    );
  }
  if (source.state === statesEnum.enum.DETACHED.key) {
    return (
      <div className={`text-danger ${styles.dettached}`}>
        <i className="fa fa-minus" />
        <b>{source.name}</b>
      </div>
    );
  }
  return (
    <div className={styles.built}>
      {source.name}
      &nbsp;
      {unsyncedPatches}
    </div>
  );
};

const Sources = (props: SourcesProps) => {
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({
    resource: "projects",
    nestedResource: "softwaresources",
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const messages = getRenderedMessages(props.messages || []);

  return (
    <CreatorPanel
      id="sources"
      title={t("Sources")}
      creatingText="Attach/Detach Sources"
      className={messages.panelClass}
      panelLevel="2"
      disableEditing={!hasEditingPermissions}
      collapsible
      customIconClass="fa-small"
      onCancel={() => cancelAction()}
      onOpen={({ setItem }) => setItem(props.softwareSources.map((source) => source.label))}
      onSave={({ closeDialog, item }) => {
        const requestParam = {
          projectLabel: props.projectId,
          softwareSources: item.map((label) => ({ label })),
        };

        onAction(requestParam, "update", props.projectId)
          .then((projectWithUpdatedSources) => {
            closeDialog();
            showSuccessToastr(t("Sources edited successfully"));
            props.onChange(projectWithUpdatedSources);
          })
          .catch((error) => {
            showErrorToastr(error.messages, { autoHide: false });
          });
      }}
      renderCreationContent={({ setItem }) => {
        return (
          <ModalSourceCreationContent
            softwareSources={props.softwareSources}
            onChange={(channelsLabel) => {
              setItem(channelsLabel);
            }}
            isLoading={isLoading}
          />
        );
      }}
      renderContent={() => (
        <div className="min-height-panel">
          {messages.messages}
          {props.softwareSources.length > 0 && (
            <Panel headingLevel="h4" title={t("Software Channels")}>
              <div className="col-xs-12">
                <React.Fragment>
                  <dl className="row">
                    <dt className="col-xs-2">Base Channel:</dt>
                    <dd className="col-xs-10">{renderSourceEntry(props.softwareSources[0])}</dd>
                  </dl>

                  <dl className="row">
                    <dt className="col-xs-2">Child Channels:</dt>
                    <dd className="col-xs-6">
                      <ul className="list-unstyled">
                        {props.softwareSources.slice(1, props.softwareSources.length).map((source) => (
                          <li key={`softwareSources_entry_${source.channelId}`}>{renderSourceEntry(source)}</li>
                        ))}
                      </ul>
                    </dd>
                  </dl>
                </React.Fragment>
              </div>
            </Panel>
          )}
        </div>
      )}
    />
  );
};

export default Sources;
