import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Password, Validate } from "components/input";

export default () => {
  const [model, setModel] = useState({
    password: "secret",
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
        labelClass="col-md-3"
        divClass="col-md-6"
        validate={Validate.minLength(4)}
      />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
