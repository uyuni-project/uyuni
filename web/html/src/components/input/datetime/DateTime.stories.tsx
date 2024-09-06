import { SubmitButton } from "components/buttons";

import { localizedMoment } from "utils";

import { Form } from "../form/Form";
import { DateTime } from "./DateTime";

export default () => {
  const model = { time: localizedMoment() };

  return (
    <>
      <p>Dates and times are shown in the user's configured timezone.</p>

      <Form
        model={model}
        onChange={(newModel) => {
          model["time"] = newModel["time"];
        }}
        onSubmit={() => Loggerhead.info(model)}
        divClass="col-md-12"
        formDirection="form-horizontal"
      >
        <DateTime name="time" label={t("Time")} required labelClass="col-md-3" divClass="col-md-6" />
        <SubmitButton className="btn-success" text={t("Submit")} />
      </Form>
    </>
  );
};
