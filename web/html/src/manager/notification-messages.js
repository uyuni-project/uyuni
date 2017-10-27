"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const MessageContainer = require("../components/messages").Messages;
const {Table, Column, SearchField, Highlight} = require("../components/table");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Utils = Functions.Utils;

const NotificationMessages = React.createClass({

  getInitialState: function() {
    return {
      serverData: null,
      error: null,
    };
  },

  componentWillMount: function() {
    this.refreshServerData();
    // setInterval(this.refreshServerData, this.props.refreshInterval);
  },

  refreshServerData: function() {
    var currentObject = this;
    Network.get("/rhn/manager/notification-messages/data", "application/json").promise
      .then(data => {
        currentObject.setState({
          serverData: data,
          error: null,
        });
      })
      .catch(response => {
        currentObject.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null
        });
      });
  },

  sortByDate: function(aRaw, bRaw, columnKey, sortDirection) {
    var a = aRaw[columnKey] || "0000-01-01T00:00:00.000Z";
    var b = bRaw[columnKey] || "0000-01-01T00:00:00.000Z";
    var result = a.toLowerCase().localeCompare(b.toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  sortByStatus: function(aRaw, bRaw, columnKey, sortDirection) {
    var statusValues = {'true': 0, 'false': 1};
    var a = statusValues[aRaw[columnKey]];
    var b = statusValues[bRaw[columnKey]];
    var result = (a > b ? 1 : (a < b ? -1 : 0)) || this.sortByDate(aRaw, bRaw, 'created', sortDirection);
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  sortByNumber: function(aRaw, bRaw, columnKey, sortDirection) {
    var a = aRaw[columnKey];
    var b = bRaw[columnKey];
    var result = a > b ? 1 : (a < b ? -1 : 0);
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  },

  searchData: function(datum, criteria) {
      if (criteria) {
        return datum.description.toLowerCase().includes(criteria.toLowerCase());
      }
      return true;
  },

  decodeStatus: function(status) {
    var cell;
    switch(status) {
      case true: cell = <div><i className="fa fa-eye"></i>{t(' read')}</div>; break;
      case false: cell = <div className="text-info"><i className="fa fa-envelope"></i>{t(' not read')}</div>; break;
      default: cell = null;
    }
    return cell;
  },

  buildRows: function(jobs) {
    return Object.keys(jobs).map((id) => jobs[id]);
  },

  render: function() {
    const data = this.state.serverData;
    const title =
      <div className="spacewalk-toolbar-h1">
        <h1>
          <i className="fa fa-envelope"></i>
          {t('Notification Messages')}
          {/* <a href="/rhn/help/reference/en-US/ref.webui.admin.status.jsp"
              target="_blank"><i className="fa fa-question-circle spacewalk-help-link"></i>
          </a> */}
        </h1>
      </div>
    ;
    
    if (data != null) {
      if (Object.keys(data).length > 0) {
        return  (
          <div key="notification-messages-content">
            {title}
            <ErrorMessage error={this.state.error} />
            <p>{t('The server has collected the following notification messages.')}</p>
            <p>{t('Data are refreshed every ')}{this.props.refreshInterval/1000}{t(' seconds')}.</p>
            <Table
              data={this.buildRows(data)}
              identifier={(row) => row["id"]}
              cssClassFunction={(row) => row["status"] == true ? 'text-muted' : null }
              initialSortColumnKey="created"
              initialSortDirection={-1}
              searchField={
                  <SearchField filter={this.searchData}
                      criteria={""}
                      placeholder={t("Filter by description")} />
              }>
              <Column
                columnKey="id"
                comparator={Utils.sortByNumber}
                header={t("Id")}
                cell={ (row) => row["id"] }
              />
              <Column
                columnKey="description"
                comparator={Utils.sortByText}
                header={t("Description")}
                cell={ (row) => row["description"] }
              />
              <Column
                columnKey="created"
                comparator={this.sortByDate}
                header={t("Created")}
                cell={ (row) => row["created"] == null ? "" : moment(row["created"]).format("DD/MM/YYYY HH:mm:ss") }
              />
              <Column
                columnKey="modified"
                comparator={this.sortByDate}
                header={t("Modified")}
                cell={ (row) => row["modified"] == null ? "" : moment(row["modified"]).format("DD/MM/YYYY HH:mm:ss") }
              />
              <Column
                columnKey="read"
                comparator={this.sortByStatus}
                header={t("Read")}
                cell={ (row) => this.decodeStatus(row["read"]) }
              />
            </Table>
          </div>
        );
      }
      else {
        return (
          <div key="notification-messages-no-content">
            {title}
            <p>{t('There are no notification messages.')}</p>
          </div>
        );
      }
    }
    else {
      return (
        <div key="notification-messages-content">
          {title}
        </div>
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
  <NotificationMessages refreshInterval={60 * 1000} />,
  document.getElementById("notification-messages")
);
