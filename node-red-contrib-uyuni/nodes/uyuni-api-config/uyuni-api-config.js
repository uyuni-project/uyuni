const xmlrpc = require('xmlrpc');

module.exports = function (RED) {
  function UyuniApiConfigNode(config) {
    RED.nodes.createNode(this, config);
    const node = this;

    node.url = config.url || 'https://localhost/rpc/api';
    node.username = node.credentials?.username || '';
    node.password = node.credentials?.password || '';

    // Certificates are verified unless the user opts out for a self-signed
    // development server.
    node.allowSelfSigned = config.allowSelfSigned === true;

    // A session fault is retried once. Retrying without a limit would spin
    // forever whenever the server keeps returning the same fault.
    const MAX_LOGIN_RETRIES = 1;

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
        rejectUnauthorized: !node.allowSelfSigned
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

        const executeCall = (session, attempt) => {
          // Prepend session key to parameters
          const fullParams = [session, ...params];
          client.methodCall(method, fullParams, (error, value) => {
            if (error) {
              // Only a fault that actually mentions the session is worth
              // retrying. Matching loosely on words like "invalid" would treat
              // an ordinary bad-argument fault as an expired session.
              const isSessionError = error.message && /session/i.test(error.message);

              if (isSessionError && attempt < MAX_LOGIN_RETRIES) {
                // Clear cache and try to login again
                node.sessionKey = null;
                node.login().then((newSession) => {
                  executeCall(newSession, attempt + 1);
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
          executeCall(node.sessionKey, 0);
        } else {
          node.login().then((session) => {
            executeCall(session, 0);
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
