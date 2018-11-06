// @flow

const React = require('react');
const Network = require('utils/network');

type Props = {
  serverId: string,
  refreshInterval: number,
  children: Function,
};

type State = {
  guests: Array<{}>,
  error?: Object,
};

class VirtualizationGuestsListRefreshApi extends React.Component<Props, State> {
  intervalId = undefined;

  constructor(props: Props) {
    super(props);

    this.state = {
      guests: [],
      error: undefined,
    };
  }

  // eslint-disable-next-line
  UNSAFE_componentWillMount() {
    this.refreshServerData();
    this.intervalId = setInterval(this.refreshServerData, this.props.refreshInterval);
  }

  componentWillUnmount() {
    clearInterval(this.intervalId);
  }

  refreshServerData = () => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.serverId}/data`, 'application/json').promise
      .then((data) => {
        this.setState({
          guests: data,
          error: undefined,
        });
      })
      .catch((response) => {
        this.setState({
          error: Network.errorMessageByStatus(response.status),
        });
      });
  }

  render() {
    return this.props.children({
      guests: this.state.guests,
      error: this.state.error,
    });
  }
}

module.exports = {
  VirtualizationGuestsListRefreshApi,
};
