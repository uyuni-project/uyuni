// @flow
'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const Loading = require('../components/loading').Loading;

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
          :
          this.state.availableChannels.map(g =>
            <div className='child-channels-block'>
              {
                this.state.availableChannels.length > 1 ?
                  <h4>{g.base.name}</h4>
                  : null
              }
              {
                g.children.length > 0 ?
                  g.children.map(c =>
                    <div className='checkbox'>
                      <input type='checkbox'
                          value={c.id}
                          id={'child_' + c.id}
                          name='childChannels'
                          checked={this.state.currentChildSelectedIds.includes(c.id)}
                          onChange={this.handleChildChange}
                      />
                      <label htmlFor={'child_' + c.id}>{c.name}</label>
                      &nbsp;
                      {
                        c.recommended
                          ? <span className='recommended-tag-base' title={'This channel is recommended'}>{t('recommended')}</span>
                          : null
                      }
                    </div>
                  )
                : <span>&nbsp;{t('no child channels')}</span>
              }
              <hr/>
            </div>
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
                  this.state.availableBaseChannels.map(b =>
                    <option value={b.id}>{b.name}</option>
                  )
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

// receive parameters from the backend
// if nothing from the backend, fallback on defaults
window.pageRenderers = window.pageRenderers || {};
const customValues = window.pageRenderers.customValues || {DOMid: 'activation-key-channels'};

ReactDOM.render(
  <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : -1} />,
  document.getElementById(customValues.DOMid)
);