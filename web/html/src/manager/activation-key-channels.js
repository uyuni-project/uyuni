// @flow
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Network = require("../utils/network");
const Loading = require("../components/loading").Loading;

class ActivationKeyChannels extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      loading: true,
      activationKeyId: this.props.activationKeyId,
      activationKeyData: new Map(), //{base: null, children: []},
      currentEditData: new Map(), //{base: null, children: []},
      availableBaseChannels: [], //[base1, base2],
      availableChannels: [] //[{base : null, children: []}]
    }
  }

  componentWillMount() {
    this.fetchActivationKeyChannels().then(this.fetchBaseChannels).then(this.fetchChildChannels);
  }

  getDefaultBase = () => {
    return { id: -1, name: t("SUSE Manager Default"), custom: false, subscribable: true};
  }

  getCurrentBase = () => {
    return this.state.currentEditData.base ? this.state.currentEditData.base : this.getDefaultBase();
  }

  fetchActivationKeyChannels = () => {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/${this.props.activationKeyId}/channels`)
        .promise.then(data => {
          this.setState({
            activationKeyData: data.data,
            currentEditData: data.data,
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

  fetchBaseChannels = () => {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/base-channels`)
        .promise.then(data => {
          this.setState({
            availableBaseChannels: Array.from(data.data).map(g => g.base),
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

  fetchChildChannels = () => {
    let future;
    if (this.props.activationKeyId != -1) {
      this.setState({loading: true});

      future = Network.get(`/rhn/manager/api/activation-keys/base-channels/${this.getCurrentBase().id}/child-channels`)
        .promise.then(data => {
          this.setState({
            availableChannels: data.data,
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

  handleBaseChange = (event) => {
    const newBaseId = event.target.value;
    var currentEditData = this.state.currentEditData;
    currentEditData.base = this.state.availableBaseChannels.find(b => b.id == newBaseId);
    this.setState({
      currentEditData: currentEditData
    })
    this.fetchChildChannels();
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
      const currentBase = this.getCurrentBase();
      const childChannelList =
        Array.from(this.state.availableChannels.values()).map(g =>
          <div className='child-channels-block'>
            <h4>{g.base.name}</h4>
            {
              g.children.length > 0 ?
                g.children.map(c =>
                  <div className='checkbox'>
                    <input type='checkbox' value={c.id} id={'child_' + c.id} name='childChannels' />
                    <label htmlFor={'child_' + c.id}>{c.name}</label>
                  </div>
                )
              : <span>&nbsp;{t('no child channels')}</span>
            }
          </div>
        );
      return (
        <div>
          <div className='form-group'>
            <label className='col-lg-3 control-label'>{t('Base Channel:')}</label>
            <div className='col-lg-6'>
              <select name='selectedBaseChannel' className='form-control'
                  value={currentBase.id}
                  onChange={this.handleBaseChange}>
                <option value={this.getDefaultBase().id}>{this.getDefaultBase().name}</option>
                {
                  this.state.availableBaseChannels.map(b =>
                    <option value={b.id}>{b.name}</option>
                  )
                }
              </select>
              <span className="help-block">
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
  <ActivationKeyChannels activationKeyId={customValues.activationKeyId ? customValues.activationKeyId : ''} />,
  document.getElementById(customValues.DOMid)
);