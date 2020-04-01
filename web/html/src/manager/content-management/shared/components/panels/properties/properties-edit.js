// @flow
import React from 'react';
import CreatorPanel from "../../../../../../components/panels/CreatorPanel";
import PropertiesForm from "./properties-form";
import {Loading} from "components/utils/Loading";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";
import PropertiesView from "./properties-view";
import produce from "immer";

import useRoles from "core/auth/use-roles";
import {isOrgAdmin} from "core/auth/auth.utils";
import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import getRenderedMessages from "../../messages/messages";

import type {ProjectMessageType, ProjectHistoryEntry, ProjectPropertiesType}
  from '../../../type/project.type.js';

type Props = {
  projectId: string,
  properties: ProjectPropertiesType,
  showDraftVersion: boolean,
  onChange: Function,
  currentHistoryEntry: ProjectHistoryEntry,
  messages?: Array<ProjectMessageType>
};

const PropertiesEdit = (props: Props) => {
  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({
    resource: 'projects', nestedResource: "properties"
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const defaultDraftHistory = {
    version: props.currentHistoryEntry ? props.currentHistoryEntry.version + 1 : 1 ,
    message:t('(draft - not built) - Check the changes below')
  };

  const messages = getRenderedMessages(props.messages || []);

  let propertiesToShow = produce(props.properties, draftProperties => {
      if(props.showDraftVersion){
        draftProperties.historyEntries.unshift(defaultDraftHistory);
      }
    });

  return (
      <CreatorPanel
        id="properties"
        creatingText={t("Edit Properties")}
        className={messages.panelClass}
        panelLevel="2"
        disableEditing={!hasEditingPermissions}
        title={t('Project Properties')}
        collapsible
        customIconClass="fa-small"
        onOpen={({ setItem }) => setItem(props.properties)}
        disableOperations={isLoading}
        onSave={({ item, closeDialog }) => {
          return onAction(item, "update", props.projectId)
            .then((editedProject) => {
              closeDialog();
              showSuccessToastr(t("Project properties updated successfully"));
              props.onChange(editedProject)
            })
            .catch((error) => {
              showErrorToastr(error, {autoHide: false});
            })
        }}
        onCancel={() => cancelAction()}
        renderContent={() =>
          <React.Fragment>
            {messages.messages}
            <PropertiesView properties={propertiesToShow}/>
          </React.Fragment>
        }
        renderCreationContent={({ open, item, setItem }) => {

          if (isLoading) {
            return (
              <Loading text={t('Editing properties..')}/>
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
