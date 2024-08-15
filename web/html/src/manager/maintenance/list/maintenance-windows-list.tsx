import * as React from "react";
import { useState } from "react";

import { Button } from "components/buttons";
import { IconTag } from "components/icontag";
import { HelpLink } from "components/utils";

import MaintenanceCalendarList from "./calendar-list";
import MaintenanceScheduleList from "./schedule-list";

type MaintenanceListProps = {
  type: "schedule" | "calendar";
  data: any[];
  onActionChanged: (...args: any[]) => any;
  onSelect: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  onDelete: (...args: any[]) => any;
};

const MaintenanceWindowsList = (props: MaintenanceListProps) => {
  const [type] = useState(props.type);

  const buttons = [
    <div className="btn-group pull-right">
      <Button
        className="btn-default"
        disabled={!window.isAdmin}
        icon="fa-plus"
        text={t("Create")}
        title={t("Create a new maintenance schedule")}
        handler={() => props.onActionChanged("create")}
      />
    </div>,
  ];

  return (
    <div>
      <>
        <h1>
          <IconTag type="header-schedule" />
          {type === "schedule" ? t("Maintenance Schedules") : t("Maintenance Calendars")}
          <HelpLink url="reference/schedule/maintenance-windows.html" />
        </h1>
        <p>
          {type === "schedule"
            ? t("Below is a list of Maintenance Schedules available to the current user.")
            : t("Below is a list of Maintenance Calendars available to the current user.")}
        </p>
        <div className="pull-right btn-group">{buttons}</div>
        <h3>{t(type === "schedule" ? "Schedules" : "Calendars")}</h3>

        {(type === "schedule" && (
          <MaintenanceScheduleList
            data={props.data}
            onSelect={props.onSelect}
            onEdit={props.onEdit}
            onDelete={props.onDelete}
          />
        )) ||
          (type === "calendar" && (
            <MaintenanceCalendarList
              data={props.data}
              onSelect={props.onSelect}
              onEdit={props.onEdit}
              onDelete={props.onDelete}
            />
          ))}
      </>
    </div>
  );
};

export { MaintenanceWindowsList };
