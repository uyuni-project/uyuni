/* eslint-disable */
'use strict';

const React = require("react");
const {Button} = require("components/buttons");
const {ModalButton} = require("components/dialog/ModalButton");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const { TopPanel } = require('components/panels/TopPanel');
const {Messages} = require("components/messages");
const {Form} = require("components/input/Form");
const {Check} = require("components/input/Check");
const { BootstrapPanel } = require('components/panels/BootstrapPanel');

const scheduleTypeToString = (type) => {
    switch (type) {
        case "MULTI":
            return "Multi";
        case "SINGLE":
            return "Single";
    }
    return null;
}

class MaintenanceWindowsDetails extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            calendarData: this.props.data.calendarData,
            messages: [],
        };
    }

    setCheck = (model) => {
        model.strategy === "" && (model.strategy = false);
        this.setState(model);
    };

    addStrategy = () => {
        const item = this.props.data;
        item.strategy = (this.state.strategy ? "Cancel" : "Fail");
        return item;
    };

    renderScheduleDetails(data) {
        return (
            <BootstrapPanel title={t("Schedule Details")}>
                <div className="table-responsive">
                    <table className="table">
                        <tbody>
                        <tr>
                            <td>{t("Schedule Name")}</td>
                            <td>{t(data.scheduleName)}</td>
                        </tr>
                        <tr>
                            <td>{t("Assigned Calendar")}:</td>
                            <td>{t(data.calendarName)}</td>
                        </tr>
                        <tr>
                            <td>{t("Schedule Type")}:</td>
                            <td>{t(scheduleTypeToString(data.scheduleType))}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>
                <DeleteDialog id="delete-modal"
                              title={t("Delete maintenance schedule")}
                              content={t("Are you sure you want to delete the selected item? \n" +
                                  "Deleting the schedule will unassign all systems from this schedule.")}
                              onConfirm={() => this.props.onDelete(this.props.data)}
                />
            </BootstrapPanel>
        );
    }

    renderCalendarDetails(data) {
        return (
            <BootstrapPanel title={t("Schedule Details")}>
                <div className="table-responsive">
                    <table className="table">
                        <tbody>
                        <tr>
                            <td>{t("Calendar Name")}</td>
                            <td>{t(data.calendarName)}</td>
                        </tr>
                        <tr>
                            <td>{t("Used by schedule")}:</td>
                            <td>{t(data.scheduleNames)}</td>
                        </tr>
                        {data.calendarUrl &&
                        <tr>
                            <td>{t("Url")}:</td>
                            <td>{t(data.calendarUrl)}</td>
                        </tr>
                        }
                        </tbody>
                    </table>
                </div>
                <DeleteDialog id="delete-modal"
                              title={t("Delete maintenance calendar")}
                              content={
                                  <Form model={{strategy: this.state.strategy}} onChange={this.setCheck}>
                                      <div>{t("Are you sure you want to delete the selected item?")}</div>
                                      <div>{t("Deleting this calendar will unassign all schedules from this calendar.")}</div>
                                      <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
                                  </Form>
                              }
                              onConfirm={() => this.props.onDelete(this.addStrategy())}
                />
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
                {type === "schedule" ? this.renderScheduleDetails(this.props.data)
                    : this.renderCalendarDetails(this.props.data)}
                {this.props.data.calendarData &&
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>
                            {this.props.data.calendarName}
                        </h4>
                    </div>
                    <div className="panel-body">
                        <pre>
                            {this.props.data.calendarData}
                        </pre>
                    </div>
                </div>
                }

            </TopPanel>
        );
    }
}

module.exports = {
    MaintenanceWindowsDetails: MaintenanceWindowsDetails
};
