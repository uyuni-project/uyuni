import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Select } from "./Select";

export default () => {
  const model = {
    level: "beginner",
  };

  return (
    <>
      <p>A simple dropdown:</p>

      <Form
        model={model}
        onChange={(newModel) => {
          model["level"] = newModel["level"];
        }}
        onSubmit={() => Loggerhead.info(model)}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <Select
          name="level"
          label={t("Level")}
          required
          labelClass="col-md-3"
          divClass="col-md-6"
          options={["beginner", "normal", "expert"]}
        />
        <SubmitButton className="btn-success" text={t("Submit")} />
      </Form>
    </>
  );
};
