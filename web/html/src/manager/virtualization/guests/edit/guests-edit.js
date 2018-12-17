// @flow
/* global module */

const { hot } = require('react-hot-loader');
const React = require('react');
const { TopPanel } = require('components/panels/TopPanel');
const MessagesUtils = require('components/messages').Utils;
const { Loading } = require('components/loading/loading');
const { GuestProperties } = require('../guest-properties');
const { VirtualizationGuestActionApi } = require('../virtualization-guest-action-api');
const { VirtualizationGuestDefinitionApi } = require('../virtualization-guest-definition-api');

declare function t(msg: string): string;

type Props = {
  host: Object,
  guestUuid: string,
};

class GuestsEdit extends React.Component<Props> {
  static getModelFromDefinition(definition: Object) : Object {
    return {
      memory: definition.maxMemory / 1024,
      vcpu: definition.vcpu.max,
    };
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
                  const onSubmit = (properties) => {
                    const newProperties = Object.assign(properties, { memory: properties.memory * 1024 });
                    return onAction('update', [this.props.guestUuid], newProperties);
                  };
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
