import { FormContext, Select } from "components/input";
import * as React from "react";
import { Props as FilterFormProps } from "../filter-form";
import LivePatching from "./live-patching";
import AppStreams from "./app-streams";

export enum Template {
  LivePatchingSystem = "LivePatchingSystem",
  LivePatchingProduct = "LivePatchingProduct",
  AppStreamsWithDefaults = "AppStreamsWithDefaults",
}

const TemplateForm = (props: FilterFormProps) => {
  const formContext = React.useContext(FormContext);
  const template = formContext.model.template;
  switch (template) {
    case Template.LivePatchingSystem:
    case Template.LivePatchingProduct:
      return <LivePatching template={template} {...props} />;
    case Template.AppStreamsWithDefaults:
      return <AppStreams template={template} {...props} />;
    default:
      return null;
  }
};

export default (props: FilterFormProps) => {
  const templates = [
    {
      label: t("Live patching based on a specific system"),
      value: Template.LivePatchingSystem,
    },
    {
      label: t("Live patching based on a SUSE product"),
      value: Template.LivePatchingProduct,
    },
    {
      label: t("AppStream modules with defaults"),
      value: Template.AppStreamsWithDefaults,
    },
  ];
  return (
    <>
      <Select
        label={t("Template")}
        name="template"
        labelClass="col-md-3"
        divClass="col-md-8"
        defaultValue={Template.LivePatchingSystem}
        options={templates}
      />
      <TemplateForm {...props} />
    </>
  );
};
