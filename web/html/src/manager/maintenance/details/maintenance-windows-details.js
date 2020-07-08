/* eslint-disable */
'use strict';

import React, {useState} from "react";
import {Button} from "components/buttons";
import {ModalButton} from "components/dialog/ModalButton";
import {TopPanel} from "components/panels/TopPanel";

import MaintenanceScheduleDetails from "./schedule-details";
import MaintenanceCalendarDetails from "./calendar-details";

type MaintenanceDetailsProps = {
    type: 'schedule' | 'calendar',
    data: Object,
    onCancel: () => void,
    onEdit: () => void,
    onDelete: () => void
};

const MaintenanceWindowsDetails = (props: MaintenanceDetailsProps) => {
    const [type] = useState(props.type);

    const buttons = [
        <div className="btn-group pull-right">
            <Button
                text={t("Back")}
                icon="fa-chevron-left"
                title={t("Back")}
                className="btn-default"
                handler={() => props.onCancel("back")}
            />
            <Button
                text={t("Edit")}
                disabled={!isAdmin}
                icon="fa-edit"
                title={t("Edit")}
                className="btn-default"
                handler={() => props.onEdit(
                    type === "schedule" ? props.data.scheduleId : props.data.calendarId
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
            title={props.type === "schedule"
                ? props.data.scheduleName
                : props.data.calendarName
            }
            icon="spacewalk-icon-schedule"
            helpUrl=""
            button={buttons}
        >
            {
                type === "schedule" &&
                <MaintenanceScheduleDetails
                    scheduleName={props.data.scheduleName}
                    calendarName={props.data.calendarName}
                    scheduleType={props.data.scheduleType}
                    maintenanceWindows={props.data.maintenanceWindows}
                    onDelete={props.onDelete}
                /> ||
                type === "calendar" &&
                <MaintenanceCalendarDetails
                    calendarName={props.data.calendarName}
                    scheduleNames={props.data.scheduleNames}
                    calendarUrl={props.data.calendarUrl}
                    calendarData={props.data.calendarData}
                    onDelete={props.onDelete}
                />
            }
        </TopPanel>
    );
};

export {
    MaintenanceWindowsDetails
};
