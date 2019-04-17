// @flow

import React, {useEffect, useState} from 'react';
import {TopPanel} from "../../../components/panels/TopPanel";
import Sources from "../shared/components/panels/sources/sources";
import PropertiesEdit from "../shared/components/panels/properties/properties-edit";
import Build from "../shared/components/panels/build/build";
import EnvironmentLifecycle from "../shared/components/panels/environment-lifecycle/environment-lifecycle";
import {showErrorToastr, showSuccessToastr} from 'components/toastr/toastr';
import _isEmpty from "lodash/isEmpty";
import "./project.css";
import {DeleteDialog} from "components/dialog/DeleteDialog";
import {ModalButton} from "components/dialog/ModalButton";
import withPageWrapper from 'components/general/with-page-wrapper';

import type {ProjectType} from '../shared/type/project.type';
import {hot} from 'react-hot-loader';
import _last from "lodash/last";
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";
import useInterval from "core/hooks/use-interval";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import FiltersProject from "../shared/components/panels/filters-project/filters-project";

type Props = {
  project: ProjectType,
  wasFreshlyCreatedMessage?: string,
};

const Project = (props: Props) => {

  const [project, setProject] = useState(props.project);
  const {onAction, cancelAction: cancelRefreshAction} = useLifecycleActionsApi({resource: 'projects'});
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useInterval(() => {
    onAction({}, 'get', project.properties.label)
      .then(project => {
        setProject(project)
      });
  }, 5000);

  useEffect(()=> {
    if(props.wasFreshlyCreatedMessage) {
      showSuccessToastr(props.wasFreshlyCreatedMessage)
    }
  }, []);

  // TODO: [LuNeves] after beta2 transform this in an enum and reuse in sources.js as well
  const editedStates = ["ATTACHED","DETACHED"];
  const statesDesc = {
    "ATTACHED": "added",
    "DETACHED": "deleted",
    "BUILT": "built",
  };

  const projectId = project.properties.label;
  const currentHistoryEntry = _last(project.properties.historyEntries);

  let changesToBuild = project.softwareSources
    .filter(source => editedStates.includes(source.state))
    .map(source => `Source: ${source.type} ${source.name} ${statesDesc[source.state]}`);
  changesToBuild = changesToBuild.concat(
    project
      .filters
      .filter(filter => editedStates.includes(filter.state))
      .map(filter => `Filter: ${filter.name}: deny ${filter.type} containing ${filter.criteria} in the name ${statesDesc[filter.state]}`)
  );
  const isProjectEdited = changesToBuild.length > 0;
  const isBuildDisabled = !hasEditingPermissions || _isEmpty(project.environments) || _isEmpty(project.softwareSources);

  return (
    <TopPanel
      title={t('Content Lifecycle Project - {0}', project.properties.name)}
      // icon="fa-plus"
      button= {
        hasEditingPermissions &&
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

                      onAction(project, 'delete', project.properties.label)
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
          setProject(projectWithNewProperties);
          cancelRefreshAction();
        }}
      />

      <div className="panel-group-contentmngt">
        <Sources
          projectId={projectId}
          softwareSources={project.softwareSources}
          onChange={(projectWithNewSources) => {
            setProject(projectWithNewSources);
            cancelRefreshAction();
          }}
        />
        <FiltersProject
          projectId={projectId}
          selectedFilters={project.filters}
          onChange={(projectWithNewSources) => {
            setProject(projectWithNewSources)
            cancelRefreshAction();
          }}
        />
      </div>

      <Build
        projectId={projectId}
        disabled={isBuildDisabled}
        currentHistoryEntry={currentHistoryEntry}
        onBuild={(projectWithNewSources) => {
          setProject(projectWithNewSources);
          cancelRefreshAction();
        }}
        changesToBuild={changesToBuild}
      />

      <EnvironmentLifecycle
        projectId={projectId}
        environments={project.environments}
        historyEntries={project.properties.historyEntries}
        onChange={(projectWithNewEnvironment) => {
          setProject(projectWithNewEnvironment);
          cancelRefreshAction();
        }}
      />
    </TopPanel>
  );
};

export default hot(module)(withPageWrapper<Props>(Project));
