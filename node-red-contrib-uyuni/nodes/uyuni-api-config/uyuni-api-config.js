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
    if (node.allowSelfSigned) {
      // Deliberately loud. With verification off the connection can be
      // intercepted, and the credentials below travel over it.
      node.warn('Certificate verification is disabled for ' + node.url +
        '. Intended for development only; use a trusted certificate in production.');
    }

    // Requests give up rather than hanging a flow indefinitely.
    node.timeout = parseInt(config.timeout, 10) > 0 ? parseInt(config.timeout, 10) : 30000;

    // A session fault is retried once. Retrying without a limit would spin
    // forever whenever the server keeps returning the same fault.
    const MAX_LOGIN_RETRIES = 1;

    // Cache the session key, and the login currently in flight (if any) so
    // that concurrent calls wait on one login rather than each starting their
    // own and hammering the server.
    node.sessionKey = null;
    node.loginPromise = null;

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
        rejectUnauthorized: !node.allowSelfSigned,
        timeout: node.timeout
      };

      if (secure) {
        return xmlrpc.createSecureClient(clientOptions);
      } else {
        return xmlrpc.createClient(clientOptions);
      }
    };

    // Returns the cached session, joining an in-flight login when one is
    // already running.
    node.ensureSession = function() {
      if (node.sessionKey) {
        return Promise.resolve(node.sessionKey);
      }
      if (!node.loginPromise) {
        node.loginPromise = node.login().finally(() => {
          node.loginPromise = null;
        });
      }
      return node.loginPromise;
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
                // Clear cache and log in again, joining any login already
                // in flight rather than starting another one.
                node.sessionKey = null;
                node.ensureSession().then((newSession) => {
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

        node.ensureSession().then((session) => {
          executeCall(session, 0);
        }).catch((loginErr) => {
          reject(loginErr);
        });
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
