// @flow
import React, { useState } from 'react';
import useProjectActionsApi from "../shared/api/use-project-actions-api";
import {TopPanel} from "components/panels/TopPanel";
import TopPanelButtons from "./top-panel-buttons";
import PropertiesCreate from "../shared/components/panels/properties/properties-create";
import {handlePropertiesChange} from "../shared/state/project/project.state";
import {showErrorToastr} from "components/toastr/toastr";
import withPageWrapper from 'components/general/with-page-wrapper';
import { hot } from 'react-hot-loader';

const CreateProject = () => {

  const [project, setProject] = useState({
      properties: {
        label: null,
        name: null,
        description: null,
        historyEntries: [],
      }
  });

  const { onAction } = useProjectActionsApi({});

  return (
          <TopPanel
            title={t('Create a new Content Lifecycle Project')}
            icon="fa-plus"
            button={
              <TopPanelButtons
                onCreate={() =>
                  onAction(project, "create")
                    .then(
                      (data) => {
                        window.location.href = `/rhn/manager/contentmanagement/project/${project.properties.label || ''}`
                      }
                    )
                    .catch((error) => {
                      showErrorToastr(error);
                    })
                }
              />
            }
          >
            <PropertiesCreate
              properties={project.properties}
              onChange={(newProperties) => setProject(handlePropertiesChange(project, newProperties))}
            />
          </TopPanel>
  )
}

export default hot(module)(withPageWrapper<{}>(CreateProject));
