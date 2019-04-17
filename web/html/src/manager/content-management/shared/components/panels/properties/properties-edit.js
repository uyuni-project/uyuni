// @flow
import React from 'react';
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import PropertiesForm from "./properties-form";
import {Loading} from "components/loading/loading";
import useProjectActionsApi from "../../../api/use-project-actions-api";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import PropertiesView from "./properties-view";
import produce from "immer";

import type {ProjectHistoryEntry, ProjectPropertiesType} from '../../../type/project.type.js';
import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";

type Props = {
  projectId: string,
  properties: ProjectPropertiesType,
  showDraftVersion: boolean,
  onChange: Function,
  currentHistoryEntry: ProjectHistoryEntry
};

const PropertiesEdit = (props: Props) => {
  const {onAction, cancelAction, isLoading} = useProjectActionsApi({
    projectId: props.projectId, projectResource: "properties"
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const defaultDraftHistory = {
    version: props.currentHistoryEntry ? props.currentHistoryEntry.version + 1 : 1 ,
    message:'(draft - not built) - Check the colors below for all the changes'
  };

  let propertiesToShow = produce(props.properties, draftProperties => {
      if(props.showDraftVersion){
        draftProperties.historyEntries.unshift(defaultDraftHistory);
      }
    });

  return (
      <CreatorPanel
        id="properties"
        creatingText="Edit Properties"
        panelLevel="2"
        disableEditing={!hasEditingPermissions}
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
        renderContent={() => <PropertiesView properties={propertiesToShow}/>}
        renderCreationContent={({ open, item, setItem }) => {

          if (isLoading) {
            return (
              <Loading text='Editing properties..'/>
            )
          }

          return (
            <PropertiesForm
              properties={{...item}}
              onChange={(editedProperties) => setItem(editedProperties)}
              editing
            />
          )
        }}
      />
  );
}

export default PropertiesEdit;
