import * as React from "react";

import { Panel } from "components/panels/Panel";
import { Form, Text } from "components/input";


type AccessGroupDetaolsType = {
  name: string;
  label: string;
  description?: string;
  org_id?: string;
};

type Props = {
  properties: AccessGroupDetaolsType;
  errors: any;
  onChange: Function;
};

const Properties = (props: Props) => {
  return (
    <Form
      model={props.properties}
      errors={props.errors}
      onChange={(model) => {
        console.log("Form Model:", model); // Debugging model change
        // props.onChange(model); // Pass updated properties
        props.onChange({
          ...props.properties, // Keep existing values
          ...model, // Merge new changes
        });
      }}
    >
      {props.errors}
      <div className="row">
        <Text
          required
          name="label"
          label={t("Label")}
          labelClass="col-md-2"
          divClass="col-md-10"
        />
      </div>
      <div className="row">
        <Text required name="name" label={t("Name")} labelClass="col-md-2" divClass="col-md-10" />
      </div>
      <div className="row">
        <Text name="description" label={t("Description")} labelClass="col-md-2" divClass="col-md-10" />
      </div>
      <div className="row">
        <Text name="org_id" label={t("Org_id")} labelClass="col-md-2" divClass="col-md-10" />
      </div>
    </Form>
  );
};

export default Properties;
