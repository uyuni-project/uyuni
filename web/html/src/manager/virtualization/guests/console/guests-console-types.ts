export abstract class ConsoleClientType {
  // Enforce identical constructor signatures across specific implementations
  // eslint-disable-next-line @typescript-eslint/no-useless-constructor
  constructor(canvasId: string, socketUrl: string, connected: Function, disconnected: Function, askPassword: Function) {
    // Intentionally left blank
  }
  abstract connect(): void;
  abstract toggleScale(expanded: boolean): void;
  abstract canResize: boolean;
  abstract removeErrorHandler(): void;
}
