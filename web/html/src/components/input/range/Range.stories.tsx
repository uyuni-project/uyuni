import { useState } from "react";

import { SubmitButton } from "components/buttons";
import { Form, Range } from "components/input";

export default () => {
  const [model, setModel] = useState({
    port_start: "1000",
    port_end: "1100",
  });

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        console.log(newModel);
        setModel(newModel);
      }}
      onSubmit={() => Loggerhead.info(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Range prefix="port" label={t("Port range")} required labelClass="col-md-3" divClass="col-md-6" />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
