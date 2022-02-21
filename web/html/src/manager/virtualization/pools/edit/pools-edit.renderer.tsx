import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";

import { PoolsEdit } from "./pools-edit";

type RendererProps = {
  serverId: string;
  poolName: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export const renderer = (id: string, { serverId, actionChains, timezone, localTime, poolName }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <PoolsEdit
      serverId={serverId}
      poolName={poolName}
      actionChains={actionChains}
      timezone={timezone}
      localTime={localTime}
    />,
    document.getElementById(id)
  );
};
