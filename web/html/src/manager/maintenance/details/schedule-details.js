// @flow

import React, {useState, useEffect} from "react";

import {Utils} from "utils/functions";
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
import {Toggler} from "components/toggler";

import CancelActionsDialog from "../shared/cancel-actions-dialog";

import type {MessageType} from "components/messages";

declare var userPrefPageSize: number;

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
                        text={t("Assign Systems")}
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
                    onAssign={() => setActiveTab("overview")}
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
    onAssign: () => void,
    onMessage: (messages: MessageType[]) => void
};

const SystemPicker = (props: SystemPickerProps) => {
    const [systems, setSystems] = useState([]);
    const [selectedSystems, setSelectedSystems] = useState([]);
    const [isCancelActions, setCancelActions] = useState(false);
    const [isLoading, setLoading] = useState(false);

    useEffect(() => {
        setLoading(true);
        Network.get(`/rhn/manager/api/systems/targetforschedule/${props.scheduleId}`).promise
            .then(setSystems)
            .catch(xhr => props.onMessage(
                MessagesUtils.error(Network.errorMessageByStatus(xhr.status))))
            .finally(() => setLoading(false));
    }, [props.scheduleId]);

    const onAssign = () => {
        return Network.post(`/rhn/manager/api/maintenance/schedule/${props.scheduleId}/assign`,
                JSON.stringify({systemIds: selectedSystems, cancelActions: isCancelActions}),
                "application/json", false).promise
            .then(() => props.onMessage(
                MessagesUtils.success(t("Maintenance schedule has been assigned to {0} system(s)", selectedSystems.length))))
            .then(props.onAssign)
            .catch(xhr => props.onMessage(
                MessagesUtils.error(Network.errorMessageByStatus(xhr.status))));
    };

    return (
        <>
            <InnerPanel
                title={t("Assign Systems")}
                icon="fa-desktop"
                buttons={[
                    <Toggler
                        text={t("Cancel affected actions")}
                        className="btn"
                        handler={() => setCancelActions(!isCancelActions)}
                        value={isCancelActions}
                    />,
                    ( isCancelActions ?
                        <ModalButton
                            target="cancel-confirm"
                            text={t("Assign")}
                            className="btn-success"
                            disabled={selectedSystems.length === 0}
                        /> :
                        <AsyncButton
                            action={onAssign}
                            defaultType="btn-success"
                            text={t("Assign")}
                            disabled={selectedSystems.length === 0}
                        />
                    )
                ]}
            >
                <Table
                    data={systems}
                    identifier={system => system.id}
                    selectable
                    selectedItems={selectedSystems}
                    onSelect={setSelectedSystems}
                    loading={isLoading}
                    initialItemsPerPage={userPrefPageSize}
                >
                    <Column
                        columnKey="systemName"
                        comparator={Utils.sortByText}
                        header={t("System")}
                        cell={system => <SystemLink id={system.id} newWindow>{system.name}</SystemLink>}
                    />
                </Table>
            </InnerPanel>
            <CancelActionsDialog id="cancel-confirm" onConfirmAsync={onAssign}/>
        </>
    );
}

export default MaintenanceScheduleDetails;
