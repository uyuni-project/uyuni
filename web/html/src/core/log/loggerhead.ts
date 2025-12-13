/* eslint-disable no-console */
// This file is allowed to use console, everyone else is not

type Headers = Record<string, string>;
type Level = "info" | "debug" | "trace" | "warning" | "error";
type LogParams = Parameters<(typeof console)["log"]>;

export default class Loggerhead {
  private _log = console.log.bind(console);
  private _info = console.info.bind(console);
  private _debug = console.debug.bind(console);
  private _warn = console.warn.bind(console);
  private _error = console.error.bind(console);

  private url = "";
  private setHeaders: (headers: Headers) => Headers = (headers) => headers;

  constructor(url: string, setHeaders: (headers: Headers) => Headers) {
    this.url = url;
    this.setHeaders = setHeaders;

    // We hijack the global console to ensure errors thrown in third-party code get logged too
    // If we're running unit tests in a Node env, skip this
    if (window.location.hostname !== "localhost" && typeof window !== "undefined" && process.env.NODE_ENV !== "test") {
      console.log = this.log;
      console.info = this.info;
      console.debug = this.debug;
      console.warn = this.warn;
      console.error = this.error;

      // See https://stackoverflow.com/q/12571650/1470607
      window.addEventListener("unhandledrejection", (event) => {
        this.error(`Unhandled promise rejection: ${String(event.reason)}`);
        return false;
      });
      window.addEventListener("error", (event) => {
        this.error(event.error);
        return false;
      });
    }
  }

  // This is only used for binding third-party code, to log with the default level, use info()
  private log = (
    // Since we also wrap logging for external code, we need to support a variable number of arguments here
    ...args: LogParams
  ) => {
    // Use level "info" for our own logs, console.log() for the browser
    this._log(...args);
    const message = String(args);
    this.postData({ level: "info", message });
    this.mark({ level: "info", message });
  };

  info = (...args: LogParams) => {
    this._info(...args);
    const message = String(args);
    this.postData({ level: "info", message });
    this.mark({ level: "info", message });
  };

  debug = (...args: LogParams) => {
    this._debug(...args);
    const message = String(args);
    this.postData({ level: "debug", message });
    this.mark({ level: "debug", message });
  };

  warn = (...args: LogParams) => {
    this._warn(...args);
    const message = String(args);
    this.postData({ level: "warning", message });
    this.mark({ level: "warning", message });
  };

  error = (...args: LogParams) => {
    this._error(...args);
    const message = String(args);
    this.postData({ level: "error", message });
    this.mark({ level: "error", message });
  };

  private mark(input: { level: Level; message: string }) {
    // Older Node doesn't have this method, this check should be safe to remove once we upgrade to Node 20
    if (typeof performance !== "undefined" && "mark" in performance) {
      performance.mark(JSON.stringify(input));
    }
  }

  private postData(data: { level: Level; message: string }) {
    if (!this.url) {
      return;
    }

    const headers: Headers = {
      "Content-Type": "application/json; charset=utf-8",
    };

    const xhr = new XMLHttpRequest();
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
            `The POST request to the url '${this.url}' completed with status ${xhr.status} and the response cannot be parsed.`
          );
        }
      }
    };

    xhr.send(JSON.stringify(data));

    return xhr;
  }
}
