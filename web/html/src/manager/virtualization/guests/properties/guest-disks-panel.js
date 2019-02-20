// @flow

const React = require('react');
const { Panel } = require('components/panels/Panel');
const { Text } = require('components/input/Text');
const { Select } = require('components/input/Select');
const { Messages } = require('components/messages');
const { Utils: MessagesUtils } = require('components/messages');
const { Button } = require('components/buttons');
const GuestPropertiesUtils = require('./guest-properties-utils');

function getFileSourceFields(model: Object, index: number, pools: Array<Object>, onlyHandledDisks: boolean) {
  if (Object.keys(model).includes(`disk${index}_editable`)) {
    // We can't assign a disk image, size or pool to a cdrom
    if (model[`disk${index}_device`] === 'cdrom') {
      return [];
    }
    return [
      <Select
        key={`disk${index}_source_pool`}
        name={`disk${index}_source_pool`}
        label={t('Source storage pool')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!onlyHandledDisks}
        required
        defaultValue={pools.find(pool => pool.name === 'default') ? 'default' : pools[0]}
      >
        {
          pools.map(k => <option key={k.name} value={k.name}>{k.name}</option>)
        }
      </Select>,
      <Text
        key={`disk${index}_source_size`}
        name={`disk${index}_source_size`}
        label={t('Disk size (GiB)')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!onlyHandledDisks}
      />,
      <Text
        key={`disk${index}_source_template`}
        name={`disk${index}_source_template`}
        label={t('Source template image URL')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!onlyHandledDisks}
      />,
    ];
  }
  return (
    <Text
      name={`disk${index}_source_file`}
      label={t('File')}
      disabled
      labelClass="col-md-3"
      divClass="col-md-6"
    />
  );
}

function handleDiskTypeChange(model: Object, index: number, value: string, changeModel: Function) {
  const sourceProperties = {
    file: { pool: undefined, template: undefined },
  };

  // Make sure we have the needed disk properties for the newly selected field type
  const newProperties = sourceProperties[value];
  const modelNewItems = Object.keys(newProperties).reduce((res, key) => {
    if (!Object.keys(model).includes(`disk${index}_source_${key}`)) {
      return Object.assign(res, { [`disk${index}_source_${key}`]: newProperties[key] });
    }
    return res;
  }, { });

  if (Object.keys(modelNewItems).length !== 0) {
    changeModel(Object.assign(model, modelNewItems));
  }
}

function addDisk(model: Object, changeModel: Function, domainCaps: Object, pools: Array<Object>) {
  const busTypes = domainCaps ? domainCaps.devices.disk.bus : [];

  const allDisks = GuestPropertiesUtils.getOrderedDevicesFromModel(model, 'disk');
  const index = Number.parseInt(allDisks[allDisks.length - 1].substring('disk'.length), 10) + 1;
  const preferredBusses = ['virtio', 'xen'].filter(type => busTypes.includes(type));

  changeModel(Object.assign(model, {
    [`disk${index}_editable`]: true,
    [`disk${index}_type`]: 'file',
    [`disk${index}_device`]: 'disk',
    [`disk${index}_bus`]: preferredBusses.length > 0 ? preferredBusses[0] : busTypes[0],
    [`disk${index}_source_pool`]: pools.find(item => item.name === 'default') ? 'default' : pools[0].name,
    [`disk${index}_source_template`]: undefined,
    [`disk${index}_source_size`]: 10,
  }));
}

function guestDiskFields(model: Object, index: number,
  domainCaps: Object, pools: Array<Object>, changeModel: Function, onlyHandledDisks: boolean) {
  const sourceHandlers = {
    file: getFileSourceFields,
  };
  const sourceType = model[`disk${index}_type`];
  const sourceHandler = sourceHandlers[sourceType];
  const device = model[`disk${index}_device`] || 'disk';
  const icons = {
    disk: 'fa-hdd-o',
    lun: 'fa-hdd-o',
    cdrom: 'spacewalk-icon-autoinstallations',
    floppy: 'fa-floppy-o',
  };
  const busTypes = domainCaps
    ? domainCaps.devices.disk.bus
      .filter(bus => (device === 'floppy' && bus === 'fdc') || (device !== 'floppy' && bus !== 'fdc'))
    : [];
  const preferredBusses = ['virtio', 'xen'].filter(type => busTypes.includes(type));

  const removeDisk = (): void => {
    changeModel(Object.entries(model).reduce((res, entry) => {
      const property = !entry[0].startsWith(`disk${index}_`) ? { [entry[0]]: entry[1] } : undefined;
      return Object.assign(res, property);
    }, {}));
  };

  const similarDisks = [...Array(index).keys()]
    .filter(i => model[`disk${i}_bus`] === model[`disk${index}_bus`]
                 && model[`disk${i}_device`] === model[`disk${index}_device`]);

  return (
    <Panel
      icon={icons[device]}
      key={`disk${index}`}
      title={`${model[`disk${index}_bus`]} ${device} ${similarDisks.length + 1}`}
      headingLevel="h3"
      buttons={(
        <Button
          icon="fa-minus"
          title={t('Remove')}
          id={`remove_disk${index}`}
          className="btn-default btn-sm"
          handler={removeDisk}
          disabled={!onlyHandledDisks}
        />
      )}
    >
      { Object.keys(model).includes(`disk${index}_editable`)
        && [
          <Select
            key={`disk${index}_device`}
            name={`disk${index}_device`}
            required
            label={t('Device type')}
            labelClass="col-md-3"
            divClass="col-md-6"
            disabled={!onlyHandledDisks}
            defaultValue="disk"
          >
            <option key="disk" value="disk">{t('Disk')}</option>
            <option key="cdrom" value="cdrom">{t('CDROM')}</option>
            <option key="floppy" value="floppy">{t('Floppy')}</option>
          </Select>,
          <Select
            key={`disk${index}_type`}
            name={`disk${index}_type`}
            required
            label={t('Source type')}
            labelClass="col-md-3"
            divClass="col-md-6"
            onChange={(name, value) => handleDiskTypeChange(model, index, value, changeModel)}
            disabled={!onlyHandledDisks}
            defaultValue="file"
          >
            <option key="file" value="file">{t('File')}</option>
          </Select>,
        ]
      }
      { sourceHandler && sourceHandler(model, index, pools, onlyHandledDisks) }
      {
        !sourceHandler
        && (
          <Messages items={MessagesUtils.warning(`Unhandled disk type: ${sourceType}`)} />
        )
      }
      <Select
        name={`disk${index}_bus`}
        required
        label={t('Bus')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!onlyHandledDisks}
        defaultValue={preferredBusses.length > 0 ? preferredBusses[0] : busTypes[0]}
      >
        { busTypes.map(bus => <option key={bus} name={bus}>{bus}</option>) }
      </Select>
    </Panel>
  );
}

function guestDisksPanel(model: Object, changeModel: Function, pools: Array<Object>, caps: Object): React.Node {
  const onlyHandledDisks = GuestPropertiesUtils.getOrderedDevicesFromModel(model, 'disk')
    .every(disk => model[`${disk}_type`] === 'file');
  return (
    <Panel
      key="disks"
      title={t('Disks')}
      headingLevel="h2"
      buttons={(
        <Button
          icon="fa-plus"
          title={t('Add')}
          id="add_disk"
          className="btn-default btn-sm"
          handler={() => addDisk(model, changeModel, caps, pools)}
          disabled={!onlyHandledDisks}
        />
      )}
    >
      {
        !onlyHandledDisks
        && <Messages items={MessagesUtils.warning('At least one unsupported disk: disabling editing.')} />
      }
      {
        GuestPropertiesUtils.getOrderedDevicesFromModel(model, 'disk')
          .map(property => guestDiskFields(model,
            Number.parseInt(property.substring('disk'.length), 10), caps,
            pools,
            changeModel,
            onlyHandledDisks))
      }
    </Panel>
  );
}

function getRequestParams(model: Object, disk: string): Object {
  const sourceMappers = {
    file: (): Object => {
      if (model[`${disk}_source_file`] !== undefined) {
        const sourceFile = model[`${disk}_source_file`] !== '' ? model[`${disk}_source_file`] : null;
        return { source_file: sourceFile };
      }
      return Object.assign(
        {},
        { pool: model[`${disk}_source_pool`] },
        model[`${disk}_source_size`] !== '' ? { size: model[`${disk}_source_size`] } : {},
        model[`${disk}_source_template`] !== '' ? { template: model[`${disk}_source_template`] } : {},
      );
    },
  };
  const sourceMapper = sourceMappers[model[`${disk}_type`]];
  const source = sourceMapper !== undefined ? sourceMapper() : {};

  return Object.assign({
    device: model[`${disk}_device`],
    type: model[`${disk}_type`],
    bus: model[`${disk}_bus`],
  },
  source);
}

function getModelFromDefinition(definition: Object) {
  return definition.disks != null
    ? definition.disks.reduce(
      (result, disk, index) => {
        if (disk != null) {
          const sourceToModel = {
            file: (() => ({ [`disk${index}_source_file`]: disk.source ? disk.source.file : '' })),
            dir: (() => ({ [`disk${index}_source_dir`]: disk.source ? disk.source.dir : '' })),
            block: (() => ({ [`disk${index}_source_dev`]: disk.source ? disk.source.dev : '' })),
            network: (
              () => Object.assign(
                {
                  [`disk${index}_source_protocol`]: disk.source ? disk.source.protocol : '',
                  [`disk${index}_source_name`]: disk.source ? disk.source.name : '',
                },
                disk.source && disk.source.host
                  ? disk.source.host.reduce((resultHosts, host, hostIndex) => Object.assign(resultHosts,
                    {
                      [`disk${index}_source_host${hostIndex}_name`]: host.name,
                      [`disk${index}_source_host${hostIndex}_port`]: host.port,
                    }), { })
                  : { },
              )
            ),
            volume: (() => ({
              [`disk${index}_source_pool`]: disk.source ? disk.source.pool : '',
              [`disk${index}_source_volume`]: disk.source ? disk.source.volume : '',
            })),
          };

          return Object.assign(result, {
            [`disk${index}_type`]: disk.type,
            [`disk${index}_device`]: disk.device,
            [`disk${index}_target`]: disk.target,
            [`disk${index}_bus`]: disk.bus,
          }, sourceToModel[disk.type]());
        }
        return result;
      },
      { },
    )
    : { };
}

module.exports = {
  guestDisksPanel,
  getRequestParams,
  getModelFromDefinition,
};
