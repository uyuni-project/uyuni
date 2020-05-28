// @flow

import * as React from 'react';
import { Select } from 'components/input/Select';
import { Text } from 'components/input/Text';
import { FormContext } from 'components/input/Form';
import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';

type Props = {
  index: number,
  domainCaps: Object,
  pools: Array<Object>,
  onlyHandledDisks: boolean,
}

export function GuestDiskFields(props: Props) : React.Node {
  const formContext = React.useContext(FormContext);

  const sourceType = formContext.model[`disk${props.index}_type`];
  const device = formContext.model[`disk${props.index}_device`] || 'disk';
  const busTypes = props.domainCaps
    ? props.domainCaps.devices.disk.bus
      .filter(bus => (device === 'floppy' && bus === 'fdc') || (device !== 'floppy' && bus !== 'fdc'))
    : [];
  const preferredBusses = ['virtio', 'xen'].filter(type => busTypes.includes(type));
  const first_pool = props.pools.length > 0 ? props.pools[0].name : '';

  return (
    <>
      { Object.keys(formContext.model).includes(`disk${props.index}_editable`)
        && [
          <Select
            key={`disk${props.index}_device`}
            name={`disk${props.index}_device`}
            required
            label={t('Device type')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks}
            defaultValue="disk"
          >
            <option key="disk" value="disk">{t('Disk')}</option>
            <option key="cdrom" value="cdrom">{t('CDROM')}</option>
            <option key="floppy" value="floppy">{t('Floppy')}</option>
          </Select>,
          <Select
            key={`disk${props.index}_source_pool`}
            name={`disk${props.index}_source_pool`}
            label={t('Source storage pool')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks}
            required
            defaultValue={props.pools.find(pool => pool.name === 'default') ? 'default' : first_pool}
          >
            {
              props.pools.map(k => <option key={k.name} value={k.name}>{k.name}</option>)
            }
          </Select>,
          <Text
            key={`disk${props.index}_source_size`}
            name={`disk${props.index}_source_size`}
            label={t('Disk size (GiB)')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks}
          />,
          <Text
            key={`disk${props.index}_source_template`}
            name={`disk${props.index}_source_template`}
            label={t('Source template image URL')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!props.onlyHandledDisks}
          />,
        ]
      }
      { !Object.keys(formContext.model).includes(`disk${props.index}_editable`) &&
        <Text
          name={`disk${props.index}_source_file`}
          label={t('File')}
          disabled
          labelClass="col-md-3"
          divClass="col-md-6"
        />
      }
      {
        !["file", "volume"].includes(sourceType)
        && (
          <Messages items={MessagesUtils.warning(`Unhandled disk type: ${sourceType}`)} />
        )
      }
      <Select
        name={`disk${props.index}_bus`}
        required
        label={t('Bus')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks}
        defaultValue={preferredBusses.length > 0 ? preferredBusses[0] : busTypes[0]}
      >
        { busTypes.map(bus => <option key={bus} name={bus}>{bus}</option>) }
      </Select>
    </>
  );
}
