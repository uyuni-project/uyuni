import { Fragment } from "react";

import { isOrgAdmin } from "core/auth/auth.utils";
import useRoles from "core/auth/use-roles";

import { LinkButton } from "components/buttons";
import CreatorPanel from "components/panels/CreatorPanel";
import { showErrorToastr, showSuccessToastr } from "components/toastr";

import useLifecycleActionsApi from "../../../api/use-lifecycle-actions-api";
import { getClmFilterDescription } from "../../../business/filters.enum";
import statesEnum from "../../../business/states.enum";
import { ProjectFilterServerType, ProjectMessageType } from "../../../type";
import getRenderedMessages from "../../messages/messages";
import styles from "./filters-project.module.scss";
import FiltersProjectSelection from "./filters-project-selection";

type FiltersProps = {
  projectId: string;
  selectedFilters: ProjectFilterServerType[];
  onChange: (...args: any[]) => any;
  messages?: ProjectMessageType[];
};

const renderFilterEntry = (filter, projectId) => {
  const descr = getClmFilterDescription(filter);
  const filterButton = (
    <div>
      <LinkButton
        id={`edit-filter-${filter.id}`}
        icon="fa-edit"
        title={t("Edit {name}", { name: filter.name })}
        className="btn-tertiary pull-right js-spa"
        href={`/rhn/manager/contentmanagement/filters?openFilterId=${filter.id}&projectLabel=${projectId}`}
      />
    </div>
  );

  let filterClassName;

  if (filter.state === statesEnum.enum.EDITED.key) {
    filterClassName = `text-warning`;
  } else if (filter.state === statesEnum.enum.DETACHED.key) {
    filterClassName = `text-danger ${styles.dettached}`;
  } else {
    filterClassName = `${styles.wrapper}`;
  }

  return (
    <Fragment key={`filter_list_item_${filter.id}`}>
      <li className={` ${styles.filter_list_wrapper} ${filterClassName}`}>
        <div>{descr}</div>
        {filterButton}
      </li>
    </Fragment>
  );
};

const FiltersProject = (props: FiltersProps) => {
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({
    resource: "projects",
    nestedResource: "filters",
  });
  const roles = useRoles();
  const hasEditingPermissions = isOrgAdmin(roles);

  const displayingFilters = [...props.selectedFilters];
  displayingFilters.sort((a, b) => a.name.toLowerCase().localeCompare(b.name.toLowerCase()));

  const allowFilters = displayingFilters.filter((filter) => filter.entityType !== "module" && filter.rule === "allow");
  const denyFilters = displayingFilters.filter((filter) => filter.entityType !== "module" && filter.rule === "deny");
  const moduleFilters = displayingFilters.filter((filter) => filter.entityType === "module");

  const messages = getRenderedMessages(props.messages || []);

  return (
    <CreatorPanel
      id="filters"
      title={t("Filters")}
      creatingText={t("Attach/Detach Filters")}
      className={messages.panelClass}
      panelLevel="2"
      collapsible
      customIconClass="fa-small"
      disableEditing={!hasEditingPermissions}
      onCancel={() => cancelAction()}
      onOpen={({ setItem }) => setItem(props.selectedFilters.map((filter) => filter.id))}
      onSave={({ closeDialog, item, setErrors }) => {
        const requestParam = {
          projectLabel: props.projectId,
          filtersIds: item,
        };

        onAction(requestParam, "update", props.projectId)
          .then((projectWithUpdatedSources) => {
            closeDialog();
            showSuccessToastr(t("Filter edited successfully"));
            props.onChange(projectWithUpdatedSources);
          })
          .catch((error) => {
            setErrors(error.errors);
            showErrorToastr(error.messages, { autoHide: false });
          });
      }}
      renderCreationContent={({ setItem }) => {
        return (
          <FiltersProjectSelection
            isUpdatingFilter={isLoading}
            projectId={props.projectId}
            initialSelectedFiltersIds={props.selectedFilters
              .filter((filter) => !statesEnum.isDeletion(filter.state))
              .map((filter) => filter.id)}
            onChange={setItem}
          />
        );
      }}
      renderContent={() => (
        <div className="min-height-panel">
          {messages.messages}
          <div className="row">
            <div className="col-md-12">
              {moduleFilters.length > 0 && (
                <>
                  <h4>
                    {t("AppStreams")} <small>{t("enabled module streams")}</small>
                  </h4>
                  <ul className="list-group">
                    {moduleFilters.map((filter) => renderFilterEntry(filter, props.projectId))}
                  </ul>
                </>
              )}
            </div>
          </div>

          <div className="row">
            <div className="col-md-6">
              {denyFilters.length > 0 && (
                <>
                  <h4>
                    {t("Deny")} <small>{t("filter out")}</small>
                  </h4>
                  <ul className="list-group">
                    {denyFilters.map((filter) => renderFilterEntry(filter, props.projectId))}
                  </ul>
                </>
              )}
            </div>

            <div className="col-md-6">
              {allowFilters.length > 0 && (
                <>
                  <h4>
                    {t("Allow")}{" "}
                    <small>{t("select from the full source even if you have excluded them before with deny")}</small>
                  </h4>
                  <ul className="list-group">
                    {allowFilters.map((filter) => renderFilterEntry(filter, props.projectId))}
                  </ul>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    />
  );
};

export default FiltersProject;
