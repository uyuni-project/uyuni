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
                handler={() => props.onEdit(props.data.id)}
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
            title={props.data.name}
            icon="spacewalk-icon-schedule"
            helpUrl=""
            button={buttons}
        >
            {
                type === "schedule" &&
                <MaintenanceScheduleDetails
                    name={props.data.name}
                    calendarName={props.data.calendarName}
                    type={props.data.type}
                    maintenanceWindows={props.data.maintenanceWindows}
                    onDelete={props.onDelete}
                /> ||
                type === "calendar" &&
                <MaintenanceCalendarDetails
                    name={props.data.name}
                    scheduleNames={props.data.scheduleNames}
                    url={props.data.url}
                    data={props.data.data}
                    onDelete={props.onDelete}
                />
            }
        </TopPanel>
    );
};

export {
    MaintenanceWindowsDetails
};
