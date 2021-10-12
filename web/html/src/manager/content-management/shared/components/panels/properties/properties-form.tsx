import * as React from "react";
import { Form, Text } from "components/input";

import { ProjectPropertiesType } from "../../../type";

type Props = {
  properties: ProjectPropertiesType;
  errors: any;
  onChange: Function;
  editing?: boolean;
};

const PropertiesForm = (props: Props) => (
  <Form
    model={props.properties}
    errors={props.errors}
    onChange={(model) => {
      props.onChange(model);
    }}
  >
    <div className="row">
      <Text required name="name" label={t("Name")} labelClass="col-md-2" divClass="col-md-10" />
    </div>
    <div className="row">
      <Text
        required
        name="label"
        label={t("Label")}
        labelClass="col-md-2"
        divClass="col-md-10"
        disabled={props.editing}
      />
    </div>
    <div className="row">
      <Text name="description" label={t("Description")} labelClass="col-md-2" divClass="col-md-10" />
    </div>
  </Form>
);

export default PropertiesForm;
