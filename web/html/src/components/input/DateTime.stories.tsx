import * as React from "react";
import { Form } from "./Form";
import { DateTime } from "./DateTime";
import { SubmitButton } from "components/buttons";

export default {
  component: DateTime,
  title: "Forms/DateTime",
};

let model = {
  time: new Date(),
};

export const Example = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["time"] = newModel["time"];
    }}
    onSubmit={() => alert(`Set time: ${model["time"].toISOString()}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <DateTime name="time" label={t("Time")} required labelClass="col-md-3" divClass="col-md-6" />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);
