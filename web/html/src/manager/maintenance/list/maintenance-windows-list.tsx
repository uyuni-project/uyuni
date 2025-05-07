import * as React from "react";
import { useEffect, useState } from "react";

import { Button } from "components/buttons";
import { IconTag } from "components/icontag";
import { HelpLink } from "components/utils";

import Network from "utils/network";

import MaintenanceWindowsApi from "../api/maintenance-windows-api";
import MaintenanceCalendarList from "./calendar-list";
import MaintenanceScheduleList from "./schedule-list";

type MaintenanceListProps = {
  type: "schedule" | "calendar";
};

const MaintenanceWindowsList = (props: MaintenanceListProps) => {
  const [items, setItems] = useState<any[]>([]);

  const getMaintenanceWindowItems = () => {
    /* Returns a list of maintenance schedules or calendars depending on the type provided */
    return MaintenanceWindowsApi.list(props.type)
      .then((newItems) => setItems(newItems))
      .catch((jqXHR) => Network.responseErrorMessage(jqXHR));
  };

  useEffect(() => {
    getMaintenanceWindowItems();
  }, []);

  const buttons = [
    <div className="btn-group pull-right">
      <Button
        className="btn-primary"
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
          {props.type === "schedule" ? t("Maintenance Schedules") : t("Maintenance Calendars")}
          <HelpLink url="reference/schedule/maintenance-windows.html" />
        </h1>
        <p>
          {props.type === "schedule"
            ? t("Below is a list of Maintenance Schedules available to the current user.")
            : t("Below is a list of Maintenance Calendars available to the current user.")}
        </p>
        <div className="pull-right btn-group">{buttons}</div>
        <h3>{t(props.type === "schedule" ? "Schedules" : "Calendars")}</h3>

        {(props.type === "schedule" && (
          <MaintenanceScheduleList
            data={items}
            onSelect={props.onSelect}
            onEdit={props.onEdit}
            onDelete={props.onDelete}
          />
        )) ||
          (props.type === "calendar" && (
            <MaintenanceCalendarList
              data={items}
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
