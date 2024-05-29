import * as React from "react";

import _sortBy from "lodash/sortBy";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, Utils as MessagesUtils } from "components/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { localizedMoment } from "utils";
import Network from "utils/network";

import { DisplayHighstate } from "../state/display-highstate";
import { isReadOnly, targetNameLink, targetTypeToString } from "./recurring-actions-utils";

function channelIcon(channel) {
  let iconClass, iconTitle, iconStyle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else if (channel.type === "internal_state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("Internal State");
    iconStyle = { border: "1px solid black" };
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} style={iconStyle} />;
}

type RecurringActionsDetailsProps = {
  data?: any;
  minions?: any;
  onCancel: (arg0: string) => any;
  onEdit: (arg0: any) => any;
  onError: (arg0: any) => any;
  onDeleteError: (arg0: any) => any;
  onSetMessages: (arg0: any) => any;
};

type RecurringActionsDetailsState = {
  messages: any[];
  minions: any;
  details: any;
};

class RecurringActionsDetails extends React.Component<RecurringActionsDetailsProps, RecurringActionsDetailsState> {
  weekDays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

  constructor(props: RecurringActionsDetailsProps) {
    super(props);

    this.state = {
      messages: [],
      minions: props.minions,
      details: null,
    };
  }

  componentDidMount(): void {
    Network.get(`/rhn/manager/api/recurringactions/${this.props.data.recurringActionId}/details`)
      .then((details) => {
        this.setState({
          details,
        });
      })
      .catch(this.props.onError);
  }

  deleteSchedule = (item) => {
    return Network.del("/rhn/manager/api/recurringactions/" + item.recurringActionId + "/delete")
      .then((_) => {
        this.props.onSetMessages(MessagesUtils.info("Schedule '" + item.scheduleName + "' has been deleted."));
        this.props.onCancel("back");
      })
      .catch(this.props.onDeleteError);
  };

  getExecutionText(details) {
    if (details.type !== "cron") {
      return (
        <tr>
          <td>{t("Execution time")}:</td>
          {details.type === "hourly" ? (
            <td>
              {"Every hour at minute "}
              <b>{details.cronTimes.minute}</b>
            </td>
          ) : details.type === "daily" ? (
            <td>
              {"Every day at "}
              <b>{`${details.cronTimes.hour}:${details.cronTimes.minute} `}</b>
              {localizedMoment.serverTimeZoneAbbr}
            </td>
          ) : details.type === "weekly" ? (
            <td>
              {"Every "}
              <b>{this.weekDays[details.cronTimes.dayOfWeek - 1]}</b>
              {" at "}
              <b>{`${details.cronTimes.hour}:${details.cronTimes.minute} `}</b>
              {localizedMoment.serverTimeZoneAbbr}
            </td>
          ) : (
            <td>
              {"Every "}
              <b>
                {
                  // TODO: Refactor this when https://github.com/SUSE/spacewalk/issues/20449 is implemented
                  details.cronTimes.dayOfMonth +
                    (details.cronTimes.dayOfMonth === "1"
                      ? "st "
                      : details.cronTimes.dayOfMonth === "2"
                      ? "nd "
                      : details.cronTimes.dayOfMonth === "3"
                      ? "rd "
                      : "th ")
                }
              </b>
              {"of the month at "}
              <b>{details.cronTimes.hour + ":" + details.cronTimes.minute}</b>
            </td>
          )}
        </tr>
      );
    } else {
      return (
        <tr>
          <td>{"Type"}:</td>
          <td>{"Custom Quartz string"}</td>
        </tr>
      );
    }
  }

  showScheduleDetails(data) {
    const { details } = this.state;
    if (details == null) {
      return false;
    }
    details.cronTimes.hour = details.cronTimes.hour.padStart(2, "0");
    details.cronTimes.minute = details.cronTimes.minute.padStart(2, "0");

    // Creation time is always retrieved in server's timezone
    // Parse the date and show it in user's timezone
    const createdAt = localizedMoment.fromServerDateTimeString(details.createdAt).toUserDateTimeString();

    return (
      <BootstrapPanel title={t("Schedule Details")}>
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <td>{t("State:")}</td>
                <td>{data.active ? t("Active") : <b>{t("Inactive")}</b>}</td>
              </tr>
              {data.test === "true" && (
                <tr>
                  <td>{t("Test")}:</td>
                  <td>{t("True")}</td>
                </tr>
              )}
              <tr>
                <td>{t("Action type")}:</td>
                <td>{data.actionTypeDescription}</td>
              </tr>
              <tr>
                <td>{t("Target type")}:</td>
                <td>{targetTypeToString(data.targetType)}</td>
              </tr>
              <tr>
                <td>{t("Target name")}:</td>
                <td>{targetNameLink(data.targetName, data.targetType, data.targetId, data.targetAccessible)}</td>
              </tr>
              <tr>
                <td>{t("Created by")}:</td>
                <td>{t(details.creatorLogin)}</td>
              </tr>
              {
                <tr>
                  <td>{t("Created at")}:</td>
                  <td>{createdAt + " " + localizedMoment.userTimeZoneAbbr}</td>
                </tr>
              }
              {this.getExecutionText(details)}
              <tr>
                <td>{t("Quartz format string")}:</td>
                <td>{data.cron}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );
  }

  render() {
    const buttons = [
      <div className="btn-group pull-right">
        <Button
          text={t("Back")}
          icon="fa-chevron-left"
          title={t("Back")}
          className="btn-default"
          handler={() => this.props.onCancel("back")}
        />
        <Button
          text={t("Edit")}
          icon="fa-edit"
          disabled={isReadOnly(this.props.data)}
          title={t("Edit")}
          className="btn-default"
          handler={() => this.props.onEdit(this.props.data)}
        />
        <ModalButton
          text={t("Delete")}
          icon="fa-trash"
          disabled={isReadOnly(this.props.data)}
          title={t("Delete")}
          target="delete-modal"
          className="btn-default"
        />
      </div>,
    ];

    return (
      <TopPanel title={this.props.data.scheduleName} icon="spacewalk-icon-salt" helpUrl="" button={buttons}>
        {this.state.messages ? <Messages items={this.state.messages} /> : null}
        {this.showScheduleDetails(this.props.data)}
        <DeleteDialog
          id="delete-modal"
          title={t("Delete Recurring Action Schedule")}
          content={<span>{t("Are you sure you want to delete this schedule?")}</span>}
          onConfirm={() => this.deleteSchedule(this.props.data)}
        />
        {window.entityType === "NONE" || this.props.data.actionType !== "HIGHSTATE" ? null : (
          <DisplayHighstate minions={this.state.minions} />
        )}
        {!(this.props.data.actionType === "CUSTOMSTATE" && this.state.details) ? null : (
          <div className="row">
            <h3>{t("State configuration for {name}", { name: this.props.data.targetName })}</h3>
            <Table
              identifier={(item) => item.position}
              selectable={false}
              data={_sortBy(this.state.details.states, "position")}
              initialItemsPerPage={0}
            >
              <Column header={t("Order")} columnKey="position" cell={(row) => row.position} />
              <Column
                header={t("State Name")}
                columnKey="name"
                cell={(row) => (
                  <>
                    {channelIcon(row)}{" "}
                    {row.type !== "internal_state" ? (
                      <a href={"/rhn/configuration/ChannelOverview.do?ccid=" + row.id}>{row.name}</a>
                    ) : (
                      row.name
                    )}
                  </>
                )}
              />
              <Column header={t("State Label")} columnKey="typeName" cell={(row) => row.label} />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                header={t("Description")}
                columnKey="description"
                cell={(row) => <i className="fa fa-info-circle fa-1-5x text-primary" title={row.description} />}
              />
            </Table>
          </div>
        )}
      </TopPanel>
    );
  }
}

export { RecurringActionsDetails };
