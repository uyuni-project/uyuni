const SpaRenderer  = require("core/spa/spa-renderer").default;
const React = require('react');
const { GuestsList } = require('./guests-list');

export const renderer = (id, { serverId, pageSize, saltEntitled, foreignEntitled, isAdmin, hypervisor }) => {
  SpaRenderer.renderNavigationReact(
    <GuestsList
      serverId={serverId}
      pageSize={pageSize}
      saltEntitled={saltEntitled}
      foreignEntitled={foreignEntitled}
      isAdmin={isAdmin}
      hypervisor={hypervisor}
    />,
    document.getElementById(id),
  );
};
