// @flow

import React, {useState, useEffect} from "react";

import * as Network from "utils/network";

import {AsyncButton} from "components/buttons";
import {DeleteDialog} from "components/dialog/DeleteDialog";
import {ModalButton} from "components/dialog/ModalButton";
import {SystemLink} from "components/links";
import {Utils as MessagesUtils} from "components/messages";
import {BootstrapPanel} from "components/panels/BootstrapPanel";
import {InnerPanel} from "components/panels/InnerPanel";
import {TabLabel} from "components/tab-container";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {SearchField}  from "components/table/SearchField";
import {Toggler} from "components/toggler";

import CancelActionsDialog from "../shared/cancel-actions-dialog";

import type {MessageType} from "components/messages";

type MaintenanceWindowType = {
    start: string,
    end: string
}

type MaintenanceScheduleDetailsProps = {
    id: number,
    name: string,
    type: 'SINGLE' | 'MULTI',
    calendarName: string,
    maintenanceWindows?: MaintenanceWindowType[],
    onDelete: (item: {name: string}) => Promise<any>,
    onMessage: (messages: MessageType[]) => void
};

const MaintenanceScheduleDetails = (props: MaintenanceScheduleDetailsProps) => {
    const [activeTab, setActiveTab] = useState("overview");

    useEffect(() => setActiveTab("overview"), [props.id]);
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
            <div className="spacewalk-content-nav">
                <ul className="nav nav-tabs">
                    <TabLabel
                        active={activeTab === "overview"}
                        text={t("Overview")}
                        onClick={() => setActiveTab("overview")}
                    />
                    <TabLabel
                        active={activeTab === "assignment"}
                        text={t("Assigned Systems")}
                        onClick={() => setActiveTab("assignment")}
                    />
                </ul>
            </div>
            { activeTab === "overview" &&
                <MaintenanceScheduleOverview
                    name={props.name}
                    calendarName={props.calendarName}
                    type={props.type}
                    maintenanceWindows={props.maintenanceWindows}
                />
            }
            { activeTab === "assignment" &&
                <SystemPicker
                    scheduleId={props.id}
                    onBack={() => setActiveTab("overview")}
                    onMessage={props.onMessage}
                />
            }
        </>
    );
}

type OverviewProps = {
    name: string,
    calendarName: string,
    type: 'SINGLE' | 'MULTI',
    maintenanceWindows?: MaintenanceWindowType[]
};

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
                        data={props.maintenanceWindows || []}
                        identifier={row => (props.maintenanceWindows || []).indexOf(row)}
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

type SystemPickerProps = {
    scheduleId: number,
    onBack: () => void,
    onMessage: (messages: MessageType[]) => void
};

const SystemPicker = (props: SystemPickerProps) => {
    const [hasChanges, setHasChanges] = useState(false);
    const [selectedSystems, setSelectedSystems] = useState([]);
    const [isCancelActions, setCancelActions] = useState(false);

    useEffect(() => {
        Network.get(`/rhn/manager/api/maintenance/schedule/${props.scheduleId}/systems`).promise
            .then(setSelectedSystems)
            .catch(xhr => props.onMessage(Network.responseErrorMessage(xhr)));
    }, [props.scheduleId]);

    const onAssign = () => {
        return Network.post(`/rhn/manager/api/maintenance/schedule/${props.scheduleId}/setsystems`,
                JSON.stringify({systemIds: selectedSystems, cancelActions: isCancelActions}),
                "application/json", false).promise
            .then(() => props.onMessage(
                MessagesUtils.success(t("Maintenance schedule has been assigned to {0} system(s)", selectedSystems.length))))
            .then(props.onBack)
            .catch(xhr => props.onMessage(
                Network.responseErrorMessage(xhr)));
    };

    const onSelect = (systems) => {
      setHasChanges(true);
      setSelectedSystems(systems);
    }

    return (
        <>
            <InnerPanel
                title={t("Assigned Systems")}
                icon="fa-desktop"
                buttons={[
                    <Toggler
                        text={t("Cancel affected actions")}
                        className="btn"
                        handler={() => setCancelActions(!isCancelActions)}
                        value={isCancelActions}
                    />,
                    ( isCancelActions && selectedSystems.length > 0 ?
                        <ModalButton
                            target="cancel-confirm"
                            text={t("Save Changes")}
                            className="btn-success"
                            disabled={!hasChanges}
                        /> :
                        <AsyncButton
                            action={onAssign}
                            defaultType="btn-success"
                            text={t("Save Changes")}
                            disabled={!hasChanges}
                        />
                    )
                ]}
            >
                <Table
                    data="/rhn/manager/api/maintenance/schedule/systems"
                    identifier={system => system.id}
                    searchField={<SearchField placeholder={t("Search systems")} criteria=""/>}
                    selectable
                    selectedItems={selectedSystems}
                    onSelect={onSelect}
                    initialSortColumnKey="name"
                >
                    <Column
                        columnKey="name"
                        sortable
                        header={t("System")}
                        cell={system => <SystemLink id={system.id} newWindow>{system.name}</SystemLink>}
                    />
                    <Column
                        columnKey="scheduleName"
                        sortable
                        header={t("Current Schedule")}
                        cell={system => system.scheduleId &&
                          ( system.scheduleId === props.scheduleId ?
                            <span>{system.scheduleName}</span>
                            :
                            <a href={`/rhn/manager/schedule/maintenance/schedules#/details/${system.scheduleId}`}>
                                {system.scheduleName}
                            </a>
                          )
                        }
                    />
                </Table>
            </InnerPanel>
            <CancelActionsDialog id="cancel-confirm" onConfirmAsync={onAssign}/>
        </>
    );
}

export default MaintenanceScheduleDetails;
