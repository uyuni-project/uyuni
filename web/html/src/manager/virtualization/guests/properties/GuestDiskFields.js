// @flow

import * as React from 'react';
import { Select } from 'components/input/Select';
import { FormContext } from 'components/input/Form';
import { Messages } from 'components/messages';
import { Utils as MessagesUtils } from 'components/messages';
import { GuestDiskFileFields } from './GuestDiskFileFields';
import { GuestDiskVolumeFields } from './GuestDiskVolumeFields';

type Props = {
  index: number,
  domainCaps: Object,
  poolCaps: Object,
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
      .filter(bus => (device === 'cdrom' && !['virtio', 'xen'].includes(bus)) || device !== 'cdrom')
    : [];
  const preferredBusses = ['virtio', 'xen'].filter(type => busTypes.includes(type));
  const defaultBus = preferredBusses.length > 0 ? preferredBusses[0] : busTypes[0];

  // Ensure the value is in the options
  if (!busTypes.includes(formContext.model[`disk${props.index}_bus`])) {
    formContext.setModelValue(`disk${props.index}_bus`, defaultBus);
  }

  const onDiskDeviceChange = (name: string, value: string) => {
    const specialValues = {
      cdrom: {
        type: 'file',
      }
    };
    const newType = (specialValues[value] || {})['type'] || 'volume';
    if (newType !== sourceType) {
      formContext.setModelValue(`disk${props.index}_type`, newType);
    }
  }

  return (
    <>
      <Select
        key={`disk${props.index}_device`}
        name={`disk${props.index}_device`}
        required
        label={t('Device type')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!props.onlyHandledDisks || !Object.keys(formContext.model).includes(`disk${props.index}_editable`)}
        defaultValue="disk"
        onChange={onDiskDeviceChange}
      >
        <option key="disk" value="disk">{t('Disk')}</option>
        <option key="cdrom" value="cdrom">{t('CDROM')}</option>
        <option key="floppy" value="floppy">{t('Floppy')}</option>
      </Select>
      { sourceType === "file" && <GuestDiskFileFields {...props}/> }
      { sourceType === "volume" && <GuestDiskVolumeFields {...props}/> }
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
        defaultValue={defaultBus}
      >
        { busTypes.map(bus => <option key={bus} name={bus}>{bus}</option>) }
      </Select>
    </>
  );
}
