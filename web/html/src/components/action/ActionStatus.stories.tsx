import * as React from "react";
import { ActionStatus } from "./ActionStatus";

export default {
  component: ActionStatus,
  title: "Actions/ActionStatus",
};

export const allFlavors = () => {
  return [
    <ActionStatus serverId="server123" actionId="456" status="Queued" />,
    <ActionStatus serverId="server123" actionId="456" status="Picked Up" />,
    <ActionStatus serverId="server123" actionId="456" status="Failed" />,
    <ActionStatus serverId="server123" actionId="456" status="Completed" />,
  ];
};
