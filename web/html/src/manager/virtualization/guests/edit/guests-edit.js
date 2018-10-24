// @flow

const React = require('react');
const { TopPanel } = require('components/panels/TopPanel');
const { GuestProperties } = require('../guest-properties');
const { VirtualizationGuestActionApi } = require('../virtualization-guest-action-api');

declare function t(msg: string): string;

type Props = {
  host: Object,
  guest: Object,
};

class GuestsEdit extends React.Component<Props> {
  initialGuestMemory = 1024;

  initialGuestCpu = 1;

  constructor(props: Props) {
    super(props);
    this.initialGuestMemory = props.guest.memory / 1024;
    this.initialGuestCpu = props.guest.vcpus;
  }

  getInitialModel = () => ({
    memory: this.initialGuestMemory,
    vcpu: this.initialGuestCpu,
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
            messages,
          }) => {
            const onSubmit = (properties) => {
              const newProperties = Object.assign(properties, { memory: properties.memory * 1024 });
              return onAction('update', [this.props.guest.uuid], newProperties);
            };
            return (
              <TopPanel title={this.props.guest.name} icon="fa spacewalk-icon-virtual-guest">
                <GuestProperties
                  host={this.props.host}
                  submitText={t('Update')}
                  submit={onSubmit}
                  messages={messages}
                  getInitialModel={this.getInitialModel}
                />
              </TopPanel>
            );
          }
        }
      </VirtualizationGuestActionApi>);
  }
}

module.exports = {
  GuestsEdit,
};
