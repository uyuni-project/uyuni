import { hot } from "react-hot-loader/root";
import * as React from "react";
import { useState } from "react";
import { TopPanel } from "components/panels/TopPanel";
import TopPanelButtons from "./top-panel-buttons";
import PropertiesCreate from "../shared/components/panels/properties/properties-create";
import { showErrorToastr } from "components/toastr/toastr";
import withPageWrapper from "components/general/with-page-wrapper";
import useRoles from "core/auth/use-roles";
import { isOrgAdmin } from "core/auth/auth.utils";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";

const CreateProject = () => {
  const [project, setProject] = useState({
    properties: {
      label: "",
      name: "",
      description: "",
      historyEntries: [],
    },
    errors: {},
  });
  const { onAction } = useLifecycleActionsApi({ resource: "projects" });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  if (!hasEditingPermissions) {
    return <p>{t("You do not have permissions to perform this action")}</p>;
  }

  return (
    <TopPanel
      title={t("Create a new Content Lifecycle Project")}
      icon="fa-plus"
      button={
        <TopPanelButtons
          onCreate={() =>
            onAction(project, "create")
              .then(() => {
                window.pageRenderers?.spaengine?.navigate?.(
                  `/rhn/manager/contentmanagement/project/${project.properties.label || ""}`
                );
              })
              .catch(error => {
                setProject({ ...project, errors: error.errors });
                showErrorToastr(error.messages, { autoHide: false });
              })
          }
        />
      }
    >
      <PropertiesCreate
        properties={project.properties}
        errors={project.errors}
        onChange={newProperties => setProject({ ...project, properties: newProperties })}
      />
    </TopPanel>
  );
};

export default hot(withPageWrapper<{}>(CreateProject));
