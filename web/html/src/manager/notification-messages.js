"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const MessageContainer = require("../components/messages").Messages;
const {Table, Column, SearchField, Highlight} = require("../components/table");
const Network = require("../utils/network");
const Functions = require("../utils/functions");
const Utils = Functions.Utils;
const Buttons = require("../components/buttons");
const AsyncButton = Buttons.AsyncButton;
const Panels = require("../components/panel");
const Panel = Panels.Panel;

function reloadData() {
  return Network.get("/rhn/manager/notification-messages/data-unread", "application/json").promise;
}

const CheckRead = React.createClass({
  getInitialState: function() {
    return {
      messageId: this.props.messageId,
      readState: this.props.isRead
    }
  },

  updateReadStatus: function() {
    var currentObject = this;
    var messageData = {};
    messageData.messageId = this.state.messageId;
    messageData.isRead = !this.state.readState;
    Network.post("/rhn/manager/notification-messages/update-message-status", JSON.stringify(messageData), "application/json").promise
    .then(data => {
        console.log(data.message);
        this.setState({readState : !this.state.readState})
    })
    .catch(response => {
      currentObject.setState({
        error: response.status == 401 ? "authentication" :
          response.status >= 500 ? "general" :
          null
      });
    });
  },

  render: function() {
    return (
      <div className="col-md-6">
        <input type="checkbox" onClick={this.updateReadStatus} />
      </div>
    );
  }
});

const NotificationMessages = React.createClass({

  getInitialState: function() {
    return {
      serverData: null,
      error: null,
    };
  },

  componentWillMount: function() {
    this.refreshServerData();
  },

  refreshServerData: function() {
    var currentObject = this;
    reloadData()
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
    const panelButtons = <div className="pull-right btn-group">
      <AsyncButton id="reload" icon="refresh" name="Refresh" text action={this.refreshServerData} />
    </div>;

    if (data != null) {
      if (Object.keys(data).length > 0) {
        return  (
          <Panel title={t("Notification Messages")} icon="fa-envelope" button={ panelButtons }>
            <ErrorMessage error={this.state.error} />
            <p>{t('The server has collected the following notification messages.')}</p>
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
                columnKey="type"
                comparator={Utils.sortByText}
                header={t("Type")}
                cell={ (row) => row["type"] }
              />
              <Column
                columnKey="priority"
                comparator={Utils.sortByNumber}
                header={t("Priority")}
                cell={ (row) => row["priority"] }
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
                columnKey="isRead"
                comparator={this.sortByStatus}
                header={t("Is read")}
                cell={ (row) => <CheckRead messageId={row['id']} isRead={row['isRead']} />}
              />
            </Table>
          </Panel>
        );
      }
      else {
        return (
          <Panel title={t("Notification Messages")} icon="fa-envelope" button={ panelButtons }>
            <ErrorMessage error={this.state.error} />
            <p>{t('There are no notification messages.')}</p>
          </Panel>
        );
      }
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
