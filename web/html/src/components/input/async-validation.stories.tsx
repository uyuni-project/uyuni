import { useState } from "react";

import { Form } from "./form/Form";
import { Text } from "./text/Text";

export default () => {
  const [model, setModel] = useState({
    firstname: "John",
  });

  const isValid = (value: string): Promise<boolean> => {
    return new Promise((resolve) => {
      window.setTimeout(() => resolve(value.length > 2), 500);
    });
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => setModel(newModel)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <p>Inputs support async validation:</p>
      <Text
        name="firstname"
        label={t("First Name")}
        required
        invalidHint={t("Minimum 3 characters")}
        labelClass="col-md-3"
        divClass="col-md-6"
        validators={[isValid]}
      />
    </Form>
  );
};
