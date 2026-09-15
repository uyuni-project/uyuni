module.exports = function (RED) {
  function UyuniActionNode(config) {
    RED.nodes.createNode(this, config);
    const node = this;

    node.server = RED.nodes.getNode(config.server);
    node.actionType = config.actionType || 'applyHighstate';

    if (!node.server) {
      node.error('No Uyuni API server configured');
      node.status({ fill: 'red', shape: 'ring', text: 'no api config' });
      return;
    }

    node.status({ fill: 'grey', shape: 'ring', text: 'idle' });

    node.on('input', function (msg, send, done) {
      // Compatibility fallback for older Node-RED versions
      send = send || function() { node.send.apply(node, arguments) };
      done = done || function(err) { if (err) { node.error(err, msg); } };

      // Resolve systemId from message properties
      let systemId = null;
      if (msg.payload && typeof msg.payload === 'object') {
        systemId = msg.payload.systemId || msg.payload.serverId || msg.payload.id;
      }
      if (!systemId && msg.systemId) {
        systemId = msg.systemId;
      }

      node.status({ fill: 'blue', shape: 'dot', text: 'executing...' });

      // Helper to process XML-RPC call
      const handleApiCall = (method, params) => {
        node.server.callMethod(method, params)
          .then((result) => {
            msg.payload = result;
            node.status({ fill: 'green', shape: 'dot', text: 'success' });
            send(msg);
            done();
          })
          .catch((err) => {
            node.status({ fill: 'red', shape: 'ring', text: 'error' });
            done(err);
          });
      };

      // Perform the selected action
      switch (node.actionType) {
        case 'applyHighstate':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // system.applyHighstate expects: sessionKey, serverId, earliestOccurrence (date), force (boolean)
          // Earliest occurrence: new Date() (represented as XML-RPC dateTime.iso8601)
          handleApiCall('system.applyHighstate', [parseInt(systemId, 10), new Date(), false]);
          break;

        case 'scheduleReboot':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // system.scheduleReboot expects: sessionKey, serverId, earliestOccurrence
          handleApiCall('system.scheduleReboot', [parseInt(systemId, 10), new Date()]);
          break;

        case 'schedulePackageInstall':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // Resolve packageIds from msg.payload
          let packageIds = null;
          if (msg.payload && typeof msg.payload === 'object') {
            packageIds = msg.payload.packageIds;
          }
          if (!packageIds) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing packageIds' });
            done(new Error('packageIds (array of ints) is required in msg.payload.packageIds'));
            return;
          }
          // Ensure packageIds is an array
          if (!Array.isArray(packageIds)) {
            packageIds = [packageIds];
          }
          // Convert all array elements to integer
          const intPackageIds = packageIds.map(id => parseInt(id, 10));

          // system.schedulePackageInstall expects: sessionKey, serverId, packageIds, earliestOccurrence
          handleApiCall('system.schedulePackageInstall', [parseInt(systemId, 10), intPackageIds, new Date()]);
          break;

        case 'listAllChannels':
          // channel.software.listAllChannels expects: sessionKey
          handleApiCall('channel.software.listAllChannels', []);
          break;

        default:
          node.status({ fill: 'red', shape: 'ring', text: 'unknown action' });
          done(new Error(`Unknown action type: ${node.actionType}`));
      }
    });
  }

  RED.nodes.registerType('uyuni-action', UyuniActionNode);
};
