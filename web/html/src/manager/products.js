'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const MessageContainer = require('../components/messages').Messages;
const {Table, Column, SearchField, Highlight} = require('../components/table');
const Functions = require('../utils/functions');
const Utils = Functions.Utils;
const StatePersistedMixin = require('../components/util').StatePersistedMixin;

const _DATA_ROOT_ID = 'baseProducts';

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
    url: location.href.split(/\?|#/)[0],
    active: true
  }
];

function reloadData() {
  return Network.get('/rhn/manager/api/admin/products', 'application/json').promise;
}

const Products = React.createClass({
  getInitialState: function() {
    return {
      issMaster: issMaster_flag_from_backend,
      refreshNeeded: refreshNeeded_flag_from_backend,
      refreshRunning: refreshRunning_flag_from_backend,
      serverData: {_DATA_ROOT_ID : []},
      error: null,
      loading: true,
      selectedItems: []
    }
  },

  componentWillMount: function() {
    this.refreshServerData();
  },

  refreshServerData: function(dataUrlTag) {
    this.setState({loading: true});
    var currentObject = this;
    reloadData()
      .then(data => {
        currentObject.setState({
          serverData: data[_DATA_ROOT_ID],
          error: null,
          loading: false
        });
      })
      .catch(response => {
        currentObject.setState({
          error: response.status == 401 ? 'authentication' :
            response.status >= 500 ? 'general' :
            null,
          loading: false
        });
      });
  },

  searchData: function(datum, criteria) {
      if (criteria) {
        return (datum.label).toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  },

  buildRows: function(message) {
    return Object.keys(message).map((id) => message[id]);
  },

  handleSelectItems: function(items) {
    const removed = this.state.selectedItems.filter(i => !items.includes(i));
    const isAdd = removed.length === 0;
    const list = isAdd ? items : removed;

    this.setState({ selectedItems: items });
  },

  submit: function() {
    alert(this.state.selectedItems);
  },

  render: function() {
    const data = this.state.serverData;

    const title =
      <div className='spacewalk-toolbar-h1'>
        <h1>
          <i className='fa fa-cogs'></i>
          &nbsp;
          {t('Setup Wizard')}
          &nbsp;
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
        <div className='alert alert-warning' role='alert'>
          {t('A refresh of the product data is currently running in the background. Please try again later.')}
        </div>
      );
    }
    else if (this.state.issMaster) {
      pageContent = (
        <div className='row' id='suse-products'>
          <div className='col-sm-9'>
            {
              this.state.error == null  ?
                <div>
                  <div className='spacewalk-section-toolbar'>
                    <div className='action-button-wrapper'>
                      <div className='btn-group'>
                        <button className='btn btn-default'
                            id='refresh' data-toggle='tooltip'
                            title={t('Refreshes the product catalog from the Customer Center')}>
                          <i className='fa fa-refresh'></i>{t('Refresh')}
                        </button>
                        <button className='btn btn-success' id='synchronize' onClick={this.submit}>
                          <i className='fa fa-plus'></i>{t('Add products')}
                        </button>
                      </div>
                    </div>
                  </div>
                  <Table
                    data={this.buildRows(data)}
                    identifier={(row) => row['identifier']}
                    cssClassFunction={''}
                    initialSortColumnKey='label'
                    initialSortDirection={1}
                    initialItemsPerPage={userPrefPageSize}
                    loading={this.state.loading}
                    selectable={true}
                    onSelect={this.handleSelectItems}
                    selectedItems={this.state.selectedItems}
                    searchField={
                        <SearchField filter={this.searchData}
                            criteria={''}
                            placeholder={t('Filter by product name')} />
                    }>
                    <Column
                      columnKey='label'
                      comparator={Utils.sortByText}
                      header={t('Product Name')}
                      cell={ (row) => row['label']}
                    />
                    <Column
                      columnKey='arch'
                      comparator={Utils.sortByText}
                      header={t('Architecture')}
                      cell={ (row) => row['arch']}
                    />
                    <Column
                      columnKey='recommended'
                      comparator={Utils.sortByText}
                      header={t('Recommended')}
                      cell={ (row) => row['recommended']}
                    />
                  </Table>
                </div>
                : <ErrorMessage error={this.state.error} />
            }
          </div>
          <div className='col-sm-3 hidden-xs' id='wizard-faq'>
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
        <div className='alert alert-warning' role='alert'>
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
              <a className='btn btn-default' href={setupWizartSteps[currentStepIndex-1].url}>
                <i className='fa fa-arrow-left'></i>{t('Prev')}
              </a> : null
          }
          {
            currentStepIndex < (setupWizartSteps.length - 1) ?
              <a className='btn btn-success' href={setupWizartSteps[currentStepIndex+1].url}>
                <i className='fa fa-arrow-right'></i>{t('Next')}
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
        <div className='hidden' id='iss-master' data-iss-master={this.state.issMaster}></div>
        <div className='hidden' id='refresh-running' data-refresh-running={this.state.refreshRunning}></div>
        <div className='hidden' id='sccconfig.jsp.refresh'>{t('Refreshing data from SUSE Customer Center')}</div>
        {footer}
      </div>
    )
  }
});

const ErrorMessage = (props) => <MessageContainer items={
  props.error == 'authentication' ?
    MessagesUtils.warning(t('Session expired, please reload the page to see up-to-date data.')) :
  props.error == 'general' ?
    MessagesUtils.warning(t('Server error, please check log files.')) :
  []
} />
;

const SCCDialog = React.createClass({
  render: function() {
    return (
      <div>
        <div id='scc-migration-dialog' className='modal fade bs-example-modal-sm'
            tabIndex='-1' role='dialog' aria-labelledby='scc-refresh-dialog-title' aria-hidden='true'>
          <div className='modal-dialog modal-sm'>
              <div className='modal-content'>
                  <div className='modal-header'>
                    <h4 className='modal-title'>
                      <span id='scc-refresh-dialog-title'></span>
                    </h4>
                  </div>
                  <div className='modal-body'>
                      <p>{t('Please be patient, this might take several minutes.')}</p>
                      <ul id='scc-task-list'>
                      </ul>
                      <div id='scc-migration-current-task'></div>
                  </div>
                  <div className='modal-footer row'>
                      <div id='scc-migration-dialog-status' className='col-md-9 text-left'></div>
                      <div className='col-md-3 text-right'>
                        <button id='scc-migrate-dialog-close-btn' type='button' className='btn btn-default' data-dismiss='modal'>
                          {t('Close')}
                        </button>
                      </div>
                  </div>
              </div>
          </div>
        </div>

        <div className='hidden' id='sccconfig.jsp.channels'>{t('Channels')}</div>
        <div className='hidden' id='sccconfig.jsp.channelfamilies'>{t('Channel Families')}</div>
        <div className='hidden' id='sccconfig.jsp.products'>{t('Products')}</div>
        <div className='hidden' id='sccconfig.jsp.productchannels'>{t('Product Channles')}</div>
        <div className='hidden' id='sccconfig.jsp.subscriptions'>{t('Subscriptions')}</div>
        <div className='hidden' id='sccconfig.jsp.completed'>{t('Completed')}</div>
        <div className='hidden' id='sccconfig.jsp.failed'>{t('Operation not successful')}</div>
        <div className='hidden' id='sccconfig.jsp.failed.details.link'>{t('Details')}</div>
      </div>
    )
  }
});

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
