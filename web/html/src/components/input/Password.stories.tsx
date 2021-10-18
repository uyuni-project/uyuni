import * as React from "react";
import { Form } from "./Form";
import { Password } from "./Password";
import { SubmitButton } from "components/buttons";

export default {
  component: Password,
  title: "Forms/Password",
};

let model = {
  password: "secret",
};

export const Example = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["password"] = newModel["password"];
    }}
    onSubmit={() => alert(`Secret revealed: ${model["password"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Password
      name="password"
      label={t("Password")}
      required
      invalidHint={t("Minimum 4 characters")}
      labelClass="col-md-3"
      divClass="col-md-6"
      validators={[(value) => value.length > 4]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);
