import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import GuestsConsole from "./guests-console";

type RendererProps = {
  hostId: string;
  guestUuid: string;
  guestName: string;
  guestState: string;
  graphicsType: string;
  token: string;
};

export const renderer = (
  id: string,
  { hostId, guestUuid, guestName, guestState, graphicsType, token }: RendererProps
) => {
  SpaRenderer.renderNavigationReact(
    <GuestsConsole
      hostId={hostId}
      guestUuid={guestUuid}
      guestName={guestName}
      guestState={guestState}
      graphicsType={graphicsType}
      token={token}
    />,
    document.getElementById(id)
  );
};
