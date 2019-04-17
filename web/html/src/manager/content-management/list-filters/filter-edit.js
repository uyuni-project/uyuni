// @flow
import React, {useState, useEffect} from 'react';
import {ModalLink} from "../../../components/dialog/ModalLink";
import {closeDialog, Dialog} from "../../../components/dialog/Dialog";
import {Button} from "../../../components/buttons";
import useLifecycleActionsApi from "../shared/api/use-lifecycle-actions-api";
import {Loading} from "components/loading/loading";
import {showErrorToastr, showSuccessToastr} from "components/toastr/toastr";

import type {FilterType} from '../shared/type/filter.type.js';
import FilterForm from "./filter-form";
import {showDialog} from "components/dialog/util";

const FilterEditModalContent = ({open, isLoading, filter, onChange, editing}) => {
  if (!open) {
    return null;
  }

  if (isLoading) {
    return (
      <Loading text={t('Updating the filter...')}/>
    )
  }

  return (
    <FilterForm
      filter={filter}
      editing={editing}
      onChange={(updatedFilter) => onChange(updatedFilter)}/>
  )
}

type FilterEditProps = {
  id: string,
  filter: FilterType,
  icon: string,
  buttonText: string,
  onChange: Function,
  openFilterId: number,
  editing?: boolean,
};

const FilterEdit = (props: FilterEditProps) => {

  const [open, setOpen] = useState(false);
  const [item, setItem] = useState(props.filter);

  const modalNameId = `${props.id}-modal`;

  useEffect(() => {
    if(props.filter.id === props.openFilterId) {
      showDialog(modalNameId);
      setOpen(true);
      setItem(props.filter);
    }
  }, []);

  const {onAction, cancelAction, isLoading} = useLifecycleActionsApi({resource: 'filters'});

  const modalTitle = props.editing
    ? t('Update filter')
    : t('Create a new filter')

  return (
    <React.Fragment>
      <ModalLink
        id={`${props.id}-modal-link`}
        icon={props.icon}
        className="btn-link"
        text={props.buttonText}
        target={modalNameId}
        onClick={() => {
          setOpen(true);
          setItem(props.filter);
        }}
      />

      <Dialog id={modalNameId}
              title={modalTitle}
              closableModal={false}
              className="modal-lg"
              content={
                <FilterEditModalContent
                  filter={item}
                  open={open}
                  onChange={setItem}
                  isLoading={isLoading}
                  editing={props.editing}
                />}
              onClosePopUp={() => setOpen(false)}
              buttons={
                <React.Fragment>
                  <div className="btn-group col-lg-6">
                    {
                      props.editing && <Button
                        id={`${props.id}-modal-delete-button`}
                        className="btn-danger"
                        text={t('Delete')}
                        disabled={isLoading}
                        handler={() => {
                          onAction(item, "delete", item.id)
                            .then((updatedListOfFilters) => {
                              closeDialog(modalNameId);
                              showSuccessToastr(t("Filter deleted successfully"));
                              props.onChange(updatedListOfFilters);
                            })
                            .catch((error) => {
                              showErrorToastr(error);
                            })
                        }}
                      />
                    }
                  </div>
                  <div className="col-lg-6">
                    <div className="pull-right btn-group">
                      <Button
                        id={`${props.id}-modal-cancel-button`}
                        className="btn-default"
                        text={t('Cancel')}
                        handler={() => {
                          cancelAction();
                          closeDialog(modalNameId);
                        }}
                      />
                      <Button
                        id={`${props.id}-modal-save-button`}
                        className="btn-primary"
                        text={t('Save')}
                        disabled={isLoading}
                        handler={() => {
                          if(props.editing) {
                            onAction(item, "update", item.id)
                              .then((updatedListOfFilters) => {
                                closeDialog(modalNameId);
                                showSuccessToastr(t("Filter updated successfully"));
                                props.onChange(updatedListOfFilters);
                              })
                              .catch((error) => {
                                showErrorToastr(error);
                              })
                          } else {
                            onAction(item, "create")
                              .then((updatedListOfFilters) => {
                                closeDialog(modalNameId);
                                showSuccessToastr(t("Filter created successfully"));
                                props.onChange(updatedListOfFilters);
                              })
                              .catch((error) => {
                                showErrorToastr(error);
                              })
                          }
                        }}
                      />
                    </div>
                  </div>
                </React.Fragment>
              } />
    </React.Fragment>
  );
};

export default FilterEdit;
