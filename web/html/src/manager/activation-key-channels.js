// @flow
'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const Loading = require('../components/loading').Loading;
const ChannelUtils = require('../utils/channels');

class ActivationKeyChannels extends React.Component {
  constructor(props) {
    super(props);
    
    ['getDefaultBase', 'fetchActivationKeyChannels', 'fetchBaseChannels', 'fetchChildChannels',
    'handleResponseError', 'handleBaseChange', 'handleChildChange']
    .forEach(method => this[method] = this[method].bind(this));

    this.state = {
      messages: [],
      loading: true,
      loadingChildren: true,
      activationKeyId: this.props.activationKeyId,
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

  fetchChildChannels(baseId) {
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

  handleBaseChange(event) {
    const newBaseId = event.target.value;
    this.setState({currentSelectedBaseId: newBaseId});
    this.fetchChildChannels(newBaseId);
  }

  handleChildChange(event) {
    const childId = parseInt(event.target.value);
    const isSelected = event.target.checked;
    var selectedIds = [...this.state.currentChildSelectedIds];
    if (isSelected) {
      selectedIds = [childId, ...selectedIds];
    }
    else {
      selectedIds = [...selectedIds.filter(c => c != childId)];
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
                  handleChannelChange={this.handleChildChange}
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
            </div>
          </div>
        </div>
      )
    }
  }
}


class ChildChannels extends React.Component {
  constructor(props) {
    super(props);

    this.state = {
      requiredChannels: new Map(),
      requiredByChannels: new Map(),
      mandatoryChannelsRaw: new Map(),
      dependencyDataAvailable: false
    }
  }

  componentWillMount() {
    // fetch dependencies data for all child channels and base channel as well
    const needDepsInfoChannels = this.props.base && this.props.base.id != -1 ?
        [this.props.base.id, ...this.props.channels.map(c => c.id)]
      : this.props.channels.map(c => c.id);
    this.fetchMandatoryChannelsByChannelIds(needDepsInfoChannels);
  }

  fetchMandatoryChannelsByChannelIds(channelIds: Array<number>) {
    const mandatoryChannelsNotCached = channelIds.filter((channelId) => !this.state.mandatoryChannelsRaw[channelId]);
    if(mandatoryChannelsNotCached.length > 0) {
      Network.post('/rhn/manager/api/admin/mandatoryChannels', JSON.stringify(mandatoryChannelsNotCached), "application/json").promise
        .then((data : JsonResult<Map<number, Array<number>>>) => {
          const allTheNewMandatoryChannelsData = Object.assign({}, this.state.mandatoryChannelsRaw, data.data);
          let {requiredChannels, requiredByChannels} = ChannelUtils.processChannelDependencies(allTheNewMandatoryChannelsData);

          this.setState({
            mandatoryChannelsRaw: allTheNewMandatoryChannelsData,
            requiredChannels,
            requiredByChannels,
            dependencyDataAvailable: true,
          });
        })
        .catch(this.handleResponseError);
    } else {
      this.setState({
        dependencyDataAvailable: true,
      })
    }
  }

  handleResponseError(jqXHR, arg = '') {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState((prevState) => ({
        messages: prevState.messages.concat(msg)
      })
    );
  }

  dependenciesTooltip = (channelId) => {
    const resolveChannelNames = (channelIds) => {
      return Array.from(channelIds || new Set())
        .map(channelId => this.props.channels.find(c => c.id == channelId))
        .filter(channel => channel != null)
        .map(channel => channel.name);
    }
    return ChannelUtils.dependenciesTooltip(
      resolveChannelNames(this.state.requiredChannels.get(channelId)),
      resolveChannelNames(this.state.requiredByChannels.get(channelId)));
  }

  render() {
    let channels;
    if(!this.state.dependencyDataAvailable) {
      channels = <Loading text='Loading dependencies..' />;
    }
    else {
      if (this.props.channels.length == 0) {
        channels = <span>&nbsp;{t('no child channels')}</span>;
      }
      else {
        channels =
          this.props.channels.map(c => {
              const toolTip = this.dependenciesTooltip(c.id);
              const isMandatory =
                  this.props.base &&
                  this.state.requiredChannels.has(this.props.base.id) &&
                  this.state.requiredChannels.get(this.props.base.id).has(c.id);
              return (
                <div key={c.id} className='checkbox'>
                  <input type='checkbox'
                      value={c.id}
                      id={'child_' + c.id}
                      name='childChannels'
                      checked={this.props.selectedChannelsIds.includes(c.id)}
                      disabled={isMandatory && this.props.selectedChannelsIds.includes(c.id)}
                      onChange={this.props.handleChannelChange}
                  />
                  <label title={toolTip} htmlFor={"child_" + c.id}>{c.name}</label>
                  &nbsp;
                  {
                    toolTip ?
                      <a href="#"><i className="fa fa-info-circle spacewalk-help-link" title={toolTip}></i></a>
                      : null
                  }
                  &nbsp;
                  {
                    c.recommended ?
                      <span className='recommended-tag-base' title={'This channel is recommended'}>{t('recommended')}</span>
                      : null
                  }
                  {
                    isMandatory ?
                      <span className='mandatory-tag-base' title={'This channel is mandatory'}>{t('mandatory')}</span>
                      : null
                  }
                </div>
              )
          })
      }
    }

    return (
      <div className='child-channels-block'>
        {
          this.props.showBase ?
            <h4>{this.props.base.name}</h4>
            : null
        }
        {channels}
        <hr/>
      </div>
    );
  }
}

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

ReactDOM.render(
  <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : -1} />,
  document.getElementById(customValues.DOMid)
);