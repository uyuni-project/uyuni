import * as React from "react";
import { useState, useEffect } from "react";
import { ModalLink } from "components/dialog/ModalLink";
import { closeDialog, Dialog } from "components/dialog/LegacyDialog";
import { Button } from "components/buttons";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import { Loading } from "components/utils/Loading";
import { showErrorToastr, showSuccessToastr } from "components/toastr/toastr";
import FilterForm from "./filter-form";
import { showDialog } from "components/dialog/util";
import { mapFilterFormToRequest } from "./filter.utils";
import { FilterFormType } from "../shared/type/filter.type";

type FilterEditModalContentProps = React.ComponentProps<typeof FilterForm> & {
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
  icon: string;
  buttonText: string;
  onChange: Function;
  openFilterId?: number;
  projectLabel?: string;
  editing?: boolean;
};

const redirectToProject = (projectLabel: string) => {
  window.pageRenderers?.spaengine?.navigate?.(`/rhn/manager/contentmanagement/project/${projectLabel || ""}`);
};

const FilterEdit = (props: FilterEditProps) => {
  const [open, setOpen] = useState(false);
  const [item, setFormData] = useState(props.initialFilterForm);
  const [errors, setErrors] = useState({});
  const [formValidInClient, setFormValidInClient] = useState(true);
  const { onAction, cancelAction, isLoading } = useLifecycleActionsApi({ resource: "filters" });

  const itemId = item.id?.toString() ?? undefined;
  const modalNameId = `${props.id}-modal`;

  useEffect(() => {
    const openWithInitial = props.initialFilterForm.id && props.initialFilterForm.id === props.openFilterId;
    const openCreateWithParams = props.openFilterId === -1 && !props.editing;
    if (openWithInitial || openCreateWithParams) {
      showDialog(modalNameId);
      setOpen(true);
      setFormData(props.initialFilterForm);
    }
  }, []);

  const onSave = () => {
    if (!formValidInClient) {
      showErrorToastr(t("Check the required fields below"), { autoHide: false });
    } else {
      if (props.editing) {
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
            showErrorToastr(error.messages, { autoHide: false });
          });
      }
    }
  };

  const modalTitle = props.editing ? t("Filter Details") : t("Create a new filter");

  return (
    <React.Fragment>
      <ModalLink
        id={`${props.id}-modal-link`}
        icon={props.icon}
        text={props.buttonText}
        target={modalNameId}
        onClick={() => {
          setOpen(true);
          setFormData(props.initialFilterForm);
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
            onChange={setFormData}
            onClientValidate={setFormValidInClient}
            isLoading={isLoading}
            editing={props.editing}
          />
        }
        onClosePopUp={() => setOpen(false)}
        buttons={
          <React.Fragment>
            <div className="btn-group col-lg-6">
              {props.editing && (
                <Button
                  id={`${props.id}-modal-delete-button`}
                  className="btn-danger"
                  text={t("Delete")}
                  disabled={isLoading}
                  handler={() => {
                    onAction(mapFilterFormToRequest(item, props.projectLabel), "delete", itemId)
                      .then((updatedListOfFilters) => {
                        closeDialog(modalNameId);
                        showSuccessToastr(t("Filter deleted successfully"));
                        props.onChange(updatedListOfFilters);
                      })
                      .catch((error) => {
                        showErrorToastr(error.messages, { autoHide: false });
                      });
                  }}
                />
              )}
            </div>
            <div className="col-lg-6">
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
                  text={t("Save")}
                  disabled={isLoading}
                  handler={onSave}
                />
              </div>
            </div>
          </React.Fragment>
        }
      />
    </React.Fragment>
  );
};

export default FilterEdit;
