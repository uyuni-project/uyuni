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
    Network.get("/rhn/manager/schedule/taskotop/data", "application/json").promise
      .then(data => {
        this.setState({
          serverData: data,
          error: null,
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

  decodeStatus: function(status) {
    var cell;
    switch(status) {
      case 'running': cell = <div><i className="fa fa-cog fa-spin"></i>{t(' running')}</div>; break;
      case 'finished': cell = <div className="text-success"><i className="fa fa-thumbs-o-up"></i>{t(' finished')}</div>; break;
      case 'failed': cell = <div className="text-danger"><i className="fa fa-exclamation-triangle"></i>{t(' failed')}</div>; break;
      case 'interrupted': cell = <div className="text-warning"><i className="fa fa-stop"></i>{t(' interrupted')}</div>; break;
      case 'ready_to_run': cell = <div className="text-primary"><i className="fa fa-list-ul"></i>{t(' ready to run')}</div>; break;
      case 'skipped': cell = <div className="text-muted"><i className="fa fa-angle-double-right"></i>{t(' skipped')}</div>; break;
      default: cell = null;
    }
    return cell;
  },

  buildRows: function(rawJobs) {
    const thisObject = this;
    const result = rawJobs.map(function(rawJob, index) {
      const columns = [
        <TableCell key="runId" content={rawJob["id"]} />,
        <TableCell key="name" content={rawJob["name"]} />,
        <TableCell key="startTime" content={moment(rawJob["startTime"]).format("HH:mm:ss")} />,
        <TableCell key="endTime" content={rawJob["endTime"] == null ? "" : moment(rawJob["endTime"]).format("HH:mm:ss")} />,
        <TableCell key="elapsedTime" content={rawJob["elapsedTime"] == null ? "" : rawJob["elapsedTime"] + ' seconds'} />,
        <TableCell key="status" content={thisObject.decodeStatus(rawJob["status"])} />,
        <TableCell key="data" content={rawJob["data"].map(c => <div>{c}</div>)} />
      ];
      return <TableRow key={index}
          className={rawJob["status"] == 'running' ? 'info' : ''}
          columns={columns}
          rawData={rawJob} />;
    });
    return result;
  },

  render: function() {
    const data = this.state.serverData;

    if (data != null && data.length > 0) {
      return (
        <div>
          <div className="spacewalk-toolbar-h1">
            <h1><i className="fa fa-gears"></i>{t("TaskoTop")}</h1>
            <ErrorMessage error={this.state.error} />
            <p>{t('Taskomatic is running or has finished executing the following tasks during the latest 5 minutes.')}</p>
            <p>{t('Data are refreshed every ')}{this.props.refreshInterval/1000}{t(' seconds')}</p>
          </div>
          <Table
            headers={[t("Task Id"), t("Task Name"), t("Start Time"), t("End Time"), t('Elapsed Time'), t('Status'), t('Data')]}
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
  <TaskoTop refreshInterval={5 * 1000} />,
  document.getElementById("taskotop")
);
