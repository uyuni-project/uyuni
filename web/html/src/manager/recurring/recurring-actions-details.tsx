import * as React from "react";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages } from "components/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { TopPanel } from "components/panels/TopPanel";

import Network from "utils/network";

import { DisplayHighstate } from "../state/display-highstate";
import { targetNameLink, targetTypeToString } from "./recurring-actions-utils";

type RecurringActionsDetailsProps = {
  data?: any;
  minions?: any;
  onCancel: (arg0: string) => any;
  onEdit: (arg0: any) => any;
  onDelete: (arg0: any) => any;
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
      .catch((e) => console.log(e));
  }

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
              <b>{details.cronTimes.hour + ":" + details.cronTimes.minute}</b>
            </td>
          ) : details.type === "weekly" ? (
            <td>
              {"Every "}
              <b>{this.weekDays[details.cronTimes.dayOfWeek - 1]}</b>
              {" at "}
              <b>{details.cronTimes.hour + ":" + details.cronTimes.minute}</b>
            </td>
          ) : (
            <td>
              {"Every "}
              <b>
                {details.cronTimes.dayOfMonth +
                  (details.cronTimes.dayOfMonth === "1"
                    ? "st "
                    : details.cronTimes.dayOfMonth === "2"
                    ? "nd "
                    : details.cronTimes.dayOfMonth === "3"
                    ? "rd "
                    : "th ")}
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
              <tr>
                <td>{t("Target name")}:</td>
                <td>{targetNameLink(data.targetName, data.targetType, data.targetId)}</td>
              </tr>
              <tr>
                <td>{t("Created by")}:</td>
                <td>{t(details.creatorLogin)}</td>
              </tr>
              {
                <tr>
                  <td>{t("Created at")}:</td>
                  <td>{details.createdAt + " " + window.timezone}</td>
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
          title={t("Delete Recurring Action Schedule")}
          content={<span>{t("Are you sure you want to delete this schedule?")}</span>}
          onConfirm={() => this.props.onDelete(this.props.data)}
        />
        {window.entityType === "NONE" ? null : <DisplayHighstate minions={this.state.minions} />}
      </TopPanel>
    );
  }
}

export { RecurringActionsDetails };
