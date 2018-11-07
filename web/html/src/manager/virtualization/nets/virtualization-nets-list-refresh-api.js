// @flow

const React = require('react');
const Network = require('utils/network');

type Props = {
  serverId: string,
  refreshInterval?: number,
  children: Function,
};

type State = {
  nets: ?Array<{}>,
  errors: Array<string>,
};

class VirtualizationNetsListRefreshApi extends React.Component<Props, State> {
  static defaultProps = {
    refreshInterval: undefined,
  };

  intervalId = undefined;

  constructor(props: Props) {
    super(props);

    this.state = {
      nets: undefined,
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
    Network.get(`/rhn/manager/api/systems/details/virtualization/nets/${this.props.serverId}/data`, 'application/json').promise
      .then((data) => {
        this.setState({
          nets: data,
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
      networks: this.state.nets,
      errors: this.state.errors,
    });
  }
}

module.exports = {
  VirtualizationNetsListRefreshApi,
};
