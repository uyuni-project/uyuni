'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const MessageContainer = require('../components/messages').Messages;
const {Table, Column, SearchField, Highlight} = require('../components/table');
const Functions = require('../utils/functions');
const Utils = Functions.Utils;
const StatePersistedMixin = require('../components/util').StatePersistedMixin;
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
    url: location.href.split(/\?|#/)[0],
    active: true
  }
];

function reloadData() {
  return Network.get('/rhn/manager/api/admin/products', 'application/json').promise;
}

const ProductSelector = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
      checked: false
    }
  },

  onChange: function() {
    this.setState({ checked: !this.state.checked });
    this.props.onChange(this.props.id, !this.state.checked);
  },

  render: function() {
    return (
      <input type='checkbox' id={this.props.id} value={this.props.id}
          checked={this.state.checked ? 'checked' : ''}
          onChange={this.onChange} />
    )
  }
});

const Products = React.createClass({
  getInitialState: function() {
    return {
      issMaster: issMaster_flag_from_backend,
      refreshNeeded: refreshNeeded_flag_from_backend,
      refreshRunning: refreshRunning_flag_from_backend,
      serverData: {'baseProducts' : []},
      error: null,
      loading: true
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
          serverData: data['baseProducts'],
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

  handleProductSelection: function(productId, isSelected) {
    console.log(productId + ' - ' + isSelected);
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
                        <button className='btn btn-success' id='synchronize'>
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
                    searchField={
                        <SearchField filter={this.searchData}
                            criteria={''}
                            placeholder={t('Filter by product name')} />
                    }>
                    <Column
                      columnKey='checkbox'
                      header={t('')}
                      cell={ (row) =>
                          <ProductSelector id={row['identifier']} value={row['identifier']}
                              onChange={(id, checked) => this.handleProductSelection(id, checked)}
                              saveState={(state) => {this.state[row['identifier']] = state;}}
                              loadState={() => this.state[row['identifier']]}
                          />
                      }
                    />
                    <Column
                      columnKey='id'
                      comparator={Utils.sortByNumber}
                      header={t('Id')}
                      cell={ (row) => row['identifier']}
                    />
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

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
