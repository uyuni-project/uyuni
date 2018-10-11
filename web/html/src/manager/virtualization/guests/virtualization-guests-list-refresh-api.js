/* eslint-disable */
// @flow
const React = require("react");
const PropTypes = React.PropTypes;
const Network = require("utils/network");

class VirtualizationGuestsListRefreshApi extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      guests: [],
      error: undefined,
    };

    this.refreshServerData = () => {
      Network.get(`/rhn/manager/api/systems/details/virtualization/guests/${this.props.server_id}/data`, "application/json").promise
        .then(data => {
          this.setState({
            guests: data,
            error: undefined,
          });
        })
        .catch(response => {
          this.setState({
            error: response.status == 401 ? t("Session expired, please reload the page to see up-to-date data.") :
              response.status >= 500 ? t("Server error, please check log files.") :
              undefined
          });
        }
      );
    }
  }

  componentWillMount() {
    this.refreshServerData();
    setInterval(this.refreshServerData, this.props.refreshInterval);
  }

  componentWillUnmount() {
    clearInterval(this.refreshInterval);
  }

  render() {
    return this.props.children({
      guests: this.state.guests,
      error: this.state.error,
    });
  }
}

VirtualizationGuestsListRefreshApi.propTypes = {
    server_id: PropTypes.string.isRequired,
    refreshInterval: PropTypes.number.isRequired,
};

export default VirtualizationGuestsListRefreshApi;
