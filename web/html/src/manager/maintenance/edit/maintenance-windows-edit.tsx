import * as React from "react";
import { useState, useRef } from "react";
import { AsyncButton } from "components/buttons";
import { Button } from "components/buttons";
import { InnerPanel } from "components/panels/InnerPanel";

import MaintenanceScheduleEdit from "./schedule-edit";
import MaintenanceCalendarEdit from "./calendar-edit";

type MaintenanceEditProps = {
  type: "schedule" | "calendar";
  calendarNames?: any[];
  selected: any;
  messages: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  onActionChanged: (...args: any[]) => any;
  onRefresh: (...args: any[]) => any;
};

const MaintenanceWindowsEdit = (props: MaintenanceEditProps) => {
  /**
   * This ref is a parent-child bus with `useImperativeHandle`
   * See https://stackoverflow.com/a/37950970/1470607
   */
  const child = useRef<any>();
  const [type] = useState(props.type);
  const [icalLoading, setIcalLoading] = useState(false);

  const isEdit = () => {
    return props.selected ? true : false;
  };

  const buttons = [
    <div className="btn-group pull-right">
      <AsyncButton
        id={"editButton"}
        action={() => child.current?.onEdit()}
        defaultType="btn-success"
        disabled={icalLoading === true}
        text={(isEdit() ? t("Update") : t("Create")) + " " + (type === "schedule" ? t("Schedule") : t("Calendar"))}
      />
    </div>,
  ];

  const buttonsLeft = [
    <div className="btn-group pull-left">
      <Button
        id="back-btn"
        className="btn-default"
        icon="fa-chevron-left"
        text={t("Back")}
        handler={() => props.onActionChanged("back")}
      />
    </div>,
  ];

  return (
    <InnerPanel
      title={t("Maintenance") + " " + (type === "schedule" ? t("Schedule") : t("Calendar"))}
      icon="spacewalk-icon-schedule"
      buttonsLeft={buttonsLeft}
      buttons={buttons}
    >
      {(type === "schedule" && (
        <MaintenanceScheduleEdit
          ref={child}
          isEdit={isEdit()}
          schedule={props.selected}
          calendarNames={props.calendarNames}
          onEdit={props.onEdit}
        />
      )) ||
        (type === "calendar" && (
          <MaintenanceCalendarEdit
            ref={child}
            messages={props.messages}
            isEdit={isEdit()}
            calendar={props.selected}
            onRefresh={props.onRefresh}
            onEdit={props.onEdit}
            isLoading={(i) => setIcalLoading(i)}
          />
        ))}
    </InnerPanel>
  );
};

export { MaintenanceWindowsEdit };
