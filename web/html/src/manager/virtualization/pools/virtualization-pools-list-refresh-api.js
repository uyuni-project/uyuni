// @flow

const React = require('react');
const Network = require('utils/network');

type Props = {
  serverId: string,
  refreshInterval?: number,
  children: Function,
};

type State = {
  pools: ?Array<{}>,
  errors: Array<string>,
};

class VirtualizationPoolsListRefreshApi extends React.Component<Props, State> {
  static defaultProps = {
    refreshInterval: undefined,
  };

  intervalId = undefined;

  constructor(props: Props) {
    super(props);

    this.state = {
      pools: undefined,
      errors: [],
    };
  }

  componentDidMount() {
    if (this.props.refreshInterval !== undefined) {
      this.intervalId = setInterval(this.refreshServerData, this.props.refreshInterval);
    }
    this.refreshServerData();
  }

  componentWillUnmount() {
    if (this.props.refreshInterval !== undefined) {
      clearInterval(this.intervalId);
    }
  }

  refreshServerData = () => {
    Network.get(`/rhn/manager/api/systems/details/virtualization/pools/${this.props.serverId}/data`, 'application/json').promise
      .then((data) => {
        this.setState({
          pools: data,
          errors: [],
        });
      })
      .catch((response) => {
        this.setState({
          errors: [Network.errorMessageByStatus(response.status)],
        });
      });
  }

  render() {
    return this.props.children({
      pools: this.state.pools,
      errors: this.state.errors,
    });
  }
}

module.exports = {
  VirtualizationPoolsListRefreshApi,
};
