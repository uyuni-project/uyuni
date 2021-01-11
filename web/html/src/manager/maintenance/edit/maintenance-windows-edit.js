/* eslint-disable */
'use strict';

import * as React from 'react';
import {useState, useRef} from "react";
import {AsyncButton} from "components/buttons";
import {Button} from "components/buttons";
import {InnerPanel} from "components/panels/InnerPanel";

import MaintenanceScheduleEdit from "./schedule-edit";
import MaintenanceCalendarEdit from "./calendar-edit";

type MaintenanceEditProps = {
    type: 'schedule' | 'calendar',
    calendarNames?: Array,
    selected: Object,
    messages: () => void,
    onEdit: () => void,
    onActionChanged: () => void,
    onRefresh: () => void
};

const MaintenanceWindowsEdit = (props: MaintenanceEditProps) => {
    const child = useRef();
    const [type] = useState(props.type);
    const [icalLoading, setIcalLoading] = useState(false);

    const isEdit = () => {
        return props.selected ? true : false;
    };

    const buttons = [
        <div className="btn-group pull-right">
            <AsyncButton id={"editButton"} action={() => child.current.onEdit()} defaultType="btn-success"
                         disabled={icalLoading === true}
                         text={(isEdit() ? t("Update") : t("Create")) + " " +
                             (type === "schedule" ? t("Schedule") : t("Calendar"))}
            />
        </div>
    ];

    const buttonsLeft = [
        <div className="btn-group pull-left">
            <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}
                    handler={() => props.onActionChanged("back")}/>
        </div>
    ];

    return (
        <InnerPanel title={t("Maintenance") + " " + (type === "schedule" ? t("Schedule") : t("Calendar"))}
                    icon="spacewalk-icon-schedule" buttonsLeft={buttonsLeft} buttons={buttons} >
            {
                type === "schedule" &&
                <MaintenanceScheduleEdit
                    ref={child}
                    isEdit={isEdit()}
                    schedule={props.selected}
                    calendarNames={props.calendarNames}
                    onEdit={props.onEdit}
                /> ||
                type === "calendar" &&
                <MaintenanceCalendarEdit
                    ref={child}
                    messages={props.messages}
                    isEdit={isEdit()}
                    calendar={props.selected}
                    onRefresh={props.onRefresh}
                    onEdit={props.onEdit}
                    isLoading={i => setIcalLoading(i)}
                />
            }
        </InnerPanel>
    );
};

export {
    MaintenanceWindowsEdit
};
