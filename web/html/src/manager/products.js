'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const Messages = require('../components/messages').Messages;
const MessagesUtils = require("../components/messages").Utils;
const {Table, Column, SearchField, Highlight} = require('../components/table');
const Functions = require('../utils/functions');
const Utils = Functions.Utils;
const ModalButton = require("../components/dialogs").ModalButton;
const Button = require('../components/buttons').Button;
const SCCDialog = require('./products-scc-dialog').SCCDialog;

const _DATA_ROOT_ID = 'baseProducts';

const _SETUP_WIZARD_STEPS = [
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
      errors: [],
      loading: true,
      selectedItems: [],
      showPopUp: // trigger the refresh at the first page load if
        refreshNeeded_flag_from_backend &&
        issMaster_flag_from_backend &&
        !refreshRunning_flag_from_backend,
      syncRunning: false,
      addingProducts: false
    }
  },

  componentWillMount: function() {
    if (!this.state.refreshRunning) {
      this.refreshServerData();
    }
  },

  refreshServerData: function(dataUrlTag) {
    this.setState({loading: true});
    var currentObject = this;
    reloadData()
      .then(data => {
        currentObject.setState({
          serverData: data[_DATA_ROOT_ID],
          errors: [],
          loading: false
        });
      })
      .catch(this.handleResponseError);
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
    this.setState({ selectedItems: items });
  },

  showPopUp: function() {
    this.setState({showPopUp: true});
  },

  closePopUp: function() {
    this.setState({showPopUp: false});
  },

  updateSyncRunning: function(syncStatus) {
    // if it was running and now it's finished
    if (this.state.syncRunning && !syncStatus) {
      this.refreshServerData(); // reload data
    }

    if (syncStatus) {
      this.setState({ errors: MessagesUtils.info(t('The product catalog refresh is running..')) });
    }
    this.setState({ syncRunning: syncStatus });
  },

  submit: function() {
    const currentObject = this;
    currentObject.setState({ addingProducts: true });
    Network.post(
        '/rhn/manager/admin/setup/products',
        JSON.stringify(currentObject.state.selectedItems), 'application/json'
    ).promise.then(data => {
      if(data) {
        currentObject.setState(
          {
            errors: MessagesUtils.success(data),
            selectedItems : [],
            addingProducts: false}
        );
      }
      else {
        currentObject.setState(
          {
            errors: MessagesUtils.warning(data),
            selectedItems : [],
            addingProducts: false}
        );
      }
    })
    .catch(currentObject.handleResponseError);
  },

  handleResponseError: function(jqXHR, arg = "") {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState({ errors: this.state.errors.concat(msg) });
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
          { _SETUP_WIZARD_STEPS.map(step => <li className={step.active ? 'active' : ''}><a href={step.url}>{t(step.label)}</a></li>)}
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

      const submitButtonTitle =
        this.state.syncRunning ?
          t('The product catalog is still refreshing, please wait.')
          : this.state.selectedItems.length == 0 ?
              t('Select some product first.')
              : null;
      const addProductButton = (
        this.state.syncRunning || this.state.selectedItems.length == 0 || this.state.addingProducts ?
        <Button
            id="addProducts"
            icon={this.state.addingProducts ? 'fa-plus-circle fa-spin' : 'fa-plus'}
            className='btn-default text-muted'
            title={submitButtonTitle}
            text={t('Add products')}
        />
        :
        <Button
            id="addProducts"
            icon="fa-plus"
            className={'btn-success'}
            text={t('Add products')}
            handler={this.submit}
        />
      );
      pageContent = (
        <div className='row' id='suse-products'>
          <div className='col-sm-9'>
            <Messages items={this.state.errors}/>
            <div>
              <div className='spacewalk-section-toolbar'>
                <div className='action-button-wrapper'>
                  <div className='btn-group'>
                    <ModalButton
                        className='btn btn-default'
                        id='sccRefresh'
                        icon={'fa-refresh ' + (this.state.syncRunning ? 'fa-spin' : '')}
                        title={
                          this.state.syncRunning ?
                            t('The product catalog refresh is running..')
                            : t('Refreshes the product catalog from the Customer Center')
                        }
                        text={t('Refresh')}
                        target='scc-refresh-popup'
                        onClick={() => this.showPopUp()}
                    />
                    {addProductButton}
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
    const currentStepIndex = _SETUP_WIZARD_STEPS.indexOf(_SETUP_WIZARD_STEPS.find(step => step.active));
    const footer =
      <div className='panel-footer'>
        <div className='btn-group'>  
          {
            currentStepIndex > 1 ?
              <a className='btn btn-default' href={_SETUP_WIZARD_STEPS[currentStepIndex-1].url}>
                <i className='fa fa-arrow-left'></i>{t('Prev')}
              </a> : null
          }
          {
            currentStepIndex < (_SETUP_WIZARD_STEPS.length - 1) ?
              <a className='btn btn-success' href={_SETUP_WIZARD_STEPS[currentStepIndex+1].url}>
                <i className='fa fa-arrow-right'></i>{t('Next')}
              </a> : null
          }
        </div>
        <span style={prevStyle}>
          { currentStepIndex+1 }&nbsp;{t('of')}&nbsp;{ _SETUP_WIZARD_STEPS.length }
        </span>
      </div>;

    return (
      <div className='responsive-wizard'>
        {title}
        {tabs}
        <div className='panel panel-default' id='products-content'>
            <div className='panel-body'>
              {pageContent}
            </div>
        </div>
        <SCCDialog
            onClose={() => this.closePopUp}
            start={this.state.showPopUp && !this.state.syncRunning}
            updateSyncRunning={(syncStatus) => this.updateSyncRunning(syncStatus)}
          />
        {footer}
      </div>
    )
  }
});

ReactDOM.render(
  <Products />,
  document.getElementById('products')
);
