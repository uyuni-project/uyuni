import * as React from "react";
import { Form } from "./Form";
import { Text } from "./Text";
import { SubmitButton } from "components/buttons";

export default {
  component: Form,
  title: "Forms/Form",
};

let model = {
  firstname: "John",
};

export const Example = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["firstname"] = newModel["firstname"];
    }}
    onSubmit={() => alert(`Hello ${model["firstname"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Text
      name="firstname"
      label={t("First Name")}
      required
      invalidHint={t("Minimum 2 characters")}
      labelClass="col-md-3"
      divClass="col-md-6"
      validators={[(value) => value.length > 2]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);
