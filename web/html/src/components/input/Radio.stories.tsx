import * as React from "react";
import { Form } from "./Form";
import { Radio } from "./Radio";
import { SubmitButton } from "components/buttons";

export default {
  component: Radio,
  title: "Forms/Radio",
};

let model = {
  level: "beginner",
};

export const Vertical = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["level"] = newModel["level"];
    }}
    onSubmit={() => alert(`Level: ${model["level"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Radio
      name="level"
      label={t("Level")}
      required
      labelClass="col-md-3"
      divClass="col-md-6"
      items={[
        { label: t("Beginner"), value: "beginner" },
        { label: t("Normal"), value: "normal" },
        { label: t("Expert"), value: "expert" },
      ]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);

export const VerticalWithOpenOption = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["level"] = newModel["level"];
    }}
    onSubmit={() => alert(`Level: ${model["level"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Radio
      name="level"
      label={t("Level")}
      required
      openOption={true}
      labelClass="col-md-3"
      divClass="col-md-6"
      items={[
        { label: t("Beginner"), value: "beginner" },
        { label: t("Normal"), value: "normal" },
        { label: t("Expert"), value: "expert" },
      ]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);

export const Horizontal = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["level"] = newModel["level"];
    }}
    onSubmit={() => alert(`Level: ${model["level"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Radio
      name="level"
      inline={true}
      label={t("Level")}
      required
      labelClass="col-md-3"
      divClass="col-md-6"
      items={[
        { label: t("Beginner"), value: "beginner" },
        { label: t("Normal"), value: "normal" },
        { label: t("Expert"), value: "expert" },
      ]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);

export const HorizontalWithOpenOption = () => (
  <Form
    model={model}
    onChange={(newModel) => {
      model["level"] = newModel["level"];
    }}
    onSubmit={() => alert(`Level: ${model["level"]}`)}
    divClass="col-md-12"
    formDirection="form-horizontal"
  >
    <Radio
      name="level"
      inline={true}
      label={t("Level")}
      required
      openOption={true}
      labelClass="col-md-3"
      divClass="col-md-6"
      items={[
        { label: t("Beginner"), value: "beginner" },
        { label: t("Normal"), value: "normal" },
        { label: t("Expert"), value: "expert" },
      ]}
    />
    <SubmitButton id="submit-btn" className="btn-success" text={t("Submit")} />
  </Form>
);
