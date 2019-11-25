// @flow

import type { ActionChain } from 'components/action-schedule';

const React = require('react');
const { hot } = require('react-hot-loader');
const { TopPanel } = require('components/panels/TopPanel');
const MessagesUtils = require('components/messages').Utils;
const { getOrderedItemsFromModel } = require('components/input/FormMultiInput');
const { GuestProperties } = require('../guest-properties');
const { VirtualizationGuestActionApi } = require('../virtualization-guest-action-api');
const GuestNicsPanel = require('../properties/guest-nics-panel');
const GuestDisksPanel = require('../properties/guest-disks-panel');
const Functions = require('utils/functions');

type Props = {
  host: Object,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

type State = {
  model: Object,
  isInvalid: boolean,
  messages: Array<string>,
};

class GuestsCreate extends React.Component<Props, State> {
  static getRequestParameterFromModel(model: Object) {
    const nics = getOrderedItemsFromModel(model, 'network')
      .map(index => GuestNicsPanel.getRequestParams(model, index));

    // Diff the model with the initial one to avoid changing disks if user hasn't touched them.
    const disks = getOrderedItemsFromModel(model, 'disk')
      .map(index => GuestDisksPanel.getRequestParams(model, index));

    const filteredProps = ['disk', 'network', 'vmType'];
    return Object.assign(
      Object.entries(model).reduce((res, entry) => Object.assign(res,
        filteredProps.every(prop => !entry[0].startsWith(prop)) ? { [entry[0]]: entry[1] } : undefined),
      {}),
      {
        type: model.vmType,
        memory: model.memory * 1024,
      },
      nics.length !== 0 ? { interfaces: nics } : undefined,
      disks.length !== 0 ? { disks } : undefined,
      {earliest: Functions.Formats.LocalDateTime(model.earliest)}
    );
  }

  initModel = () => ({
    memory: 1024,
    vcpu: 1,
    disk0_editable: true,
    disk0_type: 'file',
    disk0_device: 'disk',
    network0_type: 'network',
    graphicsType: 'vnc',
  })

  render() {
    return (
      <VirtualizationGuestActionApi
        hostid={this.props.host.id}
        bounce={`/rhn/manager/systems/details/virtualization/guests/${this.props.host.id}`}
      >
        {
          ({
            onAction,
            messages: actionMessages,
          }) => {
            const initialModel = this.initModel();
            const onSubmit = properties => onAction('update', [],
              GuestsCreate.getRequestParameterFromModel(properties));
            const messages = actionMessages
              .filter(item => item)
              .map(item => MessagesUtils.error(item));
            return (
              <TopPanel
                title={t('Create Virtual Machine')}
                icon="fa spacewalk-icon-virtual-guest"
              >
                <GuestProperties
                  host={this.props.host}
                  submitText={t('Create')}
                  submit={onSubmit}
                  messages={messages}
                  initialModel={initialModel}
                  localTime={this.props.localTime}
                  timezone={this.props.timezone}
                  actionChains={this.props.actionChains}
                />
              </TopPanel>
            );
          }
        }
      </VirtualizationGuestActionApi>
    );
  }
}

module.exports = {
  GuestsCreate: hot(module)(GuestsCreate),
};
