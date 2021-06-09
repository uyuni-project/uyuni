import { FormContext, Select } from "components/input";
import * as React from "react";
import { Props as FilterFormProps } from "../filter-form";
import LivePatching from "./live-patching";

export enum Template {
  LivePatchingSystem = "LivePatchingSystem",
  LivePatchingProduct = "LivePatchingProduct",
}

const TemplateForm = (props: FilterFormProps) => {
  const formContext = React.useContext(FormContext);
  const template = formContext.model.template;
  switch (template) {
    case Template.LivePatchingSystem:
    case Template.LivePatchingProduct:
      return <LivePatching template={template} {...props} />;
    default:
      return null;
  }
};

export default (props: FilterFormProps) => {
  const templates = [
    // TODO: To be implemented
    // {
    //   label: t("Live patching based on a specific system"),
    //   value: Template.LivePatchingSystem,
    // },
    {
      label: t("Live patching based on a SUSE product"),
      value: Template.LivePatchingProduct,
    },
  ];
  return (
    <>
      <Select
        label={t("Template")}
        name="template"
        labelClass="col-md-3"
        divClass="col-md-8"
        defaultValue={Template.LivePatchingProduct}
        options={templates}
      />
      <TemplateForm {...props} />
    </>
  );
};
