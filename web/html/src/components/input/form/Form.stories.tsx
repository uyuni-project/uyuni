import { SubmitButton } from "components/buttons";

import { Text } from "../text/Text";
import { Form } from "./Form";

export default () => {
  const model = {
    name: "John",
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        model["name"] = newModel["name"];
      }}
      onSubmit={() => Loggerhead.info(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Text
        name="name"
        label={t("Name")}
        required
        invalidHint={t("Minimum 2 characters")}
        labelClass="col-md-3"
        divClass="col-md-6"
        validate={[(value) => value.length > 2]}
      />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
