/* eslint-disable */
'use strict';

import React from "react";
import {BootstrapPanel} from "components/panels/BootstrapPanel";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {DeleteDialog} from "components/dialog/DeleteDialog";

type ScheduleDetailsProps = {
    name: string,
    calendarName: string,
    type: 'SINGLE' | 'MULTI',
    maintenanceWindows?: Array,
    onDelete: () => void
};

const MaintenanceScheduleDetails = (props: ScheduleDetailsProps) => {
    return (
        <>
            <DeleteDialog
                id="delete-modal"
                title={t("Delete maintenance schedule")}
                content={
                    <>
                        <div>{t("Are you sure you want to delete the selected item?")}</div>
                        <div>{t("This will remove the current schedule from all the systems assigned to it.")}</div>
                    </>
                }
                onConfirm={() => props.onDelete({name: props.name})}
            />
            <MaintenanceScheduleOverview
                name={props.name}
                calendarName={props.calendarName}
                type={props.type}
                maintenanceWindows={props.maintenanceWindows}
            />
        </>
    );
}

type OverviewProps = {
    name: string,
    calendarName: string,
    type: 'SINGLE' | 'MULTI',
    maintenanceWindows?: Array
}

const MaintenanceScheduleOverview = (props: OverviewProps) => {
    const tableData = [
        {left: t("Schedule name") + ":", right: props.name},
        {left: t("Assigned calendar") + ":", right: props.calendarName},
        {left: t("Schedule type") + ":", right: props.type === "SINGLE" ? t("Single") : t("Multi")}
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
