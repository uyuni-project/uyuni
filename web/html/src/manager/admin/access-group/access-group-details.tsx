import * as React from "react";
import { useEffect, useState } from "react";

import { AccessGroupState } from "manager/admin/access-group/access-group";

import { Form } from "components/formik";
import { Field } from "components/formik/field";

import Network from "utils/network";

type Props = {
  state: AccessGroupState;
  onChange: Function;
  errors: any;
};

type Organization = {
  value: number;
  label: string;
};

const options = [
  { value: "Activation KeyAdmin", label: "ActivationKeyAdmin" },
  { value: "Image Administrator", label: "ImageAdministrator" },
  { value: "Configuration Administrator", label: "ConfigurationAdministrator" },
  { value: "Channel Administrator", label: "ChannelAdministrator" },
  { value: "System Group Administrator", label: "SystemGroupAdministrator" },
  { value: "KeyAdmin", label: "KeyAdmin" },
  { value: "Image and channel", label: "ImageChannelAdmi" },
  { value: "Configurations", label: "ConfigurationAdmin" },
  { value: "Channel readonly", label: "ChannelAReadOnly" },
  { value: "SystemModify", label: "SystemModify" },
];

const AccessGroupDetails = (props: Props) => {
  const [organizations, setOrganizations] = useState<Organization[]>([]);

  useEffect(() => {
    getOrganizations();
  }, []);

  const getOrganizations = () => {
    const endpoint = "/rhn/manager/api/admin/access-group/organizations";
    return (
      Network.get(endpoint)
        .then((orgs) => {
          setOrganizations(orgs.map((org) => ({ value: org.orgId, label: org.orgName })));
        })
        // TODO: Handle errors
        .catch(props.errors)
    );
  };

  const handleFormChange = (model) => {
    props.onChange({ ...model, orgName: organizations.filter((org) => org.value === model.orgId)[0]?.label });
  };

  return (
    <Form
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
            options={options}
            as={Field.Select}
            placeholder={t("Search for existing access groups...")}
            emptyText={t("No Access group")}
            labelClass="col-md-3"
            divClass="col-md-6"
            isMulti
          />
          <div className="offset-md-3 col-md-6">
            {t(
              "This action copy permissions from an existing access group to a new one. Once created, the new access group will function independently, unaffected by future updates to the original."
            )}
          </div>
        </div>
      ) : null}
    </Form>
  );
};

export default AccessGroupDetails;
