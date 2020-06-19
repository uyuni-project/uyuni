import React from 'react';
import GuestsConsole from './guests-console';
import SpaRenderer from "core/spa/spa-renderer";

export const renderer = (id, {
  hostId,
  guestUuid,
  guestName,
  graphicsType,
  token,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsConsole
      hostId={hostId}
      guestUuid={guestUuid}
      guestName={guestName}
      graphicsType={graphicsType}
      token={token}
    />,
    document.getElementById(id),
  );
};
