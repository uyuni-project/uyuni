import * as React from "react";
import { useContext } from "react";

import { DEPRECATED_Select, FormContext } from "components/input";

type ModuleSelectorProps = {
  modules: Record<
    string,
    {
      default: string;
      streams: string[];
    }
  >;
  isLoading: boolean;
};

export default function ModuleSelector(props: ModuleSelectorProps) {
  const formContext = useContext(FormContext);
  const createOption = (value) => ({ value: value, label: value });
  const moduleOptions = Object.keys(props.modules).map(createOption);
  const getModuleName = (ctx) => ctx.model["moduleName"];
  const getStreamOptions = (ctx) => ((props.modules[getModuleName(ctx)] || {}).streams || []).map(createOption);
  const getDefaultStream = (module) => (props.modules[module] || {}).default;

  return (
    <>
      <DEPRECATED_Select
        name="moduleName"
        label={t("Module")}
        isLoading={props.isLoading}
        options={moduleOptions}
        labelClass="col-md-3"
        divClass="col-md-6"
        placeholder={t("Select a module...")}
        emptyText={t("No modules available")}
        onChange={(name, module) => {
          formContext.model["moduleStream"] = getDefaultStream(module);
        }}
        required
      />
      <DEPRECATED_Select
        name="moduleStream"
        label={t("Stream")}
        options={getStreamOptions(formContext)}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!getModuleName(formContext)}
        placeholder={t("Select a stream...")}
        emptyText={t("No streams available")}
      />
    </>
  );
}
