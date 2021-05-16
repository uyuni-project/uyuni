import * as React from "react";
import { GuestsEdit } from "./guests-edit";
import SpaRenderer from "core/spa/spa-renderer";
import { ActionChain } from "components/action-schedule";

type RendererProps = {
  host: any;
  guestUuid: string;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
};

export const renderer = (id: string, { host, guestUuid, timezone, localTime, actionChains }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <GuestsEdit
      host={host}
      guestUuid={guestUuid}
      timezone={timezone}
      localTime={localTime}
      actionChains={actionChains}
    />,
    document.getElementById(id)
  );
};
