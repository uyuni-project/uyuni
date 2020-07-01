/* eslint-disable */
'use strict';

const React = require("react");
const { InnerPanel } = require('components/panels/InnerPanel');
const Button = require("components/buttons").Button;

import MaintenanceScheduleList from "./schedule-list";
import MaintenanceCalendarList from "./calendar-list";

class MaintenanceWindowsList extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            type: props.type,
        };
    }

    render() {
        const createButton = [
            <div className="btn-group pull-right">
                <Button
                    className="btn-default"
                    disabled={!isAdmin}
                    icon="fa-plus"
                    text={t("Create")}
                    title="Create a new maintenance schedule"
                    handler={() => this.props.onActionChanged("create")}
                />
            </div>
        ];

        return (
            <div>
                <InnerPanel title={t("Maintenance " +  (this.state.type === "schedule" ? "Schedules" : "Calendars"))} icon="spacewalk-icon-schedule" buttons={this.props.disableCreate ? null : createButton}>
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <div>
                                <h3>{t(this.state.type === "schedule" ? "Schedules" : "Calendars")}</h3>
                            </div>
                        </div>
                        {
                            this.state.type === "schedule" &&
                            <MaintenanceScheduleList
                                data={this.props.data}
                                onSelect={this.props.onSelect}
                                onEdit={this.props.onEdit}
                                onDelete={this.props.onDelete}
                            /> ||
                            this.state.type === "calendar" &&
                            <MaintenanceCalendarList
                                data={this.props.data}
                                onSelect={this.props.onSelect}
                                onEdit={this.props.onEdit}
                                onDelete={this.props.onDelete}
                            />
                        }
                    </div>
                </InnerPanel>
            </div>
        );
    }
}

export {
    MaintenanceWindowsList
};
