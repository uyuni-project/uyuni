import * as React from "react";
import { Form } from "./Form";
import { Check } from "./Check";
import { SubmitButton } from "components/buttons";

export default {
  component: Check,
  title: "Forms/Check",
};

let model = {
  force: false,
};

export const Example = () => (
  <Form
    model={model}
    onSubmit={() => alert(`May${model["force"] ? "" : " NOT"} the force be with you`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
    onChange={(newModel) => {
      model["force"] = newModel["force"];
    }}
  >
    <Check name="force" label="Force action" divClass="col-md-6 col-md-offset-3" />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);
