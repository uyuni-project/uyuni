// @flow
import React from 'react';
import ReactDOM from 'react-dom';
import Network from '../../utils/network';
import {Loading} from '../../components/loading/loading';
import {ChildChannels} from './child-channels';

type ActivationKeyChannelsProps = {
  activationKeyId: number,
  currentSelectedBaseId: number,
  onNewBaseChannel: Function,
  children: Function,
}

type ActivationKeyChannelsState = {
  messages: Array,
  loading: boolean,
  loadingChildren: boolean,
  availableBaseChannels: Array, //[base1, base2],
  availableChannels: Array, //[{base : null, children: []}]
  fetchedData: Map,
}

class ActivationKeyChannelsApi extends React.Component<ActivationKeyChannelsProps, ActivationKeyChannelsState> {
  constructor(props: ActivationKeyChannelsProps) {
    super(props);

    this.state = {
      messages: [],
      loading: true,
      loadingChildren: true,
      availableBaseChannels: [], //[base1, base2],
      availableChannels: [], //[{base : null, children: []}]
      fetchedData: new Map(),
    }
  }

  componentWillMount() {
    this.fetchActivationKeyChannels()
      .then(this.fetchBaseChannels)
      .then(() => this.fetchChildChannels(this.props.currentSelectedBaseId));
  }

  fetchBaseChannels = () => {
    let future;
    this.setState({loading: true});

    future = Network.get(`/rhn/manager/api/activation-keys/base-channels`)
      .promise.then(data => {
        this.setState({
          availableBaseChannels: Array.from(data.data).map(g => g.base),
          loading: false
        });
      })
      .catch(this.handleResponseError);
    return future;
  }

  fetchActivationKeyChannels = () => {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/${this.props.activationKeyId}/channels`)
        .promise.then(data => {
          const currentSelectedBaseId = data.data.base ? data.data.base.id : this.getDefaultBase().id;
          const currentChildSelectedIds = data.data.children ? data.data.children.map(c => c.id) : [];
          this.props.onNewBaseChannel({currentSelectedBaseId, currentChildSelectedIds});
          this.setState({
            loading: false
          });
        })
        .catch(this.handleResponseError);
    } else {
      future = new Promise(function(resolve, reject) { resolve() });
    }
    return future;
  }

  fetchChildChannels = (baseId: number) => {
    let future;

    const currentObject = this;
    if (currentObject.state.fetchedData && currentObject.state.fetchedData.has(baseId)) {
      future = new Promise(function(resolve, reject) {
        resolve(
          currentObject.setState({
            availableChannels: currentObject.state.fetchedData.get(baseId),
          })
        )
      });
    }
    else {
      this.setState({loadingChildren: true});
      future = Network.get(`/rhn/manager/api/activation-keys/base-channels/${baseId}/child-channels`)
        .promise.then(data => {
          this.setState({
            availableChannels: data.data,
            fetchedData: this.state.fetchedData.set(baseId, data.data),
            loadingChildren: false
          });
        })
        .catch(this.handleResponseError);
    }
    return future;
  }

  handleResponseError = (jqXHR, arg = '') => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState((prevState) => ({
        messages: prevState.messages.concat(msg)
      })
    );
  }

  render() {
    return this.props.children({
      messages: this.state.messages,
      loading: this.state.loading,
      loadingChildren: this.state.loadingChildren,
      availableBaseChannels: this.state.availableBaseChannels,
      availableChannels: this.state.availableChannels,
      fetchChildChannels: this.fetchChildChannels
    })
  }
}

export default ActivationKeyChannelsApi;
