import * as React from "react";
import { useState } from "react";
import { Select } from "components/input";
import { showErrorToastr } from "components/toastr";
import useLifecycleActionsApi from "../../shared/api/use-lifecycle-actions-api";
import ModuleSelector from "./module-selector";

type SelectInputProps = {
  channels: { id: string; name: string }[];
};

export default function SelectInput(props: SelectInputProps) {
  const [modules, setModules] = useState({});
  const [isShowInputs, setShowInputs] = useState(false);
  const { onAction, isLoading } = useLifecycleActionsApi({ resource: "appstreams" });

  const onChannelChange = (name, value) => {
    if (value) {
      setShowInputs(true);
      onAction(null, "get", value)
        .then(setModules)
        .catch((error) => showErrorToastr(error.messages));
    } else {
      setModules({});
    }
  };

  return (
    <>
      <Select
        name="moduleChannel"
        label={t("Channel")}
        labelClass="col-md-3"
        divClass="col-md-6"
        onChange={onChannelChange}
        options={props.channels}
        getOptionValue={(c) => c.id}
        getOptionLabel={(c) => c.name}
      />
      {isShowInputs && <ModuleSelector modules={modules} isLoading={isLoading} />}
    </>
  );
}
