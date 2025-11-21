import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { DEPRECATED_Check } from "./DEPRECATED_Check";

export default () => {
  const model = {
    booleanValue: false,
  };

  return (
    <Form
      model={model}
      onSubmit={() => Loggerhead.info(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
      onChange={(newModel) => {
        model["booleanValue"] = newModel["booleanValue"];
      }}
    >
      <DEPRECATED_Check name="booleanValue" label="Boolean value" divClass="col-md-6 col-md-offset-3 offset-md-3" />
      <SubmitButton className="btn-primary" text={t("Submit")} />
    </Form>
  );
};
