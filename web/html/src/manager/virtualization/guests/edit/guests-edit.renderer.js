import * as React from 'react';
import { GuestsEdit } from './guests-edit';
import SpaRenderer from 'core/spa/spa-renderer';

export const renderer = (id, {
  host,
  guestUuid,
  timezone,
  localTime,
  actionChains,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsEdit
      host={host}
      guestUuid={guestUuid}
      timezone={timezone}
      localTime={localTime}
      actionChains={actionChains}
    />,
    document.getElementById(id),
  );
};
