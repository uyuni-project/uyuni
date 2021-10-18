import * as React from "react";
import { useState } from "react";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { Check } from "components/input/Check";
import { Form } from "components/input/Form";
import { Button } from "components/buttons";
import { ModalButton } from "components/dialog/ModalButton";
import { DeleteDialog } from "components/dialog/DeleteDialog";

type CalendarListProps = {
  data: {
    id: number;
    name: string;
    scheduleNames: Array<Map<string, string>>;
  }[];
  onSelect: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  onDelete: (...args: any[]) => any;
};

const MaintenanceCalendarList = (props: CalendarListProps) => {
  const [calendarToDelete, setCalendarToDelete] = useState<any>({});
  const [strategy, setStrategy] = useState(false);

  const setCheck = (model) => {
    /* strategy gets initialized as empty string, but we want the initial value to be false.
     * Is equivalent to: if strategy is "" then set it to false */
    model.strategy === "" && (model.strategy = false);
    setStrategy(model.strategy);
  };

  const addStrategy = () => {
    const item = calendarToDelete;
    item.strategy = strategy ? "Cancel" : "Fail";
    return item;
  };

  return (
    <div>
      <Table
        data={props.data}
        identifier={(row) => row.name}
        initialItemsPerPage={window.userPrefPageSize}
        emptyText={t("No calendars created. Use Create to add a calendar.")}
      >
        <Column columnKey="calendarName" header={t("Calendar Name")} cell={(row) => row.name} />
        <Column
          columnClass="text-center"
          headerClass="text-center"
          columnKey="usedBySchedule"
          header={t("Used by Schedule")}
          cell={(row) =>
            row.scheduleNames.map((name) => (
              <a className="link-tag" href={"/rhn/manager/schedule/maintenance/schedules#/details/" + name.id}>
                {name.name}
              </a>
            ))
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
                onClick={(i) => setCalendarToDelete(i)}
              />
            </div>
          )}
        />
      </Table>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete maintenance calendar")}
        content={
          <Form model={{ strategy: strategy }} onChange={setCheck}>
            <div>{t("Are you sure you want to delete the selected item?")}</div>
            <div>{t("This will unassign all schedules from this calendar.")}</div>
            <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
          </Form>
        }
        onConfirm={() => props.onDelete(addStrategy())}
        onClosePopUp={() => setCalendarToDelete({})}
      />
    </div>
  );
};

export default MaintenanceCalendarList;
