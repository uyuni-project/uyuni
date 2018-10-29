const { renderWithHotReload } = require('components/hot-reload/render-with-hot-reload');
const { NotificationMessages } = require('./notification-messages');

renderWithHotReload(NotificationMessages, {}, 'notification-messages');

// Despite existing a more recent library of react-hot-loader we have to use the V3 to be compatible with the react v14
if (module.hot) {
  module.hot.accept('./notification-messages.js', () => {
    // eslint-disable-next-line
    const { NotificationMessages } = require('./notification-messages');
    renderWithHotReload(NotificationMessages, {}, 'notification-messages');
  });
}
