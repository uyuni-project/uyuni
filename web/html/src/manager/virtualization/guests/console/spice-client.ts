import { SpiceMainConn } from "@spice-project/spice-html5";
import { ConsoleClientType } from "./guests-console-types";

class SpiceClient implements ConsoleClientType {
  client: any;
  canvasId: string;
  socketUrl: string;
  connected: Function;
  disconnected: Function;
  askPassword: Function;
  ignoreErrors: boolean;

  constructor(canvasId: string, socketUrl: string, connected: Function, disconnected: Function, askPassword: Function) {
    this.canvasId = canvasId;
    this.socketUrl = socketUrl;
    this.connected = connected;
    this.disconnected = disconnected;
    this.askPassword = askPassword;
    this.ignoreErrors = false;
  }

  removeErrorHandler = () => {
    this.ignoreErrors = true;
  };

  onError = (e: Error | null | undefined) => {
    this.disconnect();
    if (this.ignoreErrors) {
      return;
    }
    if (this.disconnected != null && e != null) {
      this.disconnected(e.message);
    }
    if (e != null && e.message === "Permission denied." && this.askPassword != null) {
      this.askPassword().then((password) => this.doConnect(password));
    }
  };

  onAgentConnected = () => {
    window.addEventListener("resize", this.handleResize);
    this.setClientResizeHelper();
  };

  setClientResizeHelper = () => {
    const { client } = this;
    if (client == null) {
      return;
    }
    const screen = document.getElementById("display-area");
    if (screen != null) {
      let w = screen.clientWidth;
      let h = screen.clientHeight;

      /* Xorg requires width/height be multiple of 8; round up */
      if (h % 8 > 0) {
        h -= h % 8;
      }
      if (w % 8 > 0) {
        w -= w % 8;
      }
      client.resize_window(0, w, h, 32, 0, 0);
      client.spice_resize_timer = undefined;
    }
  };

  handleResize = () => {
    if (this.client != null) {
      const { client } = this;
      if (client.spice_resize_timer) {
        window.clearTimeout(client.spice_resize_timer);
        client.spice_resize_timer = undefined;
      }

      client.spice_resize_timer = window.setTimeout(this.setClientResizeHelper, 200);
    }
  };

  onConnect = () => {
    if (this.connected != null) {
      this.connected();
    }
  };

  disconnect = () => {
    if (this.client != null) {
      this.client.stop();
    }
  };

  doConnect = (password: string | null | undefined) => {
    try {
      this.client = new SpiceMainConn({
        uri: this.socketUrl,
        screen_id: this.canvasId,
        password,
        onerror: this.onError,
        onagent: this.onAgentConnected,
        onsuccess: this.onConnect,
      });
    } catch (e) {
      this.disconnect();
      if (this.disconnected != null && e != null) {
        this.disconnected(e.message);
      }
    }
  };

  connect = () => {
    this.doConnect(undefined);
  };

  toggleScale = () => {};

  canResize = false;
}

export { SpiceClient };
