// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Network = require("../utils/network");

class ActivationKeyChannels extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      loading: true,
      activationKeyId: this.props.activationKeyId,
      activationKeyData: new Map()
    }
  }

  componentWillMount() {
    this.fetchActivationKeyChannels();
  }

  fetchActivationKeyChannels = () => {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/${this.props.activationKeyId}/channels`)
        .promise.then(data => {
          this.setState({
            activationKeyData: data.data,
            loading: false
          });
        })
        .catch(this.handleResponseError);
    }
    else {
      future = () => {};
    }
    return future;
  }

  handleResponseError = (jqXHR, arg = "") => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState((prevState) => ({
        messages: prevState.messages.concat(msg)
      })
    );
  }

  render() {
    if (this.state.loading) {
      return <div>loading..</div>
    }
    else {
      return <div>
        {
          this.state.activationKeyData && this.state.activationKeyData.base ?
            <span>selected activation key base channel: {this.state.activationKeyData.base.name}</span>
            : <span>placeholder</span>
        }
      </div>
    }
  }
}

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

ReactDOM.render(
  <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : ''} />,
  document.getElementById(customValues.DOMid)
);