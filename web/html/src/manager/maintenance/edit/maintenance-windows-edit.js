/* eslint-disable */
'use strict';

const React = require("react");
const AsyncButton = require("components/buttons").AsyncButton;
const Button = require("components/buttons").Button;
const {InnerPanel} = require("components/panels/InnerPanel");

import MaintenanceScheduleEdit from "./schedule-edit";
import MaintenanceCalendarEdit from "./calendar-edit";

class MaintenanceWindowsEdit extends React.Component {
    constructor(props) {
        super(props);

        this.child = React.createRef();

        this.state = {
            type: props.type
        }
    }

    isEdit = () => {
        return this.props.schedule ? true : false;
    };

    render() {
        const buttons = [
            <div className="btn-group pull-right">
                <AsyncButton id={"editButton"} action={() => this.child.current.onEdit()} defaultType="btn-success"
                             disabled={this.state.icalLoading === true}
                             text={(this.isEdit() ? t("Update ") : t("Create ")) +
                             (this.state.type === "schedule" ? t("Schedule") : t("Calendar"))}
                />
            </div>
        ];
        const buttonsLeft = [
            <div className="btn-group pull-left">
                <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}  handler={() => this.props.onActionChanged("back")}/>
            </div>
        ];
        return (
            <InnerPanel title={t("Maintenance ") + (this.state.type === "schedule" ? t("Schedule") : t("Calendar"))}
                        icon="spacewalk-icon-schedule" buttonsLeft={buttonsLeft} buttons={buttons} >
                {
                    this.state.type === "schedule" &&
                    <MaintenanceScheduleEdit
                        ref={this.child}
                        isEdit={this.isEdit()}
                        schedule={this.props.schedule}
                        calendarNames={this.props.calendarNames}
                        onEdit={this.props.onEdit}
                    /> ||
                    this.state.type === "calendar" &&
                    <MaintenanceCalendarEdit
                        ref={this.child}
                        isEdit={this.isEdit()}
                        calendar={this.props.schedule}
                        onRefresh={this.props.onRefresh}
                        onEdit={this.props.onEdit}
                        isLoading={i => this.setState({icalLoading: i})}
                    />
                }
            </InnerPanel>
        );
    }
}

export {
    MaintenanceWindowsEdit
};
