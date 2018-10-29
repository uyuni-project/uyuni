/* global module */
const { renderWithHotReload } = require('components/hot-reload/render-with-hot-reload');
const { GuestsEdit } = require('./guests-edit');

window.pageRenderers = window.pageRenderers || {};
window.pageRenderers.guests = window.pageRenderers.guests || {};
window.pageRenderers.guests.edit = window.pageRenderers.guests.edit || {};
window.pageRenderers.guests.edit.guestsEditRenderer = (id, { host, guest }) => {
  const guestEditProps = {
    host,
    guest,
  };

  renderWithHotReload(GuestsEdit, guestEditProps, id);

  if (module.hot) {
    module.hot.accept('./guests-edit.js', () => {
      // eslint-disable-next-line
      const { GuestsList } = require('./guests-edit');
      renderWithHotReload(GuestsEdit, guestEditProps, id);
    });
  }
};
