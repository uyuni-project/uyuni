/* eslint-disable */
'use strict';

import * as React from 'react';
import {useState} from "react";
import {InnerPanel} from "components/panels/InnerPanel";
import {Button} from "components/buttons";

import MaintenanceScheduleList from "./schedule-list";
import MaintenanceCalendarList from "./calendar-list";

type MaintenanceListProps = {
    type: 'schedule' | 'calendar',
    data?: Array<Object>,
    onActionChanged: () => void,
    onSelect: () => void,
    onEdit: () => void,
    onDelete: () => void
};

const MaintenanceWindowsList = (props: MaintenanceListProps) => {
    const [type] = useState(props.type);

    const buttons = [
        <div className="btn-group pull-right">
            <Button
                className="btn-default"
                disabled={!isAdmin}
                icon="fa-plus"
                text={t("Create")}
                title={t("Create a new maintenance schedule")}
                handler={() => props.onActionChanged("create")}
            />
        </div>
    ];

    return (
        <div>
            <InnerPanel title={t("Maintenance") + " " + (type === "schedule" ? t("Schedules") : t("Calendars"))}
                        icon="spacewalk-icon-schedule" buttons={buttons}>
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <div>
                            <h3>{t(type === "schedule" ? "Schedules" : "Calendars")}</h3>
                        </div>
                    </div>
                    {
                        type === "schedule" &&
                        <MaintenanceScheduleList
                            data={props.data}
                            onSelect={props.onSelect}
                            onEdit={props.onEdit}
                            onDelete={props.onDelete}
                        /> ||
                        type === "calendar" &&
                        <MaintenanceCalendarList
                            data={props.data}
                            onSelect={props.onSelect}
                            onEdit={props.onEdit}
                            onDelete={props.onDelete}
                        />
                    }
                </div>
            </InnerPanel>
        </div>
    );
}

export {
    MaintenanceWindowsList
};
