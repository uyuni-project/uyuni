// @flow
/* global module */


import RFB from '@novnc/novnc/core/rfb';
import type { ConsoleClientType } from './guests-console-types';

class VncClient implements ConsoleClientType {
  rfb: Object;
  canvasId: string;
  socketUrl: string;
  connected: Function;
  disconnected: Function;
  askPassword: Function;

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
      rfb.addEventListener('connect', this.onConnect);
      rfb.addEventListener('disconnect', (e) => {
        const error = e.detail.clean ? undefined : t('Something went wrong, connection is closed');
        if (this.disconnected != null) {
          this.disconnected(error);
        }
      });
      rfb.addEventListener('credentialsrequired',
        () => {
          if (this.askPassword != null) {
            this.askPassword().then(password => rfb.sendCredentials({ password }));
          }
        });
      rfb.scaleViewport = false;
      rfb.resizeSession = false;
      this.rfb = rfb;
    }
  }

  onConnect = () => {
    if (this.rfb != null) {
      this.rfb.focus();
      if (this.connected != null) {
        this.connected();
      }
    }
  }

  toggleScale = (expanded: boolean) => {
    if (this.rfb != null) {
      this.rfb.scaleViewport = expanded;
      this.rfb.resizeSession = expanded;
    }
  }

  canResize = true;
}

export {
  VncClient,
};
