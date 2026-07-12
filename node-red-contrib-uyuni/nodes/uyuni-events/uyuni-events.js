/**
 * Uyuni Events Node — subscribes to Uyuni MQTT events
 * 
 * This is the core node that system administrators drag onto their
 * Node-RED canvas to receive real-time Uyuni events. It connects
 * to the MQTT broker, subscribes to event topics based on the
 * selected category, and outputs parsed event messages.
 */
const mqtt = require('mqtt');

module.exports = function (RED) {
  function UyuniEventsNode(config) {
    RED.nodes.createNode(this, config);
    const node = this;

    // Get the shared config node
    node.server = RED.nodes.getNode(config.server);
    if (!node.server) {
      node.error('No Uyuni broker configured');
      node.status({ fill: 'red', shape: 'ring', text: 'no broker' });
      return;
    }

    // Event category/action filter
    const category = config.eventCategory || '#';
    const topicPrefix = node.server.topicPrefix || 'uyuni/+';
    const topic = category === '#' 
      ? `${topicPrefix}/#` 
      : `${topicPrefix}/${category}`;

    // Build MQTT connection options
    const mqttOpts = {
      clientId: node.server.clientId,
      clean: true,
      reconnectPeriod: 5000,
    };

    if (node.server.username) {
      mqttOpts.username = node.server.username;
      mqttOpts.password = node.server.password;
    }

    const protocol = node.server.usetls ? 'mqtts' : 'mqtt';
    const brokerUrl = `${protocol}://${node.server.broker}:${node.server.port}`;

    // Connect
    node.status({ fill: 'yellow', shape: 'ring', text: 'connecting...' });
    const client = mqtt.connect(brokerUrl, mqttOpts);

    client.on('connect', () => {
      node.status({ fill: 'green', shape: 'dot', text: `connected (${category})` });
      node.log(`Connected to ${brokerUrl}, subscribing to ${topic}`);

      client.subscribe(topic, { qos: 1 }, (err) => {
        if (err) {
          node.error(`Subscribe failed: ${err.message}`);
          node.status({ fill: 'red', shape: 'ring', text: 'subscribe failed' });
        }
      });
    });

    client.on('message', (msgTopic, payload) => {
      try {
        const event = JSON.parse(payload.toString());

        // Extract metadata from standard envelope
        const eventId = event.eventId || '';
        const timestamp = event.timestamp || '';
        const resolvedTopic = event.topic || msgTopic;
        const data = event.data || event;

        // Split topic components to identify category and eventType
        // Expected format: uyuni/<fqdn>/<category>/<action>
        const parts = resolvedTopic.split('/');
        const categoryVal = parts[2] || '';
        const eventTypeVal = parts.slice(3).join('/') || '';

        // Build the Node-RED message
        const msg = {
          topic: resolvedTopic,
          payload: data,
          uyuni: {
            eventId: eventId,
            timestamp: timestamp,
            category: categoryVal,
            eventType: eventTypeVal,
          },
        };

        node.send(msg);
      } catch (e) {
        node.warn(`Failed to parse event on ${msgTopic}: ${e.message}`);
        // Send raw payload if JSON parsing fails
        node.send({ topic: msgTopic, payload: payload.toString() });
      }
    });

    client.on('error', (err) => {
      node.error(`MQTT error: ${err.message}`);
      node.status({ fill: 'red', shape: 'ring', text: err.message });
    });

    client.on('close', () => {
      node.status({ fill: 'grey', shape: 'ring', text: 'disconnected' });
    });

    client.on('reconnect', () => {
      node.status({ fill: 'yellow', shape: 'ring', text: 'reconnecting...' });
    });

    // Cleanup on node removal or flow redeploy
    node.on('close', (done) => {
      if (client) {
        client.end(false, {}, done);
      } else {
        done();
      }
    });
  }

  RED.nodes.registerType('uyuni-events', UyuniEventsNode);
};
