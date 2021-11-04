import * as React from "react";
import { Select } from "components/input/Select";
import { Text } from "components/input/Text";
import { FormContext } from "components/input/Form";

type Props = {
  index: number;
  domainCaps: any;
  poolCaps: any;
  pools: Array<any>;
  onlyHandledDisks: boolean;
};

export function GuestDiskVolumeFields(props: Props) {
  const formContext = React.useContext(FormContext);

  const first_pool = props.pools.length > 0 ? props.pools[0].name : "";

  const selected_pool = props.pools.find((pool) => pool.name === formContext.model[`disk${props.index}_source_pool`]);

  const getPoolFormats = (pool_obj: any): { default: string; formats: Array<string> } => {
    if (!pool_obj) {
      return { default: "", formats: [] };
    }
    const pool_options =
      ((props.poolCaps.pool_types.find((item) => item.name === pool_obj.type) || {}).options || {}).volume || {};
    const format_values = pool_options.targetFormatType || [];
    const default_format = format_values.includes("qcow2") ? "qcow2" : pool_options.default_format;

    return {
      default: default_format,
      formats: format_values,
    };
  };

  const { default: default_format, formats: format_values } = getPoolFormats(selected_pool);

  const onPoolChange = (name, value: string) => {
    const new_pool = props.pools.find((pool) => pool.name === value);
    const { default: new_default, formats: new_formats } = getPoolFormats(new_pool);

    const old_format = formContext.model[`disk${props.index}_format`];
    if (!new_formats.includes(old_format)) {
      formContext.setModelValue?.(`disk${props.index}_format`, new_default);
    }
  };

  const onVolumeChange = (name, value: string) => {
    if (value) {
      formContext.setModelValue?.(`disk${props.index}_format`, undefined);
      formContext.setModelValue?.(`disk${props.index}_source_size`, undefined);
      formContext.setModelValue?.(`disk${props.index}_source_template`, undefined);
    }
  };

  const volume = formContext.model[`disk${props.index}_source_file`];
  const useCobblerProfile = !!formContext.model["cobbler_profile"];

  let volumes = volume ? [volume] : [];
  if (selected_pool && (selected_pool.volumes || []).length !== 0) {
    volumes = (selected_pool.volumes || []).map((vol) => vol.name);
  }

  return (
    <>
      <Select
        key={`disk${props.index}_source_pool`}
        name={`disk${props.index}_source_pool`}
        label={t("Source storage pool")}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
        required
        defaultValue={props.pools.find((pool) => pool.name === "default") ? "default" : first_pool}
        onChange={onPoolChange}
        options={props.pools.map((k) => k.name)}
      />
      <Select
        key={`disk${props.index}_source_file`}
        name={`disk${props.index}_source_file`}
        label={t("Source Volume")}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
        onChange={onVolumeChange}
        isClearable
        options={volumes}
      />
      {Object.keys(formContext.model).includes(`disk${props.index}_editable`) && (
        <>
          <Text
            key={`disk${props.index}_source_size`}
            name={`disk${props.index}_source_size`}
            label={t("Disk size (GiB)")}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks || volume}
          />
          <Text
            key={`disk${props.index}_source_template`}
            name={`disk${props.index}_source_template`}
            label={t("Source template image URL")}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks || volume || useCobblerProfile}
          />
        </>
      )}
      <Select
        key={`disk${props.index}_format`}
        name={`disk${props.index}_format`}
        label={t("Format")}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={
          !props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`) || volume
        }
        defaultValue={default_format}
        options={format_values}
      />
    </>
  );
}
