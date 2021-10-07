import * as React from "react";
import { Text } from "components/input/Text";
import { Select } from "components/input/Select";
import { FormContext } from "components/input/Form";

type Props = {
  index: number;
  domainCaps: any;
  poolCaps: any;
  pools: Array<any>;
  onlyHandledDisks: boolean;
};

export function GuestDiskFileFields(props: Props) {
  const formContext = React.useContext(FormContext);

  const pool_options = ((props.poolCaps.pool_types.find((item) => item.name === "dir") || {}).options || {}).volume;
  const isCdrom = formContext.model[`disk${props.index}_device`] === "cdrom";
  const format_values = pool_options.targetFormatType.filter((format) => format === "raw" || !isCdrom) || [];
  const default_format = isCdrom ? "raw" : "qcow2";
  return (
    <>
      <Text
        name={`disk${props.index}_source_file`}
        label={t("File")}
        disabled={!Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
        labelClass="col-md-3"
        divClass="col-md-6"
      />
      {!isCdrom && (
        <Select
          key={`disk${props.index}_format`}
          name={`disk${props.index}_format`}
          label={t("Format")}
          labelClass="col-md-3"
          divClass="col-md-6"
          disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
          defaultValue={default_format}
          options={format_values}
        />
      )}
    </>
  );
}
