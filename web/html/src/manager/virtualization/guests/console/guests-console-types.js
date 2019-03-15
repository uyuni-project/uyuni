// @flow
export interface ConsoleClientType {
  constructor(canvasId: string,
              socketUrl: string,
              connected: Function,
              disconnected: Function,
              askPassword: Function): void;

  connect(): void;

  toggleScale(expanded: boolean): void;

  canResize: boolean;
}
