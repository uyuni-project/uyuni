/* eslint-disable */
'use strict';

const React = require("react");
const {Button} = require("components/buttons");
const {ModalButton} = require("components/dialog/ModalButton");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const { TopPanel } = require('components/panels/TopPanel');
const { BootstrapPanel } = require('components/panels/BootstrapPanel');
const {Messages} = require("components/messages");
const {DisplayHighstate} = require("./display-highstate");

class RecurringStatesDetails extends React.Component {

    weekDays = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];

    constructor(props) {
        super(props);

        this.state = {
            messages: [],
            minions: props.minions
        };
    }

    getExecutionText(data) {
        if (data.type !== "cron") {
            return (
                <tr>
                    <td>{t("Execution time")}:</td>
                    {data.type === "daily" ?
                        <td>
                            {"Every day at "}
                            <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
                        </td>
                        : data.type === "weekly" ?
                            <td>
                                {"Every "}
                                <b>{this.weekDays[data.cronTimes.dayOfWeek - 1]}</b>
                                {" at "}
                                <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
                            </td> :
                            <td>
                                {"Every "}
                                <b>
                                    {data.cronTimes.dayOfMonth + (
                                        data.cronTimes.dayOfMonth === "1" ? "st "
                                            : data.cronTimes.dayOfMonth === "2" ? "nd "
                                            : data.cronTimes.dayOfMonth === "3" ? "rd "
                                                : "th ")}
                                </b>
                                {"of the month at "}
                                <b>{data.cronTimes.hour + ":" + data.cronTimes.minute}</b>
                            </td>
                    }
                </tr>
            );
        } else {
            /* TODO: Get execution text from custom cron string */
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
                            <td>{data.active === "true" ? t("active") : <b>{t("disabled")}</b>}</td>
                        </tr>
                        { data.test === "true" &&
                        <tr>
                            <td>{t("Test")}:</td>
                            <td>{t("True")}</td>
                        </tr>
                        }
                        <tr>
                            <td>{t("Target type")}:</td>
                            <td>{data.targetType}</td>
                        </tr>
                        { data.targetType === "Group" &&
                        <tr>
                            <td>{t("Group name")}:</td>
                            <td>{data.groupName}</td>
                        </tr>
                        }
                        { /* data.minionNames.length < 20 &&
                        <tr>
                            <td>{"System name" + (data.minionNames.length > 1 ? "s" : "")}:</td>
                            <td>{data.minionNames.join(", ")}</td>
                        </tr>
                        */ }
                        {<tr>
                            <td>{t("Created at")}:</td>
                            <td>{data.createdAt + " " + timezone}</td>
                        </tr> }
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
            </div>
        ];

        return (
            <TopPanel
                title={this.props.data.scheduleName}
                icon="spacewalk-icon-salt"
                helpUrl=""
                button={buttons}
            >
                { this.state.messages ?
                    <Messages items={this.state.messages}/> : null }
                {this.showScheduleDetails(this.props.data)}
                <DeleteDialog id="delete-modal"
                              title={t("Delete Recurring State Schedule")}
                              content={<span>{t("Are you sure you want to delete this schedule?")}</span>}
                              onConfirm={() => this.props.onDelete(this.props.data)}
                />
                <DisplayHighstate minions={this.state.minions}/>
            </TopPanel>
        );
    }
}

module.exports = {
    RecurringStatesDetails: RecurringStatesDetails
};
