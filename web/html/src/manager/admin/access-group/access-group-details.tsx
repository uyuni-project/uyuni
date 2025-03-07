import * as React from "react";

import { Form, Text, TextArea } from "components/input";

type Detailsproperties = {
  name: string,
  description: string,
}

type Props = {
  properties: Detailsproperties,
  onChange: Function;
  errors: any
};

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
        <Text required name="name" label={t("Name")} labelClass="col-md-2" divClass="col-md-6" />
      </div>
      <div className="row">
        <TextArea name="description" rows={10} label={t("Description")} labelClass="col-md-2" divClass="col-md-6" />
      </div>
    </Form>
  );
};

export default AccessGroupDetails;
