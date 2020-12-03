/** @type {import("snowpack").SnowpackUserConfig } */

/*
// See https://github.com/snowpackjs/snowpack/blob/a02b504968fa6f050153f7eea7e6a93cf72a5da9/www/_template/guides/routing.md#scenario-2-proxy-api-paths
const httpProxy = require('http-proxy');
const proxy = httpProxy.createServer({
  context: ['!/sockjs-node/**'],
  target: 'https://server.tf.local',
  ws: true,
  secure: false
});
*/

module.exports = {
  mount: {
    './': "/",
  },
  plugins: ["@snowpack/plugin-react-refresh", "@snowpack/plugin-typescript"],
  install: [
    /* ... */
  ],
  installOptions: {
    /* ... */
  },
  devOptions: {
    /* ... */
  },
  buildOptions: {
    /* ... */
  },
  proxy: {
    context: ['!/sockjs-node/**'],
    target: 'https://server.tf.local',
    ws: true,
    secure: false
  },
  // No aliases are defined by default, see https://www.snowpack.dev/reference/configuration#config.alias
  alias: {
    "components": "./components",
    "core": "./core",
    "manager": "./manager",
    "utils": "./utils",
    "vendors": "./vendors"
  },
};
