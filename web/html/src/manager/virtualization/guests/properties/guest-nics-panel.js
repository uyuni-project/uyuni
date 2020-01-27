// @flow

const React = require('react');
const { Select } = require('components/input/Select');
const { Text } = require('components/input/Text');
const { FormMultiInput } = require('components/input/FormMultiInput');
const { getOrderedItemsFromModel } = require('components/input/FormMultiInput');
const { Messages } = require('components/messages');
const { Utils: MessagesUtils } = require('components/messages');

function addNic(index: number, model: Object, changeModel: Function, networks: Array<Object>) {
  const first_nic = networks.length > 0 ? networks[0].name : '';

  changeModel(Object.assign(model, {
    [`network${index}_type`]: 'network',
    [`network${index}_source`]: networks.find(item => item.name === 'default') ? 'default' : first_nic,
    [`network${index}_mac`]: '',
  }));
}

function guestNicFields(model: Object, index: number, networks: Array<Object>,
  onlyHandledNics: boolean, changeModel: Function): React.Node {
  const first_nic = networks.length > 0 ? networks[0].name : '';

  return (
    <>
      {
        model[`network${index}_type`] !== 'network'
        && <Messages items={MessagesUtils.warning('Unhandled network interface type')} />
      }
      {
        model[`network${index}_type`] === 'network'
        && (
          <Select
            labelClass="col-md-3"
            divClass="col-md-6"
            label={t('Network')}
            name={`network${index}_source`}
            key={`network${index}_source`}
            disabled={!onlyHandledNics}
            required
            defaultValue={networks.find(net => net.name === 'default') ? 'default' : first_nic}
          >
            {
              networks.map(k => <option key={k.name} value={k.name}>{k.name}</option>)
            }
          </Select>
        )
      }
      <Text
        key={`network${index}_mac`}
        name={`network${index}_mac`}
        label={t('MAC address')}
        hint={t('Leave blank to generate a MAC address')}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={!onlyHandledNics}
      />
    </>
  );
}

function guestNicsPanel(model: Object, changeModel: Function, networks: Array<Object>): React.Node {
  const allNics = getOrderedItemsFromModel(model, 'network');
  const onlyHandledNics = allNics.every(index => model[`network${index}_type`] === 'network');

  const removeNic = (index: number): void => {
    changeModel(Object.entries(model).reduce((res, entry) => {
      const property = !entry[0].startsWith(`network${index}_`) ? { [entry[0]]: entry[1] } : undefined;
      return Object.assign(res, property);
    }, {}));
  };

  const getNicPanelTitle = (index: number): string => {
    return `Network Interface ${model[`network${index}_mac`]}`;
  };

  return (
    <>
      {
        !onlyHandledNics
        && <Messages items={MessagesUtils.warning('At least one unsupported network interface: disabling editing.')} />
      }
      <FormMultiInput
        id="networks"
        title={t('Networks')}
        prefix="network"
        onAdd={newIndex => addNic(newIndex, model, changeModel, networks)}
        onRemove={removeNic}
        disabled={!onlyHandledNics}
        panelTitle={getNicPanelTitle}
      >
        {
          (index: number) => guestNicFields(model, index, networks, onlyHandledNics, changeModel)
        }
      </FormMultiInput>
    </>
  );
}

function getModelFromDefinition(definition: Object): Object {
  if (definition.interfaces === null) {
    return {};
  }

  return definition.interfaces.reduce(
    (result, nic, index) => {
      const net = {
        [`network${index}_type`]: nic.type,
        [`network${index}_source`]: nic.source,
        [`network${index}_mac`]: nic.mac,
      };
      return Object.assign(result, net);
    },
    { },
  );
}

function getRequestParams(model: Object, index: number): Object {
  return {
    type: model[`network${index}_type`],
    source: model[`network${index}_source`],
    mac: model[`network${index}_mac`],
  };
}

module.exports = {
  guestNicsPanel,
  getRequestParams,
  getModelFromDefinition,
};
