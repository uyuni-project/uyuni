// This file is allowed to use console, everyone else is not
/* eslint-disable no-console */
type Headers = Record<string, string>;
type Level = "info" | "debug" | "warning" | "error";

export default class Loggerhead {
  private _log = console.log.bind(console);
  private _info = console.info.bind(console);
  private _debug = console.debug.bind(console);
  private _warn = console.warn.bind(console);
  private _error = console.error.bind(console);

  private url = "";
  private setHeaders: (headers: Headers) => Headers;

  constructor(url: string, setHeaders: (headers: Headers) => Headers) {
    if (!url) {
      throw new TypeError("No url provided for LoggerHead");
    }
    this.url = url;
    this.setHeaders = setHeaders;

    // We hijack the global console to ensure errors thrown in third-party code get logged too
    console.log = this.log;
    console.info = this.info;
    console.debug = this.debug;
    console.warn = this.warn;
    console.error = this.error;
  }

  // This is only used for binding third-party code, to log with the default level, use info()
  private log = (
    // Since we also wrap logging for external code, we need to support a variable number of arguments here
    ...args: Parameters<typeof console["log"]>
  ) => {
    // Use level "info" for our own logs, console.log() for the browser
    this._log(...args);
    this.postData({ level: "info", message: args.toString() });
    this.mark({ level: "info", message: args.toString() });
  };

  info = (...args: Parameters<typeof console["info"]>) => {
    this._info(...args);
    this.postData({ level: "info", message: args.toString() });
    this.mark({ level: "info", message: args.toString() });
  };

  debug = (...args: Parameters<typeof console["debug"]>) => {
    this._debug(...args);
    this.postData({ level: "debug", message: args.toString() });
    this.mark({ level: "debug", message: args.toString() });
  };

  warn = (...args: Parameters<typeof console["warn"]>) => {
    this._warn(...args);
    this.postData({ level: "warning", message: args.toString() });
    this.mark({ level: "warning", message: args.toString() });
  };

  error = (...args: Parameters<typeof console["error"]>) => {
    this._error(...args);
    this.postData({ level: "error", message: args.toString() });
    this.mark({ level: "error", message: args.toString() });
  };

  private mark(input: { level: Level; message: string }) {
    performance.mark(JSON.stringify(input));
  }

  private postData(data: { level: Level; message: string }) {
    const headers: Headers = {
      "Content-Type": "application/json; charset=utf-8",
    };

    var xhr = new XMLHttpRequest();
    xhr.open("POST", this.url);

    Object.keys(this.setHeaders(headers)).forEach((key) => {
      xhr.setRequestHeader(key, headers[key]);
    });

    xhr.onload = () => {
      if (xhr.status !== 200) {
        // Try to parse the xhr response, but catch if it fails to avoid an infinite loop of failure-and-logging
        try {
          this._error(JSON.parse(xhr.response));
        } catch (e) {
          this._error(
            `The POST request to the url: '${this.url}' was not successfully completed and the response cannot be parsed.`
          );
        }
      }
    };

    xhr.send(JSON.stringify(data));

    return xhr;
  }
}
