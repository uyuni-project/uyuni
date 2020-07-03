/* eslint-disable */
'use strict';

const React = require("react");
const {Button} = require("components/buttons");
const {ModalButton} = require("components/dialog/ModalButton");
const {TopPanel} = require('components/panels/TopPanel');
const {Messages} = require("components/messages");

import MaintenanceScheduleDetails from "./schedule-details";
import MaintenanceCalendarDetails from "./calendar-details";

class MaintenanceWindowsDetails extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            type: props.type,
            messages: [],
        };
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
                icon="spacewalk-icon-schedule"
                helpUrl=""
                button={buttons}
            >
                <Messages items={this.state.messages}/>
                {
                    this.state.type === "schedule" &&
                    <MaintenanceScheduleDetails
                        scheduleName={this.props.data.scheduleName}
                        calendarName={this.props.data.calendarName}
                        scheduleType={this.props.data.scheduleType}
                        maintenanceWindows={this.props.data.maintenanceWindows}
                        onDelete={this.props.onDelete}
                    /> ||
                    this.state.type === "calendar" &&
                    <MaintenanceCalendarDetails
                        calendarName={this.props.data.calendarName}
                        scheduleNames={this.props.data.scheduleNames}
                        calendarUrl={this.props.data.calendarUrl}
                        calendarData={this.props.data.calendarData}
                        onDelete={this.props.onDelete}
                    />
                }
            </TopPanel>
        );
    }
}

export {
    MaintenanceWindowsDetails
};
