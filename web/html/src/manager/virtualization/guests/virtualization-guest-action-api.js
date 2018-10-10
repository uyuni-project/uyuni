/* eslint-disable */
// @flow
const React = require("react");
const Network = require("utils/network");
const MessagesUtils = require("components/messages").Utils;

class VirtualizationGuestActionApi extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      results: {},
    };

    this.onAction = (action, uuids, parameters) => {
      const messageData = Object.assign({}, parameters, {'uuids': uuids});
      Network.post(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostid}/${action}`,
                   JSON.stringify(messageData), "application/json").promise
        .then(response => {
          if (Object.values(response).includes("Failed")) {
            this.setState({
              messages: [MessagesUtils.error(t(`Failed to trigger ${action}`))]
            });
          } else {
            if (this.props.callback) {
              this.props.callback(response);
            }
            if (this.props.bounce) {
              window.location.replace(this.props.bounce)
            }
          }
        }, (xhr) => {
          const errMessages = xhr.status === 0 ?
            [MessagesUtils.error(
              t("Request interrupted or invalid response received from the server. Please try again."))]
            : [MessagesUtils.error(Network.errorMessageByStatus(xhr.status))];
          this.setState({
            messages: errMessages,
          })
        });
    };
  }

  render() {
    return this.props.children({
      onAction: this.onAction,
      messages: this.state.messages,
    })
  }
}

VirtualizationGuestActionApi.propTypes = {
  hostid: React.PropTypes.string.isRequired,
  children: React.PropTypes.func.isRequired,
  bounce: React.PropTypes.string,
  callback: React.PropTypes.func,
};
export default VirtualizationGuestActionApi;
