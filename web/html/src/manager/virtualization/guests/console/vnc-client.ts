import RFB from "@novnc/novnc/core/rfb";
import { ConsoleClientType } from "./guests-console-types";

class VncClient implements ConsoleClientType {
  rfb: any;
  canvasId: string;
  socketUrl: string;
  connected: Function;
  disconnected: Function;
  askPassword: Function;

  disconnectHandler = (e) => {
    const error = e.detail.clean ? undefined : t("Something went wrong, connection is closed");
    if (this.disconnected != null) {
      this.disconnected(error);
    }
  };

  constructor(canvasId: string, socketUrl: string, connected: Function, disconnected: Function, askPassword: Function) {
    this.canvasId = canvasId;
    this.socketUrl = socketUrl;
    this.connected = connected;
    this.disconnected = disconnected;
    this.askPassword = askPassword;
  }

  connect = () => {
    if (this.canvasId != null && this.socketUrl != null) {
      const rfb = new RFB(document.getElementById(this.canvasId), this.socketUrl);
      rfb.addEventListener("connect", this.onConnect);
      rfb.addEventListener("disconnect", this.disconnectHandler);
      rfb.addEventListener("credentialsrequired", () => {
        if (this.askPassword != null) {
          this.askPassword().then((password) => rfb.sendCredentials({ password }));
        }
      });
      rfb.scaleViewport = false;
      rfb.resizeSession = false;
      this.rfb = rfb;
    }
  };

  removeErrorHandler = () => {
    this.rfb.removeEventListener("disconnect", this.disconnectHandler);
  };

  onConnect = () => {
    if (this.rfb != null) {
      this.rfb.focus();
      if (this.connected != null) {
        this.connected();
      }
    }
  };

  toggleScale = (expanded: boolean) => {
    if (this.rfb != null) {
      this.rfb.scaleViewport = expanded;
      this.rfb.resizeSession = expanded;
    }
  };

  canResize = true;
}

export { VncClient };
