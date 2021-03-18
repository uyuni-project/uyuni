import * as React from "react";
import { Button } from "components/buttons";
import { ModalButton } from "components/dialog/ModalButton";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { TopPanel } from "components/panels/TopPanel";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { Messages } from "components/messages";
import { DisplayHighstate } from "./display-highstate";

// todo extract to utils
const targetTypeToString = targetType => {
  switch (targetType) {
    case "MINION":
      return t("Minion");
    case "GROUP":
      return t("Group");
    case "ORG":
      return t("Organization");
  }
  return null;
};

type RecurringStatesDetailsProps = {
  data?: any;
  minions?: any;
  onCancel: (arg0: string) => any;
  onEdit: (arg0: any) => any;
  onDelete: (arg0: any) => any;
};

type RecurringStatesDetailsState = {
  messages: any[];
  minions: any;
};

class RecurringStatesDetails extends React.Component<RecurringStatesDetailsProps, RecurringStatesDetailsState> {
  weekDays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

  constructor(props: RecurringStatesDetailsProps) {
    super(props);

    this.state = {
      messages: [],
      minions: props.minions,
    };
  }

  getExecutionText(data) {
    if (data.type !== "cron") {
      return (
        <tr>
          <td>{t("Execution time")}:</td>
          {data.type === "hourly" ? (
            <td>
              {"Every hour at minute "}
              <b>{data.cronTimes.minute}</b>
            </td>
          ) : data.type === "daily" ? (
            <td>
              {"Every day at "}
              <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
            </td>
          ) : data.type === "weekly" ? (
            <td>
              {"Every "}
              <b>{this.weekDays[data.cronTimes.dayOfWeek - 1]}</b>
              {" at "}
              <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
            </td>
          ) : (
            <td>
              {"Every "}
              <b>
                {data.cronTimes.dayOfMonth +
                  (data.cronTimes.dayOfMonth === "1"
                    ? "st "
                    : data.cronTimes.dayOfMonth === "2"
                    ? "nd "
                    : data.cronTimes.dayOfMonth === "3"
                    ? "rd "
                    : "th ")}
              </b>
              {"of the month at "}
              <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
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
    data.cronTimes.hour = data.cronTimes.hour.padStart(2, "0");
    data.cronTimes.minute = data.cronTimes.minute.padStart(2, "0");

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
                <td>{t("Target type")}:</td>
                <td>{targetTypeToString(data.targetType)}</td>
              </tr>
              {window.entityType === "NONE" && data.targetType === "ORG" && (
                <tr>
                  <td>{t("Organization name")}:</td>
                  <td>{data.orgName}</td>
                </tr>
              )}
              <tr>
                <td>{t("Created by")}:</td>
                <td>{t(data.creatorLogin)}</td>
              </tr>
              {
                <tr>
                  <td>{t("Created at")}:</td>
                  <td>{data.createdAt + " " + window.timezone}</td>
                </tr>
              }
              {this.getExecutionText(data)}
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
          title={t("Edit")}
          className="btn-default"
          handler={() => this.props.onEdit(this.props.data)}
        />
        <ModalButton
          text={t("Delete")}
          icon="fa-trash"
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
          title={t("Delete Recurring State Schedule")}
          content={<span>{t("Are you sure you want to delete this schedule?")}</span>}
          onConfirm={() => this.props.onDelete(this.props.data)}
        />
        {window.entityType === "NONE" ? null : <DisplayHighstate minions={this.state.minions} />}
      </TopPanel>
    );
  }
}

export { RecurringStatesDetails };
