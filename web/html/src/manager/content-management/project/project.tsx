import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useEffect, useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import Sources from "../shared/components/panels/sources/sources";
import PropertiesEdit from "../shared/components/panels/properties/properties-edit";
import Build from "../shared/components/panels/build/build";
import EnvironmentLifecycle from "../shared/components/panels/environment-lifecycle/environment-lifecycle";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";
import _isEmpty from "lodash/isEmpty";
import "./project.css";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import withPageWrapper from "components/general/with-page-wrapper";

import { ProjectType } from "../shared/type/project.type";
import _last from "lodash/last";
import _groupBy from "lodash/groupBy";
import useRoles from "core/auth/use-roles";
import { isOrgAdmin } from "core/auth/auth.utils";
import useInterval from "core/hooks/use-interval";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import FiltersProject from "../shared/components/panels/filters-project/filters-project";
import statesEnum from "../shared/business/states.enum";
import { getClmFilterDescription } from "../shared/business/filters.enum";

type Props = {
  project: ProjectType;
  wasFreshlyCreatedMessage?: string;
};

const Project = (props: Props) => {
  const [project, setProject] = useState(props.project);
  const { onAction, cancelAction: cancelRefreshAction } = useLifecycleActionsApi({ resource: "projects" });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  useInterval(() => {
    onAction({}, "get", project.properties.label).then((project) => {
      setProject(project);
    });
  }, 5000);

  useEffect(() => {
    if (props.wasFreshlyCreatedMessage) {
      showSuccessToastr(props.wasFreshlyCreatedMessage);
    }
  }, []);

  if (!props.project) {
    return (
      <div className="alert alert-danger">
        <span>{t("The project you are looking for does not exist or has been deleted")}.</span>
      </div>
    );
  }

  const projectId = project.properties.label;
  const currentHistoryEntry = _last(project.properties.historyEntries);

  let changesToBuild = ["Software Channels:\n"];
  changesToBuild = changesToBuild.concat(
    project.softwareSources.map((source) => `\n ${statesEnum.findByKey(source.state).sign} ${source.name}`)
  );
  if (project.filters.length > 0) {
    changesToBuild = changesToBuild.concat("\n\nSoftware Filter:\n");
    changesToBuild = changesToBuild.concat(
      project.filters.map(
        (filter) => `\n ${statesEnum.findByKey(filter.state).sign} ${getClmFilterDescription(filter)}`
      )
    );
  }

  const hasErrors = project.messages.some((m) => m.type === "error");
  const messageGroups = _groupBy(project.messages, (m) => m.entity);

  const isProjectEdited = changesToBuild.length > 0;
  const isBuildDisabled =
    !hasEditingPermissions ||
    _isEmpty(project.environments) ||
    _isEmpty(project.softwareSources) ||
    project.environments[0].status === "building" || // already building
    (project.environments[1] || {}).status === "building" || // promoting 1st env to 2nd: we can't build as it would affect this promotion
    hasErrors;

  const hasChannelsWithUnsyncedPatches = project.softwareSources.filter((s) => s.hasUnsyncedPatches).length > 0;
  return (
    <TopPanel
      title={t("Content Lifecycle Project - {0}", project.properties.name)}
      helpUrl="reference/clm/clm-projects.html" // icon="fa-plus"
      button={
        hasEditingPermissions && (
          <div className="pull-right btn-group">
            <ModalButton className="btn-danger" title={t("Delete")} text={t("Delete")} target="delete-project-modal" />
          </div>
        )
      }
    >
      <DeleteDialog
        id="delete-project-modal"
        title={t("Delete Project")}
        content={
          <span>
            {t("Are you sure you want to delete project")} <strong>{projectId}</strong>?
          </span>
        }
        onConfirm={() =>
          onAction(project, "delete", project.properties.label)
            .then(() => {
              window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/contentmanagement/projects`);
            })
            .catch((error) => {
              showErrorToastr(error.messages, { autoHide: false });
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
        messages={messageGroups["properties"]}
      />

      <div className="panel-group-contentmngt">
        <Sources
          projectId={projectId}
          softwareSources={project.softwareSources}
          onChange={(projectWithNewSources) => {
            setProject(projectWithNewSources);
            cancelRefreshAction();
          }}
          messages={messageGroups["softwareSources"]}
        />
        <FiltersProject
          projectId={projectId}
          selectedFilters={project.filters}
          onChange={(projectWithNewSources) => {
            setProject(projectWithNewSources);
            cancelRefreshAction();
          }}
          messages={messageGroups["filters"]}
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
        hasChannelsWithUnsyncedPatches={hasChannelsWithUnsyncedPatches}
      />

      <EnvironmentLifecycle
        projectId={projectId}
        environments={project.environments}
        historyEntries={project.properties.historyEntries}
        onChange={(projectWithNewEnvironment) => {
          setProject(projectWithNewEnvironment);
          cancelRefreshAction();
        }}
        messages={messageGroups["environments"]}
      />
    </TopPanel>
  );
};

export default hot(withPageWrapper<Props>(Project));
