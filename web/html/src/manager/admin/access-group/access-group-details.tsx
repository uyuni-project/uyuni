import * as React from "react";

import {DEPRECATED_Select, Form, Text, TextArea} from "components/input";

type Detailsproperties = {
  name: string;
  description: string;
  accessGroup: string[];
};

type Props = {
  properties: Detailsproperties;
  onChange: Function;
  errors: any;
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
  return (
    <Form
      model={props.properties}
      errors={props.errors}
      onChange={(model) => {
        props.onChange(model);
      }}
    >
      <div className="row">
        <Text required name="name" label={t("Name")} labelClass="col-md-3" divClass="col-md-6" />
      </div>
      <div className="row">
        <TextArea name="description" rows={10} label={t("Description")} labelClass="col-md-3" divClass="col-md-6" />
      </div>
      <div className="row">
        <DEPRECATED_Select
          name="accessGroup"
          label={t("Copy Permissions From")}
          options={options}
          placeholder={t("Search for existing access groups...")}
          emptyText={t("No Access group")}
          labelClass="col-md-3"
          divClass="col-md-6"
          isMulti
        />
        <div className="offset-md-3 col-md-6">
          This action copy permissions from an existing access group to a new one. Once created, the new access group
          will function independently, unaffected by future updates to the original.
        </div>
      </div>
    </Form>
  );
};

export default AccessGroupDetails;
