import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Messages as MessageContainer, Utils as MessagesUtils } from "components/messages/messages";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";
import { HelpLink } from "components/utils/HelpLink";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

type Props = {
  refreshInterval: number;
};

class TaskoTop extends React.Component<Props> {
  timerId?: number;
  state = {
    serverData: null,
    error: null,
  };

  UNSAFE_componentWillMount() {
    this.refreshServerData();
    this.timerId = window.setInterval(this.refreshServerData, this.props.refreshInterval);
  }

  componentWillUnmount() {
    clearInterval(this.timerId);
  }

  refreshServerData = () => {
    Network.get("/rhn/manager/api/admin/runtime-status/data")
      .then((data) => {
        this.setState({
          serverData: data,
          error: null,
        });
      })
      .catch((response) => {
        this.setState({
          error: DEPRECATED_unsafeEquals(response.status, 401)
            ? "authentication"
            : response.status >= 500
              ? "general"
              : null,
        });
      });
  };

  sortByEndTime = (aRaw, bRaw, columnKey, sortDirection) => {
    if (DEPRECATED_unsafeEquals(aRaw[columnKey], null) || DEPRECATED_unsafeEquals(bRaw[columnKey], null)) {
      // reset the sortDirection because if 'endTime' is null it means that its status
      // it's 'running' so we want to keep it at the top of any other rows
      sortDirection = 1;
    }
    const a = aRaw[columnKey] || "0000-01-01T00:00:00.000Z";
    const b = bRaw[columnKey] || "0000-01-01T00:00:00.000Z";
    const result = a.toLowerCase().localeCompare(b.toLowerCase());
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  sortByStatus = (aRaw, bRaw, columnKey, sortDirection) => {
    const statusValues = { running: 0, ready_to_run: 1, failed: 2, interrupted: 3, skipped: 4, finished: 5 };
    const a = statusValues[aRaw[columnKey]];
    const b = statusValues[bRaw[columnKey]];
    const result = (a > b ? 1 : a < b ? -1 : 0) || this.sortByEndTime(aRaw, bRaw, "endTime", sortDirection);
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  sortByNumber = (aRaw, bRaw, columnKey, sortDirection) => {
    const a = aRaw[columnKey];
    const b = bRaw[columnKey];
    const result = a > b ? 1 : a < b ? -1 : 0;
    return (result || Utils.sortById(aRaw, bRaw)) * sortDirection;
  };

  searchData = (datum, criteria) => {
    if (criteria) {
      return datum.name.toLowerCase().includes(criteria.toLowerCase());
    }
    return true;
  };

  decodeStatus = (status) => {
    let cell;
    switch (status) {
      case "running":
        cell = (
          <div>
            <i className="fa fa-cog fa-spin"></i>
            {t(" running")}
          </div>
        );
        break;
      case "finished":
        cell = (
          <div className="text-success">
            <i className="fa fa-thumbs-o-up"></i>
            {t(" finished")}
          </div>
        );
        break;
      case "failed":
        cell = (
          <div className="text-danger">
            <i className="fa fa-exclamation-triangle"></i>
            {t(" failed")}
          </div>
        );
        break;
      case "interrupted":
        cell = (
          <div className="text-warning">
            <i className="fa fa-stop"></i>
            {t(" interrupted")}
          </div>
        );
        break;
      case "ready_to_run":
        cell = (
          <div className="text-primary">
            <i className="fa fa-list-ul"></i>
            {t(" ready to run")}
          </div>
        );
        break;
      case "skipped":
        cell = (
          <div>
            <i className="fa fa-angle-double-right"></i>
            {t(" skipped")}
          </div>
        );
        break;
      default:
        cell = null;
    }
    return cell;
  };

  buildRows = (jobs) => {
    return Object.keys(jobs).map((id) => jobs[id]);
  };

  render() {
    const data = this.state.serverData;
    const title = (
      <div className="spacewalk-toolbar-h1">
        <h1>
          <i className="fa fa-tachometer"></i>
          {t("Task Engine Status")}
          <HelpLink url="reference/admin/task-engine-status.html" />
        </h1>
      </div>
    );
    const headerTabs = (
      <div className="spacewalk-content-nav">
        <ul className="nav nav-tabs">
          <li>
            <a className="js-spa" href="/rhn/admin/TaskStatus.do">
              {t("Last Execution Times")}
            </a>
          </li>
          <li className="active js-spa">
            <a href="/rhn/manager/admin/runtime-status">{t("Runtime Status")}</a>
          </li>
        </ul>
      </div>
    );

    if (!DEPRECATED_unsafeEquals(data, null)) {
      if (Object.keys(data).length > 0) {
        return (
          <div key="taskotop-content">
            {title}
            {headerTabs}
            <ErrorMessage error={this.state.error} />
            <p>
              {t("The server is running or has finished executing the following tasks during the latest 5 minutes.")}
            </p>
            <p>
              {t("Data is refreshed every ")}
              {this.props.refreshInterval / 1000}
              {t(" seconds")}
            </p>
            <Table
              data={this.buildRows(data)}
              identifier={(row) => row["id"]}
              initialSortColumnKey="status"
              searchField={<SearchField filter={this.searchData} placeholder={t("Filter by name")} />}
            >
              <Column columnKey="id" comparator={Utils.sortById} header={t("Task Id")} cell={(row) => row["id"]} />
              <Column
                columnKey="name"
                comparator={Utils.sortByText}
                header={t("Task Name")}
                cell={(row) => row["name"]}
              />
              <Column
                columnKey="startTime"
                comparator={Utils.sortByText}
                header={t("Start Time")}
                cell={(row) => localizedMoment(row["startTime"]).toUserTimeString()}
              />
              <Column
                columnKey="endTime"
                comparator={this.sortByEndTime}
                header={t("End Time")}
                cell={(row) =>
                  DEPRECATED_unsafeEquals(row["endTime"], null)
                    ? ""
                    : localizedMoment(row["endTime"]).toUserTimeString()
                }
              />
              <Column
                columnKey="elapsedTime"
                comparator={this.sortByNumber}
                header={t("Elapsed Time")}
                cell={(row) =>
                  DEPRECATED_unsafeEquals(row["elapsedTime"], null) ? "" : row["elapsedTime"] + " seconds"
                }
              />
              <Column
                columnKey="status"
                comparator={this.sortByStatus}
                header={t("Status")}
                cell={(row) => this.decodeStatus(row["status"])}
              />
              <Column
                columnKey="data" // comparator={this.sortByText}
                header={t("Data")}
                cell={(row) => row["data"].map((c, index) => <div key={"data-" + index}>{c}</div>)}
              />
            </Table>
          </div>
        );
      } else {
        return (
          <div key="taskotop-no-content">
            {title}
            {headerTabs}
            <p>{t("There are no tasks running on the server at the moment.")}</p>
          </div>
        );
      }
    } else {
      return (
        <div key="taskotop-content">
          {title}
          {headerTabs}
        </div>
      );
    }
  }
}

const ErrorMessage = (props) => (
  <MessageContainer
    items={
      props.error === "authentication"
        ? MessagesUtils.warning(t("Session expired, please reload the page to see up-to-date data."))
        : props.error === "general"
          ? MessagesUtils.warning(t("Server error, please check log files."))
          : []
    }
  />
);

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<TaskoTop refreshInterval={5 * 1000} />, document.getElementById("taskotop"));
