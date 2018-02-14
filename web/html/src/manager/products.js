'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const SCCDialog = require('./scc-refresh-dialog-jspf').SCCDialog;

const setupWizartSteps = [
  {
    label: 'HTTP Proxy',
    url: '/rhn/admin/setup/ProxySettings.do',
    active: false
  },
  {
    label: 'Organization Credentials',
    url: '/rhn/admin/setup/MirrorCredentials.do',
    active: false
  },
  {
    label: 'SUSE Products',
    url: '/rhn/admin/setup/ProxySettings.do',
    active: true
  }
];

const Products = React.createClass({
  getInitialState: function() {
    return {
      issMaster: issMaster_flag_from_backend,
      refreshNeeded: refreshNeeded_flag_from_backend,
      refreshRunning: refreshRunning_flag_from_backend
    }
  },

  render: function() {
    const title =
      <div className='spacewalk-toolbar-h1'>
        <h1>
          <i className='fa fa-header-preferences'></i>
          {t('Setup Wizard')}
          <a href='/rhn/help/reference/en-US/ref.webui.admin.jsp#ref.webui.admin.wizard'
              target='_blank'><i className='fa fa-question-circle spacewalk-help-link'></i>
          </a>
        </h1>
      </div>
    ;

    const tabs = 
      <div className='spacewalk-content-nav'>
        <ul className='nav nav-tabs'>
          { setupWizartSteps.map(step => <li className={step.active ? 'active' : ''}><a href={step.url}>{t(step.label)}</a></li>)}
        </ul>
      </div>;

    let pageContent;
    if (this.state.refreshRunning) {
      pageContent = (
        <div className="alert alert-warning" role="alert">
          {t('A refresh of the product data is currently running in the background. Please try again later.')}
        </div>
      );
    }
    else if (this.state.issMaster) {
      pageContent = (
        <div className="row" id="suse-products">
          <div className="col-sm-9">
            <table className="table table-rounded">
              <thead>
                <tr>
                  <th><input type="checkbox" className="select-all" autoComplete="off" /></th>
                  <th>{t('Available Products Below')}</th>
                  <th>{t('Architecture')}</th>
                  <th>{t('Channels')}</th>
                  <th>{t('Status')}</th>
                  <th></th>
                </tr>
              </thead>
              <tbody className="table-content">
                <tr id="loading-placeholder">
                  <td colSpan="6">
                    <div className="spinner-container">
                        <i className="fa fa-spinner fa-spin" />
                        <span>{t('Loading...')}</span>
                    </div>
                  </td>
                </tr>
              </tbody>
              <tfoot>
                <tr>
                  <td><input type="checkbox" className="select-all" autoComplete="off" /></td>
                  <td colSpan="6">
                    <button className="btn btn-success" id="synchronize">
                      <i className="fa fa-plus"></i>{t('Add products')}
                    </button>
                    &nbsp;
                    <button className="btn btn-default"
                        id="refresh" data-toggle="tooltip"
                        title={t('Refreshes the product catalog from the Customer Center')}>
                      <i className="fa fa-refresh"></i>{t('Refresh')}
                    </button>
                  </td>
                </tr>
              </tfoot>
            </table>
          </div>
          <div className="col-sm-3 hidden-xs" id="wizard-faq">
              <h4>{t("Why aren't all SUSE products displayed in the list?")}</h4>
              <p>{t('The products displayed on this list are directly linked to your \
                  Organization credentials (Mirror credentials) as well as your SUSE subscriptions.')}</p>
              <p>{t('If you believe there are products missing, make sure you have added the correct \
                  Organization credentials in the previous wizard step.')}</p>
          </div>
        </div>
      );
    }
    else {
      pageContent = (
        <div className="alert alert-warning" role="alert">
          {t('This server is configured as an Inter-Server Synchronisation (ISS) slave. SUSE Products can only be managed on the ISS master.')}
        </div>
      );
    }

    const prevStyle = { 'marginLeft': '10px' , 'verticalAlign': 'middle'};
    const currentStepIndex = setupWizartSteps.indexOf(setupWizartSteps.find(step => step.active));
    const footer =
      <div className='panel-footer'>
        <div className='btn-group'>  
          {
            currentStepIndex > 1 ?
              <a className="btn btn-default" href={setupWizartSteps[currentStepIndex-1].url}>
                <i className="fa fa-arrow-left"></i>{t('Prev')}
              </a> : null
          }
          {
            currentStepIndex < (setupWizartSteps.length - 1) ?
              <a className="btn btn-success" href={setupWizartSteps[currentStepIndex+1].url}>
                <i className="fa fa-arrow-right"></i>{t('Next')}
              </a> : null
          }
        </div>
        <span style={prevStyle}>
          { currentStepIndex+1 }&nbsp;{t('of')}&nbsp;{ setupWizartSteps.length }
        </span>
      </div>;

    return (
      <div className='responsive-wizard'>
        {title}
        {tabs}
        <div className='panel panel-default' id='products-content' data-refresh-needed={this.state.refreshNeeded}>
            <div className='panel-body'>
              {pageContent}
            </div>
        </div>
        <SCCDialog />
        <div className="hidden" id="iss-master" data-iss-master={this.state.issMaster}></div>
        <div className="hidden" id="refresh-running" data-refresh-running={this.state.refreshRunning}></div>
        <div className="hidden" id="sccconfig.jsp.refresh">{t('Refreshing data from SUSE Customer Center')}</div>
        {footer}
      </div>
    )
  }
});

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
