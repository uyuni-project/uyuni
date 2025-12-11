import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from "react";

import { FormikProps } from "formik";

import { AccessGroupState } from "manager/admin/access-control/access-group";

import { Form } from "components/formik";
import { Field } from "components/formik/field";
import { MessagesContainer, showErrorToastr } from "components/toastr";

import Network from "utils/network";

export type AccessGroupDetailsHandle = {
  validate: () => Promise<boolean>;
};

type Props = {
  state: AccessGroupState;
  onChange: () => void;
  errors: any;
};

type Organization = {
  value: number;
  label: string;
};

type SelectOption = {
  value: number | string;
  label: string;
};

const AccessGroupDetails = forwardRef<AccessGroupDetailsHandle, Props>((props, ref) => {
  const [organizations, setOrganizations] = useState<Organization[]>([]);
  const [accessGroups, setAccessGroups] = useState<SelectOption[]>([]);
  const [isLoadingGroups, setIsLoadingGroups] = useState(false);

  const formikRef = useRef<FormikProps<AccessGroupState>>(null);

  useEffect(() => {
    getOrganizations();
  }, []);

  const getOrganizations = () => {
    const endpoint = "/rhn/manager/api/admin/access-control/access-group/organizations";
    return Network.get(endpoint)
      .then((orgs) => {
        setOrganizations(orgs.map((org) => ({ value: org.orgId, label: org.orgName })));
      })
      .catch(() => {
        showErrorToastr(t("An unexpected error occurred while fetching organizations."));
      });
  };

  useEffect(() => {
    const getAccessGroups = (orgId: number) => {
      setIsLoadingGroups(true);
      const endpoint = `/rhn/manager/api/admin/access-control/access-group/organizations/${orgId}/access-groups`;
      Network.get(endpoint)
        .then((groups) => {
          setAccessGroups(groups.map((group) => ({ value: group.id, label: group.description })));
        })
        .catch(() => {
          showErrorToastr(t("An unexpected error occurred while fetching access groups."));
        })
        .finally(() => {
          setIsLoadingGroups(false);
        });
    };

    if (props.state.orgId && !props.state.id) {
      getAccessGroups(props.state.orgId);
    } else {
      setAccessGroups([]);
    }
  }, [props.state.orgId, props.errors]);

  useImperativeHandle(ref, () => ({
    async validate() {
      if (formikRef.current) {
        await formikRef.current.submitForm();
        return formikRef.current.isValid;
      }
      return true;
    },
  }));

  const handleFormChange = (model) => {
    const selectedOrg = organizations.find((org) => org.value === model.orgId);

    // When organization changes, clear the selected access groups
    const updatedModel =
      model.orgId !== props.state.orgId
        ? { ...model, orgName: selectedOrg?.label || "", accessGroups: [] }
        : { ...model, orgName: selectedOrg?.label || "" };
    props.onChange(updatedModel);
  };

  return (
    <>
      <MessagesContainer />
      <Form
        innerRef={formikRef}
        initialValues={props.state}
        // TODO: Use onChange instead of validate to update access group details
        // onChange={(model) => {
        //   props.onChange(model);
        // }}
        onSubmit={() => {}}
        validate={handleFormChange}
      >
        <div className="row">
          <Field required name="name" label={t("Name")} labelClass="col-md-3" divClass="col-md-6" />
        </div>
        <div className="row">
          <Field
            required
            name="description"
            rows={10}
            label={t("Description")}
            as={Field.TextArea}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
        </div>
        <div className="row">
          <Field
            disabled={!!props.state.id}
            required
            name="orgId"
            label={t("Organization")}
            options={organizations}
            as={Field.Select}
            placeholder={t("Search for organizations...")}
            emptyText={t("No Organizations Found")}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
        </div>
        {!props.state.id ? (
          <div className="row">
            <Field
              name="accessGroups"
              label={t("Copy Permissions From")}
              options={accessGroups}
              as={Field.Select}
              placeholder={t("Search for existing access groups...")}
              emptyText={t("No Access group")}
              labelClass="col-md-3"
              divClass="col-md-6"
              isMulti
              isLoading={isLoadingGroups}
            />
            <div className="offset-md-3 col-md-6">
              {t(
                "This action copy permissions from an existing access group to a new one. Once created, the new access group will function independently, unaffected by future updates to the original."
              )}
            </div>
          </div>
        ) : null}
      </Form>
    </>
  );
});

export default AccessGroupDetails;
