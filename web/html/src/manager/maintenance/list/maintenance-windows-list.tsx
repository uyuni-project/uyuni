/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import { Button } from "components/buttons";
import { TopPanel } from "components/panels/TopPanel";

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
    <div className="btn-group pull-right" key="create-button">
      <Button
        className="btn-primary"
        disabled={!window.isAdmin}
        icon="fa-plus"
        text={t("Create")}
        title={type === "schedule" ? t("Create a new Maintenance Schedule") : t("Create a new Maintenance Calendar")}
        handler={() => props.onActionChanged("create")}
      />
    </div>,
  ];

  return (
    <TopPanel
      title={type === "schedule" ? t("Maintenance Schedules") : t("Maintenance Calendars")}
      iconType="header-schedule"
      helpUrl="reference/schedule/maintenance-windows.html"
      button={buttons}
    >
      <p>
        {type === "schedule"
          ? t("Below is a list of Maintenance Schedules available to the current user.")
          : t("Below is a list of Maintenance Calendars available to the current user.")}
      </p>
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
    </TopPanel>
  );
};

export { MaintenanceWindowsList };
