// @flow

import * as React from 'react';
import { Select } from 'components/input/Select';
import { Text } from 'components/input/Text';
import { FormContext } from 'components/input/Form';

type Props = {
  index: number,
  domainCaps: Object,
  poolCaps: Object,
  pools: Array<Object>,
  onlyHandledDisks: boolean,
}

export function GuestDiskVolumeFields(props: Props) : React.Node {
  const formContext = React.useContext(FormContext);

  const first_pool = props.pools.length > 0 ? props.pools[0].name : '';

  const selected_pool = props.pools.find(pool => pool.name === formContext.model[`disk${props.index}_source_pool`]);

  const getPoolFormats = (pool_obj: Object) : {default: string, formats: Array<string>} => {
    if (!pool_obj) {
      return {default: "", formats: []};
    }
    const pool_options = ((props.poolCaps.pool_types.find(item => item.name === pool_obj.type) || {})
                                    .options || {}).volume || {};
    const format_values = pool_options.targetFormatType || [];
    const default_format = format_values.includes("qcow2") ? "qcow2" : pool_options.default_format;

    return {
      default: default_format,
      formats: format_values,
    };
  }

  const { default: default_format, formats: format_values } = getPoolFormats(selected_pool);

  const onPoolChange = (name: string, value: string) => {
    const new_pool = props.pools.find(pool => pool.name === value);
    const { default: new_default, formats: new_formats } = getPoolFormats(new_pool);

    const old_format = formContext.model[`disk${props.index}_format`];
    if (!new_formats.includes(old_format)) {
      formContext.setModelValue(`disk${props.index}_format`, new_default);
    }
  }

  const onVolumeChange = (name: string, value: string) => {
    if (value) {
      formContext.setModelValue(`disk${props.index}_format`, undefined);
      formContext.setModelValue(`disk${props.index}_source_size`, undefined);
      formContext.setModelValue(`disk${props.index}_source_template`, undefined);
    }
  }

  const volume = formContext.model[`disk${props.index}_source_file`];

  return (
    <>
      <Select
        key={`disk${props.index}_source_pool`}
        name={`disk${props.index}_source_pool`}
        label={t('Source storage pool')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`) }
        required
        defaultValue={props.pools.find(pool => pool.name === 'default') ? 'default' : first_pool}
        onChange={onPoolChange}
      >
        {
          props.pools.map(k => <option key={k.name} value={k.name}>{k.name}</option>)
        }
      </Select>
      <Select
        key={`disk${props.index}_source_volume`}
        name={`disk${props.index}_source_volume`}
        label={t('Source Volume')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`) }
        onChange={onVolumeChange}
      >
        <option></option>
        {/* Adding the value is needed in case the pool is shut down and we don't have the volumes */}
        { (!selected_pool || selected_pool.volumes.length === 0) && volume &&
          <option key={volume} value={volume}>{volume}</option>
        }
        {
            selected_pool &&
              selected_pool.volumes.map(vol => <option key={vol.name} value={vol.name}>{vol.name}</option>)
        }
      </Select>
      { Object.keys(formContext.model).includes(`disk${props.index}_editable`) &&
        <>
          <Text
            key={`disk${props.index}_source_size`}
            name={`disk${props.index}_source_size`}
            label={t('Disk size (GiB)')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks || volume}
          />
          <Text
            key={`disk${props.index}_source_template`}
            name={`disk${props.index}_source_template`}
            label={t('Source template image URL')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks || volume}
          />
        </>
      }
      <Select
        key={`disk${props.index}_format`}
        name={`disk${props.index}_format`}
        label={t('Format')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`) || volume}
        defaultValue={default_format}
      >
        {
          format_values.map(opt => <option key={opt} value={opt}>{opt}</option>)
        }
      </Select>
    </>
  );
}
