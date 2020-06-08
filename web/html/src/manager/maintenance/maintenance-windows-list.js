/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const { InnerPanel } = require('components/panels/InnerPanel');
const Button = require("components/buttons").Button;
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const DeleteDialog = require("components/dialog/DeleteDialog").DeleteDialog;
const {Column} = require("components/table/Column");
const {Form} = require("components/input/Form");
const {Check} = require("components/input/Check");
const {Table} = require("components/table/Table");

class MaintenanceWindowsList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            type: props.type,
            itemToDelete: {}
        };
    }

    selectToDelete(item) {
        this.setState({
            itemToDelete: item
        });
    }

    setCheck = (model) => {
        model.strategy === "" && (model.strategy = false);
        this.setState(model);
    };

    addStrategy = () => {
        const item = this.state.itemToDelete;
        item.strategy = (this.state.strategy ? "Cancel" : "Fail");
        return item;
    };

    renderScheduleTable() {
        return <div>
            <Table
                data={this.props.data}
                identifier={row => row.scheduleId}
                initialItemsPerPage={userPrefPageSize}
                emptyText={t("No schedules created. Use Create to add a schedule.")}
            >
                <Column
                    columnKey="scheduleName"
                    header={t('Schedule Name')}
                    cell={(row) => row.scheduleName}
                />
                {/*TODO: Move styling to its own css file*/}
                <Column
                    columnClass="text-center"
                    headerClass="text-center"
                    columnKey="calendarName"
                    header={t('Calendar')}
                    cell={(row) =>
                        <a style={{
                            display: "inline-block",
                            padding: "2px 4px",
                            border: "1px solid #ccc",
                            margin: "0 3px",
                            borderRadius: "1px",
                            backgroundColor: "#eee",
                            color: "inherit"
                        }}
                           href={t("/rhn/manager/schedule/maintenance/calendars#/details/" + row.calendarId)}>
                            {row.calendarName}
                        </a>
                    }
                />
                <Column
                    columnClass="text-right"
                    headerClass="text-right"
                    header={t('Actions')}
                    cell={(row) =>
                        <div className="btn-group">
                            <Button
                                className="btn-default btn-sm"
                                title={t("Details")}
                                icon="fa-list"
                                handler={() => {this.props.onSelect(row.scheduleId)}}
                            />
                            <Button
                                className="btn-default btn-sm"
                                title={t("Edit")}
                                icon="fa-edit"
                                handler={() => {this.props.onEdit(row.scheduleId)}}
                            />
                            <ModalButton
                                className="btn-default btn-sm"
                                title={t("Delete")}
                                icon="fa-trash"
                                target="delete-modal"
                                item={row}
                                onClick={i => this.selectToDelete(i)}
                            />
                        </div>
                    }
                />
            </Table>
            <DeleteDialog id="delete-modal"
                          title={t("Delete maintenance schedule")}
                          content={t("Are you sure you want to delete the selected item? \n" +
                              "Deleting the schedule will unassign all systems from this schedule.")}
                          onConfirm={() => this.props.onDelete(this.state.itemToDelete)}
                          onClosePopUp={() => this.selectToDelete({})}
            />
        </div>
    }

    renderCalendarTable() {
        return <div>
            <Table
                data={this.props.data}
                identifier={row => row.calendarName}
                initialItemsPerPage={userPrefPageSize}
                emptyText={t("No calendars created. Use Create to add a calendar.")}
            >
                <Column
                    columnKey="calendarName"
                    header={t('Calendar Name')}
                    cell={(row) => row.calendarName}
                />
                <Column
                    columnClass="text-center"
                    headerClass="text-center"
                    columnKey="usedBySchedule"
                    header={t('Used by Schedule')}
                    cell={(row) => row.scheduleNames.map(name =>
                        <a style={{
                            display: "inline-block",
                            padding: "2px 4px",
                            border: "1px solid #ccc",
                            margin: "0 3px",
                            borderRadius: "1px",
                            backgroundColor: "#eee",
                            color: "inherit"
                        }}
                           href={"/rhn/manager/schedule/maintenance/schedules#/details/" + name.id}>
                            {name.name}
                        </a>
                    )}
                />
                <Column
                    columnClass="text-right"
                    headerClass="text-right"
                    header={t('Actions')}
                    cell={(row) =>
                        <div className="btn-group">
                            <Button
                                className="btn-default btn-sm"
                                title={t("Details")}
                                icon="fa-list"
                                handler={() => {this.props.onSelect(row.calendarId)}}
                            />
                            <Button
                                className="btn-default btn-sm"
                                title={t("Edit")}
                                icon="fa-edit"
                                handler={() => {this.props.onEdit(row.calendarId)}}
                            />
                            <ModalButton
                                className="btn-default btn-sm"
                                title={t("Delete")}
                                icon="fa-trash"
                                target="delete-modal"
                                item={row}
                                onClick={i => this.selectToDelete(i)}
                            />
                        </div>
                    }
                />
            </Table>
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
                          onClosePopUp={() => this.selectToDelete({})}
            />
        </div>
    }

    render() {
        const createButton = [
            <div className="btn-group pull-right">
                <Button
                    className="btn-default"
                    icon="fa-plus"
                    text={t("Create")}
                    title="Create a new maintenance schedule"
                    handler={() => this.props.onActionChanged("create")}
                />
            </div>
        ];

        return (
            <div>
                <InnerPanel title={t("Maintenance " +  (this.state.type === "schedule" ? "Schedules" : "Calendars"))} icon="spacewalk-icon-salt" buttons={this.props.disableCreate ? null : createButton}>
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <div>
                                <h3>{t(this.state.type === "schedule" ? "Schedules" : "Calendars")}</h3>
                            </div>
                        </div>
                        {this.state.type === "schedule" ? this.renderScheduleTable() : this.renderCalendarTable()}
                    </div>
                </InnerPanel>
            </div>
        );
    }
}

module.exports = {
    MaintenanceWindowsList: MaintenanceWindowsList
};
