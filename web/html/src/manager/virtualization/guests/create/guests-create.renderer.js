import SpaRenderer from 'core/spa/spa-renderer';
import * as React from 'react';
import { GuestsCreate } from './guests-create';

export const renderer = (id, {
  host,
  timezone,
  localTime,
  actionChains,
}) => {
  SpaRenderer.renderNavigationReact(
    <GuestsCreate
      host={host}
      localTime={localTime}
      timezone={timezone}
      actionChains={actionChains}
    />,
    document.getElementById(id),
  );
};
