// @flow
import React from 'react';
import {Select} from "../../../../../../components/input/Select";
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";

import type {ProjectSoftwareSourceType} from '../../../type/project.type.js';
import useProjectActionsApi from "../../../api/use-project-actions-api";
import ChannelsSelection from "./channels/channels-selection";
import {Panel} from "../../../../../../components/panels/Panel";
import {Loading} from "components/loading/loading";

type SourcesProps = {
  projectId: string,
  softwareSources: Array<ProjectSoftwareSourceType>,
  onChange: Function,
};

const ModalSourceCreationContent = ({isLoading, softwareSources, onChange}) => {

  // TODO: transform this in an enum and reuse in sources.js as well
  const selectedStates = ["ATTACHED","BUILT"];

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
        isLoading={isLoading}
        initialSelectedIds={
          softwareSources
            .filter(source => selectedStates.includes(source.state))
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
  if (source.state === 'ATTACHED') {
    return (
      <div
        style={{padding: "3px 3px 3px 0px"}}
        className="text-success"
        href="#">
        <i className='fa fa-plus'/>
        <b>{source.name}</b>
      </div>
    );
  }
  if (source.state === 'DETACHED') {
    return (
      <div
        style={{
          padding: "3px 3px 3px 0px",
          textDecoration: "line-through"
        }}
        className="text-danger"
        href="#">
        <i className='fa fa-minus'/>
        <b>{source.name}</b>
      </div>
    );
  }
  return (
    <div
      style={{padding: "3px 3px 3px 0px"}}
      href="#">
      {source.name}
    </div>
  );
}

const Sources = (props: SourcesProps) => {

  const {onAction, cancelAction, isLoading} = useProjectActionsApi({
    projectId: props.projectId, projectResource: "softwaresources"
  });


  return (
    <CreatorPanel
      id="sources"
      title="Sources"
      creatingText="Add new Source"
      panelLevel="2"
      collapsible
      customIconClass="fa-small"
      onCancel={() => cancelAction()}
      onOpen={({setItem}) => setItem(props.softwareSources.map(source => source.label))}
      onSave={({closeDialog, item}) => {
        const requestParam = {
          projectLabel: props.projectId,
          softwareSources: item.map(label => ({label})),
        };

        onAction(requestParam, "update")
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
