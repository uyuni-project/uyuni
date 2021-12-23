import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { ActionChain } from "components/action-schedule";

import { GuestsCreate } from "./guests-create";

type RendererProps = {
  host: any;
  localTime: string;
  timezone: string;
  actionChains: Array<ActionChain>;
  cobblerProfiles: { string: string };
};

export const renderer = (id: string, { host, timezone, localTime, actionChains, cobblerProfiles }: RendererProps) => {
  SpaRenderer.renderNavigationReact(
    <GuestsCreate
      host={host}
      localTime={localTime}
      timezone={timezone}
      actionChains={actionChains}
      cobblerProfiles={cobblerProfiles}
    />,
    document.getElementById(id)
  );
};
