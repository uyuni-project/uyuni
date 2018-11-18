/* global module, getServerId */
const { renderWithHotReload } = require('components/hot-reload/render-with-hot-reload');
const { SubscribeChannels } = require('./subscribe-channels');

renderWithHotReload(SubscribeChannels, { serverId: getServerId() }, 'subscribe-channels-div');

// Despite existing a more recent library of react-hot-loader we have to use the V3 to be compatible with the react v14
if (module.hot) {
  // eslint-disable-next-line
  module.hot.accept('./subscribe-channels.js', () => {
    // eslint-disable-next-line
    const { SubscribeChannels } = require('./subscribe-channels');
    renderWithHotReload(SubscribeChannels, { serverId: getServerId() }, 'subscribe-channels-div');
  });
}
