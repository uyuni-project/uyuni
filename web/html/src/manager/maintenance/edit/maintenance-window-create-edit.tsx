import * as React from "react";
import { useRef, useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton } from "components/buttons";
import { InnerPanel } from "components/panels/InnerPanel";
import { MessagesContainer } from "components/toastr";

import MaintenanceCalendarEdit from "./calendar-edit";
import MaintenanceScheduleEdit from "./schedule-edit";

type Props = {
  type: "schedule" | "calendar";
  calendarNames?: any[];
  selected: any;
  messages: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  onRefresh: (...args: any[]) => any;
};

const MaintenanceWindowsEdit = (props: Props) => {
  /**
   * This ref is a parent-child bus with `useImperativeHandle`
   * See https://stackoverflow.com/a/37950970/1470607
   */
  const child = useRef<any>();
  const [icalLoading, setIcalLoading] = useState(false);

  const isEdit = () => {
    return props.selected ? true : false;
  };

  const buttons = [
    <div className="btn-group pull-right">
      <AsyncButton
        id={"editButton"}
        action={() => child.current?.onEdit()}
        defaultType="btn-primary"
        disabled={icalLoading === true}
        text={
          (isEdit() ? t("Update") : t("Create")) + " " + (props.type === "schedule" ? t("Schedule") : t("Calendar"))
        }
      />
    </div>,
  ];

  // const buttonsLeft = [
  //   <div className="btn-group pull-left">
  //     <Button
  //       id="back-btn"
  //       className="btn-default"
  //       icon="fa-chevron-left"
  //       text={t("Back")}
  //       handler={() => {
  //         // TODO: Navigate back to list
  //         // props.onActionChanged("back");
  //       }}
  //     />
  //   </div>,
  // ];

  return (
    <InnerPanel
      title={t("Maintenance") + " " + (props.type === "schedule" ? t("Schedule") : t("Calendar"))}
      icon="spacewalk-icon-schedule"
      // buttonsLeft={buttonsLeft}
      buttons={buttons}
    >
      {props.type === "schedule" ? (
        <MaintenanceScheduleEdit
          ref={child}
          isEdit={isEdit()}
          schedule={props.selected}
          calendarNames={props.calendarNames}
          onEdit={props.onEdit}
        />
      ) : (
        <MaintenanceCalendarEdit
          ref={child}
          messages={props.messages}
          isEdit={isEdit()}
          calendar={props.selected}
          onRefresh={props.onRefresh}
          onEdit={props.onEdit}
          isLoading={(i) => setIcalLoading(i)}
        />
      )}
    </InnerPanel>
  );
};

export const renderer = (id: string, props: Props) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <MaintenanceWindowsEdit type={props.type} />
    </>,
    document.getElementById(id)
  );
};
