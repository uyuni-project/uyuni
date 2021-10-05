import * as React from "react";
import { useState } from "react";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { Table } from "components/table/Table";
import { Column } from "components/table/Column";
import { Check } from "components/input/Check";
import { Form } from "components/input/Form";
import { DeleteDialog } from "components/dialog/DeleteDialog";
import { WebCalendar } from "manager/maintenance/calendar/web-calendar";
import { MessageType } from "components/messages";

type CalendarDetailsProps = {
  id: number;
  name: string;
  eventNames: Array<string>;
  scheduleNames: Array<Record<string, string>>;
  url: string;
  onDelete: (...args: any[]) => any;
  onMessage: (messages: MessageType[]) => void;
  clearMessages: (messages: void) => void;
  responseError: (messages: MessageType[]) => void;
};

const MaintenanceCalendarDetails = (props: CalendarDetailsProps) => {
  const [strategy, setStrategy] = useState(false);

  const setCheck = (model) => {
    // Strategy gets initialized as empty string, but we want the initial value to be false.
    if (model.strategy === "") {
      model.strategy = false;
    }
    setStrategy(model.strategy);
  };

  return (
    <>
      <DeleteDialog
        id="delete-modal"
        title={t("Delete maintenance calendar")}
        content={
          <Form model={{ strategy }} onChange={setCheck}>
            <div>{t("Are you sure you want to delete the selected item?")}</div>
            <div>{t("This will remove the current schedule from all the systems assigned to it.")}</div>
            <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
          </Form>
        }
        onConfirm={() =>
          props.onDelete({
            name: props.name,
            strategy: strategy ? "Cancel" : "Fail",
          })
        }
      />
      <MaintenanceCalendarOverview
        id={props.id}
        name={props.name}
        eventNames={props.eventNames}
        scheduleNames={props.scheduleNames}
        url={props.url}
        onMessage={props.onMessage}
        clearMessages={props.clearMessages}
        responseError={props.responseError}
      />
    </>
  );
};

type OverviewProps = {
  id: number;
  name: string;
  eventNames: Array<string>;
  scheduleNames: Array<Record<string, string>>;
  url: string;
  onMessage: (messages: MessageType[]) => void;
  clearMessages: (messages: void) => void;
  responseError: (messages: MessageType[]) => void;
};

const MaintenanceCalendarOverview = (props: OverviewProps) => {
  const tableData = [
    { left: t("Calendar Name") + ":", right: props.name },
    { left: t("Used by Schedule") + ":", right: props.scheduleNames.map((name) => name.name).join(", ") },
  ];
  props.url && tableData.push({ left: t("Url") + ":", right: props.url });

  return (
    <div>
      <BootstrapPanel title={t("Calendar Details")}>
        <Table data={tableData} identifier={(row) => tableData.indexOf(row)} initialItemsPerPage={0}>
          <Column columnKey="left" cell={(row) => row.left} />
          <Column columnKey="right" cell={(row) => row.right} />
        </Table>
      </BootstrapPanel>
      <div className="panel panel-default">
        <div className="panel-heading">
          <h4>{props.name}</h4>
        </div>
        <div className="panel-body">
          <WebCalendar
            id={props.id}
            type={"calendar"}
            eventNames={props.eventNames}
            messages={props.onMessage}
            clearMessages={props.clearMessages}
            responseError={props.responseError}
          />
        </div>
      </div>
    </div>
  );
};

export default MaintenanceCalendarDetails;
