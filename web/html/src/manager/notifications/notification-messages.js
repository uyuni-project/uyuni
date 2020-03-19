/* eslint-disable */
"use strict";

const {SectionToolbar} = require("components/section-toolbar/section-toolbar");
const React = require("react");
const MessageContainer = require("components/messages").Messages;
const {Table} = require("components/table/Table");
const {Column} = require("components/table/Column");
const {SearchField} = require("components/table/SearchField");
const {Highlight} = require("components/table/Highlight");
const Network = require("utils/network");
const Functions = require("utils/functions");
const Utils = Functions.Utils;
const {AsyncButton, Button} = require("components/buttons");
const { TopPanel } = require('components/panels/TopPanel');
const escapeHtml = require('html-react-parser');
const {Dialog} = require("components/dialog/Dialog");
const {showDialog} = require("components/dialog/util");


function reloadData(dataUrlSlice) {
  return Network.get('/rhn/manager/notification-messages/' + dataUrlSlice, "application/json").promise;
}

class NotificationMessages extends React.Component {
  state = {
    serverData: null,
    error: null,
    dataUrlTags: ['#data-unread', '#data-all'],
    currentDataUrlTag: location.hash ? location.hash : '#data-unread',
    loading: true,
    messages: [],
    selectedItems: [],
    popupItem: null
  };

  UNSAFE_componentWillMount() {
    this.refreshServerData(this.state.currentDataUrlTag);
  }

  UNSAFE_componentWillUpdate(nextProps, nextState) {
    if (this.state.currentDataUrlTag != nextState.currentDataUrlTag) {
      this.refreshServerData(nextState.currentDataUrlTag);
    }
  }

  changeTabUrl = (nextDataUrlTag) => {
    this.setState({currentDataUrlTag : nextDataUrlTag});
  };

  decodeDataUrlSlice = (dataUrlTag) => {
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
  };

  refreshServerData = (dataUrlTag) => {
    this.setState({loading: true});
    var currentObject = this;
    reloadData(this.decodeDataUrlSlice(dataUrlTag))
      .then(data => {
        currentObject.setState({
          serverData: data,
          error: null,
          loading: false,
          messages: [],
          selectedItems: []
        });
      })
      .catch(response => {
        currentObject.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null,
          loading: false,
          messages: [],
          selectedItems: []
        });
      });
  };

  handleSelectItems = (items) => {
    this.setState({
      selectedItems: items
    });
  };

  updateReadStatus = (ids, flagAsRead) => {
    var dataRequest = {};
    dataRequest.messageIds = ids;
    dataRequest.flagAsRead = flagAsRead;

    return Network.post("/rhn/manager/notification-messages/update-messages-status", JSON.stringify(dataRequest), "application/json").promise
      .then(data => {
        const newMessage = { severity: data.severity, text: data.text };
        this.setState((prevState, props) => ({
            // serverData = prev serverData without those are changed + those changed with the changes
            serverData : prevState.serverData.filter(m => !ids.includes(m.id))
              .concat(prevState.serverData
                .filter(m => ids.includes(m.id))
                .map(m => {
                  var newM = Object.assign({}, m);
                  newM.isRead = flagAsRead;
                  return newM;
                })),
            messages : prevState.messages.concat([newMessage])
          })
        );
      })
      .catch(response => {
        this.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null
        });
      });
  };

  markAsRead = (ids) => {
    return this.updateReadStatus(ids, true);
  };

  deleteNotifications = (ids) => {
    return Network.post("/rhn/manager/notification-messages/delete", JSON.stringify(ids), "application/json").promise
      .then(data => {
        const newMessage = { severity: data.severity, text: data.text };
        this.setState((prevState, props) => ({
            serverData : prevState.serverData.filter(m => !ids.includes(m.id)),
            selectedItems : prevState.selectedItems.filter(m => !ids.includes(m)),
            messages : prevState.messages.concat([newMessage])
          })
        );
      })
      .catch(response => {
        this.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null
        });
      });
  };

  decodeTypeText = (rawType) => {
    var typeText;
    switch(rawType) {
      case 'OnboardingFailed': typeText = t('Onboarding failed'); break;
      case 'ChannelSyncFailed': typeText = t('Channel sync failed'); break;
      case 'ChannelSyncFinished': typeText = t('Channel sync finished'); break;
      case 'CreateBootstrapRepoFailed': typeText = t('Creating Bootstrap Repository failed'); break;
      case 'StateApplyFailed': typeText = t('State apply failed'); break;
    }
    return typeText;
  };

  decodeIconBySeverity = (severity) => {
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
  };

  sortBySeverity = (aRaw, bRaw, columnKey, sortDirection) => {
    var statusValues = {'info': 0, 'warning': 1, 'error': 2};
    var a = statusValues[aRaw[columnKey]];
    var b = statusValues[bRaw[columnKey]];
    var result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  sortByStatus = (aRaw, bRaw, columnKey, sortDirection) => {
    var statusValues = {'true': 0, 'false': 1};
    var a = statusValues[aRaw[columnKey]];
    var b = statusValues[bRaw[columnKey]];
    var result = (a > b ? 1 : (a < b ? -1 : 0));
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  buildSummaryText = (row) => {
    var div = document.createElement("div");
    div.innerHTML = row['summary']
    return div.textContent || div.innerText || "";
  };

  sortByText = (aRaw, bRaw, columnKey, sortDirection) => {
    var result = this.buildSummaryText(aRaw).toLowerCase().localeCompare(this.buildSummaryText(bRaw).toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  searchData = (datum, criteria) => {
      if (criteria) {
        return (this.buildSummaryText(datum)).toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  };

  buildRows = (message) => {
    return Object.keys(message).map((id) => message[id]);
  };

  showDescriptionPopup = (row) => {
    this.setState({popupItem: row});
    showDialog("notifications-popup-dialog");
  }

  buildSummary = (row) => {
    const popupLink = <a href="#" onClick={() => this.showDescriptionPopup(row)}>{t("Details")}</a>;

    return (
      <span>
        {escapeHtml(row['summary'])}
        &nbsp;
        {row['description'] && popupLink}
      </span>
    );
  };

  buildPopupSummary = () => {
    const summary = (this.state.popupItem || {}).summary || "";
    return escapeHtml(summary);
  }

  buildPopupDescription = () => {
    const description = (this.state.popupItem || {}).description || "";
    return escapeHtml(description);
  }

  retryOnboarding = (minionId) => {
    return Network.post("/rhn/manager/notification-messages/retry-onboarding/" + minionId, "application/json").promise
      .then((data) => {
        const newMessage = { severity: data.severity, text: data.text };
        this.setState((prevState, props) => ({ messages : prevState.messages.concat([newMessage]) }));
      })
      .catch(response => {
      });
  };

  retryReposync = (channelId) => {
    return Network.post("/rhn/manager/notification-messages/retry-reposync/" + channelId, "application/json").promise
      .then((data) => {
        const newMessage = { severity: data.severity, text: data.text };
        this.setState((prevState, props) => ({ messages : prevState.messages.concat([newMessage]) }));
      })
      .catch(response => {
      });
  };

  messageReaction = (messageType, messageData) => {
    let actionButton = null;
    switch(messageType) {
      case 'OnboardingFailed':
        actionButton = <AsyncButton id="retryOnboarding" icon="fa-rocket fa-1-5x"
            title={t('Retry onboarding')} action={() => this.retryOnboarding(messageData['minionId'])} />;
      break;
      case 'ChannelSyncFailed':
        actionButton = <AsyncButton id="retryReposync" icon="fa-refresh fa-1-5x"
            title={t('Retry repo sync')} action={() => this.retryReposync(messageData['channelId'])} />;
      break;
    }
    return actionButton;
  };

  render() {
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

    const panelButtons = <SectionToolbar>
        <div className='action-button-wrapper'>
          <div className='btn-group'>
            <AsyncButton id="reload" icon="fa-refresh" text={t('Refresh')} action={this.refreshServerData} />
            <AsyncButton id="delete-selected-messages" icon="fa-trash"
                title={t('Delete selected messages')} text={t('Delete selected messages')}
                action={() => this.deleteNotifications(this.state.selectedItems)}
                disabled={this.state.selectedItems.length == 0 ? 'disabled' : ''} />
            <AsyncButton id="mark-as-read" icon="fa-check-circle"
                title={t('Mark selected as read')} text={t('Mark selected as read')}
                action={() => this.markAsRead(this.state.selectedItems)}
                disabled={this.state.selectedItems.length == 0 ? 'disabled' : ''} />
        </div>
      </div>
    </SectionToolbar>;

    const visibleMessages = this.state.messages.length > 3 ? this.state.messages.slice(this.state.messages.length - 3) : this.state.messages;
    const messages = visibleMessages.map(m => <MessageContainer items={[{severity: m.severity, text: <p>{t(m.text)}.</p> }]}/>);

    if (data != null) {
      return  (
        <TopPanel title={t("Notification Messages")} icon="fa-envelope">
          <ErrorMessage error={this.state.error} />

          { messages }

          <p>{t('The server has collected the following notification messages.')}</p>
          {headerTabs}

          {panelButtons}

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
                    placeholder={t("Filter by summary")} />
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
              columnKey="summary"
              comparator={this.sortByText}
              header={t("Summary")}
              cell={ (row) => this.buildSummary(row) }
            />
            <Column
              columnKey="created"
              comparator={Utils.sortByDate}
              header={t("Created")}
              cell={ (row) => row["created"] }
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
                    <AsyncButton id="updateReadStatus"
                        icon={(row['isRead'] ? 'spacewalk-icon-envelope-open-o text-muted' : 'fa-envelope text-primary') + " fa-1-5x"}
                        title={row['isRead'] ? t('Flag as Unread') : t('Flag as Read')}
                        action={() => this.updateReadStatus([row['id']], !row['isRead'])} />
                    <AsyncButton id="delete"
                        icon="fa-trash fa-1-5x" title={t('Delete Notification')}
                        action={() => this.deleteNotifications([row['id']])} />
                  </div>
              }
            />
          </Table>
          <Dialog id="notifications-popup-dialog"
            title={this.buildPopupSummary()}
            content={this.buildPopupDescription()}
          />
        </TopPanel>
      );
    }
    else {
      return (
        <TopPanel title={t("Notification Messages")} icon="fa-envelope">
          <ErrorMessage error={this.state.error} />
        </TopPanel>
      );
    }
  }
}

const ErrorMessage = (props) => <MessageContainer items={
    props.error == "authentication" ?
      MessagesUtils.warning(t("Session expired, please reload the page to see up-to-date data.")) :
    props.error == "general" ?
      MessagesUtils.warning(t("Server error, please check log files.")) :
    []
  } />
;

const { hot } = require('react-hot-loader');
module.exports = { NotificationMessages: hot(module)(NotificationMessages) }
