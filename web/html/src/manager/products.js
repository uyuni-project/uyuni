'use strict';

const React = require('react');
const ReactDOM = require('react-dom');
const Network = require('../utils/network');
const Messages = require('../components/messages').Messages;
const MessagesUtils = require("../components/messages").Utils;
const {DataHandler, DataItem, SearchField, Highlight} = require('../components/data-handler');
const Functions = require('../utils/functions');
const Utils = Functions.Utils;
const {ModalButton, ModalLink} = require("../components/dialogs");
const Button = require('../components/buttons').Button;
const SCCDialog = require('./products-scc-dialog').SCCDialog;
const PopUp = require("../components/popup").PopUp;

const _DATA_ROOT_ID = 'baseProducts';

const _SETUP_WIZARD_STEPS = [
  {
    id: 'wizard-step-proxy',
    label: 'HTTP Proxy',
    url: '/rhn/admin/setup/ProxySettings.do',
    active: false
  },
  {
    id: 'wizard-step-credentials',
    label: 'Organization Credentials',
    url: '/rhn/admin/setup/MirrorCredentials.do',
    active: false
  },
  {
    id: 'wizard-step-suse-products',
    label: 'SUSE Products',
    url: location.href.split(/\?|#/)[0],
    active: true
  }
];

function reloadData() {
  return Network.get('/rhn/manager/api/admin/products', 'application/json').promise;
}

const ProductPageWrapper = React.createClass({
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

  handleSelectedItems: function(items) {
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
          { _SETUP_WIZARD_STEPS.map(step => <li key={step.id} className={step.active ? 'active' : ''}><a href={step.url}>{t(step.label)}</a></li>)}
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
              <Products
                  data={this.state.serverData}
                  loading={this.state.loading}
                  handleSelectedItems={this.handleSelectedItems}
                  selectedItems={this.state.selectedItems}
              />
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

const Products = React.createClass({
  handleSelectedItem: function(id) {
    let arr = this.props.selectedItems;
    if(arr.includes(id)) {
      arr = arr.filter(i => i !== id);
    } else {
      arr = arr.concat([id]);
    }
    this.props.handleSelectedItems(arr);
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

  render: function() {
    return (
      <DataHandler
        data={this.buildRows(this.props.data)}
        identifier={(raw) => raw['identifier']}
        initialItemsPerPage={userPrefPageSize}
        loading={this.props.loading}
        searchField={
            <SearchField filter={this.searchData}
                criteria={''}
                placeholder={t('Filter by product name')} />
        }>
        <CheckList data={d => d}
            nestedKey='extensions'
            isSelectable={true}
            handleSelectedItem={this.handleSelectedItem}
            selectedItems={this.props.selectedItems}
            styleClass='product-list'
            isFirstLevel={true}
        />
      </DataHandler>
    )
  }
});

const CheckList = React.createClass({
  getInitialState: function() {
    return {
      channelsItem: null
    }
  },

  handleSelectedItem: function(id) {
    this.props.handleSelectedItem(id);
  },

  showChannelsfor: function(item) {
    this.setState({channelsItem: item});
  },

  render: function() {
    const titlePopup = t('Product Channels - ') + (this.state.channelsItem != null ? this.state.channelsItem['label'] : '');
    const contentPopup = 
      this.state.channelsItem != null ?
        (
          <div>
            {
              this.state.channelsItem['channels'].filter(c => !c['optional']).length > 0 ?
                <div>
                  <h4>Mandatory Channels</h4>
                  <ul className='product-channel-list'>
                    {
                      this.state.channelsItem['channels']
                        .filter(c => !c['optional'])
                        .map(c => <li>{c['summary']}&nbsp;<small>[{c['label']}]</small></li>)
                    }
                  </ul>
                </div>
                : null
            }
            {
              this.state.channelsItem['channels'].filter(c => c['optional']).length > 0 ?
                <div>
                  <h4>Optional Channels</h4>
                  <ul className='product-channel-list'>
                    {
                      this.state.channelsItem['channels']
                        .filter(c => c['optional'])
                        .map(c => <li>{c['summary']}&nbsp;<small>[{c['label']}]</small></li>)
                    }
                  </ul>
                </div>
                : null
            }
          </div>
        )
      : null ;

    return (
      this.props.data ?
        <div>
          <ul className={this.props.styleClass}>
            {
              this.props.data.map((l, index) =>
              {
                return (
                  <CheckListItem item={l}
                      handleSelectedItem={this.handleSelectedItem}
                      selectedItems={this.props.selectedItems}
                      nestedKey='extensions'
                      isSelectable={true}
                      isFirstLevel={true}
                      index={index}
                      showChannelsfor={this.showChannelsfor}
                  />
                )
              })
            }
          </ul>
          <PopUp
              id='show-channels-popup'
              title={titlePopup}
              content={contentPopup}
              className='modal-xs'
          />
        </div>
        : null
    )
  }
});

const CheckListItem = React.createClass({
  getInitialState: function() {
    return {
      visibleList: []
    }
  },

  handleSubListVisibility: function(id) {
    let arr = this.state.visibleList;
    if(arr.includes(id)) {
      arr = arr.filter(i => i !== id);
    } else {
      arr = arr.concat([id]);
    }
    this.setState({ visibleList: arr });
  },

  handleSelectedItem: function(id) {
    this.props.handleSelectedItem(id);
  },

  render: function() {
    const sublistVisible = this.state.visibleList.includes(this.props.item['identifier']);
    var nestedData = [];
    if (this.props.nestedKey) {
      nestedData = this.props.item[this.props.nestedKey];
    }
    var openSubListIcon = sublistVisible ?
      <i className={'fa fa-caret-down fa-1-6x pointer'}
          onClick={() => this.handleSubListVisibility(this.props.item['identifier'])} />
      :
      <i className={'fa fa-caret-right fa-1-6x pointer'}
          onClick={() => this.handleSubListVisibility(this.props.item['identifier'])} />;
    let evenOddClass = (this.props.index % 2) === 0 ? "list-row-even" : "list-row-odd";

    

    return (
      <li className={evenOddClass} key={this.props.item['identifier']}>
        <div className='product-element'>
          <div className='right'>
            <div className='d-inline-block fixed-small-col' title={t('Architecture')}>
              {this.props.isFirstLevel ? this.props.item['arch'] : ''}
            </div>
            <div className='d-inline-block fixed-icon-col'>
              <ModalLink
                  id='showChannels'
                  icon='fa-list'
                  title={t('Show product\'s channels')}
                  target='show-channels-popup'
                  onClick={() => this.props.showChannelsfor(this.props.item)}
              />
            </div>
            <div className='d-inline-block fixed-small-col' title={t('Product sync status')}>
              [---%---]
            </div>
            <div className='d-inline-block fixed-icon-col'>
              <Button className='btn-default btn-sm' icon='fa-refresh' disabled title={t('Resync product')} />
            </div>
          </div>
          <div className='d-inline-block'>
            &nbsp;
            {
              this.props.isSelectable ?
                <input type='checkbox'
                    value={this.props.item['identifier']}
                    onChange={() => this.handleSelectedItem(this.props.item['identifier'])}
                    checked={this.props.selectedItems.includes(this.props.item['identifier']) ? 'checked' : ''}
                />
                : null
            }
            &nbsp;&nbsp;
            {
              nestedData && nestedData.length > 0 ?
              openSubListIcon
              : <i className='fa' />
            }
            &nbsp;&nbsp;
            {this.props.item['label']}
            &nbsp;
          </div>
        </div>
        { sublistVisible ?
          <CheckList data={nestedData}
              nestedKey={this.props.nestedKey}
              isSelectable={this.props.isSelectable}
              selectedItems={this.props.selectedItems}
              handleSelectedItem={this.handleSelectedItem}
              styleClass={this.props.styleClass}
              isFirstLevel={false}
          />
          : null }
      </li>
    )
  }
});


ReactDOM.render(
  <ProductPageWrapper />,
  document.getElementById('products')
);
