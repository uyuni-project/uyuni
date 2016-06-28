"use strict";

const React = require("react");
const ReactDOM = require("react-dom");
const TableComponent = require("../components/table");
const Table = TableComponent.Table;
const TableCell = TableComponent.TableCell;
const TableRow = TableComponent.TableRow;
const MessageContainer = require("../components/messages").Messages;
const MessagesUtils = require("../components/messages").Utils;
const Network = require("../utils/network");

const TaskoTop = React.createClass({

  getInitialState: function() {
    return {
      serverData: null,
      error: null,
    };
  },

  componentWillMount: function() {
    this.refreshServerData();
    setInterval(this.refreshServerData, this.props.refreshInterval);
  },

  refreshServerData: function() {
    Network.get("/rhn/manager/taskotop/data", "application/json").promise
      .then(data => {
        this.setState({
          serverData: data,
          error: null
        });
      })
      .catch(response => {
        this.setState({
          error: response.status == 401 ? "authentication" :
            response.status >= 500 ? "general" :
            null
        });
      });
  },

  buildRows: function(rawJobs) {
    const result = rawJobs.map(function(rawJob, index) {
      const columns = [
        <TableCell key="runId" content={rawJob["id"]} />,
        <TableCell key="name" content={rawJob["name"]} />,
        <TableCell key="startTime" content={moment(rawJob["startTime"]).format("DD/MMM/YYYY, HH:mm:ss (SSS)")} />,
        <TableCell key="endTime" content={
            rawJob["endTime"] == null ?
            "" :
            moment(rawJob["endTime"]).format("DD/MMM/YYYY, HH:mm:ss (SSS)")} />,
        <TableCell key="elapsedTime" content={
            rawJob["endTime"] == null ?
            Math.round(moment().diff(moment(rawJob["startTime"])) / 1000, 0) :
            Math.round(moment(rawJob["endTime"]).diff(moment(rawJob["startTime"]))/ 1000, 0)
          } />
      ];
      return <TableRow key={index} columns={columns} rawData={rawJob} />;
    });
    return result;
  },

  render: function() {
    const data = this.state.serverData;

    if (data != null && data.length > 0) {
      return (
        <div>
          <div className="spacewalk-toolbar-h1">
            <h1><i className="fa fa-tasks"></i>{t("TaskoTop")}</h1>
            <ErrorMessage error={this.state.error} />
            <p>{t('Taskomatic executed the following tasks during the latest 5 minutes.')}</p>
          </div>
          <Table
            headers={[t("id"), t("name"), t("startTime"), t("endTime"), t('elapsedTime')]}
            rows={this.buildRows(data)}
          />
        </div>
      );
    }
    else {
      return (
        <div>
          <div className="spacewalk-toolbar-h1">
            <h1><i className="fa fa-tasks"></i>{t("TaskoTop")}</h1>
            <p>{t('Taskomatic is not running any tasks at the moment.')}</p>
          </div>
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
  <TaskoTop refreshInterval={1 * 1000} />,
  document.getElementById("taskotop")
);
