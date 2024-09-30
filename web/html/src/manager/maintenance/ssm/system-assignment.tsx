import * as React from "react";
import { useState } from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { Messages, MessageType } from "components/messages/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";

import { SchedulePickerForm, WithMaintenanceSchedules } from "./schedule-picker";

function SystemAssignment(props: { systems: string[] }) {
  const [messages, setMessages] = useState<MessageType[]>([]);
  return (
    <>
      <Messages items={messages} />
      <BootstrapPanel
        title={t("Maintenance Schedules")}
        icon="fa-clock-o"
        header={
          <div className="page-summary">
            <p>{t("Assign a maintenance schedule to {count} system(s)", { count: props.systems.length })}</p>
            <p>{t("Assigning a schedule will replace any prior assignments in all of the systems in the set.")}</p>
          </div>
        }
      >
        <WithMaintenanceSchedules systems={props.systems} onMessage={setMessages}>
          {(schedules, onAssign) => <SchedulePickerForm schedules={schedules} onAssign={onAssign} />}
        </WithMaintenanceSchedules>
      </BootstrapPanel>
    </>
  );
}

export const renderer = (id: string, systems: string[]) =>
  SpaRenderer.renderNavigationReact(<SystemAssignment systems={systems} />, document.getElementById(id));
