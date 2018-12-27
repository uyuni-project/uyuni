// @flow
const React = require('react');
const Network = require('utils/network');
const MessagesUtils = require('components/messages').Utils;

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  hostid: string,
  guestUuid: string,
  children: Function,
};

type State = {
  messages: Array<Object>,
  definition: ?Object,
};

class VirtualizationGuestDefinitionApi extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
      definition: null,
    };
  }

  componentDidMount() {
    Network.get(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostid}/guest/${this.props.guestUuid}`,
      'application/json').promise
      .then((response) => {
        this.setState({ definition: response });
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
      definition: this.state.definition,
      messages: this.state.messages,
    });
  }
}

module.exports = {
  VirtualizationGuestDefinitionApi,
};
