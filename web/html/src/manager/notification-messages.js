"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const MessageContainer = require("../components/messages").Messages;
const {Table, Column, SearchField, Highlight} = require("../components/table");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Utils = Functions.Utils;
const {AsyncButton, Button} = require("../components/buttons");
const Panels = require("../components/panel");
const Panel = Panels.Panel;

function reloadData(dataUrlSlice) {
  return Network.get('/rhn/manager/notification-messages/' + dataUrlSlice, "application/json").promise;
}

const NotificationMessages = React.createClass({

  getInitialState: function() {
    return {
      serverData: null,
      error: null,
      dataUrlTags: ['#data-unread', '#data-all'],
      currentDataUrlTag: location.hash ? location.hash : '#data-unread',
      loading: true,
      messages: [],
      selectedItems: [],
    };
  },

  componentWillMount: function() {
    this.refreshServerData(this.state.currentDataUrlTag);
  },

  componentWillUpdate: function(nextProps, nextState) {
    if (this.state.currentDataUrlTag != nextState.currentDataUrlTag) {
      this.refreshServerData(nextState.currentDataUrlTag);
    }
  },

  changeTabUrl: function(nextDataUrlTag) {
    this.setState({currentDataUrlTag : nextDataUrlTag});
  },

  decodeDataUrlSlice: function(dataUrlTag) {
    let dataUrlSlice;
    // decode the tab, the data and the table to present
    switch (dataUrlTag) {
      case '#data-unread' : dataUrlSlice = 'data-unread'; break;
      case '#data-all' : dataUrlSlice = 'data-all'; break;
      default :
        if (this.state.dataUrlTags[location.hash] != null) {
          dataUrlSlice = location.hash.replace('#', '');
        }
        else {
          dataUrlSlice = this.state.currentDataUrlTag ? this.state.currentDataUrlTag.replace('#', '') : 'data-unread';
        }
        break;
    }
    return dataUrlSlice;
  },

  refreshServerData: function(dataUrlTag) {
    this.setState({loading: true});
    var currentObject = this;
    reloadData(this.decodeDataUrlSlice(dataUrlTag))
      .then(data => {
        currentObject.setState({
          serverData: data,
          error: null,
          loading: false,
          messages: []
        });
      })
      .catch(response => {
        currentObject.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null,
          loading: false,
          messages: []
        });
      });
  },

  handleSelectItems(items) {
    this.setState({
      selectedItems: items
    });
  },

  updateReadStatus: function(messageId) {
    var currentObject = this;

    var updatedData = this.state.serverData;
    var messageRaw = updatedData.find(m => m.id == messageId);
    messageRaw.isRead = !messageRaw.isRead;

    var messageDataRequest = {};
    messageDataRequest.messageId = messageRaw.id;
    messageDataRequest.isRead = messageRaw.isRead;
    Network.post("/rhn/manager/notification-messages/update-message-status", JSON.stringify(messageDataRequest), "application/json").promise
    .then(data => {
        this.setState({serverData : updatedData})
    })
    .catch(response => {
      currentObject.setState({
        error: response.status == 401 ? "authentication" :
          response.status >= 500 ? "general" :
          null
      });
    });
  },

  deleteNotifications: function(ids) {
    var currentObject = this;
    Network.post("/rhn/manager/notification-messages/delete", JSON.stringify(ids), "application/json").promise
    .then(data => {
      var updatedData = currentObject.state.serverData;
      ids.forEach(id =>
        updatedData = updatedData.filter(m => m.id != id)
      );
      var updatedSelectedItems = currentObject.state.selectedItems;
      ids.forEach(id =>
        updatedSelectedItems = updatedSelectedItems.filter(m => m.id != id)
      );
      this.setState({serverData : updatedData, selectedItems : updatedSelectedItems});
    })
    .catch(response => {
      currentObject.setState({
        error: response.status == 401 ? "authentication" :
          response.status >= 500 ? "general" :
          null
      });
    });
  },

  readThemAll: function() {
    var currentObject = this;
    Network.post("/rhn/manager/notification-messages/mark-all-as-read", "application/json").promise
    .then(() => {
      currentObject.refreshServerData();
    })
    .catch(response => {
      currentObject.setState({
        error: response.status == 401 ? "authentication" :
          response.status >= 500 ? "general" :
          null
      });
    });
  },

  decodeTypeText: function(rawType) {
    var typeText;
    switch(rawType) {
      case 'OnboardingFailed': typeText = t('Onboarding failed'); break;
      case 'ChannelSyncFailed': typeText = t('Channel sync failed'); break;
      case 'ChannelSyncFinished': typeText = t('Channel sync finished'); break;
    }
    return typeText;
  },

  decodeIconBySeverity: function(severity) {
    var severityHtml;
    switch(severity) {
      case 'info':
        severityHtml = <span><i className="fa fa-info-circle" title={t('Info')}></i>{t('Info')}</span>;
        break;
      case 'warning':
        severityHtml = <span><i className="fa fa-exclamation-triangle text-warning" title={t('Warning')}></i>{t('Warning')}</span>;
        break;
      case 'error':
        severityHtml = <span><i className="fa fa-times-circle-o text-danger" title={t('Error')}></i>{t('Error')}</span>;
        break;
    }
    return severityHtml;
  },

  sortBySeverity: function(aRaw, bRaw, columnKey, sortDirection) {
    var statusValues = {'info': 0, 'warning': 1, 'error': 2};
    var a = statusValues[aRaw[columnKey]];
    var b = statusValues[bRaw[columnKey]];
    var result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  sortByStatus: function(aRaw, bRaw, columnKey, sortDirection) {
    var statusValues = {'true': 0, 'false': 1};
    var a = statusValues[aRaw[columnKey]];
    var b = statusValues[bRaw[columnKey]];
    var result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  buildTextDescription: function(row) {
    let description = null;
    switch(row['type']) {
      case 'OnboardingFailed':
        description = 'Error registering minion id: ' + row['data']['minionId'];
      break;
      case 'ChannelSyncFailed':
        description = 'Error syncing the channel: ' + row['data']['channelName'];
      break;
      case 'ChannelSyncFinished':
        description = 'Channel ' + row['data']['channelName'] + ' sync completed';
      break;
      default: description = JSON.stringify(row['data']);
    }
    return description;
  },

  sortByText: function(aRaw, bRaw, columnKey, sortDirection) {
    var result = this.buildTextDescription(aRaw).toLowerCase().localeCompare(this.buildTextDescription(bRaw).toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  searchData: function(datum, criteria) {
      if (criteria) {
        return (this.buildTextDescription(datum)).toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  },

  buildRows: function(message) {
    return Object.keys(message).map((id) => message[id]);
  },

  buildDescription: function(row) {
    let description = null;
    switch(row['type']) {
      case 'OnboardingFailed':
        description = 'Error registering minion id: ' + row['data']['minionId'];
      break;
      case 'ChannelSyncFailed':
        description = <span>Error syncing the channel: <a href={"/rhn/channels/ChannelDetail.do?cid=" + row['data']['channelId']}>{row['data']['channelName']}</a></span>;
      break;
      case 'ChannelSyncFinished':
        description = <span>Channel <a href={"/rhn/channels/ChannelDetail.do?cid=" + row['data']['channelId']}>{row['data']['channelName']}</a> sync completed</span>;
      break;
      default: description = JSON.stringify(row['data']);
    }
    return description;
  },

  retryOnboarding: function(minionId) {
    Network.post("/rhn/manager/notification-messages/retry-onboarding/" + minionId, "application/json").promise
    .then((data) => {
      var newMessagesState = this.state.messages;
      newMessagesState.push({ severity: data.severity, text: data.text });
      this.setState({ messages : newMessagesState });
    })
    .catch(response => {
    });
  },

  retryReposync: function(channelId) {
    Network.post("/rhn/manager/notification-messages/retry-reposync/" + channelId, "application/json").promise
    .then((data) => {
      var newMessagesState = this.state.messages;
      newMessagesState.push({ severity: data.severity, text: data.text });
      this.setState({ messages : newMessagesState });
    })
    .catch(response => {
    });
  },

  messageReaction: function(messageType, messageData) {
    let actionButton = null;
    switch(messageType) {
      case 'OnboardingFailed':
        actionButton = <AsyncButton id="retryOnboarding" icon="fa fa-rocket fa-1-5x no-margin" classStyle="btn-sm"
            title={t('Retry onboarding')} text action={() => this.retryOnboarding(messageData['minionId'])} />;
      break;
      case 'ChannelSyncFailed':
        actionButton = <AsyncButton id="retryReposync" icon="fa fa-refresh fa-1-5x no-margin" classStyle="btn-sm"
            title={t('Retry repo sync')} text action={() => this.retryReposync(messageData['channelId'])} />;
      break;
    }
    return actionButton;
  },

  render: function() {
    const data = this.state.serverData;

    const dataHashTag = location.hash;
    const headerTabs =
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          <li className={dataHashTag == '#data-unread' || dataHashTag == '' ? 'active': ''}>
            <a href='#data-unread' onClick={() => this.changeTabUrl('#data-unread')}>{t('Unread Messages')}</a>
          </li>
          <li className={location.hash == '#data-all' ? 'active': ''}>
            <a href='#data-all' onClick={() => this.changeTabUrl('#data-all')}>{t('All Messages')}</a>
          </li>
        </ul>
      </div>
    ;

    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name={t('Refresh')} text action={this.refreshServerData} />
      <Button id="delete-selected-messages" icon="fa-trash" className='btn-default'
          title={t('Delete selected messages')} text={t('Delete selected messages')} handler={() => this.deleteNotifications(this.state.selectedItems)} />
      <Button id="mark-all-as-read" icon="fa-check-circle" className='btn-default'
          title={t('Mark all as read')} text={t('Mark all as read')} handler={this.readThemAll} />
    </div>;

    const visibleMessages = this.state.messages.length > 3 ? this.state.messages.slice(this.state.messages.length - 3) : this.state.messages;
    const messages = visibleMessages.map(m => <MessageContainer items={[{severity: m.severity, text: <p>{t(m.text)}.</p> }]}/>);

    if (data != null) {
      return  (
        <Panel title={t("Notification Messages")} icon="fa-envelope" button={ panelButtons }>
          <ErrorMessage error={this.state.error} />

          { messages }

          <p>{t('The server has collected the following notification messages.')}</p>
          {headerTabs}
          <Table
            data={this.buildRows(data)}
            identifier={(row) => row["id"]}
            cssClassFunction={(row) => row["isRead"] == true ? 'text-muted' : '' }
            initialSortColumnKey="created"
            initialSortDirection={-1}
            initialItemsPerPage={userPrefPageSize}
            loading={this.state.loading}
            selectable
            selectedItems={this.state.selectedItems}
            onSelect={this.handleSelectItems}
            searchField={
                <SearchField filter={this.searchData}
                    criteria={""}
                    placeholder={t("Filter by description")} />
            }>
            <Column
              columnKey="severity"
              comparator={this.sortBySeverity}
              header={t("Severity")}
              cell={ (row) => this.decodeIconBySeverity(row["severity"])}
            />
            <Column
              columnKey="type"
              header={t("Type")}
              cell={ (row) => this.decodeTypeText(row['type'])}
            />
            <Column
              columnKey="description"
              comparator={this.sortByText}
              header={t("Description")}
              cell={ (row) => this.buildDescription(row) }
            />
            <Column
              columnKey="created"
              comparator={Utils.sortByDate}
              header={t("Created")}
              cell={ (row) => moment(row["created"]).format("DD/MM/YYYY HH:mm:ss") }
            />
            <Column
              columnKey="action"
              header={t("Action")}
              cell={ (row) => this.messageReaction(row['type'], row['data'])}
            />
            <Column
              columnKey="isRead"
              comparator={this.sortByStatus}
              header={t("Read|Delete")}
              cell={ (row) =>
                  <div className="btn-group">
                    <AsyncButton id="updateReadStatus" classStyle="btn-sm"
                        icon={(row['isRead'] ? ' spacewalk-icon-envelope-open-o text-muted' : 'envelope text-primary') + " fa-1-5x no-margin"}
                        title={row['isRead'] ? t('Flag as Unread') : t('Flag as Read')}
                        text action={() => this.updateReadStatus(row['id'], row['isRead'])} />
                    <AsyncButton id="delete"  classStyle="btn-sm"
                        icon="trash fa-1-5x no-margin" title={t('Delete Notification')}
                        text action={() => this.deleteNotifications([row['id']])} />
                  </div>
              }
            />
          </Table>
        </Panel>
      );
    }
    else {
      return (
        <Panel title={t("Notification Messages")} icon="fa-envelope" button={ panelButtons }>
          <ErrorMessage error={this.state.error} />
        </Panel>
      );
    }
  }
});

const ErrorMessage = (props) => <MessageContainer items={
    props.error == "authentication" ?
      MessagesUtils.warning(t("Session expired, please reload the page to see up-to-date data.")) :
    props.error == "general" ?
      MessagesUtils.warning(t("Server error, please check log files.")) :
    []
  } />
;

ReactDOM.render(
  <NotificationMessages />,
  document.getElementById("notification-messages")
);
