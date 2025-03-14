import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Password } from "components/input";

import { Utils } from "utils/functions";

export default () => {
  const [model, setModel] = useState({
    password: Utils.generatePassword(),
  });

  return (
    <Form
      model={model}
      onChange={setModel}
      onSubmit={() => Loggerhead.info(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Password
        name="password"
        label={t("Password")}
        required
        invalidHint={t("Must be at least 4 characters")}
        labelClass="col-md-3"
        divClass="col-md-6"
        validators={[(value) => value.length >= 4]}
      />
      <SubmitButton className="btn-primary" text={t("Submit")} />
    </Form>
  );
};
