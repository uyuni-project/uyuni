// @flow
import React from 'react';
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import PropertiesForm from "./properties-form";
import {Loading} from "components/loading/loading";
import useProjectActionsApi from "../../../api/use-project-actions-api";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import PropertiesView from "./properties-view";

import type {projectPropertiesType} from '../../../type/project.type.js';

type Props = {
  projectId: string,
  properties: projectPropertiesType,
  onChange: Function,
};

const PropertiesEdit = (props: Props) => {
  const {onAction, cancelAction, isLoading} = useProjectActionsApi({
    projectId: props.projectId, nestedResource:"properties"
  });

  return (
      <CreatorPanel
        id="properties"
        creatingText="Edit"
        panelLevel="2"
        title={t('Project Properties')}
        collapsible
        customIconClass="fa-small"
        onOpen={({ setItem }) => setItem(props.properties)}
        disableOperations={isLoading}
        onSave={({ item, closeDialog }) => {
          return onAction(item, "update")
            .then((editedProject) => {
              closeDialog();
              showSuccessToastr(t("Project properties updated successfully"));
              props.onChange(editedProject)
            })
            .catch((error) => {
              showErrorToastr(error);
            })
        }}
        onCancel={() => cancelAction()}
        renderContent={() => <PropertiesView properties={props.properties}/>}
        renderCreationContent={({ open, item, setItem }) => {

          if (isLoading) {
            return (
              <Loading text='Editing properties..'/>
            )
          }

          return (
            <PropertiesForm
              properties={item}
              onChange={(editedProperties) => setItem(editedProperties)}
              editing
            />
          )
        }}
      />
  );
}

export default PropertiesEdit;
