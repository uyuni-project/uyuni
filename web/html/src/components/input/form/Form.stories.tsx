import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Text, Validate } from "components/input";

export default () => {
  const [model, setModel] = useState({
    name: "John",
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
        name="name"
        label={t("Name")}
        required
        labelClass="col-md-3"
        divClass="col-md-6"
        validate={Validate.minLength(2)}
      />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
