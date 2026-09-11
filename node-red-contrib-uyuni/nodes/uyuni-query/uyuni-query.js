module.exports = function (RED) {
  function UyuniQueryNode(config) {
    RED.nodes.createNode(this, config);
    const node = this;

    node.server = RED.nodes.getNode(config.server);
    node.queryType = config.queryType || 'listActiveSystems';

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

      // Perform the selected query
      switch (node.queryType) {
        case 'listActiveSystems':
          // system.listActiveSystems expects: sessionKey
          handleApiCall('system.listActiveSystems', []);
          break;

        case 'getSystemDetails':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // system.getDetails expects: sessionKey, serverId
          handleApiCall('system.getDetails', [parseInt(systemId, 10)]);
          break;

        case 'listErrata':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // system.getRelevantErrata expects: sessionKey, serverId
          handleApiCall('system.getRelevantErrata', [parseInt(systemId, 10)]);
          break;

        case 'listSystemPackages':
          if (!systemId) {
            node.status({ fill: 'red', shape: 'ring', text: 'missing systemId' });
            done(new Error('systemId is required in msg.payload.systemId or msg.systemId'));
            return;
          }
          // system.listPackages expects: sessionKey, serverId
          handleApiCall('system.listPackages', [parseInt(systemId, 10)]);
          break;

        default:
          node.status({ fill: 'red', shape: 'ring', text: 'unknown query' });
          done(new Error(`Unknown query type: ${node.queryType}`));
      }
    });
  }

  RED.nodes.registerType('uyuni-query', UyuniQueryNode);
};
