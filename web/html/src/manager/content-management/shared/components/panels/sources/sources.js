// @flow
import React from 'react';
import {Select} from "../../../../../../components/input/Select";
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";

import type {ProjectSoftwareSourceType} from '../../../type/project.type.js';
import ChannelsSelection from "./channels/channels-selection";
import {Panel} from "../../../../../../components/panels/Panel";
import styles from "./sources.css";
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import statesEnum from "../../../business/states.enum";

type SourcesProps = {
  projectId: string,
  softwareSources: Array<ProjectSoftwareSourceType>,
  onChange: Function,
};

const ModalSourceCreationContent = ({isLoading, softwareSources, onChange}) => {

  return (
    <form className="form-horizontal">
      <div className="row">
        <Select
          name="sourceType"
          label={t("Type")}
          labelClass="col-md-3"
          divClass="col-md-8">
          <option key="0" value="software">Channel</option>
        </Select>
      </div>
      <ChannelsSelection
        isSourcesApiLoading={isLoading}
        initialSelectedIds={
          softwareSources
            .filter(source => !statesEnum.isDeletion(source.state))
            .map(source => source.channelId)
        }
        onChange={(selectedChannels) => {
          onChange(selectedChannels.map(c => c.label))
        }}
      />
    </form>
  )
}

const renderSourceEntry = (source) => {
  if (source.state === statesEnum.enum.ATTACHED.key) {
    return (
      <div
        className={`text-success ${styles.attached}`}
        href="#">
        <i className='fa fa-plus'/>
        <b>{source.name}</b>
      </div>
    );
  }
  if (source.state === statesEnum.enum.DETACHED.key) {
    return (
      <div className={`text-danger ${styles.dettached}`}>
        <i className='fa fa-minus'/>
        <b>{source.name}</b>
      </div>
    );
  }
  return (
    <div className={styles.built}>
      {source.name}
    </div>
  );
}

const Sources = (props: SourcesProps) => {

  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({
    resource: 'projects', nestedResource: "softwaresources"
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  return (
    <CreatorPanel
      id="sources"
      title="Sources"
      creatingText="Attach/Detach Sources"
      panelLevel="2"
      disableEditing={!hasEditingPermissions}
      collapsible
      customIconClass="fa-small"
      onCancel={() => cancelAction()}
      onOpen={({setItem}) => setItem(props.softwareSources.map(source => source.label))}
      onSave={({closeDialog, item}) => {
        const requestParam = {
          projectLabel: props.projectId,
          softwareSources: item.map(label => ({label})),
        };

        onAction(requestParam, "update", props.projectId)
          .then((projectWithUpdatedSources) => {
            closeDialog();
            showSuccessToastr(t("Sources edited successfully"));
            props.onChange(projectWithUpdatedSources)
          })
          .catch((error) => {
            showErrorToastr(error);
          });
      }}
      renderCreationContent={({setItem}) => {
        return (
          <ModalSourceCreationContent
            softwareSources={props.softwareSources}
            onChange={(channelsLabel) => {
              setItem(channelsLabel);
            }}
            isLoading={isLoading}
          />
        )
      }}
      renderContent={() =>
        <div className="min-height-panel">
          {
            props.softwareSources.length > 0 &&
            <Panel
              headingLevel="h4"
              title={t('Software Channels')}
            >
              <div className="col-xs-12">
                <React.Fragment>
                  <dl className="row">
                    <dt className="col-xs-2">Base Channel:</dt>
                    <dd className="col-xs-10">
                      {renderSourceEntry(props.softwareSources[0])}
                    </dd>
                  </dl>

                  <dl className="row">
                    <dt className="col-xs-2">Child Channels:</dt>
                    <dd className="col-xs-6">
                      <ul className="list-unstyled">
                        {
                          props.softwareSources.slice(1, props.softwareSources.length).map(source =>
                            <li key={`softwareSources_entry_${source.channelId}`}>
                              {renderSourceEntry(source)}
                            </li>
                          )
                        }
                      </ul>
                    </dd>
                  </dl>
                </React.Fragment>
              </div>
            </Panel>
          }
        </div>
      }
    />
  )
}

export default Sources;
