/* eslint-disable */
'use strict';

const React = require("react");
const {Button} = require("components/buttons");
const {ModalButton} = require("components/dialog/ModalButton");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const {TopPanel} = require('components/panels/TopPanel');
const {Messages} = require("components/messages");
const {Form} = require("components/input/Form");
const {Check} = require("components/input/Check");
const {BootstrapPanel} = require('components/panels/BootstrapPanel');
const {Table} = require("components/table/Table");
const {Column} = require("components/table/Column");

class MaintenanceWindowsDetails extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            type: props.type,
            calendarData: this.props.data.calendarData,
            messages: [],
        };
    }

    setCheck = (model) => {
        /* strategy gets initialized as empty string, but we want the initial value to be false.
        * Is equivalent to: if strategy is "" then set it to false */
        model.strategy === "" && (model.strategy = false);
        this.setState(model);
    };

    addStrategy = () => {
        const item = this.props.data;
        item.strategy = this.state.strategy ? "Cancel" : "Fail";
        return item;
    };

    renderScheduleDetails(data) {
        const tableData = [
            {left: t("Schedule Name:"), right: data.scheduleName},
            {left: t("Assigned Calendar:"), right: data.calendarName},
            {left: t("Schedule Type:"), right: data.scheduleType === "SINGLE" ? t("Single") : t("Multi")},
        ];

        return (
            <div>
                <BootstrapPanel title={t("Schedule Details")}>
                    <Table
                        data={tableData}
                        identifier={row => tableData.indexOf(row)}
                        initialItemsPerPage={0}
                    >
                        <Column
                            columnKey="left"
                            cell={(row) => row.left}
                        />
                        <Column
                            columnKey="right"
                            cell={(row) => row.right}
                        />
                    </Table>
                </BootstrapPanel>
                {
                    data.maintenanceWindows !== undefined && data.maintenanceWindows.length > 0 &&
                    <BootstrapPanel title={t("Upcoming Maintenance Windows")}>
                        <Table
                            data={data.maintenanceWindows}
                            identifier={row => data.maintenanceWindows.indexOf(row)}
                            initialItemsPerPage={0}
                        >
                            <Column
                                header={t("Start")}
                                columnKey="start"
                                cell={(row) => row.start}
                            />
                            <Column
                                header={t("End")}
                                columnKey="end"
                                cell={(row) => row.end}
                            />
                        </Table>
                    </BootstrapPanel>
                }
                <DeleteDialog id="delete-modal"
                              title={t("Delete maintenance schedule")}
                              content={t("Are you sure you want to delete the selected item? \n" +
                                  "This will remove the current schedule from all the systems assigned to it.")}
                              onConfirm={() => this.props.onDelete(this.props.data)}
                />
            </div>
        );
    }

    renderCalendarDetails(data) {
        const tableData = [
            {left: t("Calendar Name:"), right: data.calendarName},
            {left: t("Used by Schedule:"), right: data.scheduleNames.map(name => name.name).join(", ")},
        ];
        data.calendarUrl && tableData.push({left: "Url:", right: data.calendarUrl});

        return (
            <div>
                <BootstrapPanel title={t("Schedule Details")}>
                    <Table
                        data={tableData}
                        identifier={row => tableData.indexOf(row)}
                        initialItemsPerPage={0}
                    >
                        <Column
                            columnKey="left"
                            cell={(row) => row.left}
                        />
                        <Column
                            columnKey="right"
                            cell={(row) => row.right}
                        />
                    </Table>
                </BootstrapPanel>
                {
                    this.props.data.calendarData &&
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
                <DeleteDialog id="delete-modal"
                              title={t("Delete maintenance calendar")}
                              content={
                                  <Form model={{strategy: this.state.strategy}} onChange={this.setCheck}>
                                      <div>{t("Are you sure you want to delete the selected item?")}</div>
                                      <div>{t("This will remove the current schedule from all the systems assigned to it.")}</div>
                                      <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
                                  </Form>
                              }
                              onConfirm={() => this.props.onDelete(this.addStrategy())}
                />
            </div>
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
                    disabled={!isAdmin}
                    icon="fa-edit"
                    title={t("Edit")}
                    className="btn-default"
                    handler={() => this.props.onEdit(
                        this.state.type === "schedule" ? this.props.data.scheduleId : this.props.data.calendarId
                    )}
                />
                <ModalButton
                    text={t("Delete")}
                    disabled={!isAdmin}
                    icon="fa-trash"
                    title={t("Delete")}
                    target="delete-modal"
                    className="btn-default"
                />
            </div>
        ];

        return (
            <TopPanel
                title={this.props.type === "schedule"
                    ? this.props.data.scheduleName
                    : this.props.data.calendarName
                }
                icon="spacewalk-icon-salt"
                helpUrl=""
                button={buttons}
            >
                <Messages items={this.state.messages}/>
                {
                    this.state.type === "schedule"
                        ? this.renderScheduleDetails(this.props.data)
                        : this.renderCalendarDetails(this.props.data)
                }
            </TopPanel>
        );
    }
}

module.exports = {
    MaintenanceWindowsDetails: MaintenanceWindowsDetails
};
