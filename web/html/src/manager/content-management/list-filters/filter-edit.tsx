import { type ComponentProps, Fragment, useEffect, useState } from "react";

import { Button } from "components/buttons";
import { closeDialog, Dialog } from "components/dialog/LegacyDialog";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { showDialog } from "components/dialog/util";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";
import { Loading } from "components/utils/loading/Loading";

import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import { FilterFormType } from "../shared/type/filter.type";
import { mapFilterFormToRequest } from "./filter.utils";
import FilterForm from "./filter-form";

type FilterEditModalContentProps = ComponentProps<typeof FilterForm> & {
  open: boolean;
  isLoading: boolean;
};

const FilterEditModalContent = ({
  open,
  isLoading,
  filter,
  errors,
  onChange,
  onClientValidate,
  editing,
}: FilterEditModalContentProps) => {
  if (!open) {
    return null;
  }

  if (isLoading) {
    return <Loading text={t("Updating the filter...")} />;
  }

  return (
    <FilterForm
      filter={filter}
      errors={errors}
      editing={editing}
      onChange={(updatedFilter) => onChange(updatedFilter)}
      onClientValidate={onClientValidate}
    />
  );
};

type FilterEditProps = {
  id: string;
  initialFilterForm: Partial<FilterFormType>;
  icon?: string;
  buttonText?: string;
  buttonTitle?: string;
  className?: string;
  onChange: (...args: any[]) => any;
  openFilterId?: number;
  projectLabel?: string;
  editing?: boolean;
};

const redirectToProject = (projectLabel: string) => {
  window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/contentmanagement/project/${projectLabel || ""}`);
};

const FilterEdit = (props: FilterEditProps) => {
  const [open, setOpen] = useState(false);
  const [item, setItem] = useState(props.initialFilterForm);
  const [errors, setErrors] = useState({});
  const [formValidInClient, setFormValidInClient] = useState(true);
  const [fetchingFilter, setFetchingFilter] = useState(false);
  const [filterToDelete, setFilterToDelete] = useState<FilterFormType | undefined>();
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({ resource: "filters" });

  const itemId = item.id?.toString() ?? undefined;
  const modalNameId = `${props.id}-modal`;

  useEffect(() => {
    const openWithInitial = props.initialFilterForm.id && props.initialFilterForm.id === props.openFilterId;
    const openCreateWithParams = props.openFilterId === -1 && !props.editing;

    if (openWithInitial || openCreateWithParams) {
      showDialog(modalNameId);
      setOpen(true);
      setItem(props.initialFilterForm);
    } else if (props.openFilterId && props.openFilterId > 0 && !props.editing && !props.initialFilterForm.id) {
      // Handle case where openFilterId is set but filter is not on current page
      // Fetch the filter data
      setFetchingFilter(true);
      onAction(undefined, "get")
        .then((filters: any) => {
          const foundFilter = filters.find((f: any) => f.id === props.openFilterId);
          if (foundFilter) {
            setItem(foundFilter);
            showDialog(modalNameId);
            setOpen(true);
          }
          setFetchingFilter(false);
        })
        .catch(() => {
          setFetchingFilter(false);
        });
    }
  }, [props.openFilterId, props.initialFilterForm.id, props.editing]);

  const onSave = () => {
    if (!formValidInClient) {
      showErrorToastr(t("Check the required fields below"), { autoHide: false, containerId: "filter-modal-errors" });
    } else {
      // Determine if we're editing or creating based on whether item has an id
      const isEditing = !!(item.id || props.editing);

      if (isEditing) {
        onAction(mapFilterFormToRequest(item, props.projectLabel), "update", itemId)
          .then((updatedListOfFilters) => {
            if (props.projectLabel) {
              redirectToProject(props.projectLabel);
            } else {
              closeDialog(modalNameId);
              showSuccessToastr(t("Filter updated successfully"));
              props.onChange(updatedListOfFilters);
            }
          })
          .catch((error) => {
            setErrors(error.errors);
            showErrorToastr(error.messages, { autoHide: false });
          });
      } else {
        onAction(mapFilterFormToRequest(item, props.projectLabel), "create")
          .then((updatedListOfFilters) => {
            if (props.projectLabel) {
              redirectToProject(props.projectLabel);
            } else {
              closeDialog(modalNameId);
              showSuccessToastr(t("Filter created successfully"));
              props.onChange(updatedListOfFilters);
            }
          })
          .catch((error) => {
            setErrors(error.errors);
          });
      }
    }
  };

  // Determine if we're editing based on item having an id
  const isEditingMode = !!(item.id || props.editing);
  const modalTitle = isEditingMode ? t("Filter Details") : t("Create a new filter");

  // Determine if delete is allowed - disable if filter is in use
  const isFilterInUse = !!(item.projects && item.projects.length > 0);

  const confirmDelete = async () => {
    if (!filterToDelete) return;

    if (isFilterInUse) {
      showErrorToastr(t("This filter is in use by one or more projects and cannot be deleted."), {
        autoHide: false,
      });
      closeDialog(modalNameId);
      // setFilterToDelete(undefined);
      return;
    }

    try {
      const remainingFilters = await onAction(
        mapFilterFormToRequest(filterToDelete),
        "delete",
        filterToDelete.id?.toString()
      );
      setFilterToDelete(undefined);
      closeDialog(modalNameId);
      showSuccessToastr(t("Filter deleted successfully"));
      if (props.projectLabel) {
        redirectToProject(props.projectLabel);
      } else {
        props.onChange(remainingFilters);
      }
    } catch (error: any) {
      setFilterToDelete(undefined);
      showErrorToastr(error?.messages ?? error, { autoHide: false });
    }
  };

  return (
    <Fragment>
      <ModalButton
        id={`${props.id}-modal-link`}
        icon={props.icon}
        text={props.buttonText}
        title={props.buttonTitle}
        target={modalNameId}
        className={props.className}
        onClick={() => {
          setOpen(true);
          setItem(props.initialFilterForm);
        }}
      />
      <Dialog
        id={modalNameId}
        title={modalTitle}
        closableModal={false}
        className="modal-lg"
        autoFocus={false}
        content={
          <FilterEditModalContent
            filter={item}
            errors={errors}
            open={open}
            onChange={setItem}
            onClientValidate={setFormValidInClient}
            isLoading={isLoading || fetchingFilter}
            editing={isEditingMode}
          />
        }
        onClosePopUp={() => setOpen(false)}
        buttons={
          <div className="w-100">
            {isEditingMode && (
              <Button
                id={`${props.id}-modal-delete-button`}
                className="btn-danger pull-left"
                text={t("Delete")}
                disabled={isLoading || isFilterInUse}
                title={isFilterInUse ? t("This filter is in use and cannot be deleted") : ""}
                handler={() => {
                  if (isFilterInUse && props.projectLabel) {
                    showErrorToastr(t("This filter is in use by one or more projects and cannot be deleted."), {
                      autoHide: false,
                    });
                  } else {
                    setFilterToDelete(item as FilterFormType);
                    showDialog(`${props.id}-delete-filter-modal`);
                  }
                }}
              />
            )}
            <div className="pull-right btn-group">
              <Button
                id={`${props.id}-modal-cancel-button`}
                className="btn-default"
                text={t("Cancel")}
                handler={() => {
                  cancelAction();
                  if (props.projectLabel) {
                    redirectToProject(props.projectLabel);
                  } else {
                    closeDialog(modalNameId);
                  }
                }}
              />
              <Button
                id={`${props.id}-modal-save-button`}
                className="btn-primary"
                text={isEditingMode ? t("Update") : t("Create")}
                disabled={isLoading}
                handler={onSave}
              />
            </div>
          </div>
        }
      />
      <DeleteDialog
        id={`${props.id}-delete-filter-modal`}
        title={t("Delete Filter")}
        content={
          <span>
            {t("Are you sure you want to delete the filter")} <strong>{filterToDelete?.filter_name}</strong>?
          </span>
        }
        item={filterToDelete}
        onConfirm={confirmDelete}
        onClosePopUp={() => setFilterToDelete(undefined)}
      />
    </Fragment>
  );
};

export default FilterEdit;
