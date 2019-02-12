// @flow
/* global module */

import type { ActionChain } from 'components/action-schedule';

const { hot } = require('react-hot-loader');
const React = require('react');
const _isEqual = require('lodash/isEqual');
const { TopPanel } = require('components/panels/TopPanel');
const MessagesUtils = require('components/messages').Utils;
const { Loading } = require('components/loading/loading');
const { GuestProperties } = require('../guest-properties');
const GuestPropertiesUtils = require('../properties/guest-properties-utils');
const GuestNicsPanel = require('../properties/guest-nics-panel');
const GuestDisksPanel = require('../properties/guest-disks-panel');
const { VirtualizationGuestActionApi } = require('../virtualization-guest-action-api');
const { VirtualizationGuestDefinitionApi } = require('../virtualization-guest-definition-api');

declare function t(msg: string): string;

type Props = {
  host: Object,
  guestUuid: string,
  localTime: string,
  timezone: string,
  actionChains: Array<ActionChain>,
};

class GuestsEdit extends React.Component<Props> {
  static getModelFromDefinition(definition: Object) {
    return Object.assign({
      name: definition.name,
      memory: definition.maxMemory / 1024,
      vcpu: definition.vcpu.max,
      graphicsType: definition.graphics ? definition.graphics.type : '',
      osType: definition.os.type,
      arch: definition.os.arch,
      vmType: definition.type,
    },
    GuestNicsPanel.getModelFromDefinition(definition),
    GuestDisksPanel.getModelFromDefinition(definition));
  }

  static getRequestParameterFromModel(model: Object, initialModel: Object) {
    // Diff the model with the initial one to avoid changing nics if user hasn't touched them.
    const initialNicProps = Object.entries(initialModel).filter(entry => entry[0].startsWith('network'));
    const newNicProps = Object.entries(model).filter(entry => entry[0].startsWith('network'));
    const nics = !_isEqual(initialNicProps, newNicProps)
      ? GuestPropertiesUtils.getOrderedDevicesFromModel(model, 'network')
        .map(nic => GuestNicsPanel.getRequestParams(model, nic))
      : [];

    const nicsParams = nics.length !== 0 ? { interfaces: nics } : undefined;

    // Diff the model with the initial one to avoid changing disks if user hasn't touched them.
    const initialDiskProps = Object.entries(initialModel).filter(entry => entry[0].startsWith('disk'));
    const newDiskProps = Object.entries(model).filter(entry => entry[0].startsWith('disk'));
    const disks = !_isEqual(initialDiskProps, newDiskProps)
      ? GuestPropertiesUtils.getOrderedDevicesFromModel(model, 'disk')
        .map(disk => GuestDisksPanel.getRequestParams(model, disk))
      : [];

    const disksParams = disks.length !== 0 ? { disks } : undefined;

    return Object.assign(
      Object.entries(model).reduce((res, entry) => Object.assign(res,
        !entry[0].startsWith('disk') && !entry[0].startsWith('network') ? { [entry[0]]: entry[1] } : undefined),
      {}),
      {
        memory: model.memory * 1024,
      },
      nicsParams,
      disksParams,
    );
  }

  render() {
    return (
      <VirtualizationGuestDefinitionApi
        hostid={this.props.host.id}
        guestUuid={this.props.guestUuid}
      >
        {
          ({
            definition,
            messages: definitionMessages,
          }) => (
            <VirtualizationGuestActionApi
              hostid={this.props.host.id}
              bounce={`/rhn/manager/systems/details/virtualization/guests/${this.props.host.id}`}
            >
              {
                ({
                  onAction,
                  messages: actionMessages,
                }) => {
                  if (definition == null) {
                    return <Loading text={t('Loading...')} withBorders={false} />;
                  }

                  const initialModel = GuestsEdit.getModelFromDefinition(definition);
                  const onSubmit = properties => onAction('update', [this.props.guestUuid],
                    GuestsEdit.getRequestParameterFromModel(properties, initialModel));
                  const messages = [].concat(definitionMessages, actionMessages)
                    .filter(item => item)
                    .map(item => MessagesUtils.error(item));
                  const guestName = definition !== null ? definition.name : '';
                  return (
                    <TopPanel title={guestName} icon="fa spacewalk-icon-virtual-guest">
                      <GuestProperties
                        host={this.props.host}
                        submitText={t('Update')}
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
            </VirtualizationGuestActionApi>)
        }
      </VirtualizationGuestDefinitionApi>);
  }
}

module.exports = {
  GuestsEdit: hot(module)(GuestsEdit),
};
