import React from 'react';
import GuestsConsole from './guests-console';
import SpaRenderer from "core/spa/spa-renderer";

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.console = window.pageRenderers.guests.console || {};
window.pageRenderers.guests.console.guestsConsoleRenderer = (id, {
  hostId,
  guestUuid,
  guestName,
  graphicsType,
  socketUrl,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsConsole
      hostId={hostId}
      guestUuid={guestUuid}
      guestName={guestName}
      graphicsType={graphicsType}
      socketUrl={socketUrl}
    />,
    document.getElementById(id),
  );
};
