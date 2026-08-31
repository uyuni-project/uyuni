/**
 * Uyuni Config Node — shared MQTT broker configuration
 * 
 * This config node stores the MQTT broker connection details
 * and is shared across all Uyuni event nodes in a flow.
 */
module.exports = function (RED) {
  function UyuniConfigNode(config) {
    RED.nodes.createNode(this, config);

    // Connection settings
    this.broker = config.broker || 'localhost';
    this.port = parseInt(config.port, 10) || 1883;
    this.clientId = config.clientId || `nodered-uyuni-${Math.random().toString(16).slice(2, 8)}`;
    this.topicPrefix = config.topicPrefix || 'uyuni/+';

    // TLS settings (for production)
    this.usetls = config.usetls || false;

    // Credentials (username/password injected via RED.nodes.credentials)
    this.username = this.credentials?.username || '';
    this.password = this.credentials?.password || '';
  }

  RED.nodes.registerType('uyuni-config', UyuniConfigNode, {
    credentials: {
      username: { type: 'text' },
      password: { type: 'password' },
    },
  });
};
