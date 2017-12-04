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

const CheckRead = React.createClass({
  render: function() {
    return (
      <button className="btn btn-default btn-sm" onClick={this.props.onChange}>
        {this.props.isRead ?
          <span title={t('Read')}>
            <i className='fa fa-1-5x spacewalk-icon-envelope-open-o no-margin text-muted'></i>
          </span>
          : <span title={t('Unread')}>
              <i className='fa fa-1-5x fa-envelope no-margin text-primary'></i>
            </span>
        }
      </button>
    );
  }
});

const NotificationMessages = React.createClass({

  getInitialState: function() {
    return {
      serverData: null,
      error: null,
      dataUrlTags: ['#data-unread', '#data-all'],
      currentDataUrlTag: location.hash ? location.hash : '#data-unread',
      loading: true,
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
          loading: false
        });
      })
      .catch(response => {
        currentObject.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null,
          loading: false
        });
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

  readThemAll: function() {
    var currentObject = this;
    Network.post("/rhn/manager/notification-messages/mark-all-as-read", null, "application/json").promise
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

  searchData: function(datum, criteria) {
      if (criteria) {
        return datum.description.toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  },

  buildRows: function(message) {
    return Object.keys(message).map((id) => message[id]);
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
      <Button id="mark-all-as-read" icon="fa-check-circle" className='btn-default'
          title={t('Mark all as read')} text={t('Mark all as read')} handler={this.readThemAll} />
    </div>;

    if (data != null) {
      return  (
        <Panel title={t("Notification Messages")} icon="fa-envelope" button={ panelButtons }>
          <ErrorMessage error={this.state.error} />
          <p>{t('The server has collected the following notification messages.')}</p>
          {headerTabs}
          <Table
            data={this.buildRows(data)}
            identifier={(row) => row["id"]}
            cssClassFunction={(row) => row["isRead"] == true ? 'text-muted' : '' }
            initialSortColumnKey="created"
            initialSortDirection={-1}
            loading={this.state.loading}
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
              columnKey="description"
              comparator={Utils.sortByText}
              header={t("Description")}
              cell={ (row) => row["description"] }
            />
            <Column
              columnKey="created"
              comparator={Utils.sortByDate}
              header={t("Created")}
              cell={ (row) => moment(row["created"]).format("DD/MM/YYYY HH:mm:ss") }
            />
            <Column
              columnKey="isRead"
              comparator={this.sortByStatus}
              header={t("Read")}
              cell={ (row) => <CheckRead onChange={() => this.updateReadStatus(row['id'], row['isRead'])}
                  isRead={row['isRead']} />}
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
