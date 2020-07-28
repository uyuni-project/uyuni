/* eslint-disable */
'use strict';

import React, {useState} from "react";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {Button} from "components/buttons";
import {ModalButton} from "components/dialog/ModalButton";
import {DeleteDialog} from "components/dialog/DeleteDialog";

const MaintenanceScheduleList = (props) => {
    const [scheduleToDelete, setScheduleToDelete] = useState({});

    return (
        <div>
            <Table
                data={props.data}
                identifier={row => row.scheduleId}
                initialItemsPerPage={userPrefPageSize}
                emptyText={t("No schedules created. Use Create to add a schedule.")}
            >
                <Column
                    columnKey="scheduleName"
                    header={t('Schedule Name')}
                    cell={(row) => row.scheduleName}
                />
                <Column
                    columnClass="text-center"
                    headerClass="text-center"
                    columnKey="calendarName"
                    header={t('Calendar')}
                    cell={(row) =>
                        row.calendarId &&
                        <a className="link-tag"
                           href={t("/rhn/manager/schedule/maintenance/calendars#/details/" + row.calendarId)}>
                            {row.calendarName}
                        </a>
                    }
                />
                <Column
                    columnClass="text-right"
                    headerClass="text-right"
                    header={t('Actions')}
                    cell={(row) =>
                        <div className="btn-group">
                            <Button
                                className="btn-default btn-sm"
                                title={t("Details")}
                                icon="fa-list"
                                handler={() => {props.onSelect(row.scheduleId)}}
                            />
                            <Button
                                className="btn-default btn-sm"
                                disabled={!isAdmin}
                                title={t("Edit")}
                                icon="fa-edit"
                                handler={() => {props.onEdit(row.scheduleId)}}
                            />
                            <ModalButton
                                className="btn-default btn-sm"
                                disabled={!isAdmin}
                                title={t("Delete")}
                                icon="fa-trash"
                                target="delete-modal"
                                item={row}
                                onClick={i => setScheduleToDelete(i)}
                            />
                        </div>
                    }
                />
            </Table>
            <DeleteDialog id="delete-modal"
                          title={t("Delete maintenance schedule")}
                          content={t("Are you sure you want to delete the selected item? \n" +
                              "This will remove the schedule from all the systems assigned to it.")}
                          onConfirm={() => props.onDelete(scheduleToDelete)}
                          onClosePopUp={() => setScheduleToDelete({})}
            />
        </div>
    );
}

export default MaintenanceScheduleList;
