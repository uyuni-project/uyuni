/* eslint-disable */
'use strict';

import React from "react";
import {BootstrapPanel} from "components/panels/BootstrapPanel";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {DeleteDialog} from "components/dialog/DeleteDialog";

type ScheduleDetailsProps = {
    scheduleName: string,
    calendarName: string,
    scheduleType: 'SINGLE' | 'MULTI',
    maintenanceWindows?: Array,
    onDelete: () => void
};

const MaintenanceScheduleDetails = (props: ScheduleDetailsProps) => {
    return (
        <>
            <DeleteDialog
                id="delete-modal"
                title={t("Delete maintenance schedule")}
                content={t("Are you sure you want to delete the selected item?\n" +
                    "This will remove the current schedule from all the systems assigned to it.")}
                onConfirm={() => props.onDelete({scheduleName: props.scheduleName})}
            />
            <MaintenanceScheduleOverview
                scheduleName={props.scheduleName}
                calendarName={props.calendarName}
                scheduleType={props.scheduleType}
                maintenanceWindows={props.maintenanceWindows}
            />
        </>
    );
}

type OverviewProps = {
    scheduleName: string,
    calendarName: string,
    scheduleType: 'SINGLE' | 'MULTI',
    maintenanceWindows?: Array
}

const MaintenanceScheduleOverview = (props: OverviewProps) => {
    const tableData = [
        {left: t("Schedule name") + ":", right: props.scheduleName},
        {left: t("Assigned calendar") + ":", right: props.calendarName},
        {left: t("Schedule type") + ":", right: props.scheduleType === "SINGLE" ? t("Single") : t("Multi")}
    ];

    return (
        <div>
            <BootstrapPanel title={t("Schedule Details")}>
                <Table
                    data={tableData}
                    identifier={row => tableData.indexOf(row)}
                    initialItemsPerPage={0}
                >
                    <Column columnKey="left" cell={row => row.left}/>
                    <Column columnKey="right" cell={row => row.right}/>
                </Table>
            </BootstrapPanel>
            {
                props.maintenanceWindows && props.maintenanceWindows.length > 0 &&
                <BootstrapPanel title={t("Upcoming Maintenance Windows")}>
                    <Table
                        data={props.maintenanceWindows}
                        identifier={row => props.maintenanceWindows.indexOf(row)}
                        initialItemsPerPage={0}
                    >
                        <Column header={t("Start")} columnKey="start" cell={row => row.start}/>
                        <Column header={t("End")} columnKey="end" cell={row => row.end}/>
                    </Table>
                </BootstrapPanel>
            }
        </div>
    );
}

export default MaintenanceScheduleDetails;
