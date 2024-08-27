import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Text } from "./Text";

export default () => {
  const model = {
    firstname: "John",
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        model["firstname"] = newModel["firstname"];
      }}
      onSubmit={() => console.log(model)}
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
      <p className="col-md-offset-3 offset-md-3 col-md-6">(Last name has no label.)</p>
      <Text
        name="lastname"
        required
        invalidHint={t("Minimum 2 characters")}
        divClass="col-md-6 col-md-offset-3 offset-md-3"
        validators={[(value) => value.length > 2]}
      />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
