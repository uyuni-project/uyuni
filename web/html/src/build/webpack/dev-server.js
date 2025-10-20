import path, { dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

const websocketPathname = "/ws";
const dist = path.resolve(__dirname, "../../dist");
const hmrTarget = path.resolve(dist, "./javascript/manager");
const staticCss = path.resolve(dist, "./css");

const devServer = (env) => ({
  hot: true,
  open: true,
  static: {
    directory: dist,
    publicPath: "/",
    // This is currently redundant, but will become relevant when we include static files in Webpack
    watch: true,
  },
  server: {
    type: "https",
  },
  client: {
    webSocketURL: {
      // Hardcode this so it always matches
      pathname: websocketPathname,
    },
    logging: "error",
  },
  /**
   * The documentation isn't very good for this, but shortly we're proxying everything besides what comes out of Webpack through to the provided server
   * See https://webpack.js.org/configuration/dev-server/#devserverproxy and https://github.com/chimurai/http-proxy-middleware#options
   */
  proxy: [
    {
      target: env && env.server,
      // Proxy everything, including websockets, besides the Webpack updates websocket
      context: ["!" + websocketPathname],
      ws: true,
      /**
       * Rewrite the host and port on redirects, so we stay on the proxy after logging in, logging out etc
       * See https://github.com/http-party/node-http-proxy/issues/1227
       */
      autoRewrite: true,
      changeOrigin: true,
      // Ignore sertificate errors for dev servers
      secure: false,
    },
  ],
  devMiddleware: {
    publicPath: "/",
    // When global styles or static assets change, trigger a hard reload instead of HMR
    writeToDisk: (filePath) => {
      const canHmr = filePath.startsWith(hmrTarget);
      // Since we use theme files globally, they generate empty entry points, don't trigger HMR for those changes
      const isEmptyEntry = filePath.startsWith(staticCss) && (filePath.endsWith(".js") || filePath.endsWith(".json"));
      if (canHmr || isEmptyEntry) {
        return false;
      }
      return true;
    },
    // Allow proxying requests to root "/", see https://webpack.js.org/configuration/dev-server/#devserverproxy
    index: false,
  },
});

export default devServer;
