import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Text, Validation } from "components/input";

export default () => {
  const [model, setModel] = useState({
    firstname: "John",
  });

  return (
    <Form
      model={model}
      onChange={setModel}
      onSubmit={() => Loggerhead.info(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Text
        name="firstname"
        label={t("First Name")}
        required
        labelClass="col-md-3"
        divClass="col-md-6"
        validate={Validation.minLength(2)}
      />
      <p className="col-md-offset-3 offset-md-3 col-md-6">(Last name has no label.)</p>
      <Text name="lastname" required divClass="col-md-6 col-md-offset-3 offset-md-3" validate={Validation.minLength(2)} />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
