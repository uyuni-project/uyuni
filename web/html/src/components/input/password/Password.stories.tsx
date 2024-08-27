import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Password } from "./Password";

export default () => {
  const model = {
    password: "secret",
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        model["password"] = newModel["password"];
      }}
      onSubmit={() => Loggerhead.info(model)}
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
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
