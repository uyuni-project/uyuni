const xmlrpc = require('xmlrpc');

module.exports = function (RED) {
  function UyuniApiConfigNode(config) {
    RED.nodes.createNode(this, config);
    const node = this;

    node.url = config.url || 'https://localhost/rpc/api';
    node.username = node.credentials?.username || '';
    node.password = node.credentials?.password || '';

    // Cache the session key
    node.sessionKey = null;

    // Helper to create XML-RPC client
    node.getClient = function() {
      // Parse URL
      const secure = node.url.startsWith('https');
      const cleanUrl = node.url.replace(/^https?:\/\//, '');
      const parts = cleanUrl.split('/');
      const hostAndPort = parts[0].split(':');
      const host = hostAndPort[0];
      const port = hostAndPort[1] ? parseInt(hostAndPort[1], 10) : (secure ? 443 : 80);
      const path = '/' + parts.slice(1).join('/');

      const clientOptions = {
        host: host,
        port: port,
        path: path,
        rejectUnauthorized: false // Allow self-signed certs typical in Uyuni dev setups
      };

      if (secure) {
        return xmlrpc.createSecureClient(clientOptions);
      } else {
        return xmlrpc.createClient(clientOptions);
      }
    };

    // Helper to log in and get session key
    node.login = function() {
      return new Promise((resolve, reject) => {
        const client = node.getClient();
        client.methodCall('auth.login', [node.username, node.password], (error, value) => {
          if (error) {
            reject(error);
          } else {
            node.sessionKey = value;
            resolve(value);
          }
        });
      });
    };

    // Execute XML-RPC method call with auto-relogin retry on session expiration
    node.callMethod = function(method, params) {
      return new Promise((resolve, reject) => {
        const client = node.getClient();

        const executeCall = (session) => {
          // Prepend session key to parameters
          const fullParams = [session, ...params];
          client.methodCall(method, fullParams, (error, value) => {
            if (error) {
              // Check if it's a session expiration error
              const isSessionError = error.message && 
                (error.message.includes('Session') || error.message.includes('session') || error.message.includes('expired') || error.message.includes('invalid'));
              
              if (isSessionError) {
                // Clear cache and try to login again
                node.sessionKey = null;
                node.login().then((newSession) => {
                  executeCall(newSession);
                }).catch((loginErr) => {
                  reject(loginErr);
                });
              } else {
                reject(error);
              }
            } else {
              resolve(value);
            }
          });
        };

        if (node.sessionKey) {
          executeCall(node.sessionKey);
        } else {
          node.login().then((session) => {
            executeCall(session);
          }).catch((loginErr) => {
            reject(loginErr);
          });
        }
      });
    };
  }

  RED.nodes.registerType('uyuni-api-config', UyuniApiConfigNode, {
    credentials: {
      username: { type: 'text' },
      password: { type: 'password' }
    }
  });
};
