// @flow
const React = require('react');
const Network = require('utils/network');
const MessagesUtils = require('components/messages').Utils;

declare function t(msg: string, ...args: Array<any>): string;

type Props = {
  hostId: string,
  children: Function,
};

type State = {
  messages: Array<Object>,
  osTypes: Array<string>,
  domainsCaps: Array<Object>,
};

class VirtualizationDomainsCapsApi extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      osTypes: [],
      domainsCaps: [],
      messages: [],
    };
  }

  componentDidMount() {
    Network.get(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.hostId}/domains_capabilities`,
      'application/json').promise
      .then((response) => {
        this.setState({
          osTypes: response.osTypes,
          domainsCaps: response.domainsCaps,
        });
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
      osTypes: this.state.osTypes,
      domainsCaps: this.state.domainsCaps,
      messages: this.state.messages,
    });
  }
}

module.exports = {
  VirtualizationDomainsCapsApi,
};
