import * as React from "react";
import { Text } from "components/input/Text";

export default function TextInput() {
  return (
    <>
      <Text name="moduleName" label={t("Module Name")} labelClass="col-md-3" divClass="col-md-6" required />
      <Text name="moduleStream" label={t("Stream")} labelClass="col-md-3" divClass="col-md-6" />
    </>
  );
}
