import { SubmitButton } from "components/buttons";

import { Form } from "../form/Form";
import { Range } from "./Range";

export default () => {
  const model = {
    port_start: "1000",
    port_end: "1100",
  };

  return (
    <Form
      model={model}
      onChange={(newModel) => {
        model["port_start"] = newModel["port_start"];
        model["port_end"] = newModel["port_end"];
      }}
      onSubmit={() => console.log(model)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Range prefix="port" label={t("Port range")} required labelClass="col-md-3" divClass="col-md-6" />
      <SubmitButton className="btn-success" text={t("Submit")} />
    </Form>
  );
};
