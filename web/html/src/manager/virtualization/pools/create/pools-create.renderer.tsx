import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";

import { PoolsCreate } from "./pools-create";

type RendererProps = {
  serverId: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export const renderer = (id: string, { serverId, actionChains, timezone, localTime }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <PoolsCreate serverId={serverId} actionChains={actionChains} timezone={timezone} localTime={localTime} />,
    document.getElementById(id)
  );
};
