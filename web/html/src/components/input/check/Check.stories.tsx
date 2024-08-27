import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Check } from "./Check";

export default () => {
  const model = {
    booleanValue: false,
  };

  return (
    <Form
      model={model}
      onSubmit={() => console.log(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
      onChange={(newModel) => {
        model["booleanValue"] = newModel["booleanValue"];
      }}
    >
      <Check name="booleanValue" label="Boolean value" divClass="col-md-6 col-md-offset-3 offset-md-3" />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
