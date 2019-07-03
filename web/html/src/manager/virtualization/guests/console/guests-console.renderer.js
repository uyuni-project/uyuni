import React from 'react';
import ReactDOM from 'react-dom';
import GuestsConsole from './guests-console';

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
  ReactDOM.render(
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
