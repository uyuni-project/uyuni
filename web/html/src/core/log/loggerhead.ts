// This file is allowed to use console, everyone else is not
/* eslint-disable no-console */
type Headers = Record<string, string>;
type Level = "info" | "debug" | "warning" | "error";

export default class Loggerhead {
  private url = "";
  private levels: Record<Level, boolean> = { info: true, debug: true, warning: true, error: true };
  private console: Record<Level, boolean> = { info: true, debug: true, warning: true, error: true };
  private setHeaders: (headers: Headers) => Headers;

  constructor(url: string, setHeaders: (headers: Headers) => Headers) {
    this.url = url;
    this.setHeaders = setHeaders;
  }

  info(message: string, callback?: (...args: any[]) => void) {
    if (this.levels.info) {
      this.postData({ level: "info", message }, callback);
    }
    if (this.console.info) {
      console.info(message);
    }
  }

  debug(message: string, callback?: (...args: any[]) => void) {
    if (this.levels.debug) {
      this.postData({ level: "debug", message }, callback);
    }
    if (this.console.debug) {
      console.debug(message);
    }
  }

  warn(message: string, callback?: (...args: any[]) => void) {
    if (this.levels.warning) {
      this.postData({ level: "warning", message }, callback);
    }
    if (this.console.warning) {
      console.warn(message);
    }
  }

  error(message: string, callback?: (...args: any[]) => void) {
    if (this.levels.error) {
      this.postData({ level: "error", message }, callback);
    }
    if (this.console.error) {
      console.error(message);
    }
  }

  private postData(data: { level: Level; message: string }, callback?: (...args: any[]) => void) {
    if (this.url === "") {
      var errorMessage = "[Loggerhead] ERROR: no server enpoint URL set to send the POST request!! ";
      if (this.console.error) {
        console.error(errorMessage);
      }
      return;
    }

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
          console.error(JSON.parse(xhr.response));
        } catch (e) {
          console.error(
            "The POST request to the url: '" +
              this.url +
              "' was not successfully completed and the response cannot be parsed."
          );
        }
      }
    };

    xhr.onreadystatechange = () => {
      if (xhr.readyState === 4 && xhr.status === 200) {
        if (callback && typeof callback === "function") {
          callback(JSON.parse(xhr.responseText));
        }
      }
    };
    xhr.send(JSON.stringify(data));

    return xhr;
  }
}
