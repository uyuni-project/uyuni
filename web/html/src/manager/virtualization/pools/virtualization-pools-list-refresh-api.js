// @flow
import type {MessageType} from 'components/messages';

const React = require('react');
const Network = require('utils/network');
const MessagesUtils = require('components/messages.js').Utils;

type Props = {
  serverId: string,
  refreshInterval?: number,
  children: Function,
};

type State = {
  pools: ?Array<{}>,
  errors: Array<MessageType>,
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
        const errorMessage = Network.errorMessageByStatus(response.status);
        this.setState({
          errors: errorMessage !== '' ? [MessagesUtils.error(errorMessage)] : [],
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
