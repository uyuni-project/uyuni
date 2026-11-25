/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { Component } from "react";

import _sortBy from "lodash/sortBy";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

import { localizedMoment } from "utils";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
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

  return <i className={iconClass} data-bs-toggle="tooltip" title={iconTitle} style={iconStyle} />;
}

export enum RecurringActionType {
  CUSTOMSTATE = "CUSTOMSTATE",
  HIGHSTATE = "HIGHSTATE",
  PLAYBOOK = "PLAYBOOK",
  SCAPPOLICY = "SCAPPOLICY",
  CLM_BUILD = "CLM_BUILD",
  CLM_PROMOTE = "CLM_PROMOTE",
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

class RecurringActionsDetails extends Component<RecurringActionsDetailsProps, RecurringActionsDetailsState> {
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
      .then(() => {
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
    if (DEPRECATED_unsafeEquals(details, null)) {
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
                <td>
                  {(details.active !== undefined ? details.active : data.active) ? t("Active") : <b>{t("Inactive")}</b>}
                </td>
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
                <td>
                  {targetNameLink(
                    data.targetName,
                    data.targetType,
                    data.targetId,
                    data.targetAccessible,
                    data.projectLabel ?? data.project_label ?? details.projectLabel
                  )}
                </td>
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
              {details.lastExecutionStatus && (
                <tr>
                  <td>{t("Last execution status")}:</td>
                  <td>
                    {details.lastExecutionStatus === "FINISHED" ? (
                      <span className="text-success">
                        <i className="fa fa-check-circle" /> {t("Success")}
                        {details.lastExecutionTime ? ` (${details.lastExecutionTime} ${window.timezone})` : ""}
                      </span>
                    ) : details.lastExecutionStatus === "FAILED" ? (
                      <span className="text-danger">
                        <i className="fa fa-times-circle" /> {t("Failed")}
                        {details.lastExecutionTime ? ` (${details.lastExecutionTime} ${window.timezone})` : ""}
                      </span>
                    ) : (
                      <span>{details.lastExecutionStatus}</span>
                    )}
                  </td>
                </tr>
              )}
              <tr>
                <td>{t("Next execution")}:</td>
                <td>
                  {(details.active !== undefined ? details.active : data.active) ? (
                    details.nextExecution ? (
                      `${details.nextExecution} ${window.timezone}`
                    ) : (
                      "-"
                    )
                  ) : (
                    <span className="text-muted">
                      {t("None (schedule is inactive)")}
                      {details.nextExecution ? ` - would be ${details.nextExecution} ${window.timezone}` : ""}
                    </span>
                  )}
                </td>
              </tr>
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

  showClmDetails() {
    const { details } = this.state;
    if (!details || !details.projectLabel) {
      return null;
    }
    const isBuild = this.props.data.actionType === RecurringActionType.CLM_BUILD;
    const projectUrl = `/rhn/manager/contentmanagement/project/${details.projectLabel}`;

    const rows: { label: string; value: any }[] = [
      {
        label: t("Content Project") + ":",
        value: (
          <a href={projectUrl}>
            {details.projectName || details.projectLabel} ({details.projectLabel})
          </a>
        ),
      },
    ];

    if (isBuild) {
      rows.push({
        label: t("Target Environment") + ":",
        value: details.environmentName ? `${details.environmentName} (${details.environmentLabel})` : "-",
      });
      rows.push({
        label: t("Deployed Version") + ":",
        value:
          typeof details.targetEnvironmentVersion === "number" && details.targetEnvironmentVersion > 0
            ? `v${details.targetEnvironmentVersion}`
            : t("not built"),
      });
      if (details.lastExecutionStatus === "FAILED") {
        rows.push({
          label: t("Build Status") + ":",
          value: (
            <span className="label label-danger">
              <i className="fa fa-times" /> {t("Last build failed")}
            </span>
          ),
        });
      }
    } else {
      rows.push({
        label: t("Source Environment") + ":",
        value: (
          <>
            {details.environmentName ? `${details.environmentName} (${details.environmentLabel}) ` : "-"}
            {typeof details.sourceEnvironmentVersion === "number" && details.sourceEnvironmentVersion > 0 && (
              <span className="text-muted">(v{details.sourceEnvironmentVersion})</span>
            )}
          </>
        ),
      });
      rows.push({
        label: t("Target Environment") + ":",
        value: (
          <>
            {details.nextEnvironmentName ? `${details.nextEnvironmentName} (${details.nextEnvironmentLabel}) ` : "-"}
            {typeof details.targetEnvironmentVersion === "number" && details.targetEnvironmentVersion > 0 && (
              <span className="text-muted">(v{details.targetEnvironmentVersion})</span>
            )}
          </>
        ),
      });

      let statusContent: any;
      if (details.lastExecutionStatus === "FAILED") {
        statusContent = (
          <span className="label label-danger">
            <i className="fa fa-times" /> {t("Last promotion failed")}
          </span>
        );
      } else if (
        typeof details.sourceEnvironmentVersion === "number" &&
        details.sourceEnvironmentVersion > 0 &&
        typeof details.targetEnvironmentVersion === "number"
      ) {
        if (details.sourceEnvironmentVersion === details.targetEnvironmentVersion) {
          statusContent = (
            <span className="text-success">
              <i className="fa fa-check" /> {t("In sync (version {v})", { v: details.targetEnvironmentVersion })}
            </span>
          );
        } else if (details.sourceEnvironmentVersion > details.targetEnvironmentVersion) {
          statusContent = (
            <span className="text-info">
              <i className="fa fa-arrow-circle-right" />{" "}
              {t("Ready to promote ({count} version(s) ahead)", {
                count: details.sourceEnvironmentVersion - details.targetEnvironmentVersion,
              })}
            </span>
          );
        } else {
          statusContent = t("Up to date");
        }
      } else {
        statusContent = t("Not built yet");
      }

      rows.push({
        label: t("Status") + ":",
        value: statusContent,
      });

      if (details.targetEnvironmentModified) {
        rows.push({
          label: t("Last Promoted") + ":",
          value: `${details.targetEnvironmentModified} ${window.timezone}`,
        });
      }
    }

    return (
      <BootstrapPanel title={t("Content Lifecycle Details")}>
        <Table data={rows} identifier={(row) => row.label} hideHeaderFooter="both">
          <Column columnKey="label" cell={(row) => row.label} />
          <Column columnKey="value" cell={(row) => row.value} />
        </Table>
      </BootstrapPanel>
    );
  }

  showClmBuildHistory() {
    const { details } = this.state;
    if (!details?.buildHistory?.length) {
      return null;
    }

    return (
      <div className="row">
        <h3>{t("Recent project builds for {name}", { name: this.props.data.targetName })}</h3>
        <Table
          identifier={(item) => item.version}
          selectable={false}
          data={details.buildHistory}
          hideHeaderFooter="both"
        >
          <Column
            header={t("Version")}
            columnKey="version"
            cell={(row) => (
              <>
                <strong>v{row.version}</strong>
                {row.current && <span className="label label-info">{t("Current")}</span>}
              </>
            )}
          />
          <Column header={t("Build Date")} columnKey="created" cell={(row) => `${row.created} ${window.timezone}`} />
          <Column
            header={t("Trigger Type")}
            columnKey="automated"
            cell={(row) =>
              row.automated ? (
                <span className="label label-primary">
                  <i className="fa fa-clock-o" /> {t("Automated")}
                </span>
              ) : (
                <span className="label label-default">
                  <i className="fa fa-user" /> {t("Manual")}
                </span>
              )
            }
          />
          <Column header={t("User")} columnKey="userName" cell={(row) => row.userName || "-"} />
          <Column
            header={t("Message")}
            columnKey="message"
            cell={(row) => <span style={{ whiteSpace: "pre-wrap" }}>{row.message || "-"}</span>}
          />
        </Table>
      </div>
    );
  }

  showPlaybookDetails() {
    return (
      <BootstrapPanel title={t("Playbook details")}>
        <div className="table-responsive">
          <table className="table">
            <tbody>
              <tr>
                <td>{t("Playbook path")}:</td>
                <td>{this.state.details.playbookPath}</td>
              </tr>
              <tr>
                <td>{t("Inventory path")}:</td>
                <td>{this.state.details.inventoryPath ? this.state.details.inventoryPath : "-"}</td>
              </tr>
              <tr>
                <td>{t("Flush fact cache")}:</td>
                <td>{this.state.details.flushCache ? t("true") : t("false")}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </BootstrapPanel>
    );
  }

  render() {
    const isClm =
      window.entityType === "CLM" ||
      this.props.data.actionType === RecurringActionType.CLM_BUILD ||
      this.props.data.actionType === RecurringActionType.CLM_PROMOTE;

    const buttons = [
      <div className="btn-group pull-right" key="buttons-right">
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
          handler={() =>
            this.props.onEdit({
              ...this.props.data,
              projectLabel: this.props.data.projectLabel ?? this.state.details?.projectLabel,
              environmentLabel: this.props.data.environmentLabel ?? this.state.details?.environmentLabel,
            })
          }
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
      <TopPanel
        title={this.props.data.scheduleName}
        icon={isClm ? "spacewalk-icon-lifecycle" : "spacewalk-icon-salt"}
        helpUrl=""
        button={buttons}
      >
        {this.state.messages ? <Messages items={this.state.messages} /> : null}
        {this.state.details?.lastExecutionStatus === "FAILED" && (
          <div className="alert alert-danger" role="alert">
            <i className="fa fa-exclamation-triangle" />{" "}
            {t(
              "The last execution of this recurring schedule failed{time}. Please check Taskomatic logs for details.",
              {
                time: this.state.details.lastExecutionTime
                  ? ` on ${this.state.details.lastExecutionTime} ${window.timezone}`
                  : "",
              }
            )}
          </div>
        )}
        {this.showScheduleDetails(this.props.data)}
        {isClm ? this.showClmDetails() : null}
        {this.props.data.actionType === RecurringActionType.CLM_BUILD ? this.showClmBuildHistory() : null}
        <DeleteDialog
          id="delete-modal"
          title={t("Delete Recurring Action Schedule")}
          content={<span>{t("Are you sure you want to delete this schedule?")}</span>}
          onConfirm={() => this.deleteSchedule(this.props.data)}
        />
        {window.entityType === "NONE" || this.props.data.actionType !== RecurringActionType.HIGHSTATE ? null : (
          <DisplayHighstate minions={this.state.minions} />
        )}
        {!(this.props.data.actionType === RecurringActionType.CUSTOMSTATE && this.state.details) ? null : (
          <div className="row">
            <h3>{t("State configuration for {name}", { name: this.props.data.targetName })}</h3>
            <Table
              identifier={(item) => item.position}
              selectable={false}
              data={_sortBy(this.state.details.states, "position")}
              hideHeaderFooter="both"
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
                cell={(row) => (
                  <i className="fa fa-info-circle fa-1-5x" data-bs-toggle="tooltip" title={row.description} />
                )}
              />
            </Table>
          </div>
        )}
        {!(this.props.data.actionType === RecurringActionType.SCAPPOLICY && this.state.details?.policies) ? null : (
          <div className="row">
            <h3>{t("SCAP Policy for {name}", { name: this.props.data.targetName })}</h3>
            <Table
              identifier={(item) => item.id}
              selectable={false}
              data={this.state.details.policies}
              hideHeaderFooter="both"
            >
              <Column
                header={t("Policy Name")}
                columnKey="policyName"
                cell={(row) => <a href={`/rhn/manager/audit/scap/policy/details/${row.id}`}>{row.policyName}</a>}
              />
              <Column header={t("Data Stream")} columnKey="dataStreamName" cell={(row) => row.dataStreamName} />
              <Column header={t("XCCDF Profile")} columnKey="xccdfProfileId" cell={(row) => row.xccdfProfileId} />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                header={t("Description")}
                columnKey="description"
                cell={(row) => (
                  <i
                    className="fa fa-info-circle fa-1-5x"
                    data-bs-toggle="tooltip"
                    title={row.description || t("No description")}
                  />
                )}
              />
            </Table>
          </div>
        )}
        {!(this.props.data.actionType === RecurringActionType.PLAYBOOK && this.state.details)
          ? null
          : this.showPlaybookDetails()}
      </TopPanel>
    );
  }
}

export { RecurringActionsDetails };
