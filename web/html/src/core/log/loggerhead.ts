type Headers = Record<string, string>;
type Level = "info" | "debug" | "warning" | "error";

export default class Loggerhead {
  private config = {
    url: "",
    levels: { info: true, debug: true, warning: true, error: true },
    console: { info: true, debug: true, warning: true, error: true },
  };
  private setHeaders: (headers: Headers) => Headers;

  constructor(url: string, setHeaders: (headers: Headers) => Headers) {
    this.config.url = url;
    this.setHeaders = setHeaders;
  }

  info(message: string, callback?: (...args: any[]) => void) {
    if (this.config.levels.info) {
      this.postData({ level: "info", message }, callback);
    }
    if (this.config.console.info) {
      console.info(message);
    }
  }

  debug(message: string, callback?: (...args: any[]) => void) {
    if (this.config.levels.debug) {
      this.postData({ level: "debug", message }, callback);
    }
    if (this.config.console.debug) {
      console.debug(message);
    }
  }

  warning(message: string, callback?: (...args: any[]) => void) {
    if (this.config.levels.warning) {
      this.postData({ level: "warning", message }, callback);
    }
    if (this.config.console.warning) {
      console.warn(message);
    }
  }

  error(message: string, callback?: (...args: any[]) => void) {
    if (this.config.levels.error) {
      this.postData({ level: "error", message }, callback);
    }
    if (this.config.console.error) {
      console.error(message);
    }
  }

  private postData(data: { level: Level; message: string }, callback?: (...args: any[]) => void) {
    if (this.config.url === "") {
      var errorMessage = "[Loggerhead] ERROR: no server enpoint URL set to send the POST request!! ";
      if (this.config.console.error) {
        console.error(errorMessage);
      }
      return;
    }

    const headers: Headers = {
      "Content-Type": "application/json; charset=utf-8",
    };

    var xhr = new XMLHttpRequest();
    xhr.open("POST", this.config.url);

    Object.keys(this.setHeaders(headers)).forEach((key) => {
      xhr.setRequestHeader(key, headers[key]);
    });

    xhr.onload = () => {
      if (xhr.status !== 200) {
        // try to parse the xhr response, but catch if it fails unless an infinite loop of failure-and-logging would start
        try {
          console.error(JSON.parse(xhr.response));
        } catch (e) {
          console.error(
            "The POST request to the url: '" +
              this.config.url +
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
