// @flow
'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../../utils/network');
const Loading = require('../../components/loading/loading').Loading;
const ChildChannels = require('./child-channels').ChildChannels;

type ActivationKeyChannelsProps = {
  activationKeyId: number
}

type ActivationKeyChannelsState = {
  messages: Array,
  loading: Boolean,
  loadingChildren: Boolean,
  availableBaseChannels: Array, //[base1, base2],
  availableChannels: Array, //[{base : null, children: []}]
  fetchedData: Map,
  requiredChannels: Map<number, Array<number>>,
  requiredByChannels: Map<number, Array<number>>,
  currentSelectedBaseId: number,
  currentChildSelectedIds: Array<number>,
}

class ActivationKeyChannels extends React.Component<ActivationKeyChannelsState, ActivationKeyChannelsProps> {
  constructor(props) {
    super(props);
    
    ['getDefaultBase', 'fetchActivationKeyChannels', 'fetchBaseChannels', 'fetchChildChannels',
    'handleResponseError', 'handleBaseChange', 'handleChildChange', 'selectChildChannels']
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
      messages: [],
      loading: true,
      loadingChildren: true,
      availableBaseChannels: [], //[base1, base2],
      availableChannels: [], //[{base : null, children: []}]
      fetchedData: new Map(),
      requiredChannels: new Map(),
      requiredByChannels: new Map(),
      currentSelectedBaseId: -1,
      currentChildSelectedIds: [],
    }
  }

  componentWillMount() {
    this.fetchActivationKeyChannels()
        .then(this.fetchBaseChannels)
        .then(() => this.fetchChildChannels(this.state.currentSelectedBaseId));
  }

  getDefaultBase() {
    return { id: -1, name: t('SUSE Manager Default'), custom: false, subscribable: true};
  }

  fetchActivationKeyChannels() {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/${this.props.activationKeyId}/channels`)
        .promise.then(data => {
          this.setState({
            currentSelectedBaseId: data.data.base ? data.data.base.id : this.getDefaultBase().id,
            currentChildSelectedIds: data.data.children ? data.data.children.map(c => c.id) : [],
            loading: false
          });
        })
        .catch(this.handleResponseError);
    }
    else {
      future = new Promise(function(resolve, reject) { resolve() });
    }
    return future;
  }

  fetchBaseChannels() {
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

  fetchChildChannels(baseId: number) {
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

  handleResponseError(jqXHR, arg = '') {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState((prevState) => ({
        messages: prevState.messages.concat(msg)
      })
    );
  }

  handleBaseChange(event: SyntheticInputEvent<*>) {
    const newBaseId = event.target.value;
    this.setState({currentSelectedBaseId: newBaseId});
    this.fetchChildChannels(newBaseId);
  }

  handleChildChange(event: SyntheticInputEvent<*>) {
    this.selectChildChannels([parseInt(event.target.value)], event.target.checked);
  }

  selectChildChannels(channelIds: Array<number>, selectedFlag: Boolean) {
    var selectedIds = [...this.state.currentChildSelectedIds];
    if (selectedFlag) {
      selectedIds = [...channelIds.filter(c => !selectedIds.includes(c)), ...selectedIds];
    }
    else {
      selectedIds = [...selectedIds.filter(c => !channelIds.includes(c))];
    }
    this.setState({currentChildSelectedIds: selectedIds});
  }

  render() {
    if (this.state.loading) {
      return (
        <div className='form-group'>
          <Loading text='Loading..' />
        </div>
      )
    }
    else {
      const childChannelList =
        this.state.loadingChildren ?
          <Loading text='Loading child channels..' />
          : this.state.availableChannels.map(g =>
              <ChildChannels
                  key={g.base.id}
                  channels={g.children.sort((c1, c2) => c1.name > c2.name)}
                  base={g.base}
                  showBase={this.state.availableChannels.length > 1}
                  selectedChannelsIds={this.state.currentChildSelectedIds}
                  selectChannels={this.selectChildChannels}
                  saveState={(state) => {this.state["ChildChannelsForBase" + g.base.id] = state;}}
                  loadState={() => this.state["ChildChannelsForBase" + g.base.id]}
                  collapsed={Array.from(this.state.availableChannels.keys()).length > 1}
              />
            );
      return (
        <div>
          <div className='form-group'>
            <label className='col-lg-3 control-label'>{t('Base Channel:')}</label>
            <div className='col-lg-6'>
              <select name='selectedBaseChannel' className='form-control'
                  value={this.state.currentSelectedBaseId}
                  onChange={this.handleBaseChange}>
                <option value={this.getDefaultBase().id}>{this.getDefaultBase().name}</option>
                {
                  this.state.availableBaseChannels
                      .sort((b1, b2) => b1.name > b2.name)
                      .map(b => <option key={b.id} value={b.id}>{b.name}</option>)
                }
              </select>
              <span className='help-block'>
                {t('Choose "SUSE Manager Default" to allow systems to register to the default SUSE Manager ' +
                    'provided channel that corresponds to the installed SUSE Linux version. Instead of the default, ' +
                    'you may choose a particular SUSE provided channel or a custom base channel, but if a system using ' +
                    'this key is not compatible with the selected channel, it will fall back to its SUSE Manager Default channel.')}
              </span>
            </div>
          </div>
          <div className='form-group'>
            <label className='col-lg-3 control-label'>{t('Child Channel:')}</label>
            <div className='col-lg-6'>
              {childChannelList}
              <span className='help-block'>
                {t('Any system registered using this activation key will be subscribed to the selected child channels.')}
              </span>
            </div>
          </div>
        </div>
      )
    }
  }
}

export default ActivationKeyChannels;
