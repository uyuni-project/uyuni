import * as React from "react";
import { Form } from "./Form";
import { Range } from "./Range";
import { SubmitButton } from "components/buttons";

export default {
  component: Range,
  title: "Forms/Range",
};

export const Example = () => {
  const [model, setModel] = React.useState({
    port_start: "1000",
    port_end: "1100",
  });

  const allOrNone = (value) => {
    return Object.values(value).every((item) => item != null) || Object.values(value).every((item) => item == null);
  };

  return (
    <Form
      model={model}
      onChange={setModel}
      onSubmit={() => alert(`Range ${model.port_start} - ${model.port_end}`)}
      divClass="col-md-12"
      formDirection="form-horizontal"
    >
      <Range
        prefix="port"
        label={t("Port range")}
        required
        invalidHint={t("Both values need to be positive integers")}
        labelClass="col-md-3"
        divClass="col-md-6"
        validators={[
          allOrNone,
          (value) =>
            Object.values(value).every((item) => typeof item === "string" && (item === "" || item.match(/^[0-9]+$/))),
          ({ port_start, port_end }) =>
            (port_start === "" && port_end === "") || parseInt(port_start, 10) <= parseInt(port_end, 10),
        ]}
      />
      <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
    </Form>
  );
};
