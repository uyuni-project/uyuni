import * as React from "react";
import { useEffect, useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { LinkButton } from "components/buttons";
import { TopPanel } from "components/panels";
import { MessagesContainer } from "components/toastr";

import Network from "utils/network";

import MaintenanceWindowsApi from "../api/maintenance-windows-api";
import MaintenanceCalendarList from "./calendar-list";
import MaintenanceScheduleList from "./schedule-list";

type Props = {
  type: "schedule" | "calendar";
  isAdmin: boolean;
};

const MaintenanceWindowsList = (props: Props) => {
  const [items, setItems] = useState<any[] | undefined>(undefined);

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
      <LinkButton
        className="btn-primary"
        disabled={!props.isAdmin}
        text={t("Create")}
        title={t("Create a new maintenance schedule")}
        href={`/rhn/manager/schedule/maintenance/${props.type === "calendar" ? "calendars" : "schedules"}/create`}
      />
    </div>,
  ];

  return (
    <div id="maintenance-windows">
      <TopPanel
        title={props.type === "schedule" ? t("Maintenance Schedules") : t("Maintenance Calendars")}
        icon="header-schedule"
        helpUrl="reference/schedule/maintenance-windows.html"
        button={buttons}
      >
        <p>
          {props.type === "schedule"
            ? t("Below is a list of Maintenance Schedules available to the current user.")
            : t("Below is a list of Maintenance Calendars available to the current user.")}
        </p>
        {props.type === "schedule" ? (
          <MaintenanceScheduleList data={items} />
        ) : (
          <MaintenanceCalendarList data={items} />
        )}
      </TopPanel>
    </div>
  );
};

export const renderer = (id: string, props: Props) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <MaintenanceWindowsList type={props.type} isAdmin={props.isAdmin} />
    </>,
    document.getElementById(id)
  );
};
