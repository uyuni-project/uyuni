/* eslint-disable */
'use strict';

const React = require("react");
const {Button} = require("components/buttons");
const {ModalButton} = require("components/dialog/ModalButton");
const {DeleteDialog} = require("components/dialog/DeleteDialog");
const { TopPanel } = require('components/panels/TopPanel');
const {Messages} = require("components/messages");
const { BootstrapPanel } = require('components/panels/BootstrapPanel');

class MaintenanceWindowsDetails extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            messages: [],
        };
    }

    showScheduleDetails(data) {

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
                            <td>{t("Url")}:</td>
                            <td>{t(data.scheduleUrl)}</td>
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
                              title={t("Delete maintenance schedule")}
                              content={t("Are you sure you want to delete the selected item? \n" +
                                  "Deleting the schedule will unassign all systems from this schedule.")}
                              onConfirm={() => this.props.onDelete(this.props.data)}
                />
            </TopPanel>
        );
    }
}

module.exports = {
    MaintenanceWindowsDetails: MaintenanceWindowsDetails
};
