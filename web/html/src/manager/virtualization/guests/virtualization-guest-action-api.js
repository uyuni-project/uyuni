// @flow

const React = require('react');
const Network = require('utils/network');
const MessagesUtils = require('components/messages').Utils;

type Props = {
  hostid: string,
  children: Function,
  bounce?: string,
  callback?: Function,
};

type State = {
  messages: Array<Object>,
};

class VirtualizationGuestActionApi extends React.Component<Props, State> {
  static defaultProps = {
    bounce: undefined,
    callback: undefined,
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
    };
  }

  onAction = (action: string, uuids: Array<string>, parameters: Object) => {
    const messageData = Object.assign({ }, parameters, { uuids });
    Network.post(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostid}/${action}`,
      JSON.stringify(messageData), 'application/json').promise
      .then((response) => {
        if (Object.values(response).includes('Failed')) {
          this.setState({
            messages: [MessagesUtils.error(t(`Failed to trigger ${action}`))],
          });
        } else {
          if (this.props.callback) {
            this.props.callback(response);
          }
          if (this.props.bounce) {
            window.location.replace(this.props.bounce);
          }
        }
      }, (xhr) => {
        const errMessages = xhr.status === 0
          ? [MessagesUtils.error(
            t('Request interrupted or invalid response received from the server. Please try again.'),
          )]
          : [MessagesUtils.error(Network.errorMessageByStatus(xhr.status))];
        this.setState({
          messages: errMessages,
        });
      });
  }

  render() {
    return this.props.children({
      onAction: this.onAction,
      messages: this.state.messages,
    });
  }
}

module.exports = {
  VirtualizationGuestActionApi,
};
