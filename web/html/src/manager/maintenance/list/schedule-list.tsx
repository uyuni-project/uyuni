import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { ModalButton } from "components/dialog/ModalButton";
import { Column } from "components/table/Column";
import { Table } from "components/table/Table";

type ScheduleListProps = {
  data?: {
    id: number;
    name: string;
    calendarId?: number;
    calendarName?: string;
  }[];
};

const MaintenanceScheduleList = (props: ScheduleListProps) => {
  const [scheduleToDelete, setScheduleToDelete] = useState({});

  return (
    <>
      <Table
        data={props.data ?? []}
        loading={typeof props.data === "undefined"}
        identifier={(row) => row.id}
        emptyText={t("No schedules created. Use Create to add a schedule.")}
      >
        <Column columnKey="scheduleName" header={t("Schedule Name")} cell={(row) => row.name} />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="calendarName"
          header={t("Calendar")}
          cell={(row) =>
            row.calendarId && (
              <a className="link-tag" href={"/rhn/manager/schedule/maintenance/calendars#/details/" + row.calendarId}>
                {row.calendarName}
              </a>
            )
          }
        />
        <Column
          columnClass="text-right"
          headerClass="text-right"
          header={t("Actions")}
          cell={(row) => (
            <div className="btn-group">
              <Button
                className="btn-default btn-sm"
                title={t("Details")}
                icon="fa-list"
                handler={() => {
                  // TODO: Navigate to details
                }}
              />
              <Button
                className="btn-default btn-sm"
                disabled={!window.isAdmin}
                title={t("Edit")}
                icon="fa-edit"
                handler={() => {
                  // TODO: Navigate to edit
                }}
              />
              <ModalButton
                className="btn-default btn-sm"
                disabled={!window.isAdmin}
                title={t("Delete")}
                icon="fa-trash"
                target="delete-modal"
                item={row}
                onClick={(i) => setScheduleToDelete(i)}
              />
            </div>
          )}
        />
      </Table>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete maintenance schedule")}
        content={
          <>
            <div>{t("Are you sure you want to delete the selected item?")}</div>
            <div>{t("This will remove the schedule from all the systems assigned to it.")}</div>
          </>
        }
        onConfirm={() => {
          // TODO: Delete
          //         onConfirm={() => props.onDelete(scheduleToDelete)}
        }}
        onClosePopUp={() => setScheduleToDelete({})}
      />
    </>
  );
};

export default MaintenanceScheduleList;
