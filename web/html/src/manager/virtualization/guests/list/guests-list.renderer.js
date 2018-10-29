/* global module */
const { renderWithHotReload } = require('components/hot-reload/render-with-hot-reload');
const { GuestsList } = require('./guests-list');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.list = window.pageRenderers.guests.list || {};
window.pageRenderers.guests.list.renderer = (id, { serverId, saltEntitled, isAdmin }) => {
  const guestListProps = {
    refreshInterval: 5 * 1000,
    serverId,
    saltEntitled,
    isAdmin,
  };

  renderWithHotReload(GuestsList, guestListProps, id);

  if (module.hot) {
    module.hot.accept('./guests-list.js', () => {
      // eslint-disable-next-line
      const { GuestsList } = require('./guests-list');
      renderWithHotReload(GuestsList, guestListProps, id);
    });
  }
};
