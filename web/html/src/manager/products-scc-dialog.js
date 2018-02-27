'use strict';

const React = require('react');
const Network = require('../utils/network');
const Messages = require('../components/messages').Messages;
const PopUp = require("../components/popup").PopUp;
const Button = require('../components/buttons').Button;

const _SCC_REFRESH_STEPS = [
  {
    label: 'Channels',
    url: '/rhn/manager/admin/setup/sync/channels',
    inProgress: false,
    success: null
  },
  {
    label: 'Channel Families',
    url: '/rhn/manager/admin/setup/sync/channelfamilies',
    inProgress: false,
    success: null
  },
  {
    label: 'Products',
    url: '/rhn/manager/admin/setup/sync/products',
    inProgress: false,
    success: null
  },
  {
    label: 'Product Channels',
    url: '/rhn/manager/admin/setup/sync/productchannels',
    inProgress: false,
    success: null
  },
  {
    label: 'Subscriptions',
    url: '/rhn/manager/admin/setup/sync/subscriptions',
    inProgress: false,
    success: null
  }
];

const SCCDialog = React.createClass({
  getInitialState: function() {
    return {
      steps: _SCC_REFRESH_STEPS,
      errors: [],
    }
  },

  componentWillReceiveProps: function(nextProps) {
    if(nextProps.start && // I'm told to run
        !this.isSyncRunning() && // I'm not running
        !this.hasRun() // I have never run yet
      ) {
      this.startSync(); // let's do the sync then
    }
  },

  // returns if the sync is running right now
  isSyncRunning: function() {
    return this.state.steps.some(s => s.inProgress);
  },

  // returns if the sync has run checking if
  // there is at least one step with a valid 'success' flag value
  // or the sync is running
  hasRun: function() {
    return this.state.steps.some(s => s.success != null) || this.isSyncRunning();
  },

  startSync: function() {
    this.props.updateSyncRunning(true);
    this.runSccRefreshStep(this.state.steps, 0); // start from the first element
  },

  restartSync: function() {
    // reset state
    _SCC_REFRESH_STEPS.forEach(s =>
      {
        s.inProgress = false;
        s.success = null;
      }
    );
    this.setState({ steps: _SCC_REFRESH_STEPS, errors: []});
    this.startSync();
  },

  finishSync: function() {
    this.props.updateSyncRunning(false);
  },

  runSccRefreshStep: function(stepList, i) {
    var currentObject = this;

    // if i-step exists
    if (stepList.length >= i+1) {
      // run the i-step
      var currentStep = stepList[i];
      currentStep.inProgress = true;
      currentObject.setState({
        steps: stepList
      });

      Network.post(currentStep.url, 'application/json').promise.then(data => {
        // set the result for the i-step
        currentStep.success = data;
        currentStep.inProgress = false;
        currentObject.setState({
          steps: stepList
        });

        // recoursive recall to run the next step
        currentObject.runSccRefreshStep(stepList, i+1);
      })
      .catch(this.handleResponseError);
    }
    else {
      currentObject.finishSync();
    }
  },

  handleResponseError: function(jqXHR, arg = "") {
    this.finishSync();
    const stepList = this.state.steps;
    var currentStep = stepList.find(s => s.inProgress);
    currentStep.inProgress = false;
    currentStep.success = false;
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState({ steps: stepList, errors: this.state.errors.concat(msg) });
  },

  render: function() {
    const failureLink = <a href='/rhn/admin/Catalina.do'>{t('Details')}</a>;
    const failureResult = (
      <span>
        <i className='fa fa-exclamation-triangle text-warning' />
        {t('Operation not successful: Empty reply from the server')}
        ({failureLink})
      </span>
    );
    const successResult = <span><i className='fa fa-check text-success' />{t('Completed')}</span>;

    const contentPopup = (
      <div>
        <Messages items={this.state.errors}/>
        <p>{t('Please be patient, this might take several minutes.')}</p>
        <ul id='scc-task-list' className='fa-ul'>
          {
            this.state.steps.map(s => 
              {
                return (
                  <li>
                    <i className={
                      s.success != null ?
                      (
                        s.success ?
                          'fa fa-li fa-check text-success'
                          : 'fa fa-li fa-exclamation-triangle text-warning'
                      )
                      : s.inProgress ? 'fa fa-li fa-spinner fa-spin' : 'fa fa-li fa-circle-o text-muted'}
                      /><span className={s.success == null ? 'text-muted' : ''}>{s.label}</span>
                  </li>
                )
              }
            )
          }
        </ul>
      </div>
    );

    const footerPopup = (
      !this.isSyncRunning() && this.hasRun() ?
      <div>
        <div className='col-md-7 text-left'>
          {
            this.state.steps.every(s => s.success) ?
              successResult : failureResult
          }
        </div>
        <div className='col-md-5 text-left'>
          <button className='btn btn-default' onClick={() => this.restartSync()}>{t('Sync again')}</button>
        </div>
      </div>
      : null
    );

    return (
      <PopUp
          id='scc-refresh-popup'
          title={t('Refreshing data from SUSE Customer Center')}
          content={contentPopup}
          footer={footerPopup}
          className='modal-sm'
          onClosePopUp={this.props.onClose} />
    )
  }
});


module.exports = {
  SCCDialog: SCCDialog
}
