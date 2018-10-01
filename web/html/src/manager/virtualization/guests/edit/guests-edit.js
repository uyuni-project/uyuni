// @flow
const React = require("react");
const PropTypes = React.PropTypes;
const {Panel} = require("components/panel");
const MessagesUtils = require("components/messages").Utils;
import GuestProperties from '../guest-properties';
import VirtualizationGuestActionApi from '../virtualization-guest-action-api';


class GuestsEdit extends React.Component {

  constructor(props) {
    super(props);
    this.initialGuestMemory = props.guest.memory / 1024,
    this.initialGuestCpu = props.guest.vcpus,

    this.getInitialModel = () => {
      return {
        memory: this.initialGuestMemory,
        vcpu: this.initialGuestCpu,
      }
    };
  }

  render() {
    return (
      <VirtualizationGuestActionApi
        hostid={this.props.host.id}
        bounce={`/rhn/manager/systems/details/virtualization/guests/${this.props.host.id}`}>
        {
          ({
            onAction,
            messages,
          }) => {
            const onSubmit = (properties) => {
              properties.memory = properties.memory * 1024;
              return onAction('update', [this.props.guest.uuid], properties);
            };
            return (
              <Panel title={this.props.guest.name} icon="fa spacewalk-icon-virtual-guest">
                <GuestProperties
                  host={this.props.host}
                  submitText={t("Update")}
                  submit={onSubmit}
                  messages={messages}
                  getInitialModel={this.getInitialModel}/>
              </Panel>
            );
          }
        }
      </VirtualizationGuestActionApi>);
  }
}

GuestsEdit.propTypes = {
    host: PropTypes.object.isRequired,
    guest: PropTypes.object.isRequired,
};

export default GuestsEdit;
