import * as React from "react";
import { useState } from "react";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { Button } from "components/buttons";
import { ModalButton } from "components/dialog/ModalButton";
import { DeleteDialog } from "components/dialog/DeleteDialog";

type ScheduleListProps = {
  data: {
    id: number;
    name: string;
    calendarId?: number;
    calendarName?: string;
  }[];
  onSelect: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  onDelete: (...args: any[]) => any;
};

const MaintenanceScheduleList = (props: ScheduleListProps) => {
  const [scheduleToDelete, setScheduleToDelete] = useState({});

  return (
    <>
      <Table
        data={props.data}
        identifier={(row) => row.id}
        initialItemsPerPage={window.userPrefPageSize}
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
                  props.onSelect(row.id);
                }}
              />
              <Button
                className="btn-default btn-sm"
                disabled={!window.isAdmin}
                title={t("Edit")}
                icon="fa-edit"
                handler={() => {
                  props.onEdit(row.id);
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
        onConfirm={() => props.onDelete(scheduleToDelete)}
        onClosePopUp={() => setScheduleToDelete({})}
      />
    </>
  );
};

export default MaintenanceScheduleList;
