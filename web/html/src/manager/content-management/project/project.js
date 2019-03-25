// @flow

import React, {useEffect, useState} from 'react';
import {TopPanel} from "../../../components/panels/TopPanel";
import Sources from "../shared/components/panels/sources/sources";
import PropertiesEdit from "../shared/components/panels/properties/properties-edit";
import Build from "../shared/components/panels/build/build";
import EnvironmentLifecycle from "../shared/components/panels/environment-lifecycle/environment-lifecycle";
import {showErrorToastr, showSuccessToastr} from 'components/toastr/toastr';
import Filters from "../shared/components/panels/filters/filters";
import _isEmpty from "lodash/isEmpty";
import "./project.css";
import {DeleteDialog} from "components/dialog/DeleteDialog";
import {ModalButton} from "components/dialog/ModalButton";
import useProjectActionsApi from '../shared/api/use-project-actions-api';
import withPageWrapper from 'components/general/with-page-wrapper';

import type {ProjectType} from '../shared/type/project.type';
import {hot} from 'react-hot-loader';
import _last from "lodash/last";

type Props = {
  project: ProjectType,
  wasFreshlyCreatedMessage?: string,
};

const Project = (props: Props) => {

  const [project, setProject] = useState(props.project)
  const {onAction} = useProjectActionsApi({ projectId: project.properties.label });


  useEffect(()=> {
    if(props.wasFreshlyCreatedMessage) {
      showSuccessToastr(props.wasFreshlyCreatedMessage)
    }
  }, [])

  // TODO: transform this in an enum and reuse in sources.js as well
  const editedStates = ["ATTACHED","DETACHED"];
  const statesDesc = {
    "ATTACHED": "added",
    "DETACHED": "deleted",
    "BUILT": "built",
  };

  const projectId = project.properties.label;
  const currentHistoryEntry = _last(project.properties.historyEntries);

  const changesToBuild = project.softwareSources
    .filter(source => editedStates.includes(source.state))
    .map(source => ({id: source.id, type: "Source", name: source.name, state: statesDesc[source.state]}));
  const isProjectEdited = changesToBuild.length > 0;

  return (
    <TopPanel
      title={t('Content Lifecycle Project - {0}', project.properties.name)}
      // icon="fa-plus"
      button= {
        <div className="pull-right btn-group">
          <ModalButton
            className="btn-danger"
            title={t("Delete")}
            text={t('Delete')}
            target="delete-project-modal" />
        </div>
      }
    >
      <DeleteDialog id="delete-project-modal"
          title={t("Delete Project")}
          content={<span>{t("Are you sure you want to delete project")} <strong>{projectId}</strong>?</span>}
          onConfirm={() =>
            onAction(project, 'delete')
            .then(() => {
              window.location.href = `/rhn/manager/contentmanagement/projects`
            })
            .catch((error) => {
              showErrorToastr(error);
            })
          }
      />
      <PropertiesEdit
        projectId={projectId}
        properties={project.properties}
        currentHistoryEntry={currentHistoryEntry}
        showDraftVersion={isProjectEdited}
        onChange={(projectWithNewProperties) => {
          setProject(projectWithNewProperties)
        }}
      />

      <div className="panel-group-contentmngt">
        <Sources
          projectId={projectId}
          softwareSources={project.softwareSources}
          onChange={(projectWithNewSources) => {
            setProject(projectWithNewSources)
          }}
        />
        <Filters/>
      </div>

      <Build
        projectId={projectId}
        disabled={_isEmpty(project.environments) || !isProjectEdited}
        currentHistoryEntry={currentHistoryEntry}
        onBuild={(projectWithNewSources) => {
          setProject(projectWithNewSources)
        }}
        changesToBuild={changesToBuild}
      />

      <EnvironmentLifecycle
        projectId={projectId}
        environments={project.environments}
        onChange={(projectWithNewEnvironment) => {
          setProject(projectWithNewEnvironment)
        }}
      />
    </TopPanel>
  );
}

export default hot(module)(withPageWrapper<Props>(Project));
